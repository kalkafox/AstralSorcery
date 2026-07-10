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
import hellfirepvp.astralsorcery.common.data.sync.client.ClientTimeFreezeEffects;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.time.TimeStopEffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataTimeFreezeEffects
 * Created by HellFirePvP
 * Date: 31.08.2019 / 14:01
 */
public class DataTimeFreezeEffects extends AbstractData {

    private final Map<ResourceKey<Level>, List<TimeStopEffectHelper>> serverActiveFreezeZones = new HashMap<>();

    private final List<ServerSyncAction> scheduledServerSyncChanges = new LinkedList<>();

    private DataTimeFreezeEffects(ResourceLocation key) {
        super(key);
    }

    public void addNewEffect(ResourceKey<Level> dim, TimeStopEffectHelper effectHelper) {
        List<TimeStopEffectHelper> zones = serverActiveFreezeZones.computeIfAbsent(dim, (id) -> new LinkedList<>());
        zones.add(effectHelper);
        scheduledServerSyncChanges.add(new ServerSyncAction(ServerSyncAction.ActionType.ADD, dim, effectHelper));
        setChanged();
    }

    public void removeEffect(ResourceKey<Level> dim, TimeStopEffectHelper effectHelper) {
        if (serverActiveFreezeZones.containsKey(dim)) {
            serverActiveFreezeZones.get(dim).remove(effectHelper);
        }
        scheduledServerSyncChanges.add(new ServerSyncAction(ServerSyncAction.ActionType.REMOVE, dim, effectHelper));
        setChanged();
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        this.serverActiveFreezeZones.remove(dim);
    }

    @Override
    public void clearServer() {
        this.serverActiveFreezeZones.clear();
        this.scheduledServerSyncChanges.clear();
    }

    @Override
    public void writeAllDataToPacket(CompoundTag pattern) {
        CompoundTag dimTag = new CompoundTag();
        for (ResourceKey<Level> dim : this.serverActiveFreezeZones.keySet()) {
            ListTag list = new ListTag();
            for (TimeStopEffectHelper effect : this.serverActiveFreezeZones.get(dim)) {
                list.add(effect.serializeNBT());
            }
            dimTag.put(dim.getLocation().toString(), list);
        }
        pattern.put("dimTypes", dimTag);
    }

    @Override
    public void writeDiffDataToPacket(CompoundTag pattern) {
        ListTag changes = new ListTag();
        for (ServerSyncAction action : this.scheduledServerSyncChanges) {
            changes.add(action.serializeNBT());
        }
        pattern.put("changes", changes);

        this.scheduledServerSyncChanges.clear();
    }

    public static class ServerSyncAction {

        private final ActionType type;

        private final ResourceKey<Level> dim;
        private final TimeStopEffectHelper involvedEffect;

        private ServerSyncAction(ActionType type, ResourceKey<Level> dim, TimeStopEffectHelper involvedEffect) {
            this.type = type;
            this.dim = dim;
            this.involvedEffect = involvedEffect;
        }

        private CompoundTag serializeNBT() {
            CompoundTag out = new CompoundTag();
            out.putInt("type", type.ordinal());
            out.putString("dimType", this.dim.getLocation().toString());
            switch (type) {
                case ADD:
                case REMOVE:
                    out.put("effectTag", involvedEffect.serializeNBT());
                    break;
            }
            return out;
        }

        public static ServerSyncAction deserializeNBT(CompoundTag cmp) {
            ActionType type = MiscUtils.getEnumEntry(ActionType.class, cmp.getInt("type"));
            String dimKey = cmp.getString("dimType");
            ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, ResourceLocation.parse(dimKey));
            TimeStopEffectHelper helper = null;
            switch (type) {
                case ADD:
                case REMOVE:
                    helper = TimeStopEffectHelper.deserializeNBT(cmp.getCompound("effectTag"));
                    break;
            }
            return new ServerSyncAction(type, dim, helper);
        }

        @Nullable
        public TimeStopEffectHelper getInvolvedEffect() {
            return involvedEffect;
        }

        public ResourceKey<Level> getDimKey() {
            return dim;
        }

        public ActionType getType() {
            return type;
        }

        public static enum ActionType {

            ADD,
            REMOVE,
            CLEAR

        }

    }

    public static class Provider extends AbstractDataProvider<DataTimeFreezeEffects, ClientTimeFreezeEffects> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataTimeFreezeEffects provideServerData() {
            return new DataTimeFreezeEffects(getKey());
        }

        @Override
        public ClientTimeFreezeEffects provideClientData() {
            return new ClientTimeFreezeEffects();
        }

        @Override
        public ClientDataReader<ClientTimeFreezeEffects> createReader() {
            return new ClientTimeFreezeEffects.Reader();
        }
    }
}
