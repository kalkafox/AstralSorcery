/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.item;

import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.CapabilityItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static hellfirepvp.astralsorcery.common.util.item.ItemComparator.Clause.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemUtils
 * Created by HellFirePvP
 * Date: 31.07.2016 / 17:51
 */
public class ItemUtils {

    public static final IItemHandler EMPTY_INVENTORY = new ItemHandlerEmpty();
    private static final Random random = new Random();

    public static ItemEntity dropItem(Level level, double x, double y, double z, ItemStack stack) {
        if (level.isClientSide) {
            return null;
        }
        ItemEntity ei = new ItemEntity(level, x, y, z, stack);
        ei.setDeltaMovement(new Vec3(0, 0, 0));
        level.addEntity(ei);
        ei.setPickupDelay(20);
        return ei;
    }

    public static ItemEntity dropItemNaturally(Level level, double x, double y, double z, ItemStack stack) {
        if (level.isClientSide) {
            return null;
        }
        ItemEntity ei = new ItemEntity(level, x, y, z, stack);
        applyRandomDropOffset(ei);
        level.addEntity(ei);
        ei.setPickupDelay(20);
        return ei;
    }

    public static void decrementItem(TileInventory inventory, int slot, Consumer<ItemStack> handleExcess) {
        decrementItem(() -> inventory.getStackInSlot(slot), stack -> inventory.setStackInSlot(slot, stack), handleExcess);
    }

    public static void decrementItem(Supplier<ItemStack> getFromInventory, Consumer<ItemStack> setIntoInventory, Consumer<ItemStack> handleExcess) {
        ItemStack toConsume = getFromInventory.get();
        toConsume = ItemUtils.copyStackWithSize(toConsume, toConsume.getCount());

        ItemStack toReplaceWith = ItemStack.EMPTY;
        if (toConsume.hasCraftingRemainingItem()) {
            toReplaceWith = toConsume.getCraftingRemainingItem();
        }

        toConsume.shrink(1);

        //Stuff might need to be placed back into the inventory
        if (!toReplaceWith.isEmpty()) {
            if (toConsume.isEmpty()) {
                setIntoInventory.accept(toReplaceWith);
            } else if (ItemComparator.compare(toConsume, toReplaceWith, ItemComparator.Clause.Sets.ITEMSTACK_STRICT_NOAMOUNT)) {
                toReplaceWith.grow(toConsume.getCount());
                if (toReplaceWith.getCount() > toReplaceWith.getMaxStackSize()) {
                    int overcapped = toReplaceWith.getCount() - toReplaceWith.getMaxStackSize();
                    setIntoInventory.accept(ItemUtils.copyStackWithSize(toReplaceWith, toReplaceWith.getMaxStackSize()));
                    handleExcess.accept(ItemUtils.copyStackWithSize(toReplaceWith, overcapped));
                } else {
                    setIntoInventory.accept(toReplaceWith);
                }
            } else {
                //Different item, no space left. welp.
                handleExcess.accept(toReplaceWith);
            }
        } else {
            //Or the item just doesn't have a container. then we can just set the shrunk stack back.
            setIntoInventory.accept(toConsume);
        }
    }

