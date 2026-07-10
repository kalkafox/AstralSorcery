/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.base.TileFakedState;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.*;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFakedState
 * Created by HellFirePvP
 * Date: 04.09.2020 / 19:19
 */
public abstract class BlockFakedState extends BaseEntityBlock {

    protected BlockFakedState(Properties builder) {
        super(builder);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @OnlyIn(Dist.CLIENT)
    protected void showBreakingParticles(Level level, BlockPos pos, Random random) {
        if (random.nextInt(8) == 0) {
            VFXColorFunction<?> colorFn = VFXColorFunction.WHITE;
            TileFakedState fakedState = MiscUtils.getTileAt(level, pos, TileFakedState.class, false);
            if (fakedState != null) {
                colorFn = VFXColorFunction.constant(fakedState.getOverlayColor());
            }
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.random().abs().add(pos))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .color(colorFn)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.05F)
                    .setMaxAge(25 + random.nextInt(5));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        BlockState fakeState = this.getFakedState(level, pos);
        RenderingUtils.playBlockBreakParticles(pos, state, fakeState);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState state, Level worldObj, HitResult target, ParticleEngine manager) {
        return true;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState fakeState = this.getFakedState(level, pos);
        return fakeState.getSoundType(level, pos, entity);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState fakeState = this.getFakedState(level, pos);
        return fakeState.getShape(level, pos, context);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Lists.newArrayList();
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.NONE;
    }

    //TODO custom states via state container
    //@Override
    //public Vec3 getOffset(BlockState state, BlockGetter worldIn, BlockPos pos) {
    //    BlockState fakeState = this.getFakedState(worldIn, pos);
    //    try {
    //        //if (fakeState.getBlock().getOffsetType())
    //        return fakeState.getOffset(worldIn, pos);
    //    } catch (Exception exc) {
    //        //Ignore the result if this happens to be more complex than expected
    //    }
    //    return Vector3d.ZERO;
    //}

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        BlockState fakeState = this.getFakedState(worldIn, pos);
        try {
            return fakeState.getCollisionShape(worldIn, pos, context);
        } catch (Exception exc) {
            //Ignore the result if this happens to be more complex than expected
        }
        return super.getCollisionShape(state, worldIn, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        BlockState fakeState = this.getFakedState(worldIn, pos);
        try {
            return fakeState.getOcclusionShape(worldIn, pos);
        } catch (Exception exc) {
            //Ignore the result if this happens to be more complex than expected
        }
        return super.getOcclusionShape(state, worldIn, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        BlockState fakeState = this.getFakedState(level, pos);
        try {
            return fakeState.use(level, player, handIn, hit);
        } catch (Exception exc) {
            //Ignore the result if we can't interact
        }
        return super.use(state, level, pos, player, handIn, hit);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockState fakeState = this.getFakedState(level, pos);
        try {
            return fakeState.getPickBlock(target, level, pos, player);
        } catch (Exception exc) {
            //Ignore the result. If we can't pick that stuff here, well.. guess we can't at all.
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    private BlockState getFakedState(BlockGetter level, BlockPos pos) {
        TileFakedState tb = MiscUtils.getTileAt(level, pos, TileFakedState.class, true);
        return tb != null ? tb.getFakedState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public RenderShape getRenderType(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
