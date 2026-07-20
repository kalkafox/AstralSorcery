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
 * Class: PonderAttuningStructureProvider
 * Created by HellFirePvP
 * Date: 20.07.2026
 *
 * Emits a second schematic for the "Attuning" Ponder scene: the same Attunement Altar
 * as {@link PonderAttunementAltarStructureProvider}, plus four Spectral Relays placed at
 * the Octans constellation's star offsets. Coordinates are derived from
 * {@code TileAttunementAltar#getConstellationPositions} (altar-relative
 * {@code (starX/2 - 7, 0, starY/2 - 7)}) applied to Octans' four stars in
 * {@code RegistryConstellations} ((25,25), (17,5), (11,10), (4,6)), giving relay
 * offsets of (5,0,5), (1,0,-5), (-2,0,-2) and (-5,0,-4) - the constellation with the
 * fewest stars/connections, and the simplest to demonstrate.
 */
public class PonderAttuningStructureProvider implements DataProvider {

    // Octans' four star offsets, altar-relative (see TileAttunementAltar#getConstellationPositions).
    public static final int[][] OCTANS_RELAY_OFFSETS = {
            {5, 5},
            {1, -5},
            {-2, -2},
            {-5, -4},
    };

    private final PackOutput.PathProvider pathProvider;

    public PonderAttuningStructureProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "ponder");
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        Path path = this.pathProvider.file(
                ResourceLocation.fromNamespaceAndPath(AstralSorcery.MODID, "altar/attuning"), "nbt");
        try {
            Map<BlockPos, BlockState> blocks = AttunementAltarSchematics.buildAltarBlocks();
            for (int[] offset : OCTANS_RELAY_OFFSETS) {
                AttunementAltarSchematics.addRelay(blocks, offset[0], offset[1]);
            }
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
        return "Ponder Attunement Altar Attuning Schematic";
    }
}
