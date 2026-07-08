/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.entity.technical.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemGrappleWand
 * Created by HellFirePvP
 * Date: 29.02.2020 / 18:15
 */
public class ItemGrappleWand extends Item implements AlignmentChargeConsumer {

    private static final float COST_PER_GRAPPLE = 450F;

    public ItemGrappleWand() {
        super(new Properties()
                .maxStackSize(1)
                .group(CommonProxy.ITEM_GROUP_AS));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        return player.getCooldownTracker().hasCooldown(this) ? 0 : COST_PER_GRAPPLE;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote() || held.isEmpty()) {
            return new InteractionResultHolder<>(ActionResultType.SUCCESS, held);
        }
        if (!playerIn.getCooldownTracker().hasCooldown(this) &&
                AlignmentChargeHandler.INSTANCE.drainCharge(playerIn, LogicalSide.SERVER, COST_PER_GRAPPLE, false)) {
            worldIn.addEntity(new EntityGrapplingHook(playerIn, worldIn));
            playerIn.getCooldownTracker().setCooldown(this, 40);
        }
        return new InteractionResultHolder<>(ActionResultType.SUCCESS, held);
    }
}
