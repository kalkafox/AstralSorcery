param(
    [string] $MappingDirectory = "$PSScriptRoot\..\..\build\port-mappings",
    [string] $InputSourceDirectory,
    [string] $SourceDirectory = "$PSScriptRoot\..\..\src\main\java",
    [string] $MinecraftSourcesJar
)

$ErrorActionPreference = 'Stop'

function Normalize-ClassName([string] $name) {
    return $name.Replace('/', '.').Replace('$', '.')
}

$mcpConfig = Join-Path $MappingDirectory 'mcp_config\config\joined.tsrg'
$mojangMappings = Join-Path $MappingDirectory 'client_mappings_1.16.5.txt'

if (-not (Test-Path $mcpConfig) -or -not (Test-Path $mojangMappings)) {
    throw "Missing 1.16.5 mapping inputs in $MappingDirectory"
}

if (-not $MinecraftSourcesJar) {
    $MinecraftSourcesJar = Get-ChildItem "$env:USERPROFILE\.gradle\caches\neoformruntime\intermediate_results" `
            -Filter 'sourcesAndCompiledWithNeoForge*_output.jar' |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}

if (-not $MinecraftSourcesJar -or -not (Test-Path $MinecraftSourcesJar)) {
    throw 'Could not locate the generated NeoForge Minecraft sources jar'
}

if (-not $InputSourceDirectory) {
    $InputSourceDirectory = $SourceDirectory
}

$mcpByObfuscatedName = @{}
foreach ($line in [IO.File]::ReadLines($mcpConfig)) {
    if ($line.StartsWith("`t") -or [string]::IsNullOrWhiteSpace($line)) {
        continue
    }

    $parts = $line.Split(' ', [StringSplitOptions]::RemoveEmptyEntries)
    if ($parts.Length -eq 2) {
        $mcpByObfuscatedName[(Normalize-ClassName $parts[0])] = Normalize-ClassName $parts[1]
    }
}

$officialByObfuscatedName = @{}
foreach ($line in [IO.File]::ReadLines($mojangMappings)) {
    if ($line.StartsWith(' ') -or $line.StartsWith('#')) {
        continue
    }

    if ($line -match '^(\S+) -> ([^:]+):$') {
        $officialByObfuscatedName[(Normalize-ClassName $Matches[2])] = Normalize-ClassName $Matches[1]
    }
}

