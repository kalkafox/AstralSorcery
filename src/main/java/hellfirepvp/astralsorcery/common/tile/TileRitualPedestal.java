/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import hellfirepvp.astralsorcery.client.effect.function.*;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXSpritePlane;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCrystalBase;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.base.TileAreaOfInfluence;
import hellfirepvp.astralsorcery.common.tile.base.network.TileReceiverBase;
import hellfirepvp.astralsorcery.common.tile.network.StarlightReceiverRitualPedestal;
import hellfirepvp.astralsorcery.common.util.EffectIncrementer;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.tile.TileInventoryFiltered;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileRitualPedestal
 * Created by HellFirePvP
 * Date: 09.07.2019 / 19:25
 */
public class TileRitualPedestal extends TileReceiverBase<StarlightReceiverRitualPedestal> implements CrystalAttributeTile, TileAreaOfInfluence {

    public static final BlockPos RITUAL_ANCHOR_OFFEST = new BlockPos(0, 5, 0);
    public static final Set<BlockPos> RITUAL_CIRCLE_OFFSETS;
    public static final int MAX_MIRROR_COUNT = 5;

    private TileInventoryFiltered inventory;
    private final Map<BlockPos, BlockState> offsetConfigurations = new HashMap<>();

    //Own sync data
    private UUID ownerUUID = null;
    private BlockPos ritualLinkTo = null;

    //network rec data
    private boolean working = false;
    private Map<BlockPos, Boolean> offsetMirrors = new HashMap<>();

    //client data
    private EffectIncrementer effectWork = new EffectIncrementer(64);
    private ConstellationEffect clientEffectInstance = null;
    private Object ritualHaloEffect = null;

    public TileRitualPedestal() {
        super(TileEntityTypesAS.RITUAL_PEDESTAL);

        this.inventory = new TileInventoryFiltered(this, () -> 1, Direction.DOWN);
        this.inventory.canExtract((slot, amount, existing) -> !existing.isEmpty());
        this.inventory.updateType(((slot, toAdd, existing) ->
                existing.isEmpty() && toAdd.getItem() instanceof ItemAttunedCrystalBase &&
                        ((ItemAttunedCrystalBase) toAdd.getItem()).getFocusConstellation(toAdd) != null));
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide()) {
            this.doesSeeSky();
            this.hasMultiblock();

            this.updateLinkTile();
            this.updateBlockConfigurations();
        }

        this.effectWork.update(this.working);

