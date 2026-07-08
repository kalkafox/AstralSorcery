/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.base;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AbstractData
 * Created by HellFirePvP
 * Date: 07.05.2016 / 01:11
 */
public abstract class AbstractData {

    private final ResourceLocation key;

    protected AbstractData(ResourceLocation key) {
        this.key = key;
    }

    public final void markDirty() {
        SyncDataHolder.markForUpdate(this.key);
    }

    public abstract void clear(ResourceKey<Level> dim);

    public abstract void clearServer();

    public abstract void writeAllDataToPacket(CompoundTag compound);

    public abstract void writeDiffDataToPacket(CompoundTag compound);

}