$official16ByMcpName = @{}
foreach ($entry in $mcpByObfuscatedName.GetEnumerator()) {
    if ($officialByObfuscatedName.ContainsKey($entry.Key)) {
        $official16ByMcpName[$entry.Value] = $officialByObfuscatedName[$entry.Key]
    }
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$official21Classes = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$classesBySimpleName = @{}
$archive = [IO.Compression.ZipFile]::OpenRead($MinecraftSourcesJar)
try {
    foreach ($entry in $archive.Entries) {
        if (-not $entry.FullName.EndsWith('.java', [StringComparison]::Ordinal)) {
            continue
        }
        if (-not $entry.FullName.StartsWith('net/minecraft/', [StringComparison]::Ordinal) -and
                -not $entry.FullName.StartsWith('com/mojang/', [StringComparison]::Ordinal)) {
            continue
        }

        $className = Normalize-ClassName $entry.FullName.Substring(0, $entry.FullName.Length - 5)
        [void] $official21Classes.Add($className)
        $simpleName = $className.Substring($className.LastIndexOf('.') + 1)
        if (-not $classesBySimpleName.ContainsKey($simpleName)) {
            $classesBySimpleName[$simpleName] = [Collections.Generic.List[string]]::new()
        }
        $classesBySimpleName[$simpleName].Add($className)
    }
}
finally {
    $archive.Dispose()
}

$importedMcpClasses = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$inputSourceFiles = Get-ChildItem $InputSourceDirectory -Recurse -Filter '*.java'
foreach ($file in $inputSourceFiles) {
    $text = [IO.File]::ReadAllText($file.FullName)
    foreach ($match in [regex]::Matches($text, '(?m)^import (net\.minecraft\.[^;*]+);')) {
        [void] $importedMcpClasses.Add($match.Groups[1].Value)
    }
}

$classReplacements = @{}
$unresolved = [Collections.Generic.List[string]]::new()
foreach ($mcpName in $importedMcpClasses) {
    if (-not $official16ByMcpName.ContainsKey($mcpName)) {
        $unresolved.Add("$mcpName`t(no 1.16 official mapping)")
        continue
    }

    $official16Name = $official16ByMcpName[$mcpName]
    if ($official21Classes.Contains($official16Name)) {
        $classReplacements[$mcpName] = $official16Name
        continue
    }

    $simpleName = $official16Name.Substring($official16Name.LastIndexOf('.') + 1)
    if ($classesBySimpleName.ContainsKey($simpleName) -and $classesBySimpleName[$simpleName].Count -eq 1) {
        $classReplacements[$mcpName] = $classesBySimpleName[$simpleName][0]
        continue
    }

    $unresolved.Add("$mcpName`t$official16Name")
}

$simpleNameTargets = @{}
$ambiguousSimpleNames = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
foreach ($entry in $classReplacements.GetEnumerator()) {
    $oldSimpleName = $entry.Key.Substring($entry.Key.LastIndexOf('.') + 1)
    $newSimpleName = $entry.Value.Substring($entry.Value.LastIndexOf('.') + 1)
    if ($oldSimpleName -eq $newSimpleName) {
        continue
    }
    if ($simpleNameTargets.ContainsKey($oldSimpleName) -and $simpleNameTargets[$oldSimpleName] -ne $newSimpleName) {
        [void] $ambiguousSimpleNames.Add($oldSimpleName)
        continue
    }
    $simpleNameTargets[$oldSimpleName] = $newSimpleName
}
foreach ($name in $ambiguousSimpleNames) {
    $simpleNameTargets.Remove($name)
}

$utf8 = [Text.UTF8Encoding]::new($false)
$changedFiles = 0
foreach ($file in $inputSourceFiles) {
    $text = [IO.File]::ReadAllText($file.FullName)
    $updated = $text.Replace('net.minecraftforge.eventbus.api', 'net.neoforged.bus.api')
    $updated = $updated.Replace('net.minecraftforge.fml', 'net.neoforged.fml')
    $updated = $updated.Replace('net.minecraftforge.api.distmarker', 'net.neoforged.api.distmarker')
    $updated = $updated.Replace('net.minecraftforge', 'net.neoforged.neoforge')

    $fileTokenReplacements = @{}
    foreach ($entry in $classReplacements.GetEnumerator()) {
        $fileTokenReplacements[$entry.Key] = $entry.Value
    }

    foreach ($match in [regex]::Matches($text, '(?m)^import (net\.minecraft\.[^;*]+);')) {
        $importName = $match.Groups[1].Value
        if (-not $classReplacements.ContainsKey($importName)) {
            continue
        }
        $oldSimpleName = $importName.Substring($importName.LastIndexOf('.') + 1)
        $targetName = $classReplacements[$importName]
        $newSimpleName = $targetName.Substring($targetName.LastIndexOf('.') + 1)
        if ($oldSimpleName -ne $newSimpleName) {
            $fileTokenReplacements[$oldSimpleName] = $newSimpleName
        }
    }

    foreach ($match in [regex]::Matches($text, '(?m)^import (net\.minecraft\.[^;]+)\.\*;')) {
        $packagePrefix = $match.Groups[1].Value + '.'
        foreach ($entry in $classReplacements.GetEnumerator()) {
            if (-not $entry.Key.StartsWith($packagePrefix, [StringComparison]::Ordinal)) {
                continue
            }
            $oldSimpleName = $entry.Key.Substring($entry.Key.LastIndexOf('.') + 1)
            if ($text -notmatch "\b$([regex]::Escape($oldSimpleName))\b") {
                continue
            }
            $newSimpleName = $entry.Value.Substring($entry.Value.LastIndexOf('.') + 1)
            if ($oldSimpleName -ne $newSimpleName) {
                $fileTokenReplacements[$oldSimpleName] = $newSimpleName
            }
        }
    }

    $tokensByLength = @($fileTokenReplacements.Keys | Sort-Object Length -Descending)
    $tokenPattern = if ($tokensByLength.Count -gt 0) {
        '(?<![\w.])(?:' + (($tokensByLength | ForEach-Object { [regex]::Escape($_) }) -join '|') + ')(?![\w.])'
    }

    if ($tokenPattern) {
        $updated = [regex]::Replace(
                $updated,
                $tokenPattern,
                [Text.RegularExpressions.MatchEvaluator] {
                    param($match)
                    $fileTokenReplacements[$match.Value]
                }
        )
    }

    $relativePath = [IO.Path]::GetRelativePath(
            [IO.Path]::GetFullPath($InputSourceDirectory),
            $file.FullName
    )
    $outputPath = Join-Path $SourceDirectory $relativePath
    if ($updated -ne $text) {
        [IO.File]::WriteAllText($outputPath, $updated, $utf8)
        $changedFiles++
    } elseif ([IO.Path]::GetFullPath($outputPath) -ne [IO.Path]::GetFullPath($file.FullName)) {
        [IO.File]::WriteAllText($outputPath, $text, $utf8)
    }
}

$report = [Collections.Generic.List[string]]::new()
$report.Add("Mapped imported classes: $($classReplacements.Count)")
$report.Add("Renamed simple class names: $($simpleNameTargets.Count)")
$report.Add("Changed source files: $changedFiles")
$report.Add("Unresolved imported classes: $($unresolved.Count)")
$report.Add('')
$report.AddRange([string[]]($unresolved | Sort-Object))
[IO.File]::WriteAllLines((Join-Path $MappingDirectory 'class-remap-report.txt'), $report, $utf8)

$report | Select-Object -First 80
