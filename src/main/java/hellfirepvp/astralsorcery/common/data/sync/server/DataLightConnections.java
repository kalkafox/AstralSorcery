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
import hellfirepvp.astralsorcery.common.data.sync.client.ClientLightConnections;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionChain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataStarlightConnections
 * Created by HellFirePvP
 * Date: 05.08.2016 / 20:14
 */
public class DataLightConnections extends AbstractData {

    private final Map<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>> serverPosBuffer = new HashMap<>();

    //Boolean flag: true=addition, false=removal
    private final Map<ResourceKey<Level>, LinkedList<Tuple<TransmissionChain.LightConnection, Boolean>>> serverChangeBuffer = new HashMap<>();
    private final Set<ResourceKey<Level>> dimensionClearBuffer = new HashSet<>();

    private DataLightConnections(ResourceLocation key) {
        super(key);
    }

    public void updateNewConnectionsThreaded(ResourceKey<Level> dim, List<TransmissionChain.LightConnection> newlyAddedConnections) {
        Map<BlockPos, Set<BlockPos>> posBufferDim = serverPosBuffer.computeIfAbsent(dim, k -> new HashMap<>());
        for (TransmissionChain.LightConnection c : newlyAddedConnections) {
            BlockPos start = c.getStart();
            Set<BlockPos> channels = posBufferDim.computeIfAbsent(start, k -> new HashSet<>());
            channels.add(c.getEnd());
        }
        notifyConnectionAdd(dim, newlyAddedConnections);
        if (newlyAddedConnections.size() > 0) {
            setChanged();
        }
    }

    public void removeOldConnectionsThreaded(ResourceKey<Level> dim, List<TransmissionChain.LightConnection> invalidConnections) {
        Map<BlockPos, Set<BlockPos>> posBufferDim = serverPosBuffer.get(dim);
        if (posBufferDim != null) {
            for (TransmissionChain.LightConnection c : invalidConnections) {
                BlockPos start = c.getStart();
                Set<BlockPos> ends = posBufferDim.get(start);
                if (ends == null) {
                    continue;
                }
                ends.remove(c.getEnd());
                if (ends.isEmpty()) {
                    posBufferDim.remove(start);
                }
            }
        }
        notifyConnectionRemoval(dim, invalidConnections);
        if (invalidConnections.size() > 0) {
            setChanged();
        }
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        if (this.serverPosBuffer.remove(dim) != null) {
            this.dimensionClearBuffer.add(dim);
            setChanged();
        }
    }

    @Override
    public void clearServer() {
        this.dimensionClearBuffer.clear();
        this.serverChangeBuffer.clear();
        this.serverPosBuffer.clear();
    }

    private void notifyConnectionAdd(ResourceKey<Level> dim, List<TransmissionChain.LightConnection> added) {
        LinkedList<Tuple<TransmissionChain.LightConnection, Boolean>> ch = serverChangeBuffer.computeIfAbsent(dim, k -> new LinkedList<>());
        for (TransmissionChain.LightConnection l : added) {
            ch.add(new Tuple<>(l, true));
        }
        this.dimensionClearBuffer.remove(dim);
    }

    private void notifyConnectionRemoval(ResourceKey<Level> dim, List<TransmissionChain.LightConnection> removal) {
        LinkedList<Tuple<TransmissionChain.LightConnection, Boolean>> ch = serverChangeBuffer.computeIfAbsent(dim, k -> new LinkedList<>());
        for (TransmissionChain.LightConnection l : removal) {
            ch.add(new Tuple<>(l, false));
        }
    }

    @Override
    public void writeAllDataToPacket(CompoundTag pattern) {
        for (ResourceKey<Level> dim : serverPosBuffer.keySet()) {
            Map<BlockPos, Set<BlockPos>> dat = serverPosBuffer.get(dim);
            ListTag dataList = new ListTag();
            for (BlockPos start : dat.keySet()) {
                Set<BlockPos> endPositions = dat.get(start);
                if (endPositions == null) {
                    continue;
                }

                for (BlockPos end : endPositions) {
                    CompoundTag cmp = new CompoundTag();
                    cmp.putLong("start", start.asLong());
                    cmp.putLong("end",   end.asLong());
                    dataList.add(cmp);
                }
            }

            pattern.put(dim.location().toString(), dataList);
        }
    }

    @Override
    public void writeDiffDataToPacket(CompoundTag pattern) {
        ListTag clearList = new ListTag();
        for (ResourceKey<Level> dim : this.dimensionClearBuffer) {
            clearList.add(StringTag.valueOf(dim.location().toString()));
        }
        pattern.put("clear", clearList);

        for (ResourceKey<Level> dim : serverChangeBuffer.keySet()) {
            if (this.dimensionClearBuffer.contains(dim)) {
                continue;
            }

            LinkedList<Tuple<TransmissionChain.LightConnection, Boolean>> changes = serverChangeBuffer.get(dim);
            if (!changes.isEmpty()) {
                ListTag list = new ListTag();
                for (Tuple<TransmissionChain.LightConnection, Boolean> tuple : changes) {
                    CompoundTag connection = new CompoundTag();
                    connection.putLong("start", tuple.getA().getStart().asLong());
                    connection.putLong("end",   tuple.getA().getEnd().asLong());
                    connection.putBoolean("connect", tuple.getB());
                    list.add(connection);
                }
                pattern.put(dim.location().toString(), list);
            }
        }

        this.dimensionClearBuffer.clear();
        this.serverChangeBuffer.clear();
    }

    public static class Provider extends AbstractDataProvider<DataLightConnections, ClientLightConnections> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataLightConnections provideServerData() {
            return new DataLightConnections(getKey());
        }

        @Override
        public ClientLightConnections provideClientData() {
            return new ClientLightConnections();
        }

        @Override
        public ClientDataReader<ClientLightConnections> createReader() {
            return new ClientLightConnections.Reader();
        }
    }


}
