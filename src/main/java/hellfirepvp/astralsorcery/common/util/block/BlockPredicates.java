/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.tags.TagKey;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockPredicates
 * Created by HellFirePvP
 * Date: 29.11.2019 / 21:09
 */
public class BlockPredicates {

    public static BlockPredicate isInTag(TagKey<Block> blockTag) {
        return (level, pos, state) -> state.is(blockTag);
    }

    public static BlockPredicate isBlock(Block... blocks) {
        Set<Block> applicable = new HashSet<>(Arrays.asList(blocks));
        return (level, pos, state) -> applicable.contains(state.getBlock());
    }

    public static BlockPredicate isState(BlockState... states) {
        Set<BlockState> applicable = new HashSet<>(Arrays.asList(states));
        return (level, pos, state) -> applicable.contains(state);
    }

    public static <T extends BlockEntity> BlockPredicate doesTileExist(T tile, boolean loadTileWorldAndChunk) {
        ResourceKey<Level> dim = tile.getLevel().dimension();
        BlockEntityType<?> tileType = tile.getType();
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();

        return (level, pos, state) -> {
            if (loadTileWorldAndChunk || srv.levelKeys().contains(dim)) {
                Level foundWorld = srv.getLevel(dim);
                if (foundWorld == null) {
                    //If the intent was to load the world and it doesn't exist, then the tile doesn't exist either
                    //If the intent was to NOT load the world, but the world isn't there, we assume the tile still exists.
                    return !loadTileWorldAndChunk;
                }
                if (!loadTileWorldAndChunk && !foundWorld.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    return true;
                }
                BlockEntity te = MiscUtils.getTileAt(foundWorld, pos, BlockEntity.class, true);
                return te != null && te.getType().equals(tileType);
            }
            return true;
        };
    }
}
