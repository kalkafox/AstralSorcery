## Renames MCP member (method/field) and remaining class simple names to Mojang
## official names across the mod sources.
##
## Mapping chain per member:
##   MCP name  <- mcp_snapshot csv <- SRG id <- joined.tsrg <- obf name
##   obf name  -> client_mappings (proguard, official -> obf) -> official name
##
## The resulting MCP->official rename map is name-level (not per-class), so a
## rename is only applied automatically when the mapping is (near-)unambiguous
## across all members that carry that MCP name. Ambiguous names are written to
## the report for manual triage / the overrides file.
##
## Replacements are token-based and skip string literals, char literals,
## comments, import/package statements, and dotted chains rooted in a package
## name (net.*, hellfirepvp.*, ...).

param(
    [string] $MappingDirectory = "$PSScriptRoot\..\..\build\port-mappings",
    [string] $SourceDirectory = "$PSScriptRoot\..\..\src\main\java",
    [string] $OverridesFile = "$PSScriptRoot\member-overrides.csv",
    [string] $MinecraftSourcesJar,
    [double] $DominanceThreshold = 0.75,
    [int] $MinNameLength = 3,
    [switch] $DryRun
)

$ErrorActionPreference = 'Stop'

$mcpConfig = Join-Path $MappingDirectory 'mcp_config\config\joined.tsrg'
$mcpFieldsCsv = Join-Path $MappingDirectory 'mcp_snapshot\fields.csv'
$mcpMethodsCsv = Join-Path $MappingDirectory 'mcp_snapshot\methods.csv'
$mojangMappings = Join-Path $MappingDirectory 'client_mappings_1.16.5.txt'

foreach ($mappingInput in @($mcpConfig, $mcpFieldsCsv, $mcpMethodsCsv, $mojangMappings)) {
    if (-not (Test-Path $mappingInput)) {
        throw "Missing mapping input: $mappingInput"
    }
}

function New-OrdinalMap {
    return [Collections.Generic.Dictionary[string, string]]::new([StringComparer]::Ordinal)
}

function Get-DescriptorArgCount([string] $descriptor) {
    $count = 0
    $i = 1
    while ($i -lt $descriptor.Length -and $descriptor[$i] -ne ')') {
        while ($descriptor[$i] -eq '[') { $i++ }
        if ($descriptor[$i] -eq 'L') { $i = $descriptor.IndexOf(';', $i) }
        $count++
        $i++
    }
    return $count
}

function Get-SimpleName([string] $name) {
    return $name.Substring($name.LastIndexOf('.') + 1)
}

# --- 1. joined.tsrg: obf class/member -> SRG -------------------------------

$mcpClassByObf = New-OrdinalMap    # obf class -> MCP/SRG class name (dotted)
$srgFieldByObf = New-OrdinalMap    # "obfClass obfField" -> srg field id
$srgMethodByObf = New-OrdinalMap   # "obfClass obfMethod argc" -> srg method id
$currentObfClass = $null
foreach ($line in [IO.File]::ReadLines($mcpConfig)) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    if (-not $line.StartsWith("`t")) {
        $parts = $line.Split(' ')
        if ($parts.Length -eq 2) {
            $currentObfClass = $parts[0].Replace('/', '.')
            $mcpClassByObf[$currentObfClass] = $parts[1].Replace('/', '.')
        }
        continue
    }
    if (-not $currentObfClass) { continue }
    $parts = $line.Trim().Split(' ')
    if ($parts.Length -eq 2) {
        $srgFieldByObf["$currentObfClass $($parts[0])"] = $parts[1]
    } elseif ($parts.Length -eq 3) {
        $argc = Get-DescriptorArgCount $parts[1]
        $srgMethodByObf["$currentObfClass $($parts[0]) $argc"] = $parts[2]
    }
}

# --- 2. MCP snapshot csvs: SRG -> MCP name ----------------------------------

$mcpNameBySrg = New-OrdinalMap
foreach ($csv in @($mcpFieldsCsv, $mcpMethodsCsv)) {
    $first = $true
    foreach ($line in [IO.File]::ReadLines($csv)) {
        if ($first) { $first = $false; continue }
        $parts = $line.Split(',')
        if ($parts.Length -ge 2) {
            $mcpNameBySrg[$parts[0]] = $parts[1]
        }
    }
}

# --- 3. proguard mappings: obf class/member -> official ---------------------

