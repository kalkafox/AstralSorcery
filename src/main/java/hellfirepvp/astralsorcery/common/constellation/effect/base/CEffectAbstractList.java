/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.base;

import com.mojang.datafixers.util.Either;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.lib.StructuresAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualLink;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockRandomPositionGenerator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectAbstractList
 * Created by HellFirePvP
 * Date: 11.06.2019 / 19:59
 */
public abstract class CEffectAbstractList<T extends CEffectAbstractList.ListEntry> extends ConstellationEffect {

    //Used to disable pedestal whitelisting when finding positions to break blocks at.
    protected boolean isLinkedRitual = false;
    private boolean excludesRitual = false; //Excludes exclusive ritual positions
    private boolean excludeRitualColumn = false; //Excludes everything from the pedestal and structure upwards

    protected final BlockPredicate verifier;
    protected final int maxAmount;
    private final BlockPositionGenerator positionStrategy;
    private final List<T> elements = new ArrayList<>();

    protected CEffectAbstractList(@Nonnull ILocatable origin, @Nonnull IWeakConstellation cst, int maxAmount, BlockPredicate verifier) {
        super(origin, cst);
        this.maxAmount = maxAmount;
        this.verifier = verifier;

        this.positionStrategy = this.createPositionStrategy();
    }

    protected void excludeRitualPositions() {
        this.setChunkNeedsToBeLoaded();
        if (!this.excludesRitual) {
            this.excludesRitual = true;
            this.positionStrategy.andFilter(this.createExcludeRitualPredicate());
        }
    }

    protected void excludeRitualColumn() {
        this.setChunkNeedsToBeLoaded();
        if (!this.excludeRitualColumn) {
            this.excludeRitualColumn = true;
            this.positionStrategy.andFilter(this.createExcludeRitualColumnPredicate());
        }
    }

    protected void selectSphericalPositions() {
        this.positionStrategy.andFilter((pos, radius) -> {
            double dst = new Vector3(this.getBlockPos().getLocationPos()).add(0.5, 0.5, 0.5)
                    .distanceSquared(new Vector3(pos).add(this.getBlockPos().getLocationPos()).add(0.5, 0.5, 0.5));
            return dst <= radius * radius;
        });
    }

    @Nullable
    public abstract T recreateElement(CompoundTag tag, BlockPos pos);

    @Nullable
    public abstract T createElement(Level level, BlockPos pos);

    @Nonnull
    protected BlockPositionGenerator createPositionStrategy() {
        return new BlockRandomPositionGenerator();
    }

    @Nonnull
    protected BlockPositionGenerator selectPositionStrategy(BlockPositionGenerator defaultGenerator, ConstellationEffectProperties properties) {
        return defaultGenerator;
    }

    private Predicate<BlockPos> createExcludeRitualPredicate() {
        return pos ->
                !pos.equals(BlockPos.ZERO) &&
                        this.isLinkedRitual || (
                                !pos.equals(TileRitualPedestal.RITUAL_ANCHOR_OFFEST) && (
                                        pos.getY() >= 3 || ( //Anything above the ritual is fine aswell
                                                !StructuresAS.STRUCT_RITUAL_PEDESTAL.hasBlockAt(pos) && //actual Ritual layer
                                                        !StructuresAS.STRUCT_RITUAL_PEDESTAL.hasBlockAt(pos.below()) && //Pillars & config
                                                        !StructuresAS.STRUCT_RITUAL_PEDESTAL.hasBlockAt(pos.below(2)) &&  //uh... consistency?
                                                        !StructuresAS.STRUCT_RITUAL_PEDESTAL.hasBlockAt(pos.below(3)) && //Lenses
                                                        !StructuresAS.STRUCT_RITUAL_PEDESTAL.hasBlockAt(pos.below(4))))); //Another layer of lenses
    }

    private Predicate<BlockPos> createExcludeRitualColumnPredicate() {
        return pos -> (this.isLinkedRitual && !(pos.getX() == 0 && pos.getZ() == 0)) ||
                (!this.isLinkedRitual && !StructuresAS.STRUCT_RITUAL_PEDESTAL.hasBlockAt(new BlockPos(pos.getX(), -1, pos.getZ())));
    }

    public int getCount() {
        return this.elements.size();
    }

    public void clear() {
        this.elements.clear();
    }

