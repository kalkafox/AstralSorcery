/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.assets;

import hellfirepvp.astralsorcery.common.block.marble.BlockMarblePillar;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttunementAltarSchematics
 * Created by HellFirePvP
 * Date: 20.07.2026
 *
 * Shared hand-built structure-NBT layout for the Attunement Altar's Ponder schematics
 * (mirrors {@link hellfirepvp.astralsorcery.common.structure.PatternAttunementAltar}).
 * All block coordinates here are altar-relative and un-offset (altar at (0,0,0));
 * {@link #toTemplate(Map)} applies the offset needed to make every position >= 0
 * before serializing to a vanilla {@link StructureTemplate}.
 */
public class AttunementAltarSchematics {

    // PatternAttunementAltar's footprint spans x/z -9..9 and y -1..4 relative to the
    // altar block at (0,0,0); offset everything so the schematic's min corner is (0,0,0).
    public static final int OFFSET_X = 9;
    public static final int OFFSET_Y = 1;
    public static final int OFFSET_Z = 9;

    public static Map<BlockPos, BlockState> buildAltarBlocks() {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        BlockState arch = BlocksAS.MARBLE_ARCH.defaultBlockState();
        BlockState sooty = BlocksAS.BLACK_MARBLE_RAW.defaultBlockState();

        put(blocks, 0, 0, 0, BlocksAS.ATTUNEMENT_ALTAR.defaultBlockState());

        fillCube(blocks, arch, -7, -1, -8, 7, -1, -8);
        fillCube(blocks, arch, -7, -1, 8, 7, -1, 8);
        fillCube(blocks, arch, -8, -1, -7, -8, -1, 7);
        fillCube(blocks, arch, 8, -1, -7, 8, -1, 7);

        fillCube(blocks, sooty, -7, -1, -7, 7, -1, 7);

        pillar(blocks, -8, 0, -8);
        pillar(blocks, -8, 0, 8);
        pillar(blocks, 8, 0, -8);
        pillar(blocks, 8, 0, 8);

        cornerWing(blocks, arch, -1, -1);
        cornerWing(blocks, arch, -1, 1);
        cornerWing(blocks, arch, 1, -1);
        cornerWing(blocks, arch, 1, 1);

        return blocks;
    }

    // A Spectral Relay at altar-relative (x, 0, z), matching TileAttunementAltar's
    // constellation offset math (getConstellationPositions: worldY == altar's Y).
    public static void addRelay(Map<BlockPos, BlockState> blocks, int x, int z) {
        put(blocks, x, 0, z, BlocksAS.SPECTRAL_RELAY.defaultBlockState());
    }

    // Mirrors the five loose arch blocks PatternAttunementAltar places in each of the
    // four diagonal corners, outside the radius-8 pillar ring.
    private static void cornerWing(Map<BlockPos, BlockState> blocks, BlockState arch, int signX, int signZ) {
        put(blocks, signX * 9, -1, signZ * 9, arch);
        put(blocks, signX * 9, -1, signZ * 8, arch);
        put(blocks, signX * 9, -1, signZ * 7, arch);
        put(blocks, signX * 8, -1, signZ * 9, arch);
        put(blocks, signX * 7, -1, signZ * 9, arch);
    }

    private static void pillar(Map<BlockPos, BlockState> blocks, int x, int y, int z) {
        put(blocks, x, y, z, BlocksAS.MARBLE_RUNED.defaultBlockState());
        put(blocks, x, y + 1, z, pillarState(BlockMarblePillar.PillarType.BOTTOM));
        put(blocks, x, y + 2, z, pillarState(BlockMarblePillar.PillarType.MIDDLE));
        put(blocks, x, y + 3, z, pillarState(BlockMarblePillar.PillarType.TOP));
        put(blocks, x, y + 4, z, BlocksAS.MARBLE_CHISELED.defaultBlockState());
    }

    private static BlockState pillarState(BlockMarblePillar.PillarType type) {
        return BlocksAS.MARBLE_PILLAR.defaultBlockState().setValue(BlockMarblePillar.PILLAR_TYPE, type);
    }

    private static void fillCube(Map<BlockPos, BlockState> blocks, BlockState state, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    put(blocks, x, y, z, state);
                }
            }
        }
    }

    public static void put(Map<BlockPos, BlockState> blocks, int x, int y, int z, BlockState state) {
        blocks.put(new BlockPos(x + OFFSET_X, y + OFFSET_Y, z + OFFSET_Z), state);
    }

    public static StructureTemplate toTemplate(Map<BlockPos, BlockState> blocks) {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

        ListTag size = new ListTag();
        size.add(IntTag.valueOf(2 * OFFSET_X + 1));
        size.add(IntTag.valueOf(6));
        size.add(IntTag.valueOf(2 * OFFSET_Z + 1));
        root.put("size", size);
        root.put("entities", new ListTag());

        Map<BlockState, Integer> paletteIndices = new LinkedHashMap<>();
        ListTag palette = new ListTag();
        ListTag blockList = new ListTag();

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockState state = entry.getValue();
            Integer index = paletteIndices.get(state);
            if (index == null) {
                CompoundTag paletteEntry = new CompoundTag();
                paletteEntry.putString("Name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                if (state.getBlock() == BlocksAS.MARBLE_PILLAR) {
                    CompoundTag properties = new CompoundTag();
                    properties.putString("pillartype", state.getValue(BlockMarblePillar.PILLAR_TYPE).getSerializedName());
                    paletteEntry.put("Properties", properties);
                }
                palette.add(paletteEntry);
                index = palette.size() - 1;
                paletteIndices.put(state, index);
            }

            BlockPos pos = entry.getKey();
            ListTag posTag = new ListTag();
            posTag.add(IntTag.valueOf(pos.getX()));
            posTag.add(IntTag.valueOf(pos.getY()));
            posTag.add(IntTag.valueOf(pos.getZ()));

            CompoundTag blockEntry = new CompoundTag();
            blockEntry.put("pos", posTag);
            blockEntry.putInt("state", index);
            blockList.add(blockEntry);
        }

        root.put("palette", palette);
        root.put("blocks", blockList);

        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), root);
        return template;
    }
}
