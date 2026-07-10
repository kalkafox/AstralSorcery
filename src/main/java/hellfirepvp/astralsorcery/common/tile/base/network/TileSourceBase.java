/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base.network;

import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.starlight.IStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.TileNetwork;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileSourceBase
 * Created by HellFirePvP
 * Date: 30.06.2019 / 21:06
 */
public abstract class TileSourceBase<T extends ITransmissionSource> extends TileNetwork<T> implements IStarlightSource<T>, LinkableTileEntity {

    protected boolean needsNetworkChainRebuild = false;
    private boolean linked = false;
    private final List<BlockPos> positions = new LinkedList<>();

    protected TileSourceBase(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public boolean hasBeenLinked() {
        return linked;
    }

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);
        positions.clear();

        if (pattern.contains("linked")) {
            ListTag list = pattern.getList("linked", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                positions.add(NBTHelper.readBlockPosFromNBT(tag));
            }
        }

        this.linked = pattern.getBoolean("wasLinkedBefore");
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(pos, tag);
            list.add(tag);
        }
        pattern.put("linked", list);
        pattern.putBoolean("wasLinkedBefore", linked);
    }

    @Override
    @Nonnull
    public BlockPos getTrPos() {
        return getBlockPos();
    }

    @Override
    @Nonnull
    public Level getTrWorld() {
        return getLevel();
    }

    @Override
    public void onBlockLinkCreate(Player player, BlockPos other) {
        if (other.equals(getBlockPos())) return;

        if (TransmissionNetworkHelper.createTransmissionLink(this, other)) {
            if (!this.positions.contains(other)) {
                this.positions.add(other);
                this.needsNetworkChainRebuild = true;
                markForUpdate();
            }

            if (!hasBeenLinked()) {
                this.linked = true;
            }
        }
    }

    @Override
    public void onEntityLinkCreate(Player player, LivingEntity linked) {
    }

    @Override
    public boolean tryLinkBlock(Player player, BlockPos other) {
        return !other.equals(getBlockPos()) && TransmissionNetworkHelper.canCreateTransmissionLink(this, other);
    }

    @Override
    public boolean tryLinkEntity(Player player, LivingEntity other) {
        return false;
    }

    @Override
    public boolean tryUnlink(Player player, BlockPos other) {
        if (other.equals(getBlockPos())) return false;

        if (TransmissionNetworkHelper.hasTransmissionLink(this, other)) {
            TransmissionNetworkHelper.removeTransmissionLink(this, other);
            this.positions.remove(other);
            this.needsNetworkChainRebuild = true;
            markForUpdate();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesAcceptLinks() {
        return false;
    }

    @Override
    public List<BlockPos> getLinkedPositions() {
        return positions;
    }

    @Override
    public boolean needsToRefreshNetworkChain() {
        return this.needsNetworkChainRebuild;
    }

    @Override
    public void markChainRebuilt() {
        this.needsNetworkChainRebuild = false;
    }
}
