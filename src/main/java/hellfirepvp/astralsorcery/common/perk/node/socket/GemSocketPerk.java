/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.socket;

import net.minecraft.network.chat.Component;

import hellfirepvp.astralsorcery.common.data.research.PlayerPerkData;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GemSocketPerk
 * Created by HellFirePvP
 * Date: 09.08.2019 / 07:23
 */
public interface GemSocketPerk {

    public static final String SOCKET_DATA_KEY = "socketedItem";

    default public boolean hasItem(Player player, LogicalSide direction) {
        return hasItem(player, direction, null);
    }

    default public boolean hasItem(Player player, LogicalSide direction, @Nullable CompoundTag data) {
        return !getContainedItem(player, direction, data).isEmpty();
    }

    default public ItemStack getContainedItem(Player player, LogicalSide direction) {
        return getContainedItem(player, direction, null);
    }

    default public ItemStack getContainedItem(Player player, LogicalSide direction, @Nullable CompoundTag dataOvr) {
        if (!(this instanceof AbstractPerk)) {
            throw new UnsupportedOperationException("Cannot do perk-specific socketing logic on something that's not a perk!");
        }
        CompoundTag data = dataOvr != null ? dataOvr : ((AbstractPerk) this).getPerkData(player, direction);
        if (data == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = NBTHelper.getStack(data, SOCKET_DATA_KEY);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    default public boolean setContainedItem(Player player, LogicalSide direction, ItemStack stack) {
        return setContainedItem(player, direction, null, stack);
    }

    default public <T extends AbstractPerk & GemSocketPerk> boolean setContainedItem(Player player, LogicalSide direction, @Nullable CompoundTag dataOvr, ItemStack stack) {
        if (!(this instanceof AbstractPerk)) {
            throw new UnsupportedOperationException("Cannot do perk-specific socketing logic on something that's not a perk!");
        }
        T thisPerk = (T) this;
        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        if (!prog.getPerkData().hasPerkEffect(thisPerk)) {
            return false;
        }
        //a given data override signifies that that override should be used, but not written.
        boolean useLiveData = dataOvr == null;
        CompoundTag data = dataOvr;
        if (useLiveData) {
            data = ((AbstractPerk) this).getPerkData(player, direction);
        }
        if (data == null) {
            return false;
        }
        CompoundTag prev = data.copy();

        if (stack.isEmpty()) {
            ItemStack existing = NBTHelper.getStack(data, SOCKET_DATA_KEY);
            if (!existing.isEmpty() && existing.getItem() instanceof GemSocketItem) {
                ((GemSocketItem) existing.getItem()).onExtract(existing, thisPerk, player, prog);
            }
            data.remove(SOCKET_DATA_KEY);
        } else {
            if (stack.getItem() instanceof GemSocketItem) {
                ((GemSocketItem) stack.getItem()).onInsert(stack, thisPerk, player, prog);
            }
            NBTHelper.setStack(data, SOCKET_DATA_KEY, stack);
        }

        if (useLiveData) {
            ResearchManager.setPerkData(player, thisPerk, prev, data);
        }
        return true;
    }

    default public void dropItemToPlayer(Player player) {
        dropItemToPlayer(player, null);
    }

    default public void dropItemToPlayer(Player player, @Nullable CompoundTag data) {
        if (!(this instanceof AbstractPerk)) {
            throw new UnsupportedOperationException("Cannot do perk-specific socketing logic on something that's not a perk!");
        }

        if (player.getCommandSenderWorld().isClientSide()) {
            return;
        }

        boolean updateData = data == null;
        if (updateData) {
            data = ((AbstractPerk) this).getPerkData(player, LogicalSide.SERVER);
        }
        if (data == null) {
            return;
        }
        CompoundTag prev = data.copy();

        ItemStack contained = getContainedItem(player, LogicalSide.SERVER, data);
        if (!contained.isEmpty()) {
            if (!player.getArmor(contained)) {
                ItemUtils.dropItem(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), contained);
            }
        }
        setContainedItem(player, LogicalSide.SERVER, data, ItemStack.EMPTY);

        if (updateData) {
            ResearchManager.setPerkData(player, (AbstractPerk) this, prev, data);
        }
    }

    @OnlyIn(Dist.CLIENT)
    default public <T extends AbstractPerk & GemSocketPerk> void addTooltipInfo(Collection<MutableComponent> tooltip) {
        if (!(this instanceof AbstractPerk)) {
            return;
        }
        T thisPerk = (T) this;
        PlayerProgress prog = ResearchHelper.getClientProgress();
        if (!prog.isValid()) {
            return;
        }
        PlayerPerkData perkData = prog.getPerkData();

        ItemStack contained = getContainedItem(Minecraft.getInstance().player, LogicalSide.CLIENT);
        if (contained.isEmpty()) {
            tooltip.add(Component.translatable("perk.info.astralsorcery.gem.empty").withStyle(ChatFormatting.GRAY));
            if (perkData.hasPerkEffect(thisPerk)) {
                tooltip.add(Component.translatable("perk.info.astralsorcery.gem.content.empty").withStyle(ChatFormatting.GRAY));

                boolean has = !ItemUtils.findItemsIndexedInPlayerInventory(Minecraft.getInstance().player, stack -> {
                    if (stack.isEmpty() || !(stack.getItem() instanceof GemSocketItem)) {
                        return false;
                    }
                    GemSocketItem item = (GemSocketItem) stack.getItem();
                    return item.canBeInserted(stack, thisPerk, Minecraft.getInstance().player, ResearchHelper.getClientProgress(), LogicalSide.CLIENT);
                }).isEmpty();
                if (!has) {
                    tooltip.add(Component.translatable("perk.info.astralsorcery.gem.content.empty.none")
                            .withStyle(ChatFormatting.RED));
                }
            }
        } else {
            if (contained.getItem() instanceof GemSocketItem) {
                GemSocketItem item = (GemSocketItem) contained.getItem();
                List<MutableComponent> additionalToolTip = new ArrayList<>();
                item.addTooltip(contained, thisPerk, additionalToolTip);
                if (!additionalToolTip.isEmpty()) {
                    tooltip.addAll(additionalToolTip);
                    tooltip.add(Component.literal(""));
                }
            }

            tooltip.add(Component.translatable("perk.info.astralsorcery.gem.content.item", contained.getDisplayName())
                    .withStyle(ChatFormatting.GRAY));
            if (perkData.hasPerkEffect(thisPerk)) {
                tooltip.add(Component.translatable("perk.info.astralsorcery.gem.remove").withStyle(ChatFormatting.GRAY));
            }
        }
    }

}