$officialClassByObf = New-OrdinalMap    # obf class -> official class (dotted)
$officialFieldByObf = New-OrdinalMap    # "obfClass obfField" -> official field name
$officialMethodByObf = New-OrdinalMap   # "obfClass obfMethod argc" -> official method name
$classLine = [regex]'^(\S+) -> ([^:]+):$'
$methodLine = [regex]'^\s+(?:\d+:\d+:)?[\w.$\[\]]+ ([\w$<>]+)\((.*)\) -> (\S+)$'
$fieldLine = [regex]'^\s+[\w.$\[\]]+ ([\w$]+) -> (\S+)$'
$currentObfClass = $null
foreach ($line in [IO.File]::ReadLines($mojangMappings)) {
    if ($line.StartsWith('#')) { continue }
    if (-not $line.StartsWith(' ')) {
        $match = $classLine.Match($line)
        if ($match.Success) {
            $currentObfClass = $match.Groups[2].Value
            $officialClassByObf[$currentObfClass] = $match.Groups[1].Value
        }
        continue
    }
    if (-not $currentObfClass) { continue }
    $match = $methodLine.Match($line)
    if ($match.Success) {
        $officialName = $match.Groups[1].Value
        # constructors and synthetics (lambda$..., access$...) are not real names
        if ($officialName.StartsWith('<') -or $officialName.Contains('$')) { continue }
        $params = $match.Groups[2].Value
        $argc = if ($params.Length -eq 0) { 0 } else { $params.Split(',').Length }
        $officialMethodByObf["$currentObfClass $($match.Groups[3].Value) $argc"] = $officialName
        continue
    }
    $match = $fieldLine.Match($line)
    if ($match.Success) {
        $officialFieldByObf["$currentObfClass $($match.Groups[2].Value)"] = $match.Groups[1].Value
    }
}

# --- 4. join into MCP -> official vote tables --------------------------------

function New-VoteTable {
    return [Collections.Generic.Dictionary[string, Collections.Generic.Dictionary[string, int]]]::new(
            [StringComparer]::Ordinal)
}

function Add-Vote($votes, [string] $mcpName, [string] $officialName) {
    $candidates = $null
    if (-not $votes.TryGetValue($mcpName, [ref] $candidates)) {
        $candidates = [Collections.Generic.Dictionary[string, int]]::new([StringComparer]::Ordinal)
        $votes[$mcpName] = $candidates
    }
    $count = 0
    [void] $candidates.TryGetValue($officialName, [ref] $count)
    $candidates[$officialName] = $count + 1
}

$fieldVotes = New-VoteTable
foreach ($entry in $srgFieldByObf.GetEnumerator()) {
    $officialName = $null
    if (-not $officialFieldByObf.TryGetValue($entry.Key, [ref] $officialName)) { continue }
    $mcpName = $null
    if (-not $mcpNameBySrg.TryGetValue($entry.Value, [ref] $mcpName)) { $mcpName = $entry.Value }
    Add-Vote $fieldVotes $mcpName $officialName
}

$methodVotes = New-VoteTable
foreach ($entry in $srgMethodByObf.GetEnumerator()) {
    $officialName = $null
    if (-not $officialMethodByObf.TryGetValue($entry.Key, [ref] $officialName)) { continue }
    $mcpName = $null
    if (-not $mcpNameBySrg.TryGetValue($entry.Value, [ref] $mcpName)) { $mcpName = $entry.Value }
    Add-Vote $methodVotes $mcpName $officialName
}

$classVotes = New-VoteTable
foreach ($entry in $mcpClassByObf.GetEnumerator()) {
    $officialClass = $null
    if (-not $officialClassByObf.TryGetValue($entry.Key, [ref] $officialClass)) { continue }
    Add-Vote $classVotes (Get-SimpleName $entry.Value) (Get-SimpleName $officialClass)
}

# --- 4b. class simple names that already exist in 1.21 must never be renamed --
# The sources are a mix of already-ported official names and MCP names; a token
# that is a valid 1.21 class name (e.g. SoundSource) must be left alone even if
# an unrelated 1.16 MCP class shares that name.

