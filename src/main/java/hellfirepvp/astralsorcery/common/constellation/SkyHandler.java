/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SkyHandler
 * Created by HellFirePvP
 * Date: 01.07.2019 / 06:59
 */
public class SkyHandler implements ITickHandler {
    
    private static final SkyHandler instance = new SkyHandler();

    private final Map<ResourceKey<Level>, WorldContext> worldHandlersServer = Maps.newHashMap();
    private final Map<ResourceKey<Level>, WorldContext> worldHandlersClient = Maps.newHashMap();

    private final Map<ResourceKey<Level>, Boolean> skyRevertMap = Maps.newHashMap();

    private SkyHandler() {}

    public static SkyHandler getInstance() {
        return instance;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        if (type == TickEvent.Type.WORLD) {
            Level w = (Level) context[0];
            if (!w.isClientSide() && w instanceof ServerLevel) {
                ResourceKey<Level> dimKey = w.dimension();
                skyRevertMap.put(dimKey, false);

                WorldContext ctx = worldHandlersServer.get(dimKey);
                if (ctx == null) {
                    ctx = createContext(MiscUtils.getRandomWorldSeed((ServerLevel) w));
                    worldHandlersServer.put(dimKey, ctx);
                }
                ctx.tick(w);
            }
        } else {
            handleClientTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientTick() {
        Level w = Minecraft.getInstance().level;
        if (w != null) {
            ResourceKey<Level> dimKey = w.dimension();
            WorldContext ctx = worldHandlersClient.get(dimKey);
            if (ctx == null) {
                Optional<Long> seedOpt = WorldSeedCache.getSeedIfPresent(dimKey);
                if (!seedOpt.isPresent()) {
                    return;
                }
                ctx = createContext(seedOpt.get());
                worldHandlersClient.put(dimKey, ctx);
            }
            ctx.tick(w);
        }
    }

    private WorldContext createContext(long seed) {
        return new WorldContext(seed);
    }

    @Nullable
    public static WorldContext getContext(Level level) {
        return getContext(level, level.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
    }

    @Nullable
    public static WorldContext getContext(Level level, LogicalSide dist) {
        if (level == null) {
            return null;
        }
        ResourceKey<Level> dimKey = level.dimension();
        if (dist.isClient()) {
            return getInstance().worldHandlersClient.getOrDefault(dimKey, null);
        } else {
            return getInstance().worldHandlersServer.getOrDefault(dimKey, null);
        }
    }

    public void revertWorldTimeTick(ServerLevel level) {
        ResourceKey<Level> dimKey = level.dimension();
        Boolean state = skyRevertMap.get(dimKey);
        if (!level.isClientSide && state != null && !state) {
            skyRevertMap.put(dimKey, true);
            level.setDayTime(level.getDayTime() - 1);
        }
    }

    public void clientClearCache() {
        worldHandlersClient.clear();
    }

    public void informWorldUnload(Level level) {
        worldHandlersServer.remove(level.dimension());
        worldHandlersClient.remove(level.dimension());
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.WORLD, TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "ConstellationSkyhandler";
    }
    
}
