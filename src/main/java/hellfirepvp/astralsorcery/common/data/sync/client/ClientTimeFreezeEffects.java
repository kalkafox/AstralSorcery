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
import hellfirepvp.astralsorcery.common.data.sync.server.DataTimeFreezeEffects;
import hellfirepvp.astralsorcery.common.util.time.TimeStopEffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientTimeFreezeEffects
 * Created by HellFirePvP
 * Date: 31.08.2019 / 14:02
 */
public class ClientTimeFreezeEffects extends ClientData<ClientTimeFreezeEffects> {

    private final Map<ResourceKey<Level>, List<TimeStopEffectHelper>> clientActiveFreezeZones = new HashMap<>();

    @Nonnull
    public List<TimeStopEffectHelper> getTimeStopEffects(Level level) {
        return getTimeStopEffects(level.dimension());
    }

    @Nonnull
    public List<TimeStopEffectHelper> getTimeStopEffects(ResourceKey<Level> dim) {
        return clientActiveFreezeZones.getOrDefault(dim, Collections.emptyList());
    }

    private void applyChange(DataTimeFreezeEffects.ServerSyncAction action) {
        ResourceKey<Level> worldKey = action.getDimKey();
        switch (action.getType()) {
            case ADD:
                List<TimeStopEffectHelper> zones = clientActiveFreezeZones.computeIfAbsent(worldKey, (id) -> new LinkedList<>());
                zones.add(action.getInvolvedEffect());
                break;
            case REMOVE:
                if (clientActiveFreezeZones.containsKey(worldKey)) {
                    clientActiveFreezeZones.get(worldKey).remove(action.getInvolvedEffect());
                }
                break;
            case CLEAR:
                clientActiveFreezeZones.remove(worldKey);
                break;
            default:
                break;
        }
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        this.clientActiveFreezeZones.remove(dim);
    }

    @Override
    public void clearClient() {
        this.clientActiveFreezeZones.clear();
    }

    public static class Reader extends ClientDataReader<ClientTimeFreezeEffects> {

        @Override
        public void readFromIncomingFullSync(ClientTimeFreezeEffects data, CompoundTag pattern) {
            data.clientActiveFreezeZones.clear();

            CompoundTag dimTag = pattern.getCompound("dimTypes");
            for (String dimKey : dimTag.getAllKeys()) {
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimKey));

                List<TimeStopEffectHelper> effects = new LinkedList<>();
                ListTag listEffects = dimTag.getList(dimKey, Constants.NBT.TAG_COMPOUND);
                for (Tag iNBT : listEffects) {
                    effects.add(TimeStopEffectHelper.deserializeNBT((CompoundTag) iNBT));
                }
                data.clientActiveFreezeZones.put(dim, effects);
            }
        }

        @Override
        public void readFromIncomingDiff(ClientTimeFreezeEffects data, CompoundTag pattern) {
            ListTag changes = pattern.getList("changes", Constants.NBT.TAG_COMPOUND);
            for (Tag iNBT : changes) {
                DataTimeFreezeEffects.ServerSyncAction action = DataTimeFreezeEffects.ServerSyncAction.deserializeNBT((CompoundTag) iNBT);
                data.applyChange(action);
            }
        }
    }
}
