/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.storage;

import hellfirepvp.astralsorcery.common.data.world.StorageNetworkBuffer;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StorageNetworkHandler
 * Created by HellFirePvP
 * Date: 30.05.2019 / 15:02
 */
public class StorageNetworkHandler {

    //private static final AxisAlignedBB box = new AxisAlignedBB(-3, 0, -3, 3, 0, 3);
    private static final Map<ResourceKey<Level>, NetworkHelper> mappingHelpers = new HashMap<>();

    public static NetworkHelper getHandler(Level level) {
        return mappingHelpers.computeIfAbsent(level.dimension(), id -> new NetworkHelper(level));
    }

    public static void clearHandler(Level level) {
        clearHandler(level.dimension());
    }

    public static void clearHandler(ResourceKey<Level> dimKey) {
        mappingHelpers.remove(dimKey);
    }

    public static class NetworkHelper {

        private final StorageNetworkBuffer buffer;

        private NetworkHelper(Level level) {
            this.buffer = DataAS.DOMAIN_AS.getData(level, DataAS.KEY_STORAGE_NETWORK);
        }

        @Nullable
        public StorageNetwork getNetwork(BlockPos networkMaster) {
            return buffer.getNetwork(networkMaster);
        }

        //public void addCore(TileStorageCore core) {
        //    //fusion logic
        //}

        //public void removeCore(TileStorageCore core) {
        //    //division logic
        //}

    }

    public static class MappingChange {

    }

}
