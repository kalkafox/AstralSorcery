/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.dispenser;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import java.util.Optional;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidContainerDispenseBehavior
 * Created by HellFirePvP
 * Date: 03.11.2020 / 23:06
 */
//Mostly taken from DispenseFluidContainer, but a variant that actually doesn't crash.
public class FluidContainerDispenseBehavior extends DefaultDispenseItemBehavior {

    private static final FluidContainerDispenseBehavior INSTANCE = new FluidContainerDispenseBehavior();
    private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

    private FluidContainerDispenseBehavior() {}

    public static FluidContainerDispenseBehavior getInstance() {
        return INSTANCE;
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        if (FluidUtil.getFluidContained(stack).isPresent()) {
            return dumpContainer(source, stack);
        } else {
            return fillContainer(source, stack);
        }
    }

    @Nonnull
    private ItemStack fillContainer(BlockSource source, ItemStack stack) {
        Level level = source.level();
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.pos().relative(dispenserFacing);

        FluidActionResult actionResult = FluidUtil.tryPickUpFluid(stack, null, level, blockpos, dispenserFacing.getOpposite());
        ItemStack resultStack = actionResult.getResult();

        if (!actionResult.isSuccess() || resultStack.isEmpty()) {
            return super.execute(source, stack);
        }

        if (stack.getCount() == 1) {
            return resultStack;
        } else if (!((DispenserBlockEntity) source.blockEntity()).insertItem(resultStack).isEmpty()) {
            this.defaultBehavior.dispense(source, resultStack);
        }

        ItemStack stackCopy = stack.copy();
        stackCopy.shrink(1);
        return stackCopy;
    }

    @Nonnull
    private ItemStack dumpContainer(BlockSource source, @Nonnull ItemStack stack) {
        ServerLevel level = source.level();
        ItemStack singleStack = stack.copy();
        singleStack.setCount(1);
        Optional<IFluidHandlerItem> itemFluidHandler = FluidUtil.getFluidHandler(singleStack);
        if (!itemFluidHandler.isPresent()) {
            return super.execute(source, stack);
        }
        FluidStack drained = itemFluidHandler
                .map(handler -> handler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE))
                .orElse(FluidStack.EMPTY);
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(dispenserFacing);
        Player player = AstralSorcery.getProxy().getASFakePlayerServer((ServerLevel) level);
        FluidActionResult result = FluidUtil.tryPlaceFluid(player, source.level(), InteractionHand.MAIN_HAND, pos, stack, drained);

        if (result.isSuccess()) {
            ItemStack drainedStack = result.getResult();

            if (drainedStack.getCount() == 1) {
                return drainedStack;
            } else if (!drainedStack.isEmpty() && !((DispenserBlockEntity) source.blockEntity()).insertItem(drainedStack).isEmpty()) {
                this.defaultBehavior.dispense(source, drainedStack);
            }

            ItemStack stackCopy = drainedStack.copy();
            stackCopy.shrink(1);
            return stackCopy;
        } else {
            return this.defaultBehavior.dispense(source, stack);
        }
    }
}
