/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.common.change.ChangeObserverStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEntityTick
 * Created by HellFirePvP
 * Date: 02.08.2016 / 17:34
 */
public abstract class TileEntityTick extends TileEntitySynchronized implements TickableBlockEntity, TileRequiresMultiblock {

    private boolean doesSeeSky = false;
    private int lastUpdateTick = -1;

    private ChangeSubscriber<ChangeObserverStructure> structureMatch;
    private boolean hasMultiblock = false;

    protected int tickCount = 0;

    protected TileEntityTick(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick() {
        if (tickCount == 0) {
            onFirstTick();
        }

        tickCount++;
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return null;
    }

    //Since no-sky worlds count always as "can't see sky" even if it's exposed to the sky
    //Set to true to always count as seeing the sky in no-sky worlds.
    public boolean seesSkyInNoSkyWorlds() {
        return false;
    }

    protected void onFirstTick() {}

    public int getTicksExisted() {
        return tickCount;
    }

    public boolean doesSeeSky() {
        if (getLevel().isClientSide()) {
            return this.doesSeeSky;
        }

        if (lastUpdateTick == -1 || (tickCount - lastUpdateTick) >= 20) {
            lastUpdateTick = tickCount;

            boolean prevSky = doesSeeSky;
            boolean newSky = MiscUtils.canSeeSky(this.getLevel(), this.getBlockPos().above(), true, this.seesSkyInNoSkyWorlds(), this.doesSeeSky);
            if (prevSky != newSky) {
                this.notifySkyStateUpdate(prevSky, newSky);
                this.doesSeeSky = newSky;
                this.markForUpdate();
            }
        }
        return doesSeeSky;
    }

    public boolean hasMultiblock() {
        if (getLevel().isClientSide()) {
            return this.hasMultiblock;
        }

        if (this.getRequiredStructureType() == null) {
            refreshMatcher();
            resetMultiblockState();
            return false;
        }

        refreshMatcher();
        if (this.structureMatch == null) {
            this.structureMatch = this.getRequiredStructureType().observe(getLevel(), getBlockPos());
        }
        boolean prevFound = this.hasMultiblock;
        boolean found = this.structureMatch.isValid(getLevel());
        if (prevFound != found) {
            LogCategory.STRUCTURE_MATCH.info(() ->
                    "Structure match updated: " + this.getClass().getName() + " at " + this.getBlockPos() +
                            " (" + this.hasMultiblock + " -> " + found + ")");
            this.notifyMultiblockStateUpdate(prevFound, found);
            this.hasMultiblock = found;
            this.markForUpdate();
        }
        return this.hasMultiblock;
    }

    private void refreshMatcher() {
        StructureType struct = this.getRequiredStructureType();
        if (this.structureMatch != null) {
            //Same registry name as the structure type.
            ResourceLocation key = this.structureMatch.getObserver().getProviderRegistryName();
            if (struct == null || !key.equals(struct.getRegistryName())) {
                ObserverHelper.getHelper().removeObserver(getLevel(), getBlockPos());
                this.structureMatch = null;
            }
        }
        if (struct == null && ObserverHelper.getHelper().getSubscriber(getLevel(), getBlockPos()) != null) {
            ObserverHelper.getHelper().removeObserver(getLevel(), getBlockPos());
        }
    }

    private void resetMultiblockState() {
        if (this.hasMultiblock) {
            this.notifyMultiblockStateUpdate(true, false);
            this.hasMultiblock = false;
            this.markForUpdate();
        }
    }


    protected void notifySkyStateUpdate(boolean doesSeeSkyPrev, boolean doesSeeSkyNow) {}

    protected void notifyMultiblockStateUpdate(boolean hadMultiblockPrev, boolean hasMultiblockNow) {}

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);
        
        this.tickCount = pattern.getInt("ticksExisted");
        this.doesSeeSky = pattern.getBoolean("doesSeeSky");
        this.hasMultiblock = pattern.getBoolean("hasMultiblock");
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        pattern.putInt("ticksExisted", this.tickCount);
        pattern.putBoolean("doesSeeSky", this.doesSeeSky);
        pattern.putBoolean("hasMultiblock", this.hasMultiblock);
    }

}
