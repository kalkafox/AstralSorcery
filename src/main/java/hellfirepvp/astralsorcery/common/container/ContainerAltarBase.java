/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerAltarBase
 * Created by HellFirePvP
 * Date: 15.08.2019 / 15:54
 */
public abstract class ContainerAltarBase extends ContainerTileEntity<TileAltar> {

    private final Inventory playerInv;
    private final TileInventory invHandler;

    protected ContainerAltarBase(TileAltar altar, @Nullable MenuType<?> type, Inventory inv, int containerId) {
        super(altar, type, containerId);
        this.playerInv = inv;
        this.invHandler = altar.getItems();

        bindPlayerInventory(this.playerInv);
        bindAltarInventory(this.invHandler);
    }

    abstract void bindPlayerInventory(Inventory plInventory);

    abstract void bindAltarInventory(TileInventory altarInventory);

    abstract Optional<ItemStack> handleCustomTransfer(Player player, int index);

    //Yes this is not a pretty solution. tell me a better one.
    public abstract int translateIndex(int fromIndex);

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            Optional<ItemStack> stackOpt = this.handleCustomTransfer(playerIn, index);
            if (stackOpt.isPresent()) {
                return stackOpt.get();
            }

            if (index >= 0 && index < 27) {
                if (!this.moveItemStackTo(slotStack, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 27 && index < 36) {
                if (!this.moveItemStackTo(slotStack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.getTileEntity().getBlockPos();
        if (MiscUtils.getTileAt(this.getTileEntity().getLevel(), pos, BlockEntity.class, false) != this.getTileEntity()) {
            return false;
        } else {
            return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }
    }
}
