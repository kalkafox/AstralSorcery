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

    public EntityIlluminationSpark(Level world) {
        super(EntityTypesAS.ILLUMINATION_SPARK, world);
    }

    public EntityIlluminationSpark(double x, double y, double z, Level world) {
        super(EntityTypesAS.ILLUMINATION_SPARK, x, y, z, world);
    }

    public EntityIlluminationSpark(LivingEntity thrower, Level world) {
        super(EntityTypesAS.ILLUMINATION_SPARK, thrower, world);
        this.func_234612_a_(thrower, thrower.rotationPitch, thrower.rotationYaw, 0F, 0.7F, 0.9F);
    }

    public static EntityType.IFactory<EntityIlluminationSpark> factory() {
        return (type, world) -> new EntityIlluminationSpark(world);
    }

    @Override
    protected void registerData() {}

    @Override
    public void tick() {
        super.tick();

        if (world.isRemote()) {
            spawnEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEffects() {
        FXFacingParticle p;
        for (int i = 0; i < 6; i++) {
            p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this))
                    .setMotion(new Vector3(
                            0.04F - rand.nextFloat() * 0.08F,
                            0.04F - rand.nextFloat() * 0.08F,
                            0.04F - rand.nextFloat() * 0.08F
                    ))
                    .setScaleMultiplier(0.25F);
            randomizeColor(p);
        }

        p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(Vector3.atEntityCorner(this));
        p.setScaleMultiplier(0.6F);
        randomizeColor(p);

        p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(Vector3.atEntityCorner(this).add(getMotion().mul(0.5, 0.5, 0.5)));
        p.setScaleMultiplier(0.6F);
        randomizeColor(p);

    }

    @OnlyIn(Dist.CLIENT)
    private void randomizeColor(FXFacingParticle p) {
        switch (rand.nextInt(3)) {
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
    protected void onImpact(HitResult result) {
        if (world.isRemote()) {
            return;
        }
        if (!(result instanceof BlockHitResult) || !(this.func_234616_v_() instanceof Player)) {
            remove();
            return;
        }
        Player player = (Player) this.func_234616_v_();
        BlockHitResult brtr = (BlockHitResult) result;

        BlockPlaceContext bCtx = new BlockPlaceContext(new UseOnContext(player, Hand.MAIN_HAND, brtr));

        BlockPos pos = bCtx.getPos();
        if (!BlockUtils.isReplaceable(world, pos)) {
            pos = pos.offset(bCtx.getFace());
        }

        if (!ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(world.getDimensionKey(), world, pos), bCtx.getFace())) {
            world.setBlockState(pos, BlocksAS.FLARE_LIGHT.getDefaultState());
        }
        remove();
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
