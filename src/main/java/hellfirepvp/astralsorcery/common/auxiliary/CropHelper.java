/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.IPlantable;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CropHelper
 * Created by HellFirePvP
 * Date: 11.06.2019 / 21:05
 */
public class CropHelper {

    public static final String GROWABLE = "growable";
    public static final String GROWABLE_CROP = "growable_crop";
    public static final String GROWABLE_REED = "growable_reed";
    public static final String GROWABLE_CACTUS = "growable_cactus";
    public static final String GROWABLE_NETHERWART = "growable_netherwart";
    public static final String HARVESTABLE = "harvestable";

    public static Map<String, Function<BlockPos, GrowablePlant>> growableFactoryWrapper = new HashMap<String, Function<BlockPos, GrowablePlant>>() {
        {
            put(GROWABLE, GrowableWrapper::new);
            put(GROWABLE_CROP, GrowableCropWrapper::new);
            put(GROWABLE_REED, GrowableReedWrapper::new);
            put(GROWABLE_CACTUS, GrowableCactusWrapper::new);
            put(GROWABLE_NETHERWART, GrowableNetherwartWrapper::new);
            put(HARVESTABLE, HarvestableWrapper::new);
        }
    };

    @Nullable
    public static GrowablePlant fromNBT(CompoundTag nbt, BlockPos pos) {
        return growableFactoryWrapper.getOrDefault(nbt.getString("identifier"), (p) -> null).apply(pos);
    }

