/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.assets;

import com.google.common.hash.Hashing;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.BlockPos;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PonderAttunementAltarStructureProvider
 * Created by HellFirePvP
 * Date: 20.07.2026
 *
 * Emits the Attunement Altar's build layout as a vanilla structure-NBT schematic for
 * the Ponder "how is this built" scene. Ponder loads scenes as plain structure-block
 * schematics (net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate),
 * so this hand-builds one from the exact same coordinates as
 * {@link hellfirepvp.astralsorcery.common.structure.PatternAttunementAltar} instead of
 * requiring an in-game "place it, then export" authoring step.
 */
public class PonderAttunementAltarStructureProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public PonderAttunementAltarStructureProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "ponder");
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        Path path = this.pathProvider.file(
                ResourceLocation.fromNamespaceAndPath(AstralSorcery.MODID, "altar/attunement_altar"), "nbt");
        try {
            Map<BlockPos, BlockState> blocks = AttunementAltarSchematics.buildAltarBlocks();
            byte[] bytes = serialize(AttunementAltarSchematics.toTemplate(blocks));
            cache.writeIfNeeded(path, bytes, Hashing.sha1().hashBytes(bytes));
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static byte[] serialize(StructureTemplate template) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.writeCompressed(template.save(new CompoundTag()), out);
        return out.toByteArray();
    }

    @Nonnull
    @Override
    public String getName() {
        return "Ponder Attunement Altar Schematic";
    }
}
