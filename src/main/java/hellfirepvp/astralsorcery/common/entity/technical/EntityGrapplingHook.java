/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.technical;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperDamageCancelling;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.registry.IEntityAdditionalSpawnData;
import net.neoforged.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityGrapplingHook
 * Created by HellFirePvP
 * Date: 29.02.2020 / 18:17
 */
public class EntityGrapplingHook extends ThrowableProjectile implements IEntityAdditionalSpawnData {

    private static final EntityDataAccessor<Integer> PULLING_ENTITY = SynchedEntityData.createKey(EntityGrapplingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PULLING = SynchedEntityData.createKey(EntityGrapplingHook.class, EntityDataSerializers.BOOLEAN);

    private boolean launchedThrower = false;

    //Non-moving handling
    private int timeout = 0;
    private int previousDist = 0;

    public int despawning = -1;
    public float pullFactor = 0.0F;

    private LivingEntity throwingEntity;

    public EntityGrapplingHook(Level level) {
        super(EntityTypesAS.GRAPPLING_HOOK, level);
    }

    public EntityGrapplingHook(LivingEntity thrower, Level level) {
        super(EntityTypesAS.GRAPPLING_HOOK, thrower, level);
        this.shoot(Vector3.directionFromYawPitch(thrower.getYRot(), thrower.getXRot()), 1.5F);
        this.throwingEntity = thrower;
    }

    public static EntityType.IFactory<EntityGrapplingHook> factory() {
        return (spawnEntity, level) -> new EntityGrapplingHook(level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.register(PULLING, false);
        this.entityData.register(PULLING_ENTITY, -1);
    }

    public void setPulling(boolean pull, @Nullable LivingEntity hit) {
        this.entityData.set(PULLING, pull);
        this.entityData.set(PULLING_ENTITY, hit == null ? -1 : hit.getEntityId());
    }

    public boolean isPulling() {
        return this.entityData.get(PULLING);
    }

    @Nullable
    public LivingEntity getPulling() {
        int idPull = this.entityData.get(PULLING_ENTITY);
        if (idPull > 0) {
            try {
                return (LivingEntity) this.level().getEntityByID(idPull);
            } catch (Exception exc) {}
        }
        return null;
    }

    //0 = none, 1=basically gone
    public float despawnPercentage(float partial) {
        float p = despawning - (1 - partial);
        p /= 10;
        return Mth.clamp(p, 0, 1);
    }

    public boolean isDespawning() {
        return despawning != -1;
    }

    private void setDespawning() {
        if (despawning == -1) {
            despawning = 0;
        }
    }

    private void despawnTick() {
        despawning++;
        if (despawning > 10) {
            remove();
        }
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return this.throwingEntity != null ? this.throwingEntity : super.getOwner();
    }

    @Override
    protected float getGravity() {
        return this.isPulling() ? 0 : 0.03F;
    }

    @Override
    public void tick() {
        super.tick();

        if (getOwner() == null || !getOwner().isAlive()) {
            setDespawning();
        }
        if (!isPulling() && tickCount >= 30) {
            setDespawning();
        }

        if (level.isClientSide()) {
            if (!isPulling()) {
                this.pullFactor += 0.02F;
            } else {
                this.pullFactor *= 0.7F;
            }
        }

        if (isDespawning()) {
            despawnTick();

            if (level.isClientSide() && this.despawning == 3) {
                this.playDespawnSparkles();
            }
        } else {
            Entity thrower = getOwner();
            double dist = Math.max(0.01, thrower.getDistance(this));
            if (isAlive() && isPulling()) {
                if (getPulling() != null) {
                    LivingEntity at = getPulling();
                    this.setPosition(at.getX(), at.getY(), at.getZ());
                }

                if (((getPulling() != null && tickCount > 60 && dist < 2) || (getPulling() == null && tickCount > 15 && dist < 2)) || timeout > 15) {
                    setDespawning();
                } else {
                    thrower.fallDistance = -2F;

                    double mx = this.getX() - thrower.getX();
                    double my = this.getY() - thrower.getY();
                    double mz = this.getZ() - thrower.getZ();
                    mx /= dist * 5.0D;
                    my /= dist * 5.0D;
                    mz /= dist * 5.0D;
                    Vec3 v2 = new Vec3(mx, my, mz);
                    if (v2.length() > 0.25D) {
                        v2 = v2.normalize();
                        mx = v2.x / 4.0D;
                        my = v2.y / 4.0D;
                        mz = v2.z / 4.0D;
                    }
                    Vec3 motion = thrower.getDeltaMovement();
                    motion = motion.add(mx, my + 0.04F, mz);
                    if (!launchedThrower) {
                        motion = motion.add(0, 0.4F, 0);
                        launchedThrower = true;
                    }
                    thrower.setDeltaMovement(motion);

                    if (thrower instanceof Player) {
                        EventHelperDamageCancelling.markInvulnerableToNextDamage((Player) thrower, DamageSource.FALL);
                    }

                    int roughDst = (int) (dist / 2.5D);
                    if (roughDst >= this.previousDist) {
                        this.timeout += 1;
                    } else {
                        this.timeout = 0;
                    }
                    this.previousDist = roughDst;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playDespawnSparkles() {
        if (!isPulling()) {
            Vector3 ePos = RenderingVectorUtils.interpolatePosition(this, 1F);
            List<Vector3> positions = buildLine(1F);
            for (Vector3 pos : positions) {
                if (random.nextBoolean()) {
                    Vector3 motion = Vector3.random().mul(0.005F);
                    Vector3 at = pos.add(ePos);
                    FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                            .spawn(at)
                            .setScaleMultiplier(0.3F + random.nextFloat() * 0.3F)
                            .alpha1arg(VFXAlphaFunction.FADE_OUT)
                            .color(VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE))
                            .setDeltaMovement(motion)
                            .setMaxAge(25 + random.nextInt(20));
                    if (random.nextBoolean()) {
                        p.color(VFXColorFunction.WHITE);
                    }
                }
            }
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        int id = -1;
        if(this.throwingEntity != null) {
            id = this.throwingEntity.getEntityId();
        }
        buffer.writeInt(id);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        int id = additionalData.readInt();
        try {
            if (id > 0) {
                this.throwingEntity = (LivingEntity) level.getEntityByID(id);
            }
        } catch (Exception ignored) {}
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 64D;
        if (Double.isNaN(d0)) {
            d0 = 64D;
        }
        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return BlockEntity.INFINITE_EXTENT_AABB;
    }

    public List<Vector3> buildLine(float partial) {
        Entity thrower = getOwner();
        if (thrower == null) {
            return Collections.emptyList();
        }

        List<Vector3> list = Lists.newLinkedList();
        Vector3 interpThrower = RenderingVectorUtils.interpolatePosition(thrower, partial);
        Vector3 interpHook = RenderingVectorUtils.interpolatePosition(this, partial);
        Vector3 origin = new Vector3();
        Vector3 to = interpThrower.clone().subtract(interpHook).addY(thrower.getHeight() / 4);
        float lineLength = (float) (to.length() * 5);
        list.add(origin.clone());
        int iter = (int) lineLength;
        for (int xx = 1; xx < iter - 1; xx++) {
            float dist = xx * (lineLength / iter);
            double dx = (interpThrower.getX() - interpHook.getX())                            / iter * xx + Mth.sin(dist / 10.0F) * pullFactor;
            double dy = (interpThrower.getY() - interpHook.getY() + thrower.getHeight() / 2F) / iter * xx + Mth.sin(dist / 7.0F)  * pullFactor;
            double dz = (interpThrower.getZ() - interpHook.getZ())                            / iter * xx + Mth.sin(dist / 2.0F)  * pullFactor;
            list.add(new Vector3(dx, dy, dz));
        }
        list.add(to.clone());

        return list;
    }

    public void shoot(Vector3 dir, float ap) {
        super.shoot(dir.getX(), dir.getY(), dir.getZ(), ap, 0F);
    }

    @Override
    public void shoot(double x, double y, double z, float ap, float inaccuracy) {
        super.shoot(x, y, z, ap, 0F);
    }

    @Override
    protected void onHit(HitResult result) {
        Vec3 hit = result.getHitVec();
        switch (result.getType()) {
            case BLOCK:
                setPulling(true, null);
                break;
            case ENTITY:
                Entity e = ((EntityHitResult) result).getEntity();
                if (!(e instanceof LivingEntity) || (getOwner() != null && e.equals(getOwner()))) {
                    return;
                }
                setPulling(true, (LivingEntity) ((EntityHitResult) result).getEntity());
                hit = new Vec3(hit.x, hit.y + ((EntityHitResult) result).getEntity().getHeight() * 3 / 4, hit.z);
                break;
            default:
                break;
        }
        this.setDeltaMovement(0, 0, 0);
        this.setPosition(hit.x, hit.y, hit.z);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
