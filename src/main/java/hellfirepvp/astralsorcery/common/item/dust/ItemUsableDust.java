/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.dust;

import hellfirepvp.astralsorcery.common.CommonProxy;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemUsableDust
 * Created by HellFirePvP
 * Date: 17.08.2019 / 08:44
 */
public abstract class ItemUsableDust extends Item implements DispenseItemBehavior {

    public ItemUsableDust() {
        super(new Properties().group(CommonProxy.ITEM_GROUP_AS));
    }

    abstract boolean dispense(BlockSource dispenser);

    abstract boolean rightClickAir(Level world, Player player, ItemStack dust);

    abstract boolean rightClickBlock(UseOnContext ctx);

    @Override
    public InteractionResult onItemUse(UseOnContext ctx) {
        if (!ctx.getWorld().isRemote()) {
            if (this.rightClickBlock(ctx)) {
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItem().shrink(1);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!held.isEmpty() && !world.isRemote()) {
            if (this.rightClickAir(world, player, held)) {
                if (!player.isCreative()) {
                    held.shrink(1);
                }
            }
        }

        return ActionResult.resultSuccess(held);
    }

    @Override
    public ItemStack dispense(BlockSource src, ItemStack stack) {
        if (this.dispense(src)) {
            stack.shrink(1);
        }
        return stack;
    }
}
