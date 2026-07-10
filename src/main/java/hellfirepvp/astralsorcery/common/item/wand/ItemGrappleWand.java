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
                .stacksTo(1)
);
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        return player.getCooldowns().isOnCooldown(this) ? 0 : COST_PER_GRAPPLE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack held = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide() || held.isEmpty()) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, held);
        }
        if (!playerIn.getCooldowns().isOnCooldown(this) &&
                AlignmentChargeHandler.INSTANCE.drainCharge(playerIn, LogicalSide.SERVER, COST_PER_GRAPPLE, false)) {
            worldIn.addEntity(new EntityGrapplingHook(playerIn, worldIn));
            playerIn.getCooldowns().addCooldown(this, 40);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, held);
    }
}
