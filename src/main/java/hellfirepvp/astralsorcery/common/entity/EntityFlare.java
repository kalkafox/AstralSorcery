/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingSprite;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectBootes;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.config.entry.EntityConfig;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityFlare
 * Created by HellFirePvP
 * Date: 22.02.2020 / 08:23
 */
public class EntityFlare extends FlyingMob {

    private static final int RANDOM_WANDER_RANGE = 31;

    private int entityAge = 0;
    private Vector3 currentMoveTarget = null;

    private boolean ambient = false;
    private int followingEntityId = -1;

    private Object texClientSprite = null;

    public EntityFlare(Level worldIn) {
        super(EntityTypesAS.FLARE, worldIn);
    }

    public static EntityType.IFactory<EntityFlare> factory() {
        return (type, level) -> new EntityFlare(level);
    }

    public static AttributeSupplier.MutableAttribute createAttributes() {
        return Mob.createMobAttributes()
                .createMutableAttribute(Attributes.MAX_HEALTH, 1);
    }

    public static void spawnAmbientFlare(Level level, BlockPos at) {
        if (level.isClientSide() || EntityConfig.CONFIG.flareAmbientSpawnChance.get() <= 0) {
            return;
        }
        float nightPercent = DayTimeHelper.getCurrentDaytimeDistribution(level);
        if (level.random.nextInt(EntityConfig.CONFIG.flareAmbientSpawnChance.get()) == 0 && level.random.nextFloat() < nightPercent) {
            MiscUtils.executeWithChunk(level, at, () -> {
                if (level.isEmptyBlock(at)) {
                    EntityFlare flare = EntityTypesAS.FLARE.create(level);
                    flare.setPosition(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5);
                    flare.setAmbient(true);
                    level.addEntity(flare);
                }
            });
        }
    }

