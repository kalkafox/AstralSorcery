/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.lib.TagsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktOreScan;
import hellfirepvp.astralsorcery.common.perk.modifier.DynamicAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.source.provider.equipment.EquipmentAttributeModifierProvider;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicates;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemInfusedCrystalPickaxe
 * Created by HellFirePvP
 * Date: 04.04.2020 / 09:57
 */
public class ItemInfusedCrystalPickaxe extends ItemCrystalPickaxe implements EquipmentAttributeModifierProvider {

    private static final UUID MODIFIER_ID = UUID.fromString("ecf80c60-3da6-4952-90d0-5db5429ea44a");
    private static final CacheReference<DynamicAttributeModifier> MINING_SIZE_MODIFIER =
            new CacheReference<>(() -> new DynamicAttributeModifier(MODIFIER_ID, PerkAttributeTypesAS.ATTR_TYPE_MINING_SIZE, ModifierType.ADDITION, 1F));

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.doOreScan(level, player.blockPosition(), player, held)) {
            return InteractionResultHolder.success(held);
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player != null) {
            if (this.doOreScan(ctx.getLevel(), ctx.getClickedPos(), player, player.getItemInHand(ctx.getHand()))) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(ctx);
    }

    private boolean doOreScan(Level level, BlockPos origin, Player player, ItemStack stack) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && !MiscUtils.isPlayerFakeMP(serverPlayer)) {
            if (stack.getItem() instanceof ItemInfusedCrystalPickaxe && !player.getCooldowns().isOnCooldown(stack.getItem())) {
                PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
                if (prog.doPerkAbilities()) {
                    List<BlockPos> orePositions = BlockDiscoverer.searchForBlocksAround(level, origin, 16, BlockPredicates.isInTag(TagsAS.Blocks.ORES));
                    PacketChannel.CHANNEL.sendToPlayer(player, new PktOreScan(orePositions));

                    player.getCooldowns().addCooldown(stack.getItem(), 120);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Collection<PerkAttributeModifier> getModifiers(ItemStack stack, Player player, LogicalSide direction, boolean ignoreRequirements) {
        return Collections.singletonList(MINING_SIZE_MODIFIER.get());
    }
}
