/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.base;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlockStorage
 * Created by HellFirePvP
 * Date: 23.02.2020 / 17:36
 */
public interface ItemBlockStorage {

    Random random = new Random();

    static boolean storeBlockState(ItemStack stack, Level level, BlockPos pos) {
        if (MiscUtils.getTileAt(level, pos, BlockEntity.class, true) != null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir(level, pos) ||
                state.getDestroySpeed(level, pos) == -1 ||
                ItemUtils.createBlockStack(state).isEmpty()) {
            return false;
        }
        CompoundTag persistent = NBTHelper.getPersistentData(stack);
        ListTag stored = persistent.getList("storedStates", Constants.NBT.TAG_COMPOUND);
        stored.add(NBTHelper.getBlockStateNBTTag(state));
        persistent.put("storedStates", stored);
        return true;
    }

    static void clearContainerFor(Player player) {
        Tuple<InteractionHand, ItemStack> held = MiscUtils.getMainOrOffHand(player, stack -> stack.getItem() instanceof ItemBlockStorage);
        if (held != null) {
            NBTHelper.getPersistentData(held.getB()).remove("storedStates");
        }
    }

    @Nonnull
    static List<Tuple<ItemStack, Integer>> getInventoryMatchingItemStacks(Player player, ItemStack referenceContainer) {
        Map<BlockState, Tuple<ItemStack, Integer>> storedStates = getInventoryMatching(player, referenceContainer);
        List<Tuple<ItemStack, Integer>> foundStacks = new ArrayList<>(storedStates.values());
        foundStacks.sort(Comparator.comparing(tpl -> RegistryHelper.getKey(tpl.getA().getItem())));
        return foundStacks;
    }

    @Nonnull
    static Map<BlockState, Tuple<ItemStack, Integer>> getInventoryMatching(Player player, ItemStack referenceContainer) {
        Map<BlockState, ItemStack> mappedStacks = ItemBlockStorage.getMappedStoredStates(referenceContainer);
        Map<BlockState, Tuple<ItemStack, Integer>> foundContents = new HashMap<>();
        for (BlockState state : mappedStacks.keySet()) {
            ItemStack stored = mappedStacks.get(state);

            int countDisplay = 0;
            Collection<ItemStack> stacks = ItemUtils.findItemsInPlayerInventory(player, stored, true);
            for (ItemStack found : stacks) {
                countDisplay += found.getCount();
            }
            foundContents.put(state, new Tuple<>(stored.copy(), countDisplay));
        }
        return foundContents;
    }

    @Nonnull
    static Map<BlockState, ItemStack> getMappedStoredStates(ItemStack referenceContainer) {
        List<BlockState> blockStates = getStoredStates(referenceContainer);
        Map<BlockState, ItemStack> map = new LinkedHashMap<>();
        for (BlockState state : blockStates) {
            ItemStack stack = ItemUtils.createBlockStack(state);
            if (!stack.isEmpty()) {
                map.put(state, stack);
            }
        }
        return map;
    }

    @Nonnull
    static NonNullList<BlockState> getStoredStates(ItemStack referenceContainer) {
        NonNullList<BlockState> states = NonNullList.create();
        if (!referenceContainer.isEmpty() && referenceContainer.getItem() instanceof ItemBlockStorage) {
            CompoundTag persistent = NBTHelper.getPersistentData(referenceContainer);
            ListTag stored = persistent.getList("storedStates", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < stored.size(); i++) {
                BlockState state = NBTHelper.getBlockStateFromTag(stored.getCompound(i));
                if (state != null) {
                    states.add(state);
                }
            }
        }
        return states;
    }

    static Random getPreviewRandomFromWorld(Level level) {
        long tempSeed = 0x6834F10A91B03F15L;
        tempSeed *= (level.getGameTime() / 40) << 8;
        return new Random(tempSeed);
    }
}
