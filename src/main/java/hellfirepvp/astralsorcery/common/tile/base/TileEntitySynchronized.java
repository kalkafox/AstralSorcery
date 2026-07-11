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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEntitySynchronized
 * Created by HellFirePvP
 * Date: 11.05.2016 / 18:17
 */
public abstract class TileEntitySynchronized extends BlockEntity implements ILocatable {

    protected static final Random random = new Random();
    protected static final AABB BOX = new AABB(0, 0, 0, 1, 1, 1);

    protected TileEntitySynchronized(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public BlockPos getLocationPos() {
        return this.getBlockPos();
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        readCustomNBT(nbt, registries);
        readSaveNBT(nbt, registries);
    }

    //Both Network & Chunk-saving
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {}

    //Only Network-read
    public void readNetNBT(CompoundTag pattern, HolderLookup.Provider registries) {}

    //Only Chunk-read
    public void readSaveNBT(CompoundTag pattern, HolderLookup.Provider registries) {}

    @Override
    protected final void saveAdditional(CompoundTag pattern, HolderLookup.Provider registries) {
        super.saveAdditional(pattern, registries);
        writeCustomNBT(pattern, registries);
        writeSaveNBT(pattern, registries);
    }

    //Both Network & Chunk-saving
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {}

    //Only Network-write
    public void writeNetNBT(CompoundTag pattern, HolderLookup.Provider registries) {}

    //Only Chunk-write
    public void writeSaveNBT(CompoundTag pattern, HolderLookup.Provider registries) {}

    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (be, registries) -> {
            CompoundTag pattern = new CompoundTag();
            writeCustomNBT(pattern, registries);
            writeNetNBT(pattern, registries);
            return pattern;
        });
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag pattern = new CompoundTag();
        writeCustomNBT(pattern, registries);
        return pattern;
    }

    @Override
    public final void onDataPacket(Connection manager, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        readCustomNBT(packet.getTag(), registries);
        readNetNBT(packet.getTag(), registries);
        this.onDataReceived();
    }

    @OnlyIn(Dist.CLIENT)
    protected void onDataReceived() {}

    public void markForUpdate() {
        if (getLevel() != null) {
            BlockState thisState = this.getBlockState();
            getLevel().findNearestBiome(getBlockPos(), thisState, thisState, 3);
        }
        setChanged();
    }

    public ItemEntity dropItemOnTop(ItemStack stack) {
        return ItemUtils.dropItem(getLevel(), getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.5, getBlockPos().getZ() + 0.5, stack);
    }

    public boolean removeSelf() {
        if (this.getLevel().isClientSide()) {
            return false;
        }
        return this.getLevel().setBlock(this.getBlockPos(), Blocks.AIR.defaultBlockState());
    }
}
