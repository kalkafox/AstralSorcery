/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.FXSource;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalCollector;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.tile.crystal.CollectorCrystalType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationTile;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionSourceNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.IndependentCrystalSource;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.base.network.TileSourceBase;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileCollectorCrystal
 * Created by HellFirePvP
 * Date: 10.08.2019 / 19:55
 */
public class TileCollectorCrystal extends TileSourceBase<SimpleTransmissionSourceNode> implements CrystalAttributeTile, ConstellationTile {

    public static final BlockPos[] OFFSETS_LIQUID_STARLIGHT = new BlockPos[] {
            new BlockPos(-1, -4, -1),
            new BlockPos( 0, -4, -1),
            new BlockPos( 1, -4, -1),
            new BlockPos( 1, -4,  0),
            new BlockPos( 1, -4,  1),
            new BlockPos( 0, -4,  1),
            new BlockPos(-1, -4,  1),
            new BlockPos(-1, -4,  0),
    };

    private UUID playerUUID = null;
    private CrystalAttributes crystalAttributes;
    private CollectorCrystalType collectorType = CollectorCrystalType.ROCK_CRYSTAL;
    private IWeakConstellation constellationType;
    private IMinorConstellation constellationTrait;

    private Object[] effectOrbitals = new Object[4];

