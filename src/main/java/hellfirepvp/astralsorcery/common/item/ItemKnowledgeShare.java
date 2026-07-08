/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Component;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktProgressionUpdate;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemKnowledgeShare
 * Created by HellFirePvP
 * Date: 16.08.2019 / 20:10
 */
public class ItemKnowledgeShare extends Item {

    public ItemKnowledgeShare() {
        super(new Properties()
                .maxStackSize(1)
                .group(CommonProxy.ITEM_GROUP_AS));
    }

    @Override
    public void fillItemGroup(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));

            ItemStack creative = new ItemStack(this);
            setCreative(creative);
            items.add(creative);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (isCreative(stack)) {
            tooltip.add(Component.translatable("astralsorcery.misc.knowledge.inscribed.creative").withStyle(TextFormatting.LIGHT_PURPLE));
            return;
        }
        if (getKnowledge(stack) == null) {
            tooltip.add(Component.translatable("astralsorcery.misc.knowledge.missing").withStyle(TextFormatting.GRAY));
        } else {
            MutableComponent name = getKnowledgeOwnerName(stack);
            if (name != null) {
                tooltip.add(Component.translatable("astralsorcery.misc.knowledge.inscribed", name).withStyle(TextFormatting.BLUE));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty() || world.isRemote() || !(held.getItem() instanceof ItemKnowledgeShare)) {
            return ActionResult.resultSuccess(held);
        }
        if (!isCreative(held) && (player.isSneaking() || getKnowledge(held) == null)) {
            tryInscribeKnowledge(held, player);
        } else {
            tryGiveKnowledge(held, player);
        }
        return ActionResult.resultSuccess(held);
    }

    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        ItemStack stack = context.getItem();
        Player player = context.getPlayer();
        if (stack.isEmpty() || player == null || context.getWorld().isRemote() || !(stack.getItem() instanceof ItemKnowledgeShare)) {
            return ActionResultType.SUCCESS;
        }
        if (!isCreative(stack) && (player.isSneaking() || getKnowledge(stack) == null)) {
            tryInscribeKnowledge(stack, player);
        } else {
            tryGiveKnowledge(stack, player);
        }
        return ActionResultType.SUCCESS;
    }

    private void tryGiveKnowledge(ItemStack stack, Player player) {
        if (player instanceof ServerPlayer && MiscUtils.isPlayerFakeMP((ServerPlayer) player)) {
            return;
        }

        if (isCreative(stack)) {
            ResearchManager.forceMaximizeAll(player);
            return;
        }
        if (canInscribeKnowledge(stack, player)) return; //Means it's either empty or the player that has incsribed the knowledge is trying to use it.
        PlayerProgress progress = getKnowledge(stack);
        if (progress == null) return;
        ProgressionTier prev = progress.getTierReached();
        if (ResearchHelper.mergeApplyPlayerprogress(progress, player) && progress.getTierReached().isThisLater(prev)) {
            PktProgressionUpdate pkt = new PktProgressionUpdate(progress.getTierReached());
            PacketChannel.CHANNEL.sendToPlayer(player, pkt);
        }
    }

    private void tryInscribeKnowledge(ItemStack stack, Player player) {
        if (canInscribeKnowledge(stack, player)) {
            setKnowledge(stack, player, ResearchHelper.getProgress(player, LogicalSide.SERVER));
        }
    }

    @Nullable
    public static Player getKnowledgeOwner(ItemStack stack, MinecraftServer server) {
        if (isCreative(stack)) return null;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        UUID owner = NBTHelper.getUUID(compound, "knowledgeOwnerUUID", null);
        if (owner == null) {
            return null;
        }
        return server.getPlayerList().getPlayerByUUID(owner);
    }

    @Nullable
    public static MutableComponent getKnowledgeOwnerName(ItemStack stack) {
        if (isCreative(stack)) return null;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        if (!compound.contains("knowledgeOwnerName")) {
            return null;
        }
        return ITextComponent.Serializer.getComponentFromJson(compound.getString("knowledgeOwnerName"));
    }

    @Nullable
    public static PlayerProgress getKnowledge(ItemStack stack) {
        if (isCreative(stack)) return null;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        if (!compound.contains("knowledgeTag")) {
            return null;
        }
        CompoundTag tag = compound.getCompound("knowledgeTag");
        try {
            PlayerProgress progress = new PlayerProgress();
            progress.loadKnowledge(tag);
            return progress;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean canInscribeKnowledge(ItemStack stack, Player player) {
        if (isCreative(stack)) return false;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        UUID owner = NBTHelper.getUUID(compound, "knowledgeOwnerUUID", null);
        if (owner == null) {
            return true;
        }
        return player.getUniqueID().equals(owner);
    }

    public static void setKnowledge(ItemStack stack, Player player, PlayerProgress progress) {
        if (isCreative(stack) || !progress.isValid()) return;

        CompoundTag knowledge = new CompoundTag();
        progress.storeKnowledge(knowledge);
        CompoundTag compound = NBTHelper.getPersistentData(stack);
        compound.putString("knowledgeOwnerName", ITextComponent.Serializer.toJson(player.getDisplayName()));
        compound.putUniqueId("knowledgeOwnerUUID", player.getUniqueID());
        compound.put("knowledgeTag", knowledge);
    }

    public static boolean isCreative(ItemStack stack) {
        CompoundTag cmp = NBTHelper.getPersistentData(stack);
        if (!cmp.contains("creativeKnowledge")) {
            return false;
        }
        return cmp.getBoolean("creativeKnowledge");
    }

    private void setCreative(ItemStack stack) {
        NBTHelper.getPersistentData(stack).putBoolean("creativeKnowledge", true);
    }
}
