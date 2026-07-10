/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.impl.RenderOffsetNoisePlane;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingSprite;
import hellfirepvp.astralsorcery.client.effect.vfx.FXSpritePlane;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.block.tile.fountain.BlockFountainPrime;
import hellfirepvp.astralsorcery.common.data.config.registry.TechnicalEntityRegistry;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperEntityFreeze;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.TileFountain;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FountainEffectVortex
 * Created by HellFirePvP
 * Date: 01.11.2020 / 11:25
 */
public class FountainEffectVortex extends FountainEffect<VortexContext> {

    public FountainEffectVortex() {
        super(AstralSorcery.key("effect_vortex"));
    }

    @Nonnull
    @Override
    public BlockFountainPrime getAssociatedPrime() {
        return BlocksAS.FOUNTAIN_PRIME_VORTEX;
    }

    @Nonnull
    @Override
    public VortexContext createContext(TileFountain fountain) {
        return new VortexContext();
    }

    @Override
    public void tick(TileFountain fountain, VortexContext context, int operationTick, LogicalSide direction, OperationSegment currentSegment) {
        if (direction.isClient()) {
            tickEffects(fountain, context, operationTick, currentSegment);
        } else if (direction.isServer() && currentSegment.isLaterOrEqualTo(OperationSegment.RUNNING)) {
            pullEntities(fountain);
        }
    }