    public TileCollectorCrystal(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.COLLECTOR_CRYSTAL, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide()) {
            this.doesSeeSky();
            this.hasMultiblock();
        } else {
            this.playEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEffects() {
        Vector3 thisPos = new Vector3(this).add(0.5F, 0.5F, 0.5F);
        Vector3 particlePos = thisPos.clone();
        MiscUtils.applyRandomOffset(particlePos, random, 0.75F);

        if (this.isEnhanced() &&
                this.doesSeeSky() &&
                this.getCollectorType() == CollectorCrystalType.CELESTIAL_CRYSTAL &&
                this.getAttunedConstellation() != null) {

            Color c = this.getAttunedConstellation().getConstellationColor();

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(particlePos)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .setAlphaMultiplier(0.8F)
                    .color(VFXColorFunction.constant(c))
                    .setMaxAge(20 + random.nextInt(10));

            for (int i = 0; i < this.effectOrbitals.length; i++) {
                FXOrbitalCollector fxSource = (FXOrbitalCollector) this.effectOrbitals[i];
                if (fxSource == null) {
                    FXSource<?, ?> src = new FXOrbitalCollector(new Vector3(this).add(0.5F, 0.5F, 0.5F), c)
                            .setOrbitAxis(Vector3.random())
                            .setOrbitRadius(0.8F + random.nextFloat() * 0.5F)
                            .setTicksPerRotation(40 + random.nextInt(30));
                    EffectHelper.spawnSource(src);
                    this.effectOrbitals[i] = src;
                } else {
                    if (fxSource.canRemove() && fxSource.isRemoved()) {
                        this.effectOrbitals[i] = null;
                    }
                }
            }

            BlockPos starlightSource = MiscUtils.getRandomEntry(OFFSETS_LIQUID_STARLIGHT, random).offset(this.getBlockPos());

            Vector3 from = new Vector3(starlightSource).add(random.nextFloat(), 0.85F, random.nextFloat());
            Vector3 motion = thisPos.clone().subtract(from).normalize().mul(0.08F);
            Color particleColor = MiscUtils.eitherOf(random, Color.WHITE, c, c.brighter());

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(from)
                    .setDeltaMovement(motion)
                    .alpha1arg(VFXAlphaFunction.proximity(thisPos::clone, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.constant(particleColor))
                    .setMaxAge(30 + random.nextInt(10));

            if (random.nextInt(80) == 0) {
                EffectHelper.of(EffectTemplatesAS.LIGHTNING)
                        .spawn(thisPos)
                        .makeDefault(from)
                        .color(VFXColorFunction.constant(c));
            }
        } else {
            if (random.nextBoolean()) {
                Color c = this.getCollectorType().getDisplayColor();

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(particlePos)
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                        .setAlphaMultiplier(0.8F)
                        .color(VFXColorFunction.constant(c))
                        .setMaxAge(20 + random.nextInt(10));
            }
        }
    }

    public boolean isEnhanced() {
        return this.getCollectorType() == CollectorCrystalType.CELESTIAL_CRYSTAL && this.hasMultiblock();
    }

    public boolean isPlayerMade() {
        return this.getPlayerUUID() != null;
    }

    @Override
    public IWeakConstellation getAttunedConstellation() {
        return this.constellationType;
    }

    @Override
    public boolean setAttunedConstellation(@Nullable IWeakConstellation cst) {
        if (cst != this.constellationType) {
            markForUpdate();
        }
        this.constellationType = cst;
        return true;
    }

    @Override
    public IMinorConstellation getTraitConstellation() {
        return this.constellationTrait;
    }

    @Override
    public boolean setTraitConstellation(@Nullable IMinorConstellation cst) {
        if (cst != this.constellationTrait) {
            markForUpdate();
        }
        this.constellationTrait = cst;
        return true;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return crystalAttributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        if (this.crystalAttributes == null && attributes == null) {
            return;
        }

        // this.crystalAttributes null check is covered in .equals
        if (attributes == null ||
                !attributes.equals(this.crystalAttributes)) {
            markForUpdate();
        }
        this.crystalAttributes = attributes;
    }

    public CollectorCrystalType getCollectorType() {
        return collectorType;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void onSynced(UUID playerUUID, CollectorCrystalType collectorType) {
        this.playerUUID = playerUUID;
        this.collectorType = collectorType;
        if (this.collectorType == null) {
            this.collectorType = CollectorCrystalType.ROCK_CRYSTAL;
        }

        this.markForUpdate();
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        if (this.collectorType == CollectorCrystalType.CELESTIAL_CRYSTAL) {
            return StructureTypesAS.PTYPE_ENHANCED_COLLECTOR_CRYSTAL;
        }
        return null;
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.constellationType = NBTHelper.readOptional(pattern, "constellationType", (nbt) -> {
            IConstellation cst = IConstellation.readFromNBT(nbt);
            if (cst instanceof IWeakConstellation) {
                return (IWeakConstellation) cst;
            }
            return null;
        });
        this.constellationTrait = NBTHelper.readOptional(pattern, "constellationTrait", (nbt) -> {
            IConstellation cst = IConstellation.readFromNBT(nbt);
            if (cst instanceof IMinorConstellation) {
                return (IMinorConstellation) cst;
            }
            return null;
        });
        setAttributes(CrystalAttributes.getCrystalAttributes(pattern));
        this.crystalAttributes = CrystalAttributes.getCrystalAttributes(pattern);;
        this.collectorType = NBTHelper.readEnum(pattern, "collectorType", CollectorCrystalType.class);
        this.playerUUID = NBTHelper.readOptional(pattern, "playerUUID", (nbt) -> nbt.getUUID("playerUUID"));
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        if (getAttributes() != null) {
            getAttributes().store(pattern);
        }
        NBTHelper.writeOptional(pattern, "constellationType", this.constellationType, (nbt, cst) -> cst.save(nbt));
        NBTHelper.writeOptional(pattern, "constellationTrait", this.constellationTrait, (nbt, cst) -> cst.save(nbt));
        NBTHelper.writeEnum(pattern, "collectorType", this.collectorType);
        NBTHelper.writeOptional(pattern, "playerUUID", this.playerUUID, (nbt, uuid) -> nbt.putUUID("playerUUID", uuid));
    }

    public AABB getBoundingBoxForCulling() {
        return BOX.inflate(1).move(getBlockPos());
    }

    @Nonnull
    @Override
    public IIndependentStarlightSource provideNewSourceNode() {
        return new IndependentCrystalSource();
    }

    @Nonnull
    @Override
    public SimpleTransmissionSourceNode provideSourceNode(BlockPos at) {
        return new SimpleTransmissionSourceNode(at);
    }
}
