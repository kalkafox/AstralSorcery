/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.ForgeEventFactory;
import net.neoforged.fml.network.NetworkHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityIlluminationSpark
 * Created by HellFirePvP
 * Date: 17.08.2019 / 10:45
 */
public class EntityIlluminationSpark extends ThrowableProjectile {

    public EntityIlluminationSpark(Level level) {
        super(EntityTypesAS.ILLUMINATION_SPARK, level);
    }

    public EntityIlluminationSpark(double x, double y, double z, Level level) {
        super(EntityTypesAS.ILLUMINATION_SPARK, x, y, z, level);
    }

    public EntityIlluminationSpark(LivingEntity thrower, Level level) {
        super(EntityTypesAS.ILLUMINATION_SPARK, thrower, level);
        this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0F, 0.7F, 0.9F);
    }

    public static EntityType.IFactory<EntityIlluminationSpark> factory() {
        return (type, level) -> new EntityIlluminationSpark(level);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            spawnEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEffects() {
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
                .spawn(Vector3.atEntityCorner(this).add(getDeltaMovement().mul(0.5, 0.5, 0.5)));
        p.setScaleMultiplier(0.6F);
        randomizeColor(p);

    }

    @OnlyIn(Dist.CLIENT)
    private void randomizeColor(FXFacingParticle p) {
        switch (random.nextInt(3)) {
            case 0:
                p.color(VFXColorFunction.constant(ColorsAS.ILLUMINATION_POWDER_1));
                break;
            case 1:
                p.color(VFXColorFunction.constant(ColorsAS.ILLUMINATION_POWDER_2));
                break;
            case 2:
                p.color(VFXColorFunction.constant(ColorsAS.ILLUMINATION_POWDER_3));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (level.isClientSide()) {
            return;
        }
        if (!(result instanceof BlockHitResult) || !(this.getOwner() instanceof Player)) {
            remove();
            return;
        }
        Player player = (Player) this.getOwner();
        BlockHitResult brtr = (BlockHitResult) result;

        BlockPlaceContext bCtx = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, brtr));

        BlockPos pos = bCtx.getBlockPos();
        if (!BlockUtils.isReplaceable(level, pos)) {
            pos = pos.offset(bCtx.getFace());
        }

        if (!ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), bCtx.getFace())) {
            level.setBlock(pos, BlocksAS.FLARE_LIGHT.defaultBlockState());
        }
        remove();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