    public boolean isValid(Level level, T value) {
        return this.verifier.test(level, value.getBlockPos(), level.getBlockState(value.getBlockPos()));
    }

    @Nullable
    public T getRandomElement() {
        return MiscUtils.getRandomEntry(this.elements, random);
    }

    @Nullable
    public T getRandomElementChanced() {
        if (this.elements.isEmpty()) {
            return null;
        }
        float perc = 1F - (((float) this.getCount()) / this.maxAmount);
        perc = 0.1F / ((perc / 2F) + 0.1F);
        if (random.nextFloat() < perc) {
            return getRandomElement();
        }
        return null;
    }

    @Nonnull
    public Either<T, BlockPos> peekNewPosition(Level level, BlockPos pos, ConstellationEffectProperties prop) {
        if (this.excludesRitual || this.excludeRitualColumn) {
            MiscUtils.executeWithChunk(level, pos, () -> {
                this.isLinkedRitual = MiscUtils.getTileAt(level, pos, TileRitualLink.class, true) != null;
            });
        }
        BlockPositionGenerator gen = this.selectPositionStrategy(this.positionStrategy, prop);
        if (gen != this.positionStrategy) {
            gen.copyFilterFrom(this.positionStrategy);
        }
        BlockPos at = gen.generateNextPosition(new Vector3(0.5, 0.5, 0.5), prop.getSize());
        BlockPos actual = at.offset(pos);

        if (this.getCount() >= this.maxAmount) {
            return Either.right(actual);
        }
        return MiscUtils.executeWithChunk(level, actual, () -> {
            if (this.verifier.test(level, actual, level.getBlockState(actual))) {
                T value = this.createElement(level, actual);
                if (value == null) {
                    return Either.right(actual);
                } else {
                    return Either.left(value);
                }
            }
            return Either.right(actual);
        }, Either.right(actual));
    }

    @Nonnull
    public Either<T, BlockPos> findNewPosition(Level level, BlockPos pos, ConstellationEffectProperties prop) {
        return this.peekNewPosition(level, pos, prop).ifLeft(entry -> {
            if (!this.hasElement(entry.getBlockPos())) {
                this.elements.add(entry);
            }
        });
    }

    public boolean removeElement(T entry) {
        return removeElement(entry.getBlockPos());
    }

    public boolean removeElement(BlockPos pos) {
        return this.elements.removeIf(e -> e.getBlockPos().equals(pos));
    }

    public boolean hasElement(BlockPos pos) {
        return MiscUtils.contains(this.elements, e -> e.getBlockPos().equals(pos));
    }

    @Override
    public void readFromNBT(CompoundTag cmp) {
        super.readFromNBT(cmp);

        this.elements.clear();

        ListTag list = cmp.getList("elements", Constants.NBT.TAG_COMPOUND);
        for (Tag nbt : list) {
            CompoundTag tag = (CompoundTag) nbt;
            BlockPos pos = NBTHelper.readBlockPosFromNBT(tag);
            CompoundTag tagData = tag.getCompound("data");
            T value = this.recreateElement(tagData, pos);
            if (value != null) {
                value.readFromNBT(tagData);
                this.elements.add(value);
            }
        }
    }

    @Override
    public void save(CompoundTag cmp) {
        super.save(cmp);

        ListTag list = new ListTag();
        for (T value : this.elements) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(value.getBlockPos(), tag);

            CompoundTag dataTag = new CompoundTag();
            value.save(dataTag);
            tag.put("data", dataTag);

            list.add(tag);
        }
        cmp.put("elements", list);
    }

    public static interface ListEntry {

        public BlockPos getBlockPos();

        public void save(CompoundTag nbt);

        public void readFromNBT(CompoundTag nbt);

    }

    public static class CountConfig extends Config {

        private final int defaultMaxAmount;

        public ModConfigSpec.IntValue maxAmount;

        public CountConfig(String constellationName, double defaultRange, double defaultRangePerLens, int defaultMaxAmount) {
            super(constellationName, defaultRange, defaultRangePerLens);
            this.defaultMaxAmount = defaultMaxAmount;
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.maxAmount = cfgBuilder
                    .comment("Defines the amount of blocks this ritual will try to capture at most.")
                    .translation(translationKey("maxAmount"))
                    .defineInRange("maxAmount", this.defaultMaxAmount, 1, 2048);
        }
    }

}