    private void pullEntities(TileFountain fountain) {
        Vector3 at = new Vector3(fountain).add(0.5, 0.5, 0.5);
        Vector3 vortexAt = at.clone().addY(-4);

        AABB captureBox = new AABB(0, 0, 0, 1, 1, 1)
                .offset(fountain.getBlockPos().below(4))
                .grow(2);
        AABB pullBox = captureBox.grow(14);

        float boxCapacity = 5 * 5 * 5;
        float density = 0;
        List<LivingEntity> captured = fountain.getLevel().getEntitiesWithinAABB(LivingEntity.class, captureBox);
        for (LivingEntity le : captured) {
            if (le == null || !le.isAlive() || le instanceof Player || !TechnicalEntityRegistry.INSTANCE.canAffect(le)) {
                continue;
            }
            float entitySize = le.getHeight() * le.getWidth() * le.getWidth();
            density += entitySize;

            if (entitySize > boxCapacity) {
                Vector3 heldPos = vortexAt.clone().addY(-1);
                if (heldPos.distanceSquared(le) >= 0.4F) {
                    le.setPositionAndRotation(heldPos.getX(), heldPos.getY(), heldPos.getZ(), le.getYRot(), le.getXRot());
                }

                if (le instanceof EnderDragon) {
                    GameRules rules = fountain.getLevel().getGameRules();
                    boolean prev = rules.getBoolean(GameRules.RULE_MOBGRIEFING);
                    rules.get(GameRules.RULE_MOBGRIEFING).set(false, null);
                    le.aiStep();
                    rules.get(GameRules.RULE_MOBGRIEFING).set(prev, null);
                }
            } else {
                le.setDeltaMovement(Vec3.ZERO);
            }

            EventHelperEntityFreeze.freeze(le);
        }

        float upkeep = Math.max(0, density / boxCapacity);
        fountain.consumeLiquidStarlight(Mth.ceil(upkeep / 3F));


        List<LivingEntity> pulling = fountain.getLevel().getEntitiesWithinAABB(LivingEntity.class, pullBox);
        pulling.removeAll(captured);
        for (LivingEntity le : pulling) {
            if (le == null || !le.isAlive() || le instanceof Player || !TechnicalEntityRegistry.INSTANCE.canAffect(le)) {
                continue;
            }
            EventHelperEntityFreeze.freeze(le);

            EntityUtils.applyVortexMotion(() -> Vector3.atEntityCorner(le), (v) -> {
                if (le instanceof EnderDragon) {
                    Vector3 nextPos = Vector3.atEntityCorner(le).add(v);
                    if (le.isEffectiveAi()) {
                        le.setPositionAndUpdate(nextPos.getX(), nextPos.getY(), nextPos.getZ());
                    } else {
                        le.setPositionAndRotation(nextPos.getX(), nextPos.getY(), nextPos.getZ(), le.getYRot(), le.getXRot());
                    }
                    le.setDeltaMovement(Vec3.ZERO);
                } else {
                    le.setDeltaMovement(le.getDeltaMovement().add(v.getX(), v.getY() * 2.5, v.getZ()));
                    le.hurtMarked = true;
                }
            }, vortexAt, 48, 3);

            if (vortexAt.distanceSquared(le) <= 16) {
                Vector3 randomRanges = new Vector3(
                        Math.max(0, (captureBox.getXSize() - le.getWidth()) / 2),
                        Math.max(0, (captureBox.getYSize() - le.getHeight()) / 2),
                        Math.max(0, (captureBox.getZSize() - le.getWidth()) / 2)
                );
                Vector3 randomPos = vortexAt.clone().add(
                        randomRanges.getX() * random.nextFloat() * (random.nextBoolean() ? 1 : -1),
                        randomRanges.getY() * random.nextFloat() * (random.nextBoolean() ? 1 : -1),
                        randomRanges.getZ() * random.nextFloat() * (random.nextBoolean() ? 1 : -1)
                );
                le.setPositionAndRotation(randomPos.getX(), randomPos.getY(), randomPos.getZ(), le.getYRot(), le.getXRot());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffects(TileFountain fountain, VortexContext ctx, int operationTick, OperationSegment currentSegment) {
        if (currentSegment.isLaterOrEqualTo(OperationSegment.STARTUP)) {
            FXSpritePlane sprite = (FXSpritePlane) ctx.fountainSprite;
            if (sprite == null) {
                sprite = EffectHelper.of(EffectTemplatesAS.TEXTURE_SPRITE)
                        .spawn(new Vector3(fountain).add(0.5, 0.5, 0.5))
                        .setAxis(Vector3.RotAxis.Y_AXIS)
                        .setNoRotation(45)
                        .pickSprite(SpritesAS.SPR_FOUNTAIN_VORTEX)
                        .setAlphaMultiplier(1F)
                        .alpha1arg((fx, alphaIn, pTicks) -> this.getSegmentPercent(OperationSegment.STARTUP, fountain.getTickActiveFountainEffect()))
                        .setScaleMultiplier(5.5F)
                        .refresh(RefreshFunction.tileExistsAnd(fountain, (tile, fx) -> tile.getCurrentEffect() == this));
            } else if (sprite.isRemoved() || sprite.canRemove()) {
                EffectHelper.refresh(sprite, EffectTemplatesAS.TEXTURE_SPRITE);
            }
            ctx.fountainSprite = sprite;
        }

        BlockPos fountainPos = fountain.getBlockPos();
        float segmentPercent = getSegmentPercent(currentSegment, operationTick);
        switch (currentSegment) {
            case STARTUP:
                playFountainVortexParticles(fountainPos, segmentPercent);
                playFountainArcs(fountainPos, segmentPercent);
                playCoreParticles(fountainPos, segmentPercent);
                break;
            case PREPARATION:
                playFountainArcs(fountainPos, 1F - segmentPercent);
                playFountainVortexParticles(fountainPos, 1F - segmentPercent);
                playCoreParticles(fountainPos, 1F - segmentPercent * 2F);
                playCorePrimerParticles(fountainPos, segmentPercent);
                break;
            case RUNNING:
                playFountainVortexParticles(fountainPos, 0.2F);
                playFountainArcs(fountainPos, 0.6F);
                playFountainVortexLowerParticles(fountainPos);
                playVortexEffects(fountainPos, fountain, ctx);
                break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playVortexEffects(BlockPos pos, TileFountain fountain, VortexContext ctx) {
        Vector3 at = new Vector3(pos).add(0.5, 0.5, 0.5);
        Vector3 vortexAt = at.clone().addY(-4);

        FXFacingSprite sprite = (FXFacingSprite) ctx.facingVortexPlane;
        if (sprite == null) {
            sprite = EffectHelper.of(EffectTemplatesAS.FACING_SPRITE)
                    .spawn(vortexAt)
                    .pickSprite(SpritesAS.SPR_ATTUNEMENT_FLARE)
                    .setAlphaMultiplier(1F)
                    .setScaleMultiplier(2F)
                    .refresh(RefreshFunction.tileExistsAnd(fountain, (tile, fx) -> tile.getCurrentEffect() == this));
        } else if (sprite.isRemoved() || sprite.canRemove()) {
            EffectHelper.refresh(sprite, EffectTemplatesAS.FACING_SPRITE);
        }
        ctx.facingVortexPlane = sprite;

        if (ctx.ctrlEffectNoise == null) {
            ctx.ctrlEffectNoise = Lists.newArrayList(
                    new RenderOffsetNoisePlane(1.2F),
                    new RenderOffsetNoisePlane(2.0F),
                    new RenderOffsetNoisePlane(2.8F)
            );
        }

        for (Object objPlane : ctx.ctrlEffectNoise) {
            RenderOffsetNoisePlane plane = (RenderOffsetNoisePlane) objPlane;
            for (int i = 0; i < 3; i++) {
                plane.createParticle(vortexAt)
                        .setDeltaMovement(Vector3.random().normalize().mul(0.005F))
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                        .setMaxAge(30 + random.nextInt(15));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playFountainVortexLowerParticles(BlockPos pos) {
        Vector3 at = new Vector3(pos).add(0.5, 0.5, 0.5);
        Vector3 coreAt = at.clone().addY(-1.15);
        Vector3 vortexAt = at.clone().addY(-4);

        for (int i = 0; i < 2; i++) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(coreAt)
                    .setScaleMultiplier(0.25F)
                    .setAlphaMultiplier(1F)
                    .setDeltaMovement(Vector3.random().normalize().mul(0.01F))
                    .color(VFXColorFunction.random());
        }

        for (int i = 0; i < 3; i++) {
            Vector3 spawnPos = vortexAt.clone().add(Vector3.random().mul(4.5F));
            Vector3 dir = spawnPos.clone().vectorFromHereTo(vortexAt).normalize().divide(20 + random.nextInt(10));

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(spawnPos)
                    .setDeltaMovement(dir)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .setAlphaMultiplier(1F)
                    .color(VFXColorFunction.WHITE);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playCorePrimerParticles(BlockPos pos, float chance) {
        float yOffset = -3.5F * Math.min(1F, chance * 2F);
        for (int i = 0; i < 15; i++) {
            Vector3 at = new Vector3(
                    pos.getX() + 0.5     - 0.1F + random.nextFloat() * 0.2,
                    pos.getY() + yOffset - 0.1F + random.nextFloat() * 0.2,
                    pos.getZ() + 0.5     - 0.1F + random.nextFloat() * 0.2
            );
            float mul = chance <= 0.5F ? 1F : (1F - chance);
            Vector3 dir = new Vector3(
                    random.nextFloat() * 0.035 * mul * (random.nextBoolean() ? 1 : -1),
                    random.nextFloat() * 0.035 * mul * (random.nextBoolean() ? 1 : -1),
                    random.nextFloat() * 0.035 * mul * (random.nextBoolean() ? 1 : -1)
            );

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .setDeltaMovement(dir)
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(20 + random.nextInt(40));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playCoreParticles(BlockPos pos, float chance) {
        Vector3 at = new Vector3(pos).add(0.5, -0.5, 0.5);
        for (int i = 0; i < 18; i++) {
            if (random.nextFloat() >= chance) {
                continue;
            }
            Vector3 particlePos = new Vector3(
                    pos.getX() - 1   + random.nextFloat() * 3,
                    pos.getY() - 1.5 + random.nextFloat() * 2,
                    pos.getZ() - 1   + random.nextFloat() * 3
            );
            Vector3 motion = particlePos.clone().vectorFromHereTo(at).normalize().divide(30);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(particlePos)
                    .setDeltaMovement(motion)
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(20 + random.nextInt(40));
        }
    }

    @Override
    public void transition(TileFountain fountain, VortexContext context, LogicalSide direction, OperationSegment prevSegment, OperationSegment nextSegment) {
        if (direction.isClient()) {
            if (nextSegment == OperationSegment.RUNNING) {
                doVortexExplosion(fountain.getBlockPos());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doVortexExplosion(BlockPos pos) {
        for (int i = 0; i < 140; i++) {
            Vector3 at = new Vector3(
                    pos.getX() + 0.5F - 0.1F + random.nextFloat() * 0.2F,
                    pos.getY() - 3.5F - 0.1F + random.nextFloat() * 0.2F,
                    pos.getZ() + 0.5F - 0.1F + random.nextFloat() * 0.2F
            );
            Vector3 dir = new Vector3(
                    random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                    random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                    random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1)
            );

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .setDeltaMovement(dir)
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(20 + random.nextInt(40));
        }
    }

    @Override
    public void onReplace(TileFountain fountain, VortexContext ctx, @Nullable FountainEffect<?> newEffect, LogicalSide direction) {
        if (direction.isClient()) {
            removeSprite(ctx);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void removeSprite(VortexContext ctx) {
        FXSpritePlane sprite = (FXSpritePlane) ctx.fountainSprite;
        if (sprite != null) {
            sprite.requestRemoval();
        }
    }
}
