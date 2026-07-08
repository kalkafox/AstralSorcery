/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEntitySynchronized
 * Created by HellFirePvP
 * Date: 11.05.2016 / 18:17
 */
public abstract class TileEntitySynchronized extends BlockEntity implements ILocatable {

    protected static final Random rand = new Random();
    protected static final AABB BOX = new AABB(0, 0, 0, 1, 1, 1);

    protected TileEntitySynchronized(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public BlockPos getLocationPos() {
        return this.getPos();
    }

    @Override
    public void read(BlockState state, CompoundTag nbt) {
        super.read(state, nbt);
        readCustomNBT(nbt);
        readSaveNBT(nbt);
    }

    //Both Network & Chunk-saving
    public void readCustomNBT(CompoundTag compound) {}

    //Only Network-read
    public void readNetNBT(CompoundTag compound) {}

    //Only Chunk-read
    public void readSaveNBT(CompoundTag compound) {}

    @Override
    public final CompoundTag write(CompoundTag compound) {
        compound = super.write(compound);
        writeCustomNBT(compound);
        writeSaveNBT(compound);
        return compound;
    }

    //Both Network & Chunk-saving
    public void writeCustomNBT(CompoundTag compound) {}

    //Only Network-write
    public void writeNetNBT(CompoundTag compound) {}

    //Only Chunk-write
    public void writeSaveNBT(CompoundTag compound) {}

    @Override
    public final ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag compound = new CompoundTag();
        super.write(compound);
        writeCustomNBT(compound);
        writeNetNBT(compound);
        return new ClientboundBlockEntityDataPacket(getPos(), 255, compound);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = new CompoundTag();
        super.write(compound);
        writeCustomNBT(compound);
        return compound;
    }

    public final void onDataPacket(Connection manager, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(manager, packet);
        readCustomNBT(packet.getNbtCompound());
        readNetNBT(packet.getNbtCompound());
        this.onDataReceived();
    }

    @OnlyIn(Dist.CLIENT)
    protected void onDataReceived() {}

    public void markForUpdate() {
        if (getWorld() != null) {
            BlockState thisState = this.getBlockState();
            getWorld().notifyBlockUpdate(getPos(), thisState, thisState, 3);
        }
        markDirty();
    }

    public ItemEntity dropItemOnTop(ItemStack stack) {
        return ItemUtils.dropItem(getWorld(), getPos().getX() + 0.5, getPos().getY() + 1.5, getPos().getZ() + 0.5, stack);
    }

    public boolean removeSelf() {
        if (this.getWorld().isRemote()) {
            return false;
        }
        return this.getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());
    }
}