if (-not $MinecraftSourcesJar) {
    $MinecraftSourcesJar = Get-ChildItem "$PSScriptRoot\..\..\build\moddev\artifacts" -Filter 'neoforge-*-sources.jar' -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $MinecraftSourcesJar -or -not (Test-Path $MinecraftSourcesJar)) {
    throw 'Could not locate the generated NeoForge Minecraft sources jar'
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$official21SimpleNames = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$archive = [IO.Compression.ZipFile]::OpenRead($MinecraftSourcesJar)
try {
    foreach ($entry in $archive.Entries) {
        if (-not $entry.FullName.EndsWith('.java', [StringComparison]::Ordinal)) { continue }
        if (-not $entry.FullName.StartsWith('net/minecraft/', [StringComparison]::Ordinal) -and
                -not $entry.FullName.StartsWith('com/mojang/', [StringComparison]::Ordinal)) {
            continue
        }
        $className = $entry.FullName.Substring(0, $entry.FullName.Length - 5)
        [void] $official21SimpleNames.Add($className.Substring($className.LastIndexOf('/') + 1))
    }
} finally {
    $archive.Dispose()
}

# --- 5. mod-declared type names must never be renamed ------------------------

$modTypeNames = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$typeDeclaration = [regex]'(?m)\b(?:class|interface|enum|record)\s+([A-Za-z_]\w*)'
$sourceFiles = Get-ChildItem $SourceDirectory -Recurse -Filter '*.java'
foreach ($file in $sourceFiles) {
    $text = [IO.File]::ReadAllText($file.FullName)
    foreach ($match in $typeDeclaration.Matches($text)) {
        [void] $modTypeNames.Add($match.Groups[1].Value)
    }
}

# --- 6. resolve vote tables into rename maps ---------------------------------

$ambiguousReport = [Collections.Generic.List[string]]::new()
$lowConfidenceReport = [Collections.Generic.List[string]]::new()

function Resolve-Votes($votes, [string] $kind) {
    $resolved = New-OrdinalMap
    foreach ($entry in $votes.GetEnumerator()) {
        $mcpName = $entry.Key
        if ($mcpName.Length -lt $MinNameLength) { continue }
        if ($modTypeNames.Contains($mcpName)) { continue }
        $total = 0
        $topName = $null
        $topCount = 0
        foreach ($candidate in $entry.Value.GetEnumerator()) {
            $total += $candidate.Value
            if ($candidate.Value -gt $topCount) {
                $topCount = $candidate.Value
                $topName = $candidate.Key
            }
        }
        if ($topName -eq $mcpName) { continue }
        if (($topCount / $total) -ge $DominanceThreshold) {
            $resolved[$mcpName] = $topName
            if ($total -lt 3) {
                $lowConfidenceReport.Add("$kind`t$mcpName -> $topName`t(votes: $total)")
            }
            continue
        }
        $distribution = ($entry.Value.GetEnumerator() |
                Sort-Object Value -Descending |
                ForEach-Object { "$($_.Key)=$($_.Value)" }) -join ', '
        $ambiguousReport.Add("$kind`t$mcpName`t$distribution")
    }
    return $resolved
}

$fieldMap = Resolve-Votes $fieldVotes 'field'
$methodMap = Resolve-Votes $methodVotes 'method'
$classMap = Resolve-Votes $classVotes 'class'

foreach ($mcpName in @($classMap.Keys)) {
    if ($official21SimpleNames.Contains($mcpName)) {
        [void] $classMap.Remove($mcpName)
    }
}

# --- 7. curated overrides take precedence ------------------------------------

if (Test-Path $OverridesFile) {
    foreach ($line in [IO.File]::ReadLines($OverridesFile)) {
        $line = $line.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith('#')) { continue }
        $parts = $line.Split(',')
        if ($parts.Length -ne 3) { continue }
        $map = switch ($parts[0].Trim()) {
            'field' { $fieldMap }
            'method' { $methodMap }
            'class' { $classMap }
            default { $null }
        }
        if ($null -eq $map) { continue }
        $mcpName = $parts[1].Trim()
        $officialName = $parts[2].Trim()
        if ($mcpName -eq $officialName) {
            [void] $map.Remove($mcpName)
        } else {
            $map[$mcpName] = $officialName
        }
    }
}

# --- 8. token-based replacement over the sources ------------------------------

# Import/package statements, comments, and literals are consumed as skipped
# alternatives so identifier tokens are never inspected inside them.
$scanner = [regex]::new(
        '(?m)^[ \t]*(?:import|package)\b[^;\r\n]*;' +
        '|//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|''(?:\\.|[^''\\])*''' +
        '|(?<id>[A-Za-z_]\w*)',
        [Text.RegularExpressions.RegexOptions]::Compiled)

# Dotted chains rooted in a package segment (fully qualified names in code
# bodies) must keep their segments.
$packageRoots = [Collections.Generic.HashSet[string]]::new(
        [string[]] @('hellfirepvp', 'net', 'java', 'javax', 'com', 'org', 'it', 'io'),
        [StringComparer]::Ordinal)

