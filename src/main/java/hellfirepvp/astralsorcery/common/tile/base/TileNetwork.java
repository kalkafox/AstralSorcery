/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionNetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileNetwork
 * Created by HellFirePvP
 * Date: 03.08.2016 / 18:12
 */
public abstract class TileNetwork<T extends IPrismTransmissionNode> extends TileEntityTick {

    protected static final Random random = new Random();
    private boolean isNetworkInformed = false;

    private T cachedNetworkNode = null;
    private boolean needsNetworkSync = false;

    protected TileNetwork(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Nullable
    public T getNetworkNode() {
        if (cachedNetworkNode != null) {
            if (!cachedNetworkNode.getLocationPos().equals(getBlockPos())) {
                cachedNetworkNode = null;
            }
        }
        if (cachedNetworkNode == null) {
            cachedNetworkNode = resolveNode();
        }
        return cachedNetworkNode;
    }

    @Nullable
    private T resolveNode() {
        IPrismTransmissionNode node = WorldNetworkHandler.getNetworkHandler(getLevel()).getTransmissionNode(getBlockPos());
        if (node == null) {
            return null;
        }
        return (T) node;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide()) {
            if (!this.isNetworkInformed) {
                if (!TransmissionNetworkHelper.isTileInNetwork(this)) {
                    TransmissionNetworkHelper.informNetworkTilePlacement(this);
                }
                this.isNetworkInformed = true;
            }

            if (this.needsNetworkSync) {
                this.doNetworkSync();
            }
        }
    }

    protected void doNetworkSync() {
        T networkNode = this.getNetworkNode();
        if (networkNode != null && networkNode.updateFromTileEntity(this)) {
            this.needsNetworkSync = false;
            this.markForUpdate();
            this.preventNetworkSync();
        }
    }

    @Override
    public void markForUpdate() {
        super.markForUpdate();
        this.needsNetworkSync = true;
    }

    protected void preventNetworkSync() {
        this.needsNetworkSync = false;
    }

    public boolean needsNetworkSync() {
        return needsNetworkSync;
    }

    public void onBreak() {}

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (this.getLevel() == null || this.getLevel().isClientSide()) {
            return;
        }
        // setRemoved can be called more than once (and is also used by chunk/tick lifecycle
        // code). Do not attempt to remove a node that this tile has already torn down.
        if (this.isNetworkInformed || TransmissionNetworkHelper.isTileInNetwork(this)) {
            TransmissionNetworkHelper.informNetworkTileRemoval(this);
        }
        this.isNetworkInformed = false;
        this.cachedNetworkNode = null;
    }

    @Override
    public void writeSaveNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeSaveNBT(pattern, registries);

        pattern.putBoolean("needsNetworkSync", this.needsNetworkSync);
    }

    @Override
    public void readSaveNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readSaveNBT(pattern, registries);

        this.needsNetworkSync = pattern.getBoolean("needsNetworkSync");
    }
}
