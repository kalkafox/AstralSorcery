/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import hellfirepvp.astralsorcery.common.util.block.WorldBlockPos;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.tick.TickTokenMap;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.bus.api.IEventBus;

import java.util.Map;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHelperSpawnDeny
 * Created by HellFirePvP
 * Date: 28.07.2019 / 09:15
 */
public class EventHelperSpawnDeny {

    public static TickTokenMap<WorldBlockPos, TickTokenMap.SimpleTickToken<Double>> spawnDenyRegions = new TickTokenMap<>(TickEvent.Type.SERVER);

    public static void clearServer() {
        spawnDenyRegions.clear();
    }

    public static void attachTickListener(Consumer<ITickHandler> registrar) {
        registrar.accept(spawnDenyRegions);
    }

    public static void attachListeners(IEventBus eventBus) {
        eventBus.addListener(EventHelperSpawnDeny::onSpawn);
    }

    private static void onSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel().isClientSide() ||
                event.getSpawnType() == MobSpawnType.SPAWNER) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.getTags().contains(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY)) {
            return;
        }

        if (GeneralConfig.CONFIG.mobSpawningDenyAllTypes.get() || entity.getType().getCategory() == MobCategory.MONSTER) {
            Vector3 entityPos = Vector3.atEntityCorner(entity);
            for (Map.Entry<WorldBlockPos, TickTokenMap.SimpleTickToken<Double>> entry : spawnDenyRegions.entrySet()) {
                if (!entry.getKey().getWorldKey().equals(entity.getCommandSenderWorld().dimension())) {
                    continue;
                }

                if (entityPos.distance(entry.getKey()) <= entry.getValue().getValue()) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

}
