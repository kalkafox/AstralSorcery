/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.TriFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.BlockSnapshot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TreeType
 * Created by HellFirePvP
 * Date: 04.09.2020 / 22:39
 */
public class TreeType {

    private static final List<TreeType> TYPES = new ArrayList<>();

    private final BiPredicate<Level, BlockPos> treeTest;
    private final TriFunction<ServerLevel, BlockPos, RandomSource, Supplier<List<BlockPos>>> treeGenerator;

    private TreeType(BiPredicate<Level, BlockPos> treeTest, TriFunction<ServerLevel, BlockPos, RandomSource, Supplier<List<BlockPos>>> treeGenerator) {
        this.treeTest = treeTest;
        this.treeGenerator = treeGenerator;
    }

    public static TreeType register(BiPredicate<Level, BlockPos> treeTest, TriFunction<ServerLevel, BlockPos, RandomSource, Supplier<List<BlockPos>>> treeGenerator) {
        TreeType type = new TreeType(treeTest, treeGenerator);
        TYPES.add(type);
        return type;
    }

    public Supplier<List<BlockPos>> getTreeGenerator(ServerLevel level, BlockPos pos, RandomSource random) {
        return this.treeGenerator.apply(level, pos, random);
    }

    @Nullable
    public static TreeType isTree(Level level, BlockPos pos) {
        for (TreeType type : TYPES) {
            if (type.treeTest.test(level, pos)) {
                return type;
            }
        }
        return null;
    }

    static {
        register((level, pos) -> {
            BlockState state = level.getBlockState(pos);
            return state.getBlock() instanceof SaplingBlock;
        }, (level, pos, random) -> {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof SaplingBlock sapling) {
                TreeGrower treeFeature = sapling.treeGrower; //AT'd public
                return () -> {
                    List<BlockSnapshot> blockSnapshots = MiscUtils.captureBlockChanges(level, () -> {
                        treeFeature.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
                    });
                    return blockSnapshots.stream()
                            .filter(snapshot -> {
                                BlockState current = snapshot.getCurrentState();
                                return current.is(BlockTags.LEAVES) || current.is(BlockTags.LOGS) || current.getBlock() instanceof VineBlock;
                            })
                            .map(BlockSnapshot::getPos)
                            .collect(Collectors.toList());
                };
            }
            return Collections::emptyList;
        });
    }
}
