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
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientTimeFreezeEntities
 * Created by HellFirePvP
 * Date: 03.11.2020 / 20:47
 */
public class ClientTimeFreezeEntities extends ClientData<ClientTimeFreezeEntities> {

    private final Map<ResourceKey<Level>, Set<Integer>> clientActiveEntityFreeze = new HashMap<>();

    public boolean isFrozen(Entity e) {
        return this.clientActiveEntityFreeze.getOrDefault(e.getCommandSenderWorld().dimension(), Collections.emptySet()).contains(e.getId());
    }

    @Override
    public void clear(ResourceKey<Level> dimType) {
        this.clientActiveEntityFreeze.remove(dimType);
    }

    @Override
    public void clearClient() {
        this.clientActiveEntityFreeze.clear();
    }

    public static class Reader extends ClientDataReader<ClientTimeFreezeEntities> {

        @Override
        public void readFromIncomingFullSync(ClientTimeFreezeEntities data, CompoundTag pattern) {
            this.readEntityInformation(data, pattern);
        }

        @Override
        public void readFromIncomingDiff(ClientTimeFreezeEntities data, CompoundTag pattern) {
            this.readEntityInformation(data, pattern);
        }

        private void readEntityInformation(ClientTimeFreezeEntities data, CompoundTag pattern) {
            CompoundTag dimTypes = pattern.getCompound("dimTypes");
            for (String key : dimTypes.keySet()) {
                ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, ResourceLocation.parse(key));

                ListTag list = dimTypes.getList(key, Constants.NBT.TAG_INT);
                Set<Integer> entities = new HashSet<>();
                for (int i = 0; i < list.size(); i++) {
                    entities.add(list.getInt(i));
                }

                data.clientActiveEntityFreeze.put(dim, entities);
            }
        }
    }
}
