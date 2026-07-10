/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.tile.BlockLens;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.item.lens.LensColorType;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.tile.base.network.TileTransmissionBase;
import hellfirepvp.astralsorcery.common.tile.network.StarlightTransmissionLens;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileLens
 * Created by HellFirePvP
 * Date: 24.08.2019 / 21:19
 */
public class TileLens extends TileTransmissionBase<IPrismTransmissionNode> implements CrystalAttributeTile {

    private CrystalAttributes attributes = null;
    private LensColorType colorType = null;

    private float accumulatedStarlight = 0;

    //So we can tell the client to render beams eventhough the actual connection doesn't exist.
    private List<BlockPos> occupiedConnections = new LinkedList<>();

    protected TileLens(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public TileLens() {
        super(TileEntityTypesAS.LENS);
    }

    @Override
    public void tick() {
        super.tick();

        if (colorType != null) {
            if (level.isClientSide()) {
                playColorEffects();
            }
            doColorEffects();
        }
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();

        this.markForUpdate();
    }

    public void transmissionTick(float starlightAmt, IWeakConstellation type) {
        this.accumulatedStarlight += starlightAmt;
        boolean networkSync = this.needsNetworkSync();
        this.markForUpdate();
        if (!networkSync) {
            this.preventNetworkSync();
        }
    }

    private void doColorEffects() {
        Level level = this.getLevel();
        if (!level.isClientSide() && !this.occupiedConnections.isEmpty()) {
            this.occupiedConnections.clear();
            markForUpdate();
            preventNetworkSync();
        }

        if (accumulatedStarlight <= 0) {
            return;
        }
        float effectMultiplier = accumulatedStarlight * 1.4F;
        accumulatedStarlight = 0;

        List<BlockPos> linked = getLinkedPositions();
        if (linked.isEmpty()) {
            return;
        }

        Vector3 thisVec = new Vector3(this).add(0.5, 0.5, 0.5);

        for (BlockPos linkedTo : linked) {
            PartialEffectExecutor exec = new PartialEffectExecutor((1F / ((float) linked.size())) * effectMultiplier, random);

            Vector3 to = new Vector3(linkedTo).add(0.5, 0.5, 0.5);
            RaytraceAssist rta = new RaytraceAssist(thisVec, to).includeEndPoint();
            if (colorType.getType().doBlockInteraction()) {
                if (!rta.isClear(level) && rta.positionHit() != null) {
                    BlockPos posHit = rta.positionHit();

                    BlockState stateHit = level.getBlockState(posHit);
                    colorType.blockInBeam(level, posHit, stateHit, exec);

                    if (!level.isClientSide()) {
                        this.occupiedConnections.add(posHit);
                    }
                } else {
                    if (!level.isClientSide()) {
                        this.occupiedConnections.add(linkedTo);
                    }
                }
            }
            if (colorType.getType().doEntityInteraction()) {
                exec.reset();

                rta.setCollectEntities(0.5);
                rta.isClear(level);
                List<Entity> found = rta.collectedEntities(level);
                found.forEach(e -> colorType.entityInBeam(level, thisVec, to, e, exec));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playColorEffects() {
        Vector3 at = new Vector3(this).add(0.5, 0.5, 0.5);
        Color lensColor = this.colorType.getColor();

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(this)
                        .add(0.2, 0.2, 0.2)
                        .add(random.nextFloat() * 0.6, random.nextFloat() * 0.6, random.nextFloat() * 0.6))
                .color(VFXColorFunction.constant(lensColor))
                .setScaleMultiplier(0.1F + random.nextFloat() * 0.15F);

        if (getTicksExisted() % 40 == 0) {
            for (BlockPos connected : this.occupiedConnections) {
                Vector3 to = new Vector3(connected).add(0.5, 0.5, 0.5);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(at)
                        .setup(to, 0.6, 0.6)
                        .color(VFXColorFunction.constant(lensColor));
            }
        }
    }

    public LensColorType setColorType(@Nullable LensColorType colorType) {
        if (this.getColorType() == colorType) {
            return colorType;
        }
        LensColorType prev = this.getColorType();
        this.colorType = colorType;
        this.markForUpdate();
        return prev;
    }

    @Nullable
    public LensColorType getColorType() {
        return colorType;
    }

    public Direction getPlacedAgainst() {
        BlockState state = level.getBlockState(getBlockPos());
        if (!(state.getBlock() instanceof BlockLens)) {
            return Direction.DOWN;
        }
        return state.get(BlockLens.PLACED_AGAINST);
    }

    @Override
    public boolean isSingleLink() {
        return true;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);

        this.attributes = CrystalAttributes.getCrystalAttributes(pattern);
        if (pattern.contains("colorType")) {
            this.colorType = LensColorType.byName(ResourceLocation.parse(pattern.getString("colorType")));
        } else {
            this.colorType = null;
        }
        this.occupiedConnections = NBTHelper.readList(pattern, "occupiedConnections", Constants.NBT.TAG_COMPOUND,
                nbt -> NBTHelper.readBlockPosFromNBT((CompoundTag) nbt));
    }

    @Override
    public void readNetNBT(CompoundTag pattern) {
        super.readNetNBT(pattern);
        this.accumulatedStarlight = pattern.getFloat("accumulatedStarlight");
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        if (this.attributes != null) {
            this.attributes.store(pattern);
        }
        if (this.colorType != null) {
            pattern.putString("colorType", this.colorType.getName().toString());
        }
        NBTHelper.writeList(pattern, "occupiedConnections", this.occupiedConnections,
                pos -> NBTHelper.writeBlockPosToNBT(pos, new CompoundTag()));
    }

    @Override
    public void writeNetNBT(CompoundTag pattern) {
        super.writeNetNBT(pattern);
        pattern.putFloat("accumulatedStarlight", this.accumulatedStarlight);
    }

    @Nonnull
    @Override
    public IPrismTransmissionNode provideTransmissionNode(BlockPos at) {
        return new StarlightTransmissionLens(at, this.attributes);
    }
}
