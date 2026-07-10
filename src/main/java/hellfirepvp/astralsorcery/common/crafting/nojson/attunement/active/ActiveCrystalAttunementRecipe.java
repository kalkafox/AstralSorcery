/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalCrystalAttunement;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.util.sound.FadeLoopSound;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttuneCrystalRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveCrystalAttunementRecipe
 * Created by HellFirePvP
 * Date: 05.12.2019 / 06:33
 */
public class ActiveCrystalAttunementRecipe extends AttunementRecipe.Active<AttuneCrystalRecipe> {

    private static final int DURATION_CRYSTAL_ATTUNEMENT = 500;

    private IConstellation constellation;
    private int entityId;

    //The item attunement sound, FadeLoopSound
    private Object itemAttuneSound;

    private Object innerOrbital1;
    private Object attunementFlare;

    public ActiveCrystalAttunementRecipe(AttuneCrystalRecipe recipe, IConstellation constellation, int crystalEntityId) {
        super(recipe);
        this.constellation = constellation;
        this.entityId = crystalEntityId;
    }

    public ActiveCrystalAttunementRecipe(AttuneCrystalRecipe recipe, CompoundTag nbt) {
        super(recipe);
        this.readFromNBT(nbt);
    }

    @Override
    public boolean matches(TileAttunementAltar altar) {
        if (!super.matches(altar)) {
            return false;
        }
        Entity entity;
        return (entity = altar.getLevel().getEntityByID(this.entityId)) != null &&
                entity.isAlive() &&
                entity instanceof ItemEntity &&
                this.constellation.equals(altar.getActiveConstellation()) &&
                AttuneCrystalRecipe.isApplicableCrystal((ItemEntity) entity, altar.getActiveConstellation());
    }

    @Override
    public void startCrafting(TileAttunementAltar altar) {

    }

    @Override
    public void stopCrafting(TileAttunementAltar altar) {

    }

    @Override
    public void finishRecipe(TileAttunementAltar altar) {
        ItemEntity crystal = this.getEntity(altar.getLevel());
        if (crystal != null) {
            ItemStack stack = crystal.getItem();
            if (!(stack.getItem() instanceof ConstellationItem) && stack.getItem() instanceof ItemCrystalBase) {
                CompoundTag tag = stack.getTag();
                stack = new ItemStack(((ItemCrystalBase) stack.getItem()).getTunedItemVariant(), stack.getCount());
                stack.setRepairCost(tag);
            }
            if (stack.getItem() instanceof ConstellationItem) {
                IWeakConstellation attuned = ((ConstellationItem) stack.getItem()).getAttunedConstellation(stack);
                IMinorConstellation trait = ((ConstellationItem) stack.getItem()).getTraitConstellation(stack);
                if (attuned == null) {
                    if (altar.getActiveConstellation() instanceof IWeakConstellation) {
                        ((ConstellationItem) stack.getItem()).setAttunedConstellation(stack, (IWeakConstellation) altar.getActiveConstellation());
                    }
                } else if (trait == null) {
                    if (altar.getActiveConstellation() instanceof IMinorConstellation) {
                        ((ConstellationItem) stack.getItem()).setTraitConstellation(stack, (IMinorConstellation) altar.getActiveConstellation());
                    }
                }
                crystal.setItem(stack);

                UUID throwerUUID = crystal.getThrower();
                if (throwerUUID != null) {
                    Player thrower = altar.getLevel().getPlayerByUuid(throwerUUID);
                    if (thrower instanceof ServerPlayer) {
                        AdvancementsAS.ATTUNE_CRYSTAL.trigger((ServerPlayer) thrower, altar.getActiveConstellation());
                    }
                }
            }
        }
    }

