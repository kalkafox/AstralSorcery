/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.storage.StorageNetwork;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.base.GlobalWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StorageNetworkBuffer
 * Created by HellFirePvP
 * Date: 30.05.2019 / 14:59
 */
public class StorageNetworkBuffer extends GlobalWorldData {

    private final Map<BlockPos, StorageNetwork> rawNetworks = Maps.newHashMap();
    private final Map<ChunkPos, List<StorageNetwork>> availableNetworks = Maps.newHashMap();

    public StorageNetworkBuffer(WorldCacheDomain.SaveKey<?> key) {
        super(key);
    }

    @Nullable
    public StorageNetwork getNetwork(BlockPos masterPos) {
        return this.rawNetworks.get(masterPos);
    }

    private void rebuildAccessContext() {
        this.availableNetworks.clear();

        for (StorageNetwork network : this.rawNetworks.values()) {
            for (StorageNetwork.CoreArea insideCube : network.getCores()) {
                AABB box = insideCube.getRealBox();
                ChunkPos from = Vector3.getMin(box).toChunkPos();
                ChunkPos to   = Vector3.getMax(box).toChunkPos();

                for (int chX = from.x; chX <= to.x; chX++) {
                    for (int chZ = from.z; chZ <= to.z; chZ++) {
                        this.availableNetworks
                                .computeIfAbsent(new ChunkPos(chX, chZ), pos -> Lists.newArrayList())
                                .add(network);
                    }
                }
            }
        }
    }

    @Override
    public void save(CompoundTag pattern) {
        ListTag networks = new ListTag();
        for (StorageNetwork network : this.rawNetworks.values()) {
            CompoundTag tag = new CompoundTag();
            network.save(tag);
            networks.add(tag);
        }
        pattern.put("networks", networks);
    }

    @Override
    public void readFromNBT(CompoundTag pattern) {
        this.rawNetworks.clear();

        ListTag networks = pattern.getList("networks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < networks.size(); i++) {
            CompoundTag tag = networks.getCompound(i);
            StorageNetwork net = new StorageNetwork();
            net.readFromNBT(tag);
            if (net.getCores().isEmpty()) {
                continue; //No network..
            }
            StorageNetwork.CoreArea master = net.getMaster();
            if (master == null) {
                master = MiscUtils.getRandomEntry(net.getCores(), random);
            }
            this.rawNetworks.put(master.getBlockPos(), net);
        }

        this.rebuildAccessContext();
    }

    @Override
    public void updateTick(Level level) {}

}
