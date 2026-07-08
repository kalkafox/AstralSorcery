/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.world.level.block.*;
import net.minecraft.client.resources.model.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockStructural
 * Created by HellFirePvP
 * Date: 15.01.2020 / 16:22
 */
public class BlockStructural extends Block {

    public static EnumProperty<BlockType> BLOCK_TYPE = EnumProperty.create("blocktype", BlockType.class);

    private static final VoxelShape STRUCT_TELESCOPE = VoxelShapes.create(1D / 16D, -16D / 16D, 1D / 16D, 15D / 16D, 16D / 16D, 15D / 16D);

    public BlockStructural() {
        super(Block.Properties.create(Material.BARRIER, MaterialColor.AIR)
                .sound(SoundType.GLASS));

        this.setDefaultState(this.getDefaultState().with(BLOCK_TYPE, BlockType.TELESCOPE));
    }

    @Override
    public void fillItemGroup(CreativeModeTab group, NonNullList<ItemStack> items) {}

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_TYPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return STRUCT_TELESCOPE;
        }
        return super.getShape(state, worldIn, pos, context);
    }

    @Nullable
    @Override
    public ToolType getHarvestTool(BlockState state) {
        return state.get(BLOCK_TYPE).getSupportedState().getHarvestTool();
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return state.get(BLOCK_TYPE).getSupportedState().getHarvestLevel();
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return SoundType.WOOD;
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
        EventFlags.PLAY_BLOCK_BREAK_EFFECTS.executeWithFlag(() -> {
            switch (state.get(BLOCK_TYPE)) {
                case TELESCOPE:
                    manager.addBlockDestroyEffects(pos.down(), BlocksAS.TELESCOPE.getDefaultState());
                    break;
            }
        });
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
        if (target instanceof BlockHitResult) {
            EventFlags.PLAY_BLOCK_BREAK_EFFECTS.executeWithFlag(() -> {
                switch (state.get(BLOCK_TYPE)) {
                    case TELESCOPE:
                        manager.addBlockDestroyEffects(((BlockHitResult) target).getPos().down(), BlocksAS.TELESCOPE.getDefaultState());
                        break;
                }
            });
        }
        return true;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult rayTraceResult) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                if (world.isRemote()) {
                    AstralSorcery.getProxy().openGui(entity, GuiType.TELESCOPE, pos.down());
                }
                return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, world, pos, entity, hand, rayTraceResult);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = Lists.newArrayList();
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getDrops(builder);
        }
        return drops;
    }

    /*
    TODO custom states via state container
    private static float getBlockHardness(BlockState state, BlockGetter world, BlockPos pos) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getBlockHardness(world, pos.down());
        }
        return super.getBlockHardness(state, world, pos);
    }

    private static boolean isOpaque(BlockState state, BlockGetter world, BlockPos pos) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().isNormalCube(world, pos.down());
        }
        return state.getMaterial().isOpaque() && state.hasOpaqueCollisionShape(world, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getExplosionResistance(world, pos.down(), exploder, explosion);
        }
        return super.getExplosionResistance(state, world, pos, exploder, explosion);
    }*/

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getPickBlock(target, world, pos.down(), player);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                if (world.isAirBlock(pos.down())) {
                    world.removeBlock(pos, isMoving);
                }
                return;
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (!(world instanceof IWorldWriter)) {
            return;
        }
        switch (state.get(BLOCK_TYPE)) {
            case TELESCOPE:
                if (world.isAirBlock(pos.down())) {
                    ((IWorldWriter) world).removeBlock(pos, false);
                }
        }
    }

    @Override
    public RenderShape getRenderType(BlockState p_149645_1_) {
        return BlockRenderType.INVISIBLE;
    }

    public static enum BlockType implements StringRepresentable {

        DUMMY(Blocks.AIR.getDefaultState()),
        TELESCOPE(BlocksAS.TELESCOPE.getDefaultState());

        private final BlockState supportedState;

        private BlockType(BlockState supportedState) {
            this.supportedState = supportedState;
        }

        public BlockState getSupportedState() {
            return supportedState;
        }

        @Override
        public String getString() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return this.getString();
        }
    }
}
