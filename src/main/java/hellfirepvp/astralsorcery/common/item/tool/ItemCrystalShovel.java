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
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalShovel
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:26
 */
public class ItemCrystalShovel extends ItemCrystalTierItem implements TypeEnchantableItem {

    public ItemCrystalShovel() {
        super(BlockTags.MINEABLE_WITH_SHOVEL, new Properties(), ItemAbilities.DEFAULT_SHOVEL_ACTIONS);
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
        return 3;
    }

    @Override
    double getAttackSpeed() {
        return -1.5;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        } else {
            Player player = context.getPlayer();
            BlockState modifiedState = state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
            BlockState targetState = null;
            if (modifiedState != null && level.getBlockState(pos.above()).isAir()) {
                level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                targetState = modifiedState;
            } else if ((targetState = state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false)) != null) {
                if (!level.isClientSide()) {
                    level.levelEvent(null, 1009, pos, 0);
                }
            }

            if (targetState != null) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, targetState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, targetState));
                    if (player != null) {
                        context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}
