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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;

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
                .stacksTo(1)
);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (isCreative(stack)) {
            tooltip.add(Component.translatable("astralsorcery.misc.knowledge.inscribed.creative").withStyle(ChatFormatting.LIGHT_PURPLE));
            return;
        }
        if (getKnowledge(stack) == null) {
            tooltip.add(Component.translatable("astralsorcery.misc.knowledge.missing").withStyle(ChatFormatting.GRAY));
        } else {
            MutableComponent name = getKnowledgeOwnerName(stack);
            if (name != null) {
                tooltip.add(Component.translatable("astralsorcery.misc.knowledge.inscribed", name).withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() || level.isClientSide() || !(held.getItem() instanceof ItemKnowledgeShare)) {
            return InteractionResultHolder.success(held);
        }
        if (!isCreative(held) && (player.isShiftKeyDown() || getKnowledge(held) == null)) {
            tryInscribeKnowledge(held, player);
        } else {
            tryGiveKnowledge(held, player);
        }
        return InteractionResultHolder.success(held);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (stack.isEmpty() || player == null || context.getLevel().isClientSide() || !(stack.getItem() instanceof ItemKnowledgeShare)) {
            return InteractionResult.SUCCESS;
        }
        if (!isCreative(stack) && (player.isShiftKeyDown() || getKnowledge(stack) == null)) {
            tryInscribeKnowledge(stack, player);
        } else {
            tryGiveKnowledge(stack, player);
        }
        return InteractionResult.SUCCESS;
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

        CompoundTag pattern = NBTHelper.getPersistentData(stack);
        UUID owner = NBTHelper.getUUID(pattern, "knowledgeOwnerUUID", null);
        if (owner == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(owner);
    }

    @Nullable
    public static MutableComponent getKnowledgeOwnerName(ItemStack stack) {
        if (isCreative(stack)) return null;

        CompoundTag pattern = NBTHelper.getPersistentData(stack);
        if (!pattern.contains("knowledgeOwnerName")) {
            return null;
        }
        return Component.Serializer.fromJson(pattern.getString("knowledgeOwnerName"), RegistryAccess.EMPTY);
    }

    @Nullable
    public static PlayerProgress getKnowledge(ItemStack stack) {
        if (isCreative(stack)) return null;

        CompoundTag pattern = NBTHelper.getPersistentData(stack);
        if (!pattern.contains("knowledgeTag")) {
            return null;
        }
        CompoundTag tag = pattern.getCompound("knowledgeTag");
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

        CompoundTag pattern = NBTHelper.getPersistentData(stack);
        UUID owner = NBTHelper.getUUID(pattern, "knowledgeOwnerUUID", null);
        if (owner == null) {
            return true;
        }
        return player.getUUID().equals(owner);
    }

    public static void setKnowledge(ItemStack stack, Player player, PlayerProgress progress) {
        if (isCreative(stack) || !progress.isValid()) return;

        CompoundTag knowledge = new CompoundTag();
        progress.storeKnowledge(knowledge);
        CompoundTag pattern = NBTHelper.getPersistentData(stack);
        pattern.putString("knowledgeOwnerName", Component.Serializer.toJson(player.getDisplayName(), RegistryAccess.EMPTY));
        pattern.putUUID("knowledgeOwnerUUID", player.getUUID());
        pattern.put("knowledgeTag", knowledge);
    }

    public static boolean isCreative(ItemStack stack) {
        CompoundTag cmp = NBTHelper.getPersistentData(stack);
        if (!cmp.contains("creativeKnowledge")) {
            return false;
        }
        return cmp.getBoolean("creativeKnowledge");
    }

    public static void setCreative(ItemStack stack) {
        NBTHelper.getPersistentData(stack).putBoolean("creativeKnowledge", true);
    }
}
