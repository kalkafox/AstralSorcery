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
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityNocturnalSpark
 * Created by HellFirePvP
 * Date: 17.08.2019 / 08:59
 */
public class EntityNocturnalSpark extends ThrowableProjectile {

    private static final AABB NO_DUPE_BOX = new AABB(0, 0, 0, 1, 1, 1).inflate(15);

    private static final EntityDataAccessor<Boolean> SPAWNING = SynchedEntityData.defineId(EntityNocturnalSpark.class, EntityDataSerializers.BOOLEAN);
    private int ticksSpawning = 0;

    public EntityNocturnalSpark(Level level) {
        super(EntityTypesAS.NOCTURNAL_SPARK, level);
    }

    public EntityNocturnalSpark(double x, double y, double z, Level level) {
        super(EntityTypesAS.NOCTURNAL_SPARK, x, y, z, level);
    }

    public EntityNocturnalSpark(LivingEntity thrower, Level level) {
        super(EntityTypesAS.NOCTURNAL_SPARK, thrower, level);
        this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0F, 0.7F, 0.9F);
    }

    public static EntityType.EntityFactory<EntityNocturnalSpark> factory() {
        return (type, level) -> new EntityNocturnalSpark(level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPAWNING, false);
    }

    public void setSpawning() {
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(SPAWNING, true);
    }

    public boolean isSpawning() {
        return this.entityData.get(SPAWNING);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isAlive()) {
            return;
        }

        if (!level().isClientSide()) {
            removeLights();
            if (isSpawning()) {
                ticksSpawning++;
                spawnCycle();
                removeDuplicates();

                if (ticksSpawning > 200) {
                    remove(RemovalReason.DISCARDED);
                }
            }
        } else {
            spawnEffects();
        }
    }

    private void removeLights() {
        if (this.getCommandSenderWorld() instanceof ServerLevel) {
            ServerLevel sWorld = (ServerLevel) this.getCommandSenderWorld();
            if (this.tickCount % 5 == 0) {
                List<BlockPos> lights = BlockDiscoverer.searchForBlocksAround(
                        sWorld, this.blockPosition(), 8,
                        (level, pos, state) -> !(state.getBlock() instanceof AirBlock) && state.getDestroySpeed(level, pos) != -1 && state.getLightEmission(level, pos) > 3);
                for (BlockPos light : lights) {
                    if (!BlockUtils.breakBlockWithoutPlayer(sWorld, light, sWorld.getBlockState(light), ItemStack.EMPTY, true, true)) {
                        sWorld.removeBlock(light, false);
                    }
                }
            }
        }
    }

    private void removeDuplicates() {
        List<EntityNocturnalSpark> sparks = level().getEntitiesOfClass(EntityNocturnalSpark.class, NO_DUPE_BOX.move(position()));
        for (EntityNocturnalSpark spark : sparks) {
            if (this.equals(spark)) {
                continue;
            }
            if (!spark.isAlive() || !spark.isSpawning()) {
                continue;
            }
            spark.remove(RemovalReason.DISCARDED);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEffects() {
        if (isSpawning()) {
            for (int i = 0; i < 15; i++) {
                Vector3 thisPos = Vector3.atEntityCorner(this).addY(1);
                MiscUtils.applyRandomOffset(thisPos, random, 2 + random.nextInt(4));
                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(thisPos)
                        .setScaleMultiplier(4)
                        .alpha1arg(VFXAlphaFunction.PYRAMID)
                        .setAlphaMultiplier(0.7F)
                        .color(VFXColorFunction.constant(Color.BLACK));
                if (random.nextInt(5) == 0) {
                    randomizeColor(p);
                }
                if (random.nextInt(20) == 0) {
                    Vector3 at = Vector3.atEntityCorner(this);
                    MiscUtils.applyRandomOffset(at, random, 2);
                    Vector3 to = Vector3.atEntityCorner(this);
                    MiscUtils.applyRandomOffset(to, random, 2);

                    EffectHelper.of(EffectTemplatesAS.LIGHTNING)
                            .spawn(at)
                            .makeDefault(to)
                            .color(VFXColorFunction.constant(Color.BLACK));
                }
            }
        } else {
            FXFacingParticle p;
            for (int i = 0; i < 6; i++) {
                p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(Vector3.atEntityCorner(this))
                        .setDeltaMovement(new Vector3(
                            0.04F - random.nextFloat() * 0.08F,
                            0.04F - random.nextFloat() * 0.08F,
                            0.04F - random.nextFloat() * 0.08F
                        ))
                        .setScaleMultiplier(0.25F);
                randomizeColor(p);
            }

            p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this));
            p.setScaleMultiplier(0.6F);
            randomizeColor(p);

            p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this).add(getDeltaMovement().multiply(0.5, 0.5, 0.5)));
            p.setScaleMultiplier(0.6F);
            randomizeColor(p);
        }
    }

    private void spawnCycle() {
        if (random.nextInt(12) == 0 && level() instanceof ServerLevel) {
            BlockPos pos = blockPosition();
            pos.offset(random.nextInt(2) - random.nextInt(2), 1, random.nextInt(2) - random.nextInt(2));
            pos = BlockUtils.firstSolidDown(level(), pos).above();

            if (pos.distSqr(this.blockPosition()) >= 16) {
                return;
            }
            EntityUtils.performWorldSpawningAt((ServerLevel) level(), pos, MobCategory.MONSTER, MobSpawnType.SPAWNER, true,
                    EntityUtils.SpawnConditionFlags.IGNORE_SPAWN_CONDITIONS | EntityUtils.SpawnConditionFlags.IGNORE_ENTITY_COLLISION);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void randomizeColor(FXFacingParticle p) {
        switch (random.nextInt(3)) {
            case 0:
                p.color(VFXColorFunction.constant(ColorsAS.NOCTURNAL_POWDER_1));
                break;
            case 1:
                p.color(VFXColorFunction.constant(ColorsAS.NOCTURNAL_POWDER_2));
                break;
            case 2:
                p.color(VFXColorFunction.constant(ColorsAS.NOCTURNAL_POWDER_3));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (HitResult.Type.ENTITY.equals(result.getType())) {
            return;
        }
        Vec3 hit = result.getLocation();
        this.setSpawning();
        this.setPos(hit.x, hit.y, hit.z);
    }
}
