/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.client;

import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientLightBlockEndpoints
 * Created by HellFirePvP
 * Date: 29.08.2019 / 20:10
 */
public class ClientLightBlockEndpoints extends ClientData<ClientLightBlockEndpoints> {

    private final Map<ResourceKey<Level>, Set<BlockPos>> clientPositions = new HashMap<>();

    public boolean doesPositionReceiveStarlightClient(Level world, BlockPos pos) {
        return this.clientPositions.getOrDefault(world.getDimensionKey(), Collections.emptySet()).contains(pos);
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        this.clientPositions.remove(dim);
    }

    @Override
    public void clearClient() {
        this.clientPositions.clear();
    }

    public static class Reader extends ClientDataReader<ClientLightBlockEndpoints> {

        @Override
        public void readFromIncomingFullSync(ClientLightBlockEndpoints data, CompoundTag compound) {
            data.clientPositions.clear();

            for (String dimKey : compound.keySet()) {
                ResourceKey<Level> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimKey));

                Set<BlockPos> positions = new HashSet<>();
                ListTag list = compound.getList(dimKey, Constants.NBT.TAG_COMPOUND);
                for (Tag iTag : list) {
                    CompoundTag tag = (CompoundTag) iTag;

                    BlockPos pos = BlockPos.fromLong(tag.getLong("pos"));
                    positions.add(pos);
                }
                data.clientPositions.put(dim, positions);
            }
        }

        @Override
        public void readFromIncomingDiff(ClientLightBlockEndpoints data, CompoundTag compound) {
            Set<String> clearedDimensions = new HashSet<>();
            for (Tag dimKeyNBT : compound.getList("clear", Constants.NBT.TAG_STRING)) {
                String dimKey = dimKeyNBT.getString();
                ResourceKey<Level> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimKey));
                data.clientPositions.remove(dim);

                clearedDimensions.add(dimKey);
            }

            for (String dimKey : compound.keySet()) {
                if (clearedDimensions.contains(dimKey)) {
                    continue;
                }
                ResourceKey<Level> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimKey));

                Set<BlockPos> positions = data.clientPositions.computeIfAbsent(dim, k -> new HashSet<>());

                ListTag list = compound.getList(dimKey, Constants.NBT.TAG_COMPOUND);
                for (Tag iTag : list) {
                    CompoundTag tag = (CompoundTag) iTag;

                    BlockPos pos = BlockPos.fromLong(tag.getLong("pos"));
                    boolean addNew = tag.getBoolean("add");

                    if (addNew) {
                        positions.add(pos);
                    } else {
                        positions.remove(pos);
                    }
                }
            }
        }
    }
}
