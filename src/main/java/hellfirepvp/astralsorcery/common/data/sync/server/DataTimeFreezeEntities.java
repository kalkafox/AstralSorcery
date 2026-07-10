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
import hellfirepvp.astralsorcery.common.data.sync.client.ClientTimeFreezeEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataTimeFreezeEntities
 * Created by HellFirePvP
 * Date: 03.11.2020 / 20:46
 */
public class DataTimeFreezeEntities extends AbstractData {

    private final Map<ResourceKey<Level>, Set<Integer>> serverActiveEntityFreeze = new HashMap<>();

    private final Set<ResourceKey<Level>> serverSyncTypes = new HashSet<>();

    private DataTimeFreezeEntities(ResourceLocation key) {
        super(key);
    }

    public void freezeEntity(Entity e) {
        ResourceKey<Level> dim = e.getCommandSenderWorld().dimension();
        if (this.serverActiveEntityFreeze.computeIfAbsent(dim, dimType -> new HashSet<>()).add(e.getEntityId())) {
            this.serverSyncTypes.add(dim);
            this.setChanged();
        }
    }

    public void unfreezeEntity(Entity e) {
        ResourceKey<Level> dim = e.getCommandSenderWorld().dimension();
        if (this.serverActiveEntityFreeze.getOrDefault(dim, Collections.emptySet()).remove(e.getEntityId())) {
            this.serverSyncTypes.add(dim);
            this.setChanged();
        }
    }

    public boolean isFrozen(Entity e) {
        ResourceKey<Level> dim = e.getCommandSenderWorld().dimension();
        return this.serverActiveEntityFreeze.getOrDefault(dim, Collections.emptySet()).contains(e.getEntityId());
    }

    @Override
    public void clear(ResourceKey<Level> dimType) {
        this.serverActiveEntityFreeze.remove(dimType);
    }

    @Override
    public void clearServer() {
        this.serverActiveEntityFreeze.clear();
        this.serverSyncTypes.clear();
    }

    @Override
    public void writeAllDataToPacket(CompoundTag pattern) {
        this.writeEntityInformation(pattern, this.serverActiveEntityFreeze);
    }

    @Override
    public void writeDiffDataToPacket(CompoundTag pattern) {
        Map<ResourceKey<Level>, Set<Integer>> entities = new HashMap<>();
        this.serverSyncTypes.forEach(type -> {
            entities.put(type, this.serverActiveEntityFreeze.getOrDefault(type, new HashSet<>()));
        });
        this.writeEntityInformation(pattern, entities);
        this.serverSyncTypes.clear();
    }

    private void writeEntityInformation(CompoundTag out, Map<ResourceKey<Level>, Set<Integer>> entities) {
        CompoundTag dimTag = new CompoundTag();
        entities.forEach((dim, entityIds) -> {
            ListTag nbtEntities = new ListTag();
            entityIds.forEach(id -> nbtEntities.add(IntTag.valueOf(id)));
            dimTag.put(dim.getLocation().toString(), nbtEntities);
        });
        out.put("dimTypes", dimTag);
    }

    public static class Provider extends AbstractDataProvider<DataTimeFreezeEntities, ClientTimeFreezeEntities> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataTimeFreezeEntities provideServerData() {
            return new DataTimeFreezeEntities(getKey());
        }

        @Override
        public ClientTimeFreezeEntities provideClientData() {
            return new ClientTimeFreezeEntities();
        }

        @Override
        public ClientDataReader<ClientTimeFreezeEntities> createReader() {
            return new ClientTimeFreezeEntities.Reader();
        }
    }
}
