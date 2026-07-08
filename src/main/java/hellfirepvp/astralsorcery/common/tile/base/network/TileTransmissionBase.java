/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base.network;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.starlight.IStarlightTransmission;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.TileNetwork;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileTransmissionBase
 * Created by HellFirePvP
 * Date: 30.06.2019 / 21:46
 */
public abstract class TileTransmissionBase<T extends IPrismTransmissionNode> extends TileNetwork<T> implements IStarlightTransmission<T>, LinkableTileEntity {

    private final List<BlockPos> positions = new LinkedList<>();

    protected TileTransmissionBase(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public boolean onSelect(Player player) {
        if (player.isSneaking()) {
            for (BlockPos linkTo : Lists.newArrayList(getLinkedPositions())) {
                tryUnlink(player, linkTo);
            }
            player.sendMessage(Component.translatable("astralsorcery.misc.link.unlink.all").withStyle(TextFormatting.GREEN), Util.DUMMY_UUID);
            return false;
        }
        return true;
    }

    public abstract boolean isSingleLink();

    @Override
    public void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(pos, tag);
            list.add(tag);
        }
        compound.put("linked", list);
    }

    @Override
    public void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);
        positions.clear();

        if (compound.contains("linked")) {
            ListTag list = compound.getList("linked", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                positions.add(NBTHelper.readBlockPosFromNBT(tag));
            }
        }
    }

    @Override
    public void onBlockLinkCreate(Player player, BlockPos other) {
        if (other.equals(getPos())) return;

        if (TransmissionNetworkHelper.createTransmissionLink(this, other)) {
            if (this.isSingleLink()) {
                this.positions.clear();
            }

            if (this.isSingleLink() || !this.positions.contains(other)) {
                this.positions.add(other);
                markForUpdate();
            }
        }
    }

    @Override
    public void onEntityLinkCreate(Player player, LivingEntity linked) {
    }

    @Override
    @Nonnull
    public BlockPos getTrPos() {
        return getPos();
    }

    @Override
    @Nonnull
    public Level getTrWorld() {
        return getWorld();
    }

    @Override
    public boolean tryLinkBlock(Player player, BlockPos other) {
        return !other.equals(getPos()) && TransmissionNetworkHelper.canCreateTransmissionLink(this, other);
    }

    @Override
    public boolean tryLinkEntity(Player player, LivingEntity other) {
        return false;
    }

    @Override
    public boolean tryUnlink(Player player, BlockPos other) {
        if (other.equals(getPos())) return false;

        if (TransmissionNetworkHelper.hasTransmissionLink(this, other)) {
            TransmissionNetworkHelper.removeTransmissionLink(this, other);
            this.positions.remove(other);
            markForUpdate();
            return true;
        }
        return false;
    }

    @Override
    public List<BlockPos> getLinkedPositions() {
        return positions;
    }
}