/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.server;

import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractDataProvider;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.data.sync.client.ClientLightBlockEndpoints;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataLightBlockEndpoints
 * Created by HellFirePvP
 * Date: 10.08.2016 / 18:30
 */
public class DataLightBlockEndpoints extends AbstractData {

    private final Map<ResourceKey<Level>, Set<BlockPos>> serverPositions = new HashMap<>();

    private final Map<ResourceKey<Level>, Map<BlockPos, Boolean>> serverChangeBuffer = new HashMap<>();
    private final Set<ResourceKey<Level>> dimensionClearBuffer = new HashSet<>();

    private DataLightBlockEndpoints(ResourceLocation key) {
        super(key);
    }

    public void updateNewEndpoint(ResourceKey<Level> dim, BlockPos pos) {
        Map<BlockPos, Boolean> posMap = serverChangeBuffer.computeIfAbsent(dim, k -> new HashMap<>());
        posMap.put(pos, true);

        Set<BlockPos> posBuffer = serverPositions.computeIfAbsent(dim, k -> new HashSet<>());
        posBuffer.add(pos);
        setChanged();
    }

    public void updateNewEndpoints(ResourceKey<Level> dim, Collection<BlockPos> newPositions) {
        Map<BlockPos, Boolean> posMap = serverChangeBuffer.computeIfAbsent(dim, k -> new HashMap<>());
        for (BlockPos pos : newPositions) {
            posMap.put(pos, true);
        }

        Set<BlockPos> posBuffer = serverPositions.computeIfAbsent(dim, k -> new HashSet<>());
        posBuffer.addAll(newPositions);
        setChanged();
    }

    public void removeEndpoints(ResourceKey<Level> dim, Collection<BlockPos> positions) {
        Map<BlockPos, Boolean> posMap = serverChangeBuffer.computeIfAbsent(dim, k -> new HashMap<>());
        for (BlockPos pos : positions) {
            posMap.put(pos, false);
        }

        Set<BlockPos> posBuffer = serverPositions.computeIfAbsent(dim, k -> new HashSet<>());
        if (posBuffer.removeAll(positions)) {
            setChanged();
        }
    }

    public boolean doesPositionReceiveStarlightServer(Level level, BlockPos pos) {
        return this.serverPositions.getOrDefault(level.dimension(), Collections.emptySet()).contains(pos);
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        if (this.serverPositions.remove(dim) != null) {
            this.serverChangeBuffer.remove(dim);
            this.dimensionClearBuffer.add(dim);
            setChanged();
        }
    }

    @Override
    public void clearServer() {
        this.dimensionClearBuffer.clear();
        this.serverChangeBuffer.clear();
        this.serverPositions.clear();
    }

    @Override
    public void writeAllDataToPacket(CompoundTag pattern) {
        for (ResourceKey<Level> dim : serverPositions.keySet()) {
            Set<BlockPos> dat = serverPositions.get(dim);

            ListTag dataList = new ListTag();
            for (BlockPos pos : dat) {
                CompoundTag cmp = new CompoundTag();
                cmp.putLong("pos", pos.asLong());
                dataList.add(cmp);
            }

            pattern.put(dim.getLocation().toString(), dataList);
        }
    }

    @Override
    public void writeDiffDataToPacket(CompoundTag pattern) {
        ListTag clearList = new ListTag();
        for (ResourceKey<Level> dim : this.dimensionClearBuffer) {
            clearList.add(StringTag.valueOf(dim.getLocation().toString()));
        }
        pattern.put("clear", clearList);

        for (ResourceKey<Level> dim : this.serverChangeBuffer.keySet()) {
            if (this.dimensionClearBuffer.contains(dim)) {
                continue;
            }

            Map<BlockPos, Boolean> data = this.serverChangeBuffer.get(dim);

            ListTag dataList = new ListTag();
            for (BlockPos pos : data.keySet()) {
                CompoundTag cmp = new CompoundTag();
                cmp.putLong("pos", pos.asLong());
                cmp.putBoolean("add", data.get(pos));
                dataList.add(cmp);
            }

            pattern.put(dim.getLocation().toString(), dataList);
        }

        this.dimensionClearBuffer.clear();
        this.serverChangeBuffer.clear();
    }

    public static class Provider extends AbstractDataProvider<DataLightBlockEndpoints, ClientLightBlockEndpoints> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataLightBlockEndpoints provideServerData() {
            return new DataLightBlockEndpoints(getKey());
        }

        @Override
        public ClientLightBlockEndpoints provideClientData() {
            return new ClientLightBlockEndpoints();
        }

        @Override
        public ClientDataReader<ClientLightBlockEndpoints> createReader() {
            return new ClientLightBlockEndpoints.Reader();
        }
    }
}
