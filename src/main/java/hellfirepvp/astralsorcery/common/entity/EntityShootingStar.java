/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXRenderOffsetFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXScaleFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.data.ASDataSerializers;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.network.NetworkHooks;

import java.awt.*;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityShootingStar
 * Created by HellFirePvP
 * Date: 26.11.2020 / 19:30
 */
public class EntityShootingStar extends ThrowableProjectile {

    private static final EntityDataAccessor<Long> EFFECT_SEED = SynchedEntityData.createKey(EntityShootingStar.class, ASDataSerializers.LONG);

    protected EntityShootingStar(Level worldIn) {
        super(EntityTypesAS.SHOOTING_STAR, worldIn);
        this.entityData.set(EFFECT_SEED, random.nextLong());
    }

    protected EntityShootingStar(double x, double y, double z, Level worldIn) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    public static EntityType.IFactory<EntityShootingStar> factory() {
        return (type, level) -> new EntityShootingStar(level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.register(EFFECT_SEED, 0L);
    }

    public long getEffectSeed() {
        return this.entityData.get(EFFECT_SEED);
    }

    @Override
    public void tick() {
        adjustMotion();

        super.tick();

        if (level.isClientSide()) {
            spawnEffects();
        }
    }

    private void adjustMotion() {
        Vec3 motion = getDeltaMovement();
        double y = Math.min(-0.7F, motion.getY());
        setDeltaMovement(new Vec3(motion.getX(), y, motion.getZ()));
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEffects() {
        float maxRenderPosDist = 96F;

        VFXRenderOffsetFunction<FXFacingParticle> renderFn = (fx, iPos, pTicks) -> {
            Player pl = Minecraft.getInstance().player;
            if (pl == null) {
                return iPos;
            }
            Vector3 v = fx.getPosition().clone().subtract(Vector3.atEntityCorner(pl));
            if (v.length() <= maxRenderPosDist) {
                return iPos;
            }
            return Vector3.atEntityCorner(pl).add(v.normalize().mul(maxRenderPosDist));
        };
        VFXScaleFunction<EntityVisualFX> scaleFn = (fx, scaleIn, pTicks) -> {
            Player pl = Minecraft.getInstance().player;
            if (pl == null) {
                return scaleIn;
            }
            Vector3 v = fx.getPosition().clone().subtract(Vector3.atEntityCorner(pl));
            float mul = v.length() <= maxRenderPosDist ? 1 : (float) (maxRenderPosDist / (v.length()));
            return (scaleIn * 0.25F) + ((mul * scaleIn) - (scaleIn * 0.25F));
        };

        Vector3 thisPosition = Vector3.atEntityCorner(this);
        for (int i = 0; i < 4; i++) {
            if (random.nextFloat() > 0.75F) continue;
            Vector3 dir = new Vector3(this.getDeltaMovement()).clone().mul(random.nextFloat() * -0.6F);
            dir.setX(dir.getX() + random.nextFloat() * 0.008 * (random.nextBoolean() ? 1 : -1));
            dir.setZ(dir.getZ() + random.nextFloat() * 0.008 * (random.nextBoolean() ? 1 : -1));

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(thisPosition)
                    .color(VFXColorFunction.WHITE)
                    .setDeltaMovement(dir)
                    .setAlphaMultiplier(0.85F)
                    .setScaleMultiplier(1.2F + random.nextFloat() * 0.5F)
                    .scale(VFXScaleFunction.SHRINK.andThen(scaleFn))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .renderOffset(renderFn)
                    .setMaxAge(90 + random.nextInt(40));
        }

        float scale = 4F + random.nextFloat() * 3F;
        int age = 5 + random.nextInt(2);
        Random effectSeed = new Random(this.getEffectSeed());

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(thisPosition)
                .color(VFXColorFunction.constant(Color.getHSBColor(effectSeed.nextFloat() * 360F, 1F, 1F)))
                .setScaleMultiplier(scale)
                .scale(VFXScaleFunction.SHRINK.andThen(scaleFn))
                .renderOffset(renderFn)
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .setMaxAge(age);
        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(thisPosition)
                .color(VFXColorFunction.WHITE)
                .setScaleMultiplier(scale * 0.6F)
                .scale(VFXScaleFunction.SHRINK.andThen(scaleFn))
                .renderOffset(renderFn)
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .setMaxAge(Math.round(age * 1.5F));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        int chunkX = Mth.floor(this.getX() / 16.0D);
        int chunkZ = Mth.floor(this.getZ() / 16.0D);
        int newChunkX = Mth.floor(x / 16.0D);
        int newChunkZ = Mth.floor(z / 16.0D);
        if (chunkX != newChunkX || chunkZ != newChunkZ) {
            if (!this.getCommandSenderWorld().hasChunk(newChunkX, newChunkZ)) {
                this.remove();
                return;
            }
        }
        super.setPosition(x, y, z);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
