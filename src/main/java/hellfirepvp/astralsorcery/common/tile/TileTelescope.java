/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntitySynchronized;
import hellfirepvp.astralsorcery.common.util.tile.NamedInventoryTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileTelescope
 * Created by HellFirePvP
 * Date: 15.01.2020 / 15:37
 */
public class TileTelescope extends TileEntitySynchronized implements NamedInventoryTile {

    private TelescopeRotation rotation = TelescopeRotation.N;

    public TileTelescope(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.TELESCOPE, pos, state);
    }

    public TelescopeRotation getRotation() {
        return rotation;
    }

    public void setRotation(TelescopeRotation rotation) {
        this.rotation = rotation;
        markForUpdate();
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.rotation = TelescopeRotation.values()[pattern.getInt("rotation")];
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        pattern.putInt("rotation", rotation.ordinal());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.astralsorcery.telescope");
    }

    public static enum TelescopeRotation {

        N,
        N_E,
        E,
        S_E,
        S,
        S_W,
        W,
        N_W;

        public TelescopeRotation nextClockWise() {
            return values()[(ordinal() + 1) % values().length];
        }

        public TelescopeRotation nextCounterClockWise() {
            return values()[(ordinal() + 7) % values().length];
        }

    }
}
