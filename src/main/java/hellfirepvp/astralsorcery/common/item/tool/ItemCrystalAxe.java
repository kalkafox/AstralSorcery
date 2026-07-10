/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.enchantment.AstralEnchantmentType;
import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalAxe
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:10
 */
public class ItemCrystalAxe extends ItemCrystalTierItem implements TypeEnchantableItem {

    public ItemCrystalAxe() {
        super(BlockTags.MINEABLE_WITH_AXE, new Properties(), ItemAbilities.DEFAULT_AXE_ACTIONS);
    }

    @Override
    public boolean canEnchant(ItemStack stack, AstralEnchantmentType type) {
        return type == AstralEnchantmentType.BREAKABLE || type == AstralEnchantmentType.DIGGER;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.supportsEnchantment(stack, enchantment) ||
                AstralEnchantmentType.DIGGER.contains(enchantment) ||
                AstralEnchantmentType.BREAKABLE.contains(enchantment);
    }

    @Override
    double getAttackDamage() {
        return 11;
    }

    @Override
    double getAttackSpeed() {
        return -3;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        BlockState modifiedState = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
        if (modifiedState != null) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if ((modifiedState = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false)) != null) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3005, pos, 0);
        } else if ((modifiedState = state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false)) != null) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3004, pos, 0);
        } else {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide) {
            level.setBlock(pos, modifiedState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, modifiedState));
            if (player != null) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