function Test-PackageQualified([string] $text, [int] $tokenStart) {
    $i = $tokenStart
    while ($true) {
        if ($i -le 0 -or $text[$i - 1] -ne '.') {
            break
        }
        $end = $i - 1
        $i = $end
        while ($i -gt 0 -and ([char]::IsLetterOrDigit($text[$i - 1]) -or $text[$i - 1] -eq '_')) { $i-- }
        if ($i -eq $end) {
            # preceded by '.' but no identifier before it (e.g. numeric literal)
            break
        }
    }
    if ($i -eq $tokenStart) { return $false }
    $head = ''
    $j = $i
    while ($j -lt $text.Length -and ([char]::IsLetterOrDigit($text[$j]) -or $text[$j] -eq '_')) {
        $head += $text[$j]
        $j++
    }
    return $packageRoots.Contains($head)
}

$utf8 = [Text.UTF8Encoding]::new($false)
$changedFiles = 0
$totalRenames = 0
foreach ($file in $sourceFiles) {
    $text = [IO.File]::ReadAllText($file.FullName)

    $builder = [Text.StringBuilder]::new($text.Length + 512)
    $position = 0
    $fileRenames = 0
    foreach ($match in $scanner.Matches($text)) {
        if (-not $match.Groups['id'].Success) { continue }
        $token = $match.Value

        $next = $match.Index + $match.Length
        while ($next -lt $text.Length -and [char]::IsWhiteSpace($text[$next])) { $next++ }
        $isCall = $next -lt $text.Length -and $text[$next] -eq '('
        $isMethodRef = $match.Index -ge 2 -and $text.Substring($match.Index - 2, 2) -eq '::'

        $replacement = $null
        if ($isCall -or $isMethodRef) {
            if (-not $methodMap.TryGetValue($token, [ref] $replacement)) {
                [void] $classMap.TryGetValue($token, [ref] $replacement)
            }
        } else {
            if (-not $fieldMap.TryGetValue($token, [ref] $replacement)) {
                [void] $classMap.TryGetValue($token, [ref] $replacement)
            }
        }
        if (-not $replacement) { continue }
        if (Test-PackageQualified $text $match.Index) { continue }

        [void] $builder.Append($text, $position, $match.Index - $position)
        [void] $builder.Append($replacement)
        $position = $match.Index + $match.Length
        $fileRenames++
    }

    if ($fileRenames -gt 0) {
        [void] $builder.Append($text, $position, $text.Length - $position)
        if (-not $DryRun) {
            [IO.File]::WriteAllText($file.FullName, $builder.ToString(), $utf8)
        }
        $changedFiles++
        $totalRenames += $fileRenames
    }
}

# --- 9. report ----------------------------------------------------------------

$report = [Collections.Generic.List[string]]::new()
$report.Add("Field renames in map: $($fieldMap.Count)")
$report.Add("Method renames in map: $($methodMap.Count)")
$report.Add("Class simple-name renames in map: $($classMap.Count)")
$report.Add("Changed source files: $changedFiles")
$report.Add("Renamed tokens: $totalRenames")
$report.Add('')
$report.Add('Probe (sanity-check common names):')
foreach ($probe in @('max', 'min', 'abs', 'floor', 'effect', 'client', 'world', 'rand', 'push', 'pop',
        'getWorld', 'getPos', 'getDefaultState', 'isRemote', 'add', 'get', 'set', 'size', 'apply')) {
    $inField = $null; $inMethod = $null; $inClass = $null
    [void] $fieldMap.TryGetValue($probe, [ref] $inField)
    [void] $methodMap.TryGetValue($probe, [ref] $inMethod)
    [void] $classMap.TryGetValue($probe, [ref] $inClass)
    $report.Add("  $probe`tfield=$inField`tmethod=$inMethod`tclass=$inClass")
}
$report.Add('')
$report.Add('Low-confidence renames (< 3 votes):')
$report.AddRange([string[]]($lowConfidenceReport | Sort-Object))
$report.Add('')
$report.Add("Ambiguous names below dominance threshold ${DominanceThreshold}:")
$report.AddRange([string[]]($ambiguousReport | Sort-Object))
[IO.File]::WriteAllLines((Join-Path $MappingDirectory 'member-remap-report.txt'), $report, $utf8)

$report | Select-Object -First 26
Write-Host "Full report: $(Join-Path $MappingDirectory 'member-remap-report.txt')"