        if (getLevel().isClientSide() && this.working) {
            playEffects();
        }
    }

    private void updateBlockConfigurations() {
        if (tickCount % 20 == 0) {
            for (BlockPos offset : RITUAL_CIRCLE_OFFSETS) {
                BlockPos pos = getBlockPos().offset(offset);
                MiscUtils.executeWithChunk(getLevel(), pos, pos, (at) -> {
                    BlockState savedState = this.offsetConfigurations.get(offset);
                    if (getLevel().isEmptyBlock(at)) {
                        if (savedState != null) {
                            this.offsetConfigurations.remove(offset);
                            this.markForUpdate();
                        }
                    } else {
                        BlockState actualState = getLevel().getBlockState(at);
                        if (savedState == null || !savedState.equals(actualState)) {
                            this.offsetConfigurations.put(offset, actualState);
                            this.markForUpdate();
                        }
                    }
                });
            }
        }
    }

    private void updateLinkTile() {
        boolean hasLink = ritualLinkTo != null;
        BlockPos link = getBlockPos().offset(RITUAL_ANCHOR_OFFEST);
        TileRitualLink linkTile = MiscUtils.getTileAt(level, link, TileRitualLink.class, true);
        boolean hasLinkNow;
        if (linkTile != null) {
            this.ritualLinkTo = linkTile.getLinkedTo();
            hasLinkNow = this.ritualLinkTo != null;
        } else {
            this.ritualLinkTo = null;
            hasLinkNow = false;
        }
        if (hasLink != hasLinkNow) {
            markForUpdate();
        }
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return StructureTypesAS.PTYPE_RITUAL_PEDESTAL;
    }

    @Nonnull
    public Set<BlockState> getConfiguredBlockStates() {
        return Sets.newHashSet(this.offsetConfigurations.values());
    }

    //=========================================================================================
    // AoE highlighting effects
    //=========================================================================================


    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public Color getEffectColor() {
        if (!this.providesEffect()) {
            return null;
        }

        IWeakConstellation running = this.getRitualConstellation();
        if (running == null) {
            return null;
        }
        return running.getConstellationColor();
    }

    @Override
    public float getRadius() {
        if (!this.providesEffect()) {
            return 0F;
        }

        IWeakConstellation running = this.getRitualConstellation();
        if (running == null) {
            return 0F;
        }
        ConstellationEffect effect = ConstellationEffectRegistry.createInstance(this, running);
        if (effect == null) {
            return 0F;
        }
        ConstellationEffectProperties properties = effect.createProperties(this.getMirrorCount());
        if (properties != null) {
            if (this.getRitualTrait() != null) {
                this.getRitualTrait().affectConstellationEffect(properties);
            }
            if (!this.getCurrentCrystal().isEmpty()) {
                CrystalAttributes attributes = CrystalAttributes.getCrystalAttributes(this.getCurrentCrystal());
                if (attributes != null) {
                    properties.multiplySize(CrystalCalculations.getRitualEffectRangeFactor(this, attributes));
                }
            }
            return (float) properties.getSize() * 1.3F;
        }
        return 0;
    }

    @Nonnull
    @Override
    public BlockPos getEffectOriginPosition() {
        return this.getBlockPos();
    }

    @Nonnull
    @Override
    public Vector3 getEffectPosition() {
        return new Vector3(this.getEffectOriginPosition()).add(0.5F, 0.5F, 0.5F);
    }

    @Nonnull
    @Override
    public ResourceKey<Level> dimension() {
        return this.getLevel().dimension();
    }

    @Override
    public boolean providesEffect() {
        return this.isWorking() && !this.isRemoved();
    }

    //=========================================================================================
    // Client effects
    //=========================================================================================

    @OnlyIn(Dist.CLIENT)
    private void playEffects() {
        float alphaDaytime = DayTimeHelper.getCurrentDaytimeDistribution(getLevel());
        alphaDaytime *= 0.8F;

        float percRunning = this.effectWork.getAsPercentage();
        int chance = 15 + (int) ((1F - percRunning) * 50);

        if (random.nextInt(chance) == 0) {
            Vector3 from = new Vector3(this).add(0.5, 0.05, 0.5);
            MiscUtils.applyRandomOffset(from, random, 0.05F);

            EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                    .setOwner(this.ownerUUID)
                    .spawn(from)
                    .setup(from.clone().addY(6), 1.5F, 1.5F)
                    .setAlphaMultiplier(0.5F + (0.5F * alphaDaytime))
                    .setMaxAge(64);
        }

        if (this.ritualLinkTo != null) {
            if (random.nextBoolean()) {
                Vector3 at = new Vector3(this).add(0, 0.1, 0);
                at.add(random.nextFloat() * 0.5 + 0.25, 0, random.nextFloat() * 0.5 + 0.25);

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .setOwner(this.ownerUUID)
                        .spawn(at)
                        .setAlphaMultiplier(0.7F)
                        .color(VFXColorFunction.WHITE)
                        .setDeltaMovement(Vector3.positiveYRandom(random).addY(2).normalize().mul(0.4F))
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.15F)
                        .motion(VFXMotionController.target(() -> new Vector3(this).add(RITUAL_ANCHOR_OFFEST).add(0.5, 0.5, 0.5), 0.1F))
                        .setMaxAge(30 + random.nextInt(50));
            }
        }

        List<BlockPos> activeMirrors = this.offsetMirrors.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        IWeakConstellation ritualConstellation = getRitualConstellation();
        if (this.working && ritualConstellation != null) {
            if (!activeMirrors.isEmpty() && DayTimeHelper.isNight(getLevel())) {
                if (random.nextInt(chance * 2) == 0) {
                    Vector3 from = new Vector3(this).add(0.5, 0.1, 0.5);
                    MiscUtils.applyRandomOffset(from, random, 2F);
                    from.setY(getBlockPos().getY() - 0.6 + 1 * random.nextFloat() * (random.nextBoolean() ? 1 : -1));

                    EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                            .setOwner(this.ownerUUID)
                            .spawn(from)
                            .setup(from.clone().addY(5 + random.nextInt(3)), 1.3F, 1.3F)
                            .setAlphaMultiplier(alphaDaytime)
                            .color(VFXColorFunction.constant(ritualConstellation.getConstellationColor()))
                            .setMaxAge(64);
                }
            }

            if (this.ritualHaloEffect == null) {
                this.ritualHaloEffect = EffectHelper.of(EffectTemplatesAS.TEXTURE_SPRITE)
                        .spawn(new Vector3(this).add(0.5, 0.05, 0.5))
                        .pickSprite(SpritesAS.SPR_HALO_RITUAL)
                        .setAxis(Vector3.RotAxis.Y_AXIS)
                        .setNoRotation(25)
                        .setScaleMultiplier(6.5F)
                        .refresh(RefreshFunction.tileExistsAnd(this, (tile, effect) -> tile.isWorking() && !tile.getCurrentCrystal().isEmpty()));
            }

            if (this.ritualHaloEffect != null) {
                FXSpritePlane effectPlane = ((FXSpritePlane) this.ritualHaloEffect);
                EffectHelper.refresh(effectPlane, EffectTemplatesAS.TEXTURE_SPRITE);

                float dayTimeMul = DayTimeHelper.getCurrentDaytimeDistribution(this.getLevel());
                effectPlane.setAlphaMultiplier(Math.max(0.05F, dayTimeMul * 0.75F));
            }

            Vector3 offset = Vector3.random().setY(0).normalize().mul(random.nextFloat() * 4 * (random.nextBoolean() ? 1 : -1));
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .setOwner(this.ownerUUID)
                    .spawn(new Vector3(this).add(0.5, 0.02, 0.5).add(offset))
                    .setAlphaMultiplier(1F)
                    .setGravityStrength(-0.001F)
                    .color(VFXColorFunction.constant(ritualConstellation.getConstellationColor()))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.3F + random.nextFloat() * 0.15F)
                    .setMaxAge(25 + random.nextInt(15));

            if (this.clientEffectInstance != null && !this.clientEffectInstance.getConstellation().equals(ritualConstellation)) {
                this.clientEffectInstance = null;
            }
            if (this.clientEffectInstance == null) {
                this.clientEffectInstance = ConstellationEffectRegistry.createInstance(ILocatable.fromPos(getBlockPos()), ritualConstellation);
            }
            if (this.clientEffectInstance != null) {
                clientEffectInstance.playClientEffect(getLevel(), getBlockPos(), this, percRunning, this.isFullyEnhanced());
                if (this.ritualLinkTo != null && getLevel().isLoaded(this.ritualLinkTo)) {
                    clientEffectInstance.playClientEffect(getLevel(), this.ritualLinkTo, this, percRunning, this.isFullyEnhanced());
                }
            }

            CrystalAttributes prop = this.getAttributes();
            if (prop != null && random.nextInt(3) == 0) {
                for (int i = 0; i < 3; i++) {
                    Vector3 at = new Vector3(this)
                            .add(0.5, 1.35, 0.5)
                            .add(Vector3.random().mul(0.6F));
                    Vector3 motion = Vector3.random().mul(0.02F);

                    EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                            .setOwner(this.ownerUUID)
                            .spawn(at)
                            .setDeltaMovement(motion)
                            .setAlphaMultiplier(1F)
                            .color(VFXColorFunction.constant(ritualConstellation.getConstellationColor()))
                            .alpha1arg(VFXAlphaFunction.FADE_OUT)
                            .setScaleMultiplier(0.15F + random.nextFloat() * 0.05F)
                            .setMaxAge(16 + random.nextInt(15));
                }

                if (random.nextInt(3) == 0) {
                    Vector3 from = new Vector3(this).add(0.5, 1.2, 0.5);
                    Vector3 to;
                    if (activeMirrors.isEmpty()) {
                        to = new Vector3(this).add(0.5, 3.5 + random.nextFloat() * 2.5, 0.5);
                    } else {
                        BlockPos mirror = MiscUtils.getRandomEntry(activeMirrors, random).offset(this.getBlockPos());
                        to = new Vector3(mirror).add(0.5, 0.5, 0.5);
                    }

                    EffectHelper.of(EffectTemplatesAS.LIGHTNING)
                            .setOwner(this.ownerUUID)
                            .spawn(from)
                            .makeDefault(to)
                            .color(VFXColorFunction.constant(ritualConstellation.getConstellationColor()));
                }
            }
        }

        for (BlockPos mirror : this.offsetMirrors.keySet()) {
            if (tickCount % 32 == 0) {
                Vector3 source = new Vector3(this).add(0.5, 0.9, 0.5);
                Vector3 to = new Vector3(this).add(mirror).add(0.5, 0.5, 0.5);

                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .setOwner(this.ownerUUID)
                        .spawn(source)
                        .setup(to, 0.8F, 0.8F);

                if (this.ritualLinkTo != null && this.offsetMirrors.get(mirror)) {
                    source = new Vector3(this).add(RITUAL_ANCHOR_OFFEST).add(0.5, 0.5, 0.5);

                    EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                            .setOwner(this.ownerUUID)
                            .spawn(source)
                            .setup(to, 0.8F, 0.8F)
                            .color(VFXColorFunction.random());
                }
            }
        }
    }

    //=========================================================================================
    // Getters
    //=========================================================================================

    public boolean isWorking() {
        return this.working;
    }

    public Map<BlockPos, Boolean> getMirrors() {
        return MapStream.of(this.offsetMirrors)
                .mapKey(pos -> pos.offset(this.getBlockPos()))
                .toMap();
    }

    public int getMirrorCount() {
        return (int) this.offsetMirrors.values().stream()
                .filter(b -> b)
                .count();
    }

    public boolean isFullyEnhanced() {
        return this.working && this.offsetMirrors.size() == MAX_MIRROR_COUNT;
    }

    @Nullable
    public Player getOwner() {
        if (this.ownerUUID == null || this.level == null) {
            return null;
        }
        return this.level.getPlayerByUuid(this.ownerUUID);
    }

    @Nonnull
    public ItemStack getCurrentCrystal() {
        ItemStack crystal = this.inventory.getStackInSlot(0);
        return ItemUtils.copyStackWithSize(crystal, crystal.getCount());
    }

    @Nullable
    public BlockPos getRitualLinkTo() {
        return this.ritualLinkTo;
    }

    @Nonnull
    public EffectIncrementer getWorkEffectTimer() {
        return this.effectWork;
    }

    @Nullable
    public IWeakConstellation getRitualConstellation() {
        ItemStack crystal = this.inventory.getStackInSlot(0);
        if (!crystal.isEmpty() && crystal.getItem() instanceof ConstellationItem) {
            return ((ConstellationItem) crystal.getItem()).getAttunedConstellation(crystal);
        }
        return null;
    }

    @Nullable
    public IMinorConstellation getRitualTrait() {
        ItemStack crystal = this.inventory.getStackInSlot(0);
        if (!crystal.isEmpty() && crystal.getItem() instanceof ConstellationItem) {
            return ((ConstellationItem) crystal.getItem()).getTraitConstellation(crystal);
        }
        return null;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        ItemStack crystal = this.inventory.getStackInSlot(0);
        if (!crystal.isEmpty() && crystal.getItem() instanceof CrystalAttributeItem) {
            return ((CrystalAttributeItem) crystal.getItem()).getAttributes(crystal);
        }
        return null;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        ItemStack crystal = this.inventory.getStackInSlot(0);
        if (!crystal.isEmpty() && crystal.getItem() instanceof CrystalAttributeItem) {
            ((CrystalAttributeItem) crystal.getItem()).setAttributes(crystal, attributes);
        }
    }

    //=========================================================================================
    // Modifications
    //=========================================================================================

    public void setOwner(@Nullable UUID playerUUID) {
        this.ownerUUID = playerUUID;
        this.markForUpdate();
    }

    //Returns the ItemStack to be returned to the player.
    //Inventory change automatically marks this tile for update
    @Nonnull
    public ItemStack tryPlaceCrystalInPedestal(@Nonnull ItemStack crystal) {
        ItemStack currentCatalyst = this.inventory.getStackInSlot(0);
        ItemStack toInsert = ItemUtils.copyStackWithSize(crystal, Math.min(crystal.getCount(), 1));

        if (toInsert.isEmpty()) {
            if (!this.inventory.canTakeItemThroughFace(0, 1)) {
                return ItemStack.EMPTY;
            }

            if (currentCatalyst.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.inventory.setStackInSlot(0, ItemStack.EMPTY);
                return currentCatalyst;
            }
        } else {
            if (!this.inventory.canPlaceItemThroughFace(0, crystal)) {
                return crystal;
            }

            if (currentCatalyst.isEmpty()) {
                this.inventory.setStackInSlot(0, toInsert);
                return ItemUtils.copyStackWithSize(crystal, Math.max(0, crystal.getCount() - 1));
            } else {
                return crystal;
            }
        }
    }

    //Stuff sent over from StarlightReceiverRitualPedestal
    public void setReceiverData(boolean working, Map<BlockPos, Boolean> mirrorData, @Nullable CrystalAttributes newAttributes) {
        this.working = working;
        this.offsetMirrors = new HashMap<>(mirrorData);

        ItemStack crystal = this.getCurrentCrystal();
        if (!crystal.isEmpty() && crystal.getItem() instanceof CrystalAttributeItem) {
            if (newAttributes == null) {
                this.tryPlaceCrystalInPedestal(ItemStack.EMPTY);
            } else {
                this.setAttributes(newAttributes.copy());
            }
        }

        this.markForUpdate();

        this.preventNetworkSync();
    }

    //=========================================================================================
    // Misc and I/O
    //=========================================================================================

    @Nonnull
    @Override
    public StarlightReceiverRitualPedestal provideEndpoint(BlockPos at) {
        return new StarlightReceiverRitualPedestal(at);
    }

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);

        this.inventory = this.inventory.deserialize(pattern.getCompound("inventory"));
        this.ownerUUID = NBTHelper.getUUID(pattern, "ownerUUID", null);
        this.ritualLinkTo = NBTHelper.readFromSubTag(pattern, "ritualLinkTo", NBTHelper::readBlockPosFromNBT);
        this.working = pattern.getBoolean("working");

        this.offsetMirrors.clear();
        ListTag list = pattern.getList("mirrors", Constants.NBT.TAG_COMPOUND);
        for (Tag nbt : list) {
            CompoundTag tag = (CompoundTag) nbt;
            this.offsetMirrors.put(NBTHelper.readBlockPosFromNBT(tag), tag.getBoolean("connect"));
        }

        this.offsetConfigurations.clear();
        ListTag tagBlocks = pattern.getList("blockConfiguration", Constants.NBT.TAG_COMPOUND);
        for (Tag nbt : tagBlocks) {
            CompoundTag tag = (CompoundTag) nbt;
            this.offsetConfigurations.put(NBTHelper.readBlockPosFromNBT(tag), NBTHelper.getBlockState(tag, "state"));
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        pattern.put("inventory", this.inventory.serialize());
        if (this.ownerUUID != null) {
            pattern.putUUID("ownerUUID", this.ownerUUID);
        }
        if (this.ritualLinkTo != null) {
            NBTHelper.setAsSubTag(pattern, "ritualLinkTo", cmp -> NBTHelper.writeBlockPosToNBT(this.ritualLinkTo, cmp));
        }
        pattern.putBoolean("working", this.working);

        ListTag listPositions = new ListTag();
        for (Map.Entry<BlockPos, Boolean> posEntry : this.offsetMirrors.entrySet()) {
            CompoundTag cmp = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(posEntry.getKey(), cmp);
            cmp.putBoolean("connect", posEntry.getValue());
            listPositions.add(cmp);
        }
        pattern.put("mirrors", listPositions);

        ListTag listConfigurations = new ListTag();
        for (Map.Entry<BlockPos, BlockState> posEntry : this.offsetConfigurations.entrySet()) {
            CompoundTag cmp = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(posEntry.getKey(), cmp);
            NBTHelper.setBlock(cmp, "state", posEntry.getValue());
            listConfigurations.add(cmp);
        }
        pattern.put("blockConfiguration", listConfigurations);
    }

    @Nullable
    public IItemHandler getExposedItemHandler(@Nullable Direction side) {
        return this.inventory.getItemHandler(side);
    }

    static {
        Set<BlockPos> circleOffsets = Sets.newHashSet(
                new BlockPos(4, 0, 0),
                new BlockPos(4, 0, 1),
                new BlockPos(3, 0, 2),
                new BlockPos(2, 0, 3),
                new BlockPos(1, 0, 4),
                new BlockPos(0, 0, 4),
                new BlockPos(-1, 0, 4),
                new BlockPos(-2, 0, 3),
                new BlockPos(-3, 0, 2),
                new BlockPos(-4, 0, 1),
                new BlockPos(-4, 0, 0),
                new BlockPos(-4, 0, -1),
                new BlockPos(-3, 0, -2),
                new BlockPos(-2, 0, -3),
                new BlockPos(-1, 0, -4),
                new BlockPos(0, 0, -4),
                new BlockPos(1, 0, -4),
                new BlockPos(2, 0, -3),
                new BlockPos(3, 0, -2),
                new BlockPos(4, 0, -1)
        );
        Set<BlockPos> ritualOffsets = new HashSet<>(circleOffsets);
        circleOffsets.stream().map(pos -> pos.offset(0, 1, 0)).forEach(ritualOffsets::add);
        circleOffsets.stream().map(pos -> pos.offset(0, 2, 0)).forEach(ritualOffsets::add);
        RITUAL_CIRCLE_OFFSETS = ImmutableSet.copyOf(ritualOffsets);
    }
}