    @Nullable
    public static GrowablePlant wrapPlant(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof CropBlock) {
            return new GrowableCropWrapper(pos);
        }
        if (b instanceof BonemealableBlock) {
            if (b instanceof GrassBlock) return null;
            if (b instanceof TallGrassBlock) return null;
            if (b instanceof DoublePlantBlock) return null;
            return new GrowableWrapper(pos);
        }
        if (b instanceof SugarCaneBlock) {
            if (isReedBase(level, pos)) {
                return new GrowableReedWrapper(pos);
            }
        }
        if (b instanceof CactusBlock) {
            if (isCactusBase(level, pos)) {
                return new GrowableCactusWrapper(pos);
            }
        }
        if (b instanceof NetherWartBlock) {
            return new GrowableNetherwartWrapper(pos);
        }
        return null;
    }

    @Nullable
    public static HarvestablePlant wrapHarvestablePlant(LevelAccessor level, BlockPos pos) {
        GrowablePlant growable = wrapPlant(level, pos);
        if (growable == null) return null; //Every plant has to be growable.
        Block block = level.getBlockState(growable.getBlockPos()).getBlock();
        if (growable instanceof GrowableCropWrapper) {
            return (GrowableCropWrapper) growable;
        }
        if (block instanceof SugarCaneBlock && growable instanceof GrowableReedWrapper) {
            return (GrowableReedWrapper) growable;
        }
        if (block instanceof CactusBlock && growable instanceof GrowableCactusWrapper) {
            return (GrowableCactusWrapper) growable;
        }
        if (block instanceof NetherWartBlock && growable instanceof GrowableNetherwartWrapper) {
            return (GrowableNetherwartWrapper) growable;
        }
        if (block instanceof IPlantable) {
            return new HarvestableWrapper(pos);
        }
        return null;
    }

    private static boolean isReedBase(LevelAccessor level, BlockPos pos) {
        return !level.getBlockState(pos.below()).getBlock().equals(Blocks.SUGAR_CANE);
    }

    private static boolean isCactusBase(LevelAccessor level, BlockPos pos) {
        return !level.getBlockState(pos.below()).getBlock().equals(Blocks.CACTUS);
    }

    public static interface GrowablePlant extends CEffectAbstractList.ListEntry {

        public String getIdentifier();

        public boolean isValid(LevelAccessor level);

        public boolean canGrow(LevelAccessor level);

        public boolean tryGrow(LevelAccessor level, Random random);

        @Override
        default void readFromNBT(CompoundTag nbt) {}

        @Override
        default void save(CompoundTag nbt) {
            nbt.putString("identifier", this.getIdentifier());
        }
    }

    public static interface HarvestablePlant extends GrowablePlant {

        public boolean canHarvest(LevelAccessor level);

        public NonNullList<ItemStack> harvestDropsAndReplant(ServerLevel level, Random random, int harvestFortune);

    }

    public static class HarvestableWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public HarvestableWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean canHarvest(LevelAccessor level) {
            BlockState at = level.getBlockState(pos);
            if (!(at.getBlock() instanceof BonemealableBlock)) return false;
            if (at.getBlock() instanceof StemBlock) return false;
            return !((BonemealableBlock) at.getBlock()).canGrow(level, pos, at, false);
        }

        @Override
        public NonNullList<ItemStack> harvestDropsAndReplant(ServerLevel level, Random random, int harvestFortune) {
            NonNullList<ItemStack> drops = NonNullList.create();
            if (canHarvest(level)) {
                BlockPos pos = getBlockPos();
                BlockState at = level.getBlockState(getBlockPos());
                if (at.getBlock() instanceof IPlantable) {
                    drops.addAll(BlockUtils.getDrops(level, pos, harvestFortune, random));
                    level.setBlock(pos, ((IPlantable) at.getBlock()).getPlant(level, pos));
                }
            }
            return drops;
        }

        @Override
        public String getIdentifier() {
            return HARVESTABLE;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public boolean isValid(LevelAccessor level) {
            return wrapHarvestablePlant(level, getBlockPos()) instanceof HarvestableWrapper;
        }

        @Override
        public boolean canGrow(LevelAccessor level) {
            BlockState at = level.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock) {
                if (((BonemealableBlock) at.getBlock()).canGrow(level, pos, at, false)) {
                    return true;
                }
                if (at.getBlock() instanceof StemBlock) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor level, Random random) {
            if (!(level instanceof ServerLevel)) {
                return false;
            }
            BlockState at = level.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock) {
                if (((BonemealableBlock) at.getBlock()).canGrow(level, pos, at, false)) {
                    ((BonemealableBlock) at.getBlock()).grow((ServerLevel) level, random, pos, at);
                    return true;
                }
                if (at.getBlock() instanceof StemBlock && random.nextInt(4) == 0) {
                    at.randomTick((ServerLevel) level, pos, random);
                }
            }
            return false;
        }

    }

    public static class GrowableNetherwartWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableNetherwartWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean isValid(LevelAccessor level) {
            return level.getBlockState(pos).getBlock() instanceof NetherWartBlock;
        }

        @Override
        public boolean canGrow(LevelAccessor level) {
            BlockState at = level.getBlockState(pos);
            return at.getBlock() instanceof NetherWartBlock && at.get(NetherWartBlock.AGE) < 3;
        }

        @Override
        public boolean tryGrow(LevelAccessor level, Random random) {
            if (random.nextBoolean()) {
                BlockState current = level.getBlockState(pos);
                return level.setBlock(pos, current.setValue(NetherWartBlock.AGE, (Math.min(3, current.get(NetherWartBlock.AGE) + 1))), Constants.BlockFlags.DEFAULT);
            }
            return false;
        }

        @Override
        public boolean canHarvest(LevelAccessor level) {
            BlockState current = level.getBlockState(pos);
            return current.getBlock() instanceof NetherWartBlock && current.get(NetherWartBlock.AGE) >= 3;
        }

        @Override
        public NonNullList<ItemStack> harvestDropsAndReplant(ServerLevel level, Random random, int harvestFortune) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            stacks.addAll(BlockUtils.getDrops(level, pos, harvestFortune, random));
            level.setBlock(pos, Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 0), Constants.BlockFlags.DEFAULT);
            return stacks;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_NETHERWART;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

    }

    public static class GrowableCactusWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableCactusWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean canHarvest(LevelAccessor level) {
            return level.getBlockState(pos.above()).getBlock() instanceof CactusBlock;
        }

        @Override
        public boolean isValid(LevelAccessor level) {
            return level.getBlockState(pos).getBlock() instanceof CactusBlock;
        }

        @Override
        public NonNullList<ItemStack> harvestDropsAndReplant(ServerLevel level, Random random, int harvestFortune) {
            NonNullList<ItemStack> drops = NonNullList.create();
            for (int i = 2; i > 0; i--) {
                BlockPos bp = pos.above(i);
                BlockState at = level.getBlockState(bp);
                if (at.getBlock() instanceof CactusBlock) {
                    drops.addAll(BlockUtils.getDrops(level, pos, harvestFortune, random));
                    level.removeBlock(bp, false);
                }
            }
            return drops;
        }

        @Override
        public boolean canGrow(LevelAccessor level) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = level.getBlockState(cache);
                if (upState.isAir(level, cache)) {
                    return true;
                } else if (!(upState.getBlock() instanceof CactusBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor level, Random random) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = level.getBlockState(cache);
                if (upState.isAir(level, cache)) {
                    if (random.nextBoolean()) {
                        return level.setBlock(cache, Blocks.CACTUS.defaultBlockState(), Constants.BlockFlags.DEFAULT);
                    } else {
                        return false;
                    }
                } else if (!(upState.getBlock() instanceof CactusBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_CACTUS;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }
    }

    public static class GrowableReedWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableReedWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean canHarvest(LevelAccessor level) {
            return level.getBlockState(pos.above()).getBlock() instanceof SugarCaneBlock;
        }

        @Override
        public NonNullList<ItemStack> harvestDropsAndReplant(ServerLevel level, Random random, int harvestFortune) {
            NonNullList<ItemStack> drops = NonNullList.create();
            for (int i = 2; i > 0; i--) {
                BlockPos bp = pos.above(i);
                BlockState at = level.getBlockState(bp);
                if (at.getBlock() instanceof SugarCaneBlock) {
                    drops.addAll(BlockUtils.getDrops(level, pos, harvestFortune, random));
                    level.removeBlock(bp, false);
                }
            }
            return drops;
        }

        @Override
        public boolean isValid(LevelAccessor level) {
            return level.getBlockState(pos).getBlock() instanceof SugarCaneBlock;
        }

        @Override
        public boolean canGrow(LevelAccessor level) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = level.getBlockState(cache);
                if (upState.isAir(level, cache)) {
                    return true;
                } else if (!(upState.getBlock() instanceof SugarCaneBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor level, Random random) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = level.getBlockState(cache);
                if (upState.isAir(level, cache)) {
                    if (random.nextBoolean()) {
                        return level.setBlock(cache, Blocks.SUGAR_CANE.defaultBlockState(), Constants.BlockFlags.DEFAULT);
                    } else {
                        return false;
                    }
                } else if (!(upState.getBlock() instanceof SugarCaneBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_REED;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

    }

    public static class GrowableCropWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableCropWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean isValid(LevelAccessor level) {
            return wrapPlant(level, this.pos) instanceof GrowableCropWrapper;
        }

        @Override
        public boolean canGrow(LevelAccessor level) {
            BlockState state = level.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock) {
                return ((CropBlock) state.getBlock()).canGrow(level, pos, state, false);
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor level, Random random) {
            BlockState state = level.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock) {
                CropBlock block = (CropBlock) state.getBlock();
                if (block.canGrow(level, pos, state, false)) {
                    int age = state.get(block.getAgeProperty());
                    int next = Math.min(age + 1, block.getMaxAge());
                    return level.setBlock(pos, block.getStateForAge(next), Constants.BlockFlags.DEFAULT);
                }
            }
            return false;
        }

        @Override
        public boolean canHarvest(LevelAccessor level) {
            BlockState state = level.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock) {
                return !((CropBlock) state.getBlock()).canGrow(level, pos, state, false);
            }
            return false;
        }

        @Override
        public NonNullList<ItemStack> harvestDropsAndReplant(ServerLevel level, Random random, int harvestFortune) {
            NonNullList<ItemStack> drops = NonNullList.create();
            BlockState state = level.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock) {
                CropBlock block = (CropBlock) state.getBlock();

                drops.addAll(BlockUtils.getDrops(level, pos, harvestFortune, random));
                int startingAge = MiscUtils.getMinEntry(block.getAgeProperty().getPossibleValues());
                level.setBlock(pos, block.getStateForAge(startingAge));
            }
            return drops;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_CROP;
        }

        @Override
        public BlockPos getBlockPos() {
            return this.pos;
        }
    }

    public static class GrowableWrapper implements GrowablePlant {

        private final BlockPos pos;

        public GrowableWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public boolean isValid(LevelAccessor level) {
            return wrapPlant(level, pos) instanceof GrowableWrapper;
        }

        @Override
        public boolean canGrow(LevelAccessor level) {
            BlockState at = level.getBlockState(pos);
            return at.getBlock() instanceof BonemealableBlock && (
                    ((BonemealableBlock) at.getBlock()).canGrow(level, pos, at, false) ||
                            (at.getBlock() instanceof StemBlock && !stemHasCrop(level, ((StemBlock) at.getBlock()).getFruit()))
            );
        }

        private boolean stemHasCrop(LevelAccessor level, Block stemGrownBlock) {
            for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                Block offset = level.getBlockState(pos.offset(enumfacing)).getBlock();
                if (offset.equals(stemGrownBlock)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor level, Random random) {
            BlockState at = level.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock && level instanceof ServerLevel) {
                if (((BonemealableBlock) at.getBlock()).canGrow(level, pos, at, false)) {
                    if (!((BonemealableBlock) at.getBlock()).performBonemeal((Level) level, random, pos, at)) {
                        if (random.nextInt(20) != 0) {
                            return true; //Returning true to say it could've been potentially grown - So this doesn't invalidate caches.
                        }
                    }
                    ((BonemealableBlock) at.getBlock()).grow((ServerLevel) level, random, pos, at);
                    return true;
                }
                if (at.getBlock() instanceof StemBlock) {
                    at.randomTick((ServerLevel) level, pos, random);
                    return true;
                }
            }
            return false;
        }
    }

}