    public static boolean isEquippableArmor(Entity entity, ItemStack stack) {
        for (EquipmentSlot type : EquipmentSlot.values()) {
            if (type.getType() == EquipmentSlot.Group.ARMOR) {
                if (stack.canEquip(type, entity)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack dropItemToPlayer(Player player, ItemStack stack) {
        Level level = player.getCommandSenderWorld();
        if (level.isClientSide() || stack.isEmpty()) {
            return stack;
        }
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        if (item.getItem().isEmpty()) {
            return stack;
        }
        item.setNoPickupDelay();
        try {
            item.playerTouch(player);
        } catch (Exception ignored) {
            //Guess some mod could run into an issue here...
        }
        if (item.isAlive()) {
            return item.getItem().copy();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static void applyRandomDropOffset(ItemEntity item) {
        item.setDeltaMovement(random.nextFloat() * 0.3F - 0.15D,
                random.nextFloat() * 0.3F - 0.05D,
                random.nextFloat() * 0.3F - 0.15D);
    }

    @Nonnull
    public static ItemStack changeItem(@Nonnull ItemStack stack, @Nonnull Item item) {
        CompoundTag nbt = stack.write(new CompoundTag());
        nbt.putString("id", item.getRegistryName().toString());
        return ItemStack.read(nbt);
    }

    @Nonnull
    public static ItemStack createBlockStack(BlockState state) {
        return new ItemStack(state.getBlock());
    }

    @Nullable
    public static BlockState createBlockState(ItemStack stack) {
        Block b = Block.getBlockFromItem(stack.getItem());
        if (b == Blocks.AIR) {
            return null;
        }
        return b.defaultBlockState();
    }

    @Nonnull
    public static List<ItemStack> getItemsOfTag(ResourceLocation key) {
        Tag<Item> tag = ItemTags.getAllTags().get(key);
        return tag == null ? Collections.emptyList() : getItemsOfTag(tag);
    }

    @Nonnull
    public static List<ItemStack> getItemsOfTag(Tag<Item> itemTag) {
        return itemTag.getValues().stream().map(ItemStack::new).collect(Collectors.toList());
    }

    public static Collection<ItemStack> scanInventoryFor(IItemHandler handler, Item i) {
        List<ItemStack> out = new LinkedList<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (!s.isEmpty() && s.getItem() == i)
                out.add(copyStackWithSize(s, s.getCount()));
        }
        return out;
    }

    public static Collection<ItemStack> scanInventoryForMatching(IItemHandler handler, ItemStack match, boolean strict) {
        return findItemsInInventory(handler, match, strict);
    }

    public static Collection<ItemStack> findItemsInPlayerInventory(Player player, ItemStack match, boolean strict) {
        IItemHandler handler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(EMPTY_INVENTORY);
        Collection<ItemStack> results = findItemsInInventory(handler, match, strict);

        // 1.21 port: Botania integration is excluded from the build for now.
        //if (Mods.BOTANIA.isPresent()) {
        //    results.addAll(IntegrationBotania.findProvidersProvidingItems(player, match));
        //}

        return results;
    }

    public static Collection<ItemStack> findItemsInInventory(IItemHandler handler, ItemStack match, boolean strict) {
        List<ItemStack> stacksOut = new LinkedList<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (strict ?
                    ItemComparator.compare(s, match, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE) :
                    ItemComparator.compare(s, match, ITEM)) {
                stacksOut.add(copyStackWithSize(s, s.getCount()));
            }
        }
        return stacksOut;
    }

    public static Map<Integer, ItemStack> findItemsIndexedInPlayerInventory(Player player, Predicate<ItemStack> match) {
        return findItemsIndexedInInventory(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(EMPTY_INVENTORY), match);
    }

    public static Map<Integer, ItemStack> findItemsIndexedInInventory(IItemHandler handler, ItemStack match, boolean strict) {
        return findItemsIndexedInInventory(handler,
                (s) -> strict ?
                        ItemComparator.compare(s, match, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE) :
                        ItemComparator.compare(s, match, ITEM));
    }

    public static Map<Integer, ItemStack> findItemsIndexedInInventory(IItemHandler handler, Predicate<ItemStack> match) {
        Map<Integer, ItemStack> stacksOut = new HashMap<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (match.test(s)) {
                stacksOut.put(j, copyStackWithSize(s, s.getCount()));
            }
        }
        return stacksOut;
    }

    public static boolean consumeFromPlayerInventory(Player player, ItemStack requestingItemStack, ItemStack toConsume, boolean simulate) {
        int consumed = 0;
        ItemStack tryConsume = copyStackWithSize(toConsume, toConsume.getCount() - consumed);

        if (tryConsume.isEmpty()) {
            return true;
        }

        IItemHandlerModifiable handler = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(EMPTY_INVENTORY);
        if (consumeFromInventory(handler, tryConsume, simulate)) {
            return true;
        }
        
        // 1.21 port: Botania integration is excluded from the build for now.
        //if (Mods.BOTANIA.isPresent()) {
        //    if (IntegrationBotania.consumeFromPlayerInventory(player, requestingItemStack, toConsume, simulate)) {
        //        return true;
        //    }
        //}
                
        return false;
    }

    public static boolean tryConsumeFromInventory(IItemHandler handler, ItemStack toConsume, boolean simulate) {
        return handler instanceof IItemHandlerModifiable && consumeFromInventory((IItemHandlerModifiable) handler, toConsume, simulate);
    }

    public static boolean consumeFromInventory(IItemHandlerModifiable handler, ItemStack toConsume, boolean simulate) {
        Map<Integer, ItemStack> contents = findItemsIndexedInInventory(handler, toConsume, false);
        if (contents.isEmpty()) return false;

        int cAmt = toConsume.getCount();
        for (int slot : contents.keySet()) {
            ItemStack inSlot = contents.get(slot);
            int toRemove = cAmt > inSlot.getCount() ? inSlot.getCount() : cAmt;
            cAmt -= toRemove;
            if (!simulate) {
                handler.setStackInSlot(slot, copyStackWithSize(inSlot, inSlot.getCount() - toRemove));
            }
            if (cAmt <= 0) {
                break;
            }
        }
        return cAmt <= 0;
    }

    public static void dropEquipment(IItemHandler handle, Level worldIn, BlockPos pos) {
        if (worldIn.isClientSide) {
            return;
        }
        for (int i = 0; i < handle.getSlots(); i++) {
            ItemStack stack = handle.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            dropItemNaturally(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
    }

    /*public static void decrStackInInventory(ItemStack[] stacks, int slot) {
        if (slot < 0 || slot >= stacks.length) return;
        ItemStack st = stacks[slot];
        if (st == null) return;
        st.getCount()--;
        if (st.getCount() <= 0) {
            stacks[slot] = null;
        }
    }*/

    public static void decrStackInInventory(ItemStackHandler handler, int slot) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return;
        }
        ItemStack st = handler.getStackInSlot(slot);
        if (st.isEmpty()) {
            return;
        }
        st.setCount(st.getCount() - 1);
        if (st.getCount() <= 0) {
            handler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public static boolean tryPlaceItemInInventory(@Nonnull ItemStack stack, IItemHandler handler) {
        return tryPlaceItemInInventory(stack, handler, 0, handler.getSlots());
    }

    public static boolean tryPlaceItemInInventory(@Nonnull ItemStack stack, IItemHandler handler, int start, int end) {
        ItemStack toAdd = stack.copy();
        if (!hasInventorySpace(toAdd, handler, start, end)) {
            return false;
        }
        int max = stack.getMaxStackSize();

        for (int i = start; i < end; i++) {
            ItemStack in = handler.getStackInSlot(i);
            if (in.isEmpty()) {
                int added = Math.min(stack.getCount(), max);
                stack.setCount(stack.getCount() - added);
                handler.insertItem(i, copyStackWithSize(stack, added), false);
                return true;
            } else {
                if (ItemComparator.compare(stack, in, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE)) {

                    int space = max - in.getCount();
                    int added = Math.min(stack.getCount(), space);
                    stack.setCount(stack.getCount() - added);
                    handler.getStackInSlot(i).setCount(handler.getStackInSlot(i).getCount() + added);
                    if (stack.getCount() <= 0)
                        return true;
                }
            }
        }
        return stack.getCount() == 0;
    }

    public static boolean hasInventorySpace(@Nonnull ItemStack stack, IItemHandler handler, int rangeMin, int rangeMax) {
        int size = stack.getCount();
        int max = stack.getMaxStackSize();
        for (int i = rangeMin; i < rangeMax && size > 0; i++) {
            ItemStack in = handler.getStackInSlot(i);
            if (in.isEmpty()) {
                size -= max;
            } else {
                if (ItemComparator.compare(stack, in, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE)) {
                    int space = max - in.getCount();
                    size -= space;
                }
            }
        }
        return size <= 0;
    }

    public static ItemStack copyStackWithSize(@Nonnull ItemStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack s = stack.copy();
        s.setCount(amount);
        return s;
    }

    private static class ItemHandlerEmpty implements IItemHandlerModifiable {

        @Override
        public int getSlots() {
            return 0;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean mayPlace(int slot, @Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {}
    }

}