    public EntityFlare setAmbient(boolean ambient) {
        this.ambient = ambient;
        return this;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public EntityFlare setFollowingTarget(LivingEntity entity) {
        this.followingEntityId = entity.getId();
        return this;
    }

    @Nullable
    public LivingEntity getFollowingTarget() {
        if (this.followingEntityId == -1) {
            return null;
        }
        Entity e = level.getEntity(this.followingEntityId);
        if (e == null || !e.isAlive() || !(e instanceof LivingEntity)) {
            return null;
        }
        return (LivingEntity) e;
    }

    @Override
    public void tick() {
        super.tick();

        this.entityAge++;

        if (this.getCommandSenderWorld().isClientSide()) {
            this.tickClient();
        } else {
            if (this.isAmbient() && this.entityAge > 600 && random.nextInt(600) == 0) {
                DamageUtil.hurt(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
            }

            if (this.isAlive()) {
                if (EntityConfig.CONFIG.flareAttackBats.get() && random.nextInt(30) == 0) {
                    Bat closest = EntityUtils.getNearestEntity(this.getCommandSenderWorld(), Bat.class, this.getBoundingBox().grow(10), Vector3.atEntityCenter(this));
                    if (closest != null) {
                        this.doLightningAttack(closest, 100F);
                    }
                }
                if (EntityConfig.CONFIG.flareAttackPhantoms.get() && random.nextInt(30) == 0) {
                    Phantom closest = EntityUtils.getNearestEntity(this.getCommandSenderWorld(), Phantom.class, this.getBoundingBox().grow(10), Vector3.atEntityCenter(this));
                    if (closest != null) {
                        this.doLightningAttack(closest, 100F);
                    }
                }

                if (this.isAmbient()) {
                    boolean atTarget = this.currentMoveTarget == null || this.currentMoveTarget.distance(this) < 5.0;
                    if (atTarget) {
                        this.currentMoveTarget = null;
                    }
                    if (this.currentMoveTarget == null && random.nextInt(150) == 0) {
                        BlockPos newTarget = this.position()
                                .add(random.nextInt(RANDOM_WANDER_RANGE) * (random.nextBoolean() ? 1 : -1),
                                        random.nextInt(RANDOM_WANDER_RANGE) * (random.nextBoolean() ? 1 : -1),
                                        random.nextInt(RANDOM_WANDER_RANGE) * (random.nextBoolean() ? 1 : -1));

                        if (newTarget.getY() > 1 && newTarget.getY() < 254 && new Vector3(newTarget).distance(this) >= 5.0) {
                            MiscUtils.executeWithChunk(this.getCommandSenderWorld(), newTarget, () -> {
                                this.currentMoveTarget = new Vector3(newTarget);
                            });
                        }
                    }
                } else if (this.getAttackTarget() != null) {
                    if (!this.getAttackTarget().isAlive() || (this.getFollowingTarget() != null && this.getFollowingTarget().getDistance(this) > 30.0F)) {
                        this.setAttackTarget(null);
                    } else {
                        Vector3 newTarget = Vector3.atEntityCenter(this.getAttackTarget()).addY(1.5F);

                        if (newTarget.getY() > 1 && newTarget.getY() < 254 && newTarget.distance(this) >= 3.0) {
                            this.currentMoveTarget = newTarget;
                        } else {
                            this.currentMoveTarget = null;
                        }
                    }
                } else if (this.followingEntityId != -1) {
                    LivingEntity following = this.getFollowingTarget();
                    if (following == null) {
                        DamageUtil.hurt(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
                    } else {
                        MantleEffectBootes effect = ItemMantle.getEffect(following, ConstellationsAS.bootes);
                        if (effect == null) {
                            DamageUtil.hurt(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
                            return;
                        }

                        if (this.getAttackTarget() != null && !this.getAttackTarget().isAlive()){
                            this.setAttackTarget(null);
                        }
                        if(this.getAttackTarget() == null) {
                            Vector3 newTarget = Vector3.atEntityCenter(following).addY(2.5F);

                            if (newTarget.distance(this) >= 2.0) {
                                this.currentMoveTarget = newTarget;
                            } else {
                                this.currentMoveTarget = null;
                            }
                        }
                    }
                } else {
                    DamageUtil.hurt(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
                    return;
                }

                LivingEntity target = this.getAttackTarget();
                if (target != null && target.isAlive() && target.getDistance(this) < 10 && random.nextInt(40) == 0) {
                    DamageUtil.shotgunAttack(target, e -> this.doLightningAttack(e, 2F + random.nextFloat() * 2F));
                }

                this.doMovement();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        if (this.texClientSprite == null) {
            this.texClientSprite = EffectHelper.of(EffectTemplatesAS.FACING_SPRITE)
                    .spawn(Vector3.atEntityCorner(this).addY(this.getHeight() / 2))
                    .pickSprite(SpritesAS.SPR_ENTITY_FLARE)
                    .setScaleMultiplier(0.45F)
                    .position((fx, position, motionToBeMoved) -> Vector3.atEntityCorner(this).addY(this.getHeight() / 2))
                    .scale((fx, scaleIn, pTicks) -> this.isAlive() ? scaleIn : 0)
                    .refresh(fx -> this.isAlive());
        } else if (this.isAlive()) {
            EffectHelper.refresh((FXFacingSprite) this.texClientSprite, EffectTemplatesAS.FACING_SPRITE);
        }

        if (random.nextBoolean()) {
            FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this)
                            .add(random.nextFloat() * 0.2 * (random.nextBoolean() ? 1 : -1),
                                    this.getHeight() / 2 + random.nextFloat() * 0.2 * (random.nextBoolean() ? 1 : -1),
                                    random.nextFloat() * 0.2 * (random.nextBoolean() ? 1 : -1)))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F);
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        }
    }

    private void doLightningAttack(LivingEntity target, float damage) {
        DamageUtil.hurt(target, CommonProxy.DAMAGE_SOURCE_STELLAR, damage, this);
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIGHTNING)
                .addData(buf -> {
                    ByteBufUtils.writeVector(buf, Vector3.atEntityCorner(this).addY(getHeight() / 2F));
                    ByteBufUtils.writeVector(buf, Vector3.atEntityCorner(target).addY(target.getBbHeight() / 2F));
                    buf.writeInt(ColorsAS.EFFECT_LIGHTNING.getRGB());
                });
        PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(this.getCommandSenderWorld(), this.position(), 32));
    }

    private void doMovement() {
        if (this.currentMoveTarget != null) {
            Vec3 motion = this.getDeltaMovement();
            double motionX = (Math.signum(this.currentMoveTarget.getX() - this.getX()) * 0.5D - motion.getX()) * (this.isAmbient() ? 0.01D : 0.025D);
            double motionY = (Math.signum(this.currentMoveTarget.getY() - this.getY()) * 0.7D - motion.getY()) * (this.isAmbient() ? 0.01D : 0.025D);
            double motionZ = (Math.signum(this.currentMoveTarget.getZ() - this.getZ()) * 0.5D - motion.getZ()) * (this.isAmbient() ? 0.01D : 0.025D);
            this.setDeltaMovement(motion.add(motionX, motionY, motionZ));
            this.moveForward = 0.2F;
        }
    }

    @Override
    public void push(Entity entityIn) {
        if(!(entityIn instanceof Player)) {
            super.push(entityIn);
        }
    }

    @Override
    protected void doPush(Entity entityIn) {
        if(!(entityIn instanceof Player)) {
            super.push(entityIn);
        }
    }

    @Override
    protected void actuallyHurt(DamageSource source, float consumeItemDamage) {
        super.actuallyHurt(source, consumeItemDamage);
        this.setHealth(0F);
    }

    @Override
    public boolean checkBatSpawnRules(LevelAccessor worldIn, MobSpawnType spawnReasonIn) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void tickDeath() {
        this.remove();

        if (this.getCommandSenderWorld().isClientSide()) {
            this.tickClientDeathEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClientDeathEffects() {
        if (this.texClientSprite != null) {
            ((FXFacingSprite) this.texClientSprite).requestRemoval();
        }

        List<Vector3> posList = MiscUtils.getCirclePositions(
                Vector3.atEntityCorner(this).addY(this.getHeight() / 2),
                Vector3.positiveYRandom(),
                0.3,
                10);
        posList.addAll(MiscUtils.getCirclePositions(
                Vector3.atEntityCorner(this).addY(this.getHeight() / 2),
                Vector3.positiveYRandom(),
                0.8,
                20));

        posList.forEach(pos -> {
            FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos.add(Vector3.random().mul(0.45F)))
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.25F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setMaxAge(30 + random.nextInt(40));
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        });

        for (int i = 0; i < 10; i++) {
            FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this)
                            .add(random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                                    this.getHeight() / 2 + random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                                    random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1)))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setDeltaMovement(Vector3.random().mul(0.05F))
                    .setScaleMultiplier(0.25F + random.nextFloat() * 0.1F)
                    .setMaxAge(40 + random.nextInt(40));
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        }
    }

    @Override
    public void writeAdditional(CompoundTag pattern) {
        super.writeAdditional(pattern);

        pattern.putInt("AS_entityAge", this.entityAge);
        pattern.putBoolean("AS_ambient", this.ambient);
    }

    @Override
    public void readAdditional(CompoundTag pattern) {
        super.readAdditional(pattern);

        this.entityAge = pattern.getInt("AS_entityAge");
        this.ambient = pattern.getBoolean("AS_ambient");
    }
}