    @Override
    public void doTick(LogicalSide direction, TileAttunementAltar altar) {
        ItemEntity crystal = this.getEntity(altar.getLevel());
        if (crystal == null) {
            return;
        }

        Vector3 crystalHoverPos = new Vector3(altar).add(0.5, 1.4, 0.5);
        crystal.setPosition(crystalHoverPos.getX(), crystalHoverPos.getY(), crystalHoverPos.getZ());
        crystal.xo = crystalHoverPos.getX();
        crystal.yo = crystalHoverPos.getY();
        crystal.zo = crystalHoverPos.getZ();
        crystal.setDeltaMovement(0, 0, 0);

        if (direction.isClient()) {
            doClientTick(altar);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientTick(TileAttunementAltar altar) {
        Predicate<PositionedLoopSound> activeTest = s ->
                !altar.canPlayConstellationActiveEffects() ||
                        altar.getActiveRecipe() != this;

        if (this.itemAttuneSound == null || ((FadeLoopSound) this.itemAttuneSound).hasStoppedPlaying()) {
            this.itemAttuneSound = SoundHelper.playSoundLoopFadeInClient(SoundsAS.ATTUNEMENT_ATLAR_ITEM_LOOP,
                    new Vector3(altar).add(0.5, 1, 0.5), 1F, 1F, false, activeTest)
                    .setFadeInTicks(20)
                    .setFadeOutTicks(20);
        }

        if (this.getTick() == 0) {
            SoundHelper.playSoundClientWorld(SoundsAS.ATTUNEMENT_ATLAR_ITEM_START, altar.getBlockPos(), 1F, 1F);
        }

        if (this.getTick() >= 80) {
            if (attunementFlare == null || ((EntityComplexFX) attunementFlare).isRemoved()) {
                attunementFlare = EffectHelper.of(EffectTemplatesAS.FACING_SPRITE)
                        .spawn(new Vector3(altar).add(0.5, 1.75, 0.5))
                        .pickSprite(SpritesAS.SPR_ATTUNEMENT_FLARE)
                        .setScaleMultiplier(2.5F)
                        .refresh(fx -> altar.canPlayConstellationActiveEffects() &&
                                altar.getActiveRecipe() == this);
            }
        }

        Vector3 altarPos = new Vector3(altar).add(0.5, 0, 0.5);
        if (innerOrbital1 == null) {
            innerOrbital1 = EffectHelper.spawnSource(new FXOrbitalCrystalAttunement(altarPos.clone(), altarPos.clone().addY(1.75), this.constellation))
                    .setOrbitRadius(3)
                    .setBranches(4)
                    .setOrbitAxis(Vector3.RotAxis.Y_AXIS)
                    .setTicksPerRotation(200)
                    .refresh(RefreshFunction.tileExistsAnd(altar,
                            (tile, effect) -> tile.canPlayConstellationActiveEffects() &&
                                    tile.getActiveRecipe() == this));
        }

        VFXColorFunction<?> beamColor = VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE);
        if (getTick() >= 80 && getTick() % 40 == 0) {
            for (BlockPos pos : altar.getConstellationPositions(this.constellation)) {
                Vector3 from = new Vector3(pos).add(0.5, 0, 0.5);
                MiscUtils.applyRandomOffset(from, random, 0.1F);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(from.clone().addY(6), 1.2, 1.2)
                        .setAlphaMultiplier(0.8F)
                        .color(beamColor)
                        .setMaxAge(60);
            }
        }

        float total = DURATION_CRYSTAL_ATTUNEMENT;
        float percCycle = (float) (((getTick() % total) / total) * 2 * Math.PI);
        int parts = getTick() % 50 == 0 ? 180 : 6;
        Vector3 center = new Vector3(altar).add(0.5, 0.1, 0.5);
        float angleSwirl = 120F;
        float dst = 4.5F;

        for (int i = 0; i < parts; i++) {
            Vector3 v = Vector3.RotAxis.X_AXIS.clone();
            float originalAngle = (((float) i) / ((float) parts)) * 360F;
            double angle = originalAngle + (Mth.sin(percCycle) * angleSwirl);
            v.mirror(-Math.toRadians(angle), Vector3.RotAxis.Y_AXIS).normalize().mul(dst);
            Vector3 pos = center.clone();
            Vector3 mot = center.clone().subtract(pos.clone().add(v)).normalize().mul(0.14);

            int age = 20 + random.nextInt(30);
            float size = 0.2F + random.nextFloat() * 0.7F;
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .setScaleMultiplier(size)
                    .setDeltaMovement(mot)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(age);

            if (random.nextInt(6) == 0) {
                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .setScaleMultiplier(size * 1.4F)
                        .setDeltaMovement(mot)
                        .color(VFXColorFunction.constant(this.constellation.getConstellationColor()))
                        .setGravityStrength(-0.0004F + random.nextFloat() * -0.00015F)
                        .setMaxAge(age + 30);
            }
        }

        double scale = 7.0D;
        double edgeScale = (scale * 2 + 1);
        for (int i = 0; i < 7; i++) {
            Vector3 offset = new Vector3(altar).add(-scale, 0.1, -scale);
            if (random.nextBoolean()) {
                offset.add(edgeScale * (random.nextBoolean() ? 1 : 0), 0, random.nextFloat() * edgeScale);
            } else {
                offset.add(random.nextFloat() * edgeScale, 0, edgeScale * (random.nextBoolean() ? 1 : 0));
            }
            FXFacingParticle particle = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setGravityStrength(-0.0002F + random.nextFloat() * -0.0001F)
                    .setScaleMultiplier(0.3F + random.nextFloat() * 0.15F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(40 + random.nextInt(10));
            if (random.nextBoolean()) {
                particle.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
            }
        }

        if (this.getTick() >= 200) {
            for (int i = 0; i < 3; i++) {
                Vector3 at = new Vector3(altar).add(0.5, 0, 0.5);
                at.addX(random.nextFloat() * 7F * (random.nextBoolean() ? 1 : -1));
                at.addZ(random.nextFloat() * 7F * (random.nextBoolean() ? 1 : -1));

                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .setAlphaMultiplier(0.75F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setGravityStrength(-0.001F + random.nextFloat() * -0.0005F)
                        .color(VFXColorFunction.WHITE)
                        .setScaleMultiplier(0.3F + random.nextFloat() * 0.1F)
                        .setMaxAge(20 + random.nextInt(10));

                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
                if (getTick() >= 400) {
                    p.setScaleMultiplier(0.3F + random.nextFloat() * 0.15F);
                }
            }
        }

        if (getTick() >= 460) {
            if (getTick() % 5 == 0) {
                Vector3 from = new Vector3(altar).add(0.5, 0, 0.5);
                MiscUtils.applyRandomOffset(from, random, 0.25F);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(from.clone().addY(8), 2.4, 2)
                        .setAlphaMultiplier(0.8F)
                        .setMaxAge(30 + random.nextInt(15));
            }
        }

        if (getTick() >= (DURATION_CRYSTAL_ATTUNEMENT - 10)) {
            for (int i = 0; i < 25; i++) {
                Vector3 at = new Vector3(altar)
                        .add(0.5, 0, 0.5)
                        .addY(random.nextFloat() * 0.5 + random.nextFloat() * 0.5);

                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .color(VFXColorFunction.WHITE)
                        .setDeltaMovement(Vector3.random().setY(0).normalize().mul(0.025 + random.nextFloat() * 0.075))
                        .setAlphaMultiplier(0.75F)
                        .setScaleMultiplier(0.25F + random.nextFloat() * 0.15F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(60 + random.nextInt(40));

                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
            }
        }
    }

    @Override
    public boolean isFinished(TileAttunementAltar altar) {
        return this.getTick() >= DURATION_CRYSTAL_ATTUNEMENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void stopEffects(TileAttunementAltar altar) {
        if (isFinished(altar)) {
            SoundHelper.playSoundClientWorld(SoundsAS.ATTUNEMENT_ATLAR_ITEM_FINISH, altar.getBlockPos().above(), 1F, 1F);
        }
        if (innerOrbital1 != null) {
            ((EntityComplexFX) innerOrbital1).requestRemoval();
        }
        if (attunementFlare != null) {
            ((EntityComplexFX) attunementFlare).requestRemoval();
        }
    }

    @Nullable
    private ItemEntity getEntity(Level level) {
        Entity entity = level.getEntityByID(this.entityId);
        if (entity != null && entity.isAlive() && entity instanceof ItemEntity) {
            return (ItemEntity) entity;
        }
        return null;
    }

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);

        nbt.putString("constellation", this.constellation.getRegistryName().toString());
        nbt.putInt("entityId", this.entityId);
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);

        this.constellation = RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(ResourceLocation.parse(nbt.getString("constellation")));
        this.entityId = nbt.getInt("entityId");
    }
}
