/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary.gateway;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktUpdateGateways;
import hellfirepvp.astralsorcery.common.util.SidedReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.*;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CelestialGatewayHandler
 * Created by HellFirePvP
 * Date: 23.08.2019 / 22:15
 */
public class CelestialGatewayHandler {

    public static final CelestialGatewayHandler INSTANCE = new CelestialGatewayHandler();
    private CelestialGatewayFilter filter = null;
    private boolean startUp = false;

    private final SidedReference<Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>>> cache = new SidedReference<>();

    private CelestialGatewayHandler() {}

    private CelestialGatewayFilter getFilter() {
        if (filter == null) {
            filter = new CelestialGatewayFilter();
        }
        return filter;
    }

    public void addPosition(Level level, GatewayCache.GatewayNode node) {
        if (level.isClientSide()) {
            return;
        }

        ResourceKey<Level> dimKey = level.dimension();
        if (!cache.getData(LogicalSide.SERVER).map(map -> map.get(dimKey)).isPresent()) {
            forceLoad(level.dimension());
        }

        Optional<Collection<GatewayCache.GatewayNode>> worldData = cache.getData(LogicalSide.SERVER).map(map -> map.get(dimKey));
        if (!worldData.isPresent()) {
            AstralSorcery.log.info("Couldn't add gateway at " + node.getBlockPos() + " - loading the world failed.");
            return;
        }
        Collection<GatewayCache.GatewayNode> nodes = worldData.get();

        getFilter().addDim(dimKey);
        if (!nodes.contains(node)) {
            nodes.add(node);
            syncToAll();
        }
    }

    public void removePosition(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        ResourceKey<Level> dimKey = level.dimension();
        Optional<Collection<GatewayCache.GatewayNode>> worldData = cache.getData(LogicalSide.SERVER).map(map -> map.get(dimKey));
        if (!worldData.isPresent()) {
            return;
        }
        Collection<GatewayCache.GatewayNode> nodes = worldData.get();
        if (nodes.removeIf(node -> node.getBlockPos().equals(pos))) {
            if (nodes.isEmpty()) {
                getFilter().removeDim(dimKey);
            }
            syncToAll();
        }
    }

    private void forceLoad(ResourceKey<Level> level) {
        //TODO re-check once worlds aren't ALL statically loaded.
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        srv.getLevel(level);
    }

    public void onServerStart() {
        startUp = true;
        CelestialGatewayFilter filter = getFilter();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        //TODO re-check once worlds aren't ALL statically loaded.
        //TODO gateway network startup load
        //DimensionManager.getRegistry().stream()
        //        .filter(DimensionManager::keepLoaded)
        //        .forEach(type -> {
        //            if (!filter.hasGateways(type.getRegistryName())) {
        //                return;
        //            }
        //            loadIntoCache(server.getWorld(type));
        //        });
        startUp = false;
    }

    public void onServerStop() {
        this.cache.setData(LogicalSide.SERVER, null);
    }

    public void onWorldInit(LevelEvent.Load event) {
        if (this.startUp) {
            return; //We're already loading up there.
        }

        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !(level instanceof Level)) {
            return;
        }

        this.loadIntoCache((Level) level);
        this.syncToAll();
    }

    public void syncToAll() {
        PktUpdateGateways pkt = new PktUpdateGateways(this.getGatewayCache(LogicalSide.SERVER));
        PacketChannel.CHANNEL.sendToAll(pkt);
    }

    public Collection<GatewayCache.GatewayNode> getGatewaysForWorld(Level level, LogicalSide direction) {
        return this.cache.getData(direction)
                .map(data -> data.getOrDefault(level.dimension(), Collections.emptyList()))
                .orElse(Collections.emptyList());
    }

    public Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> getGatewayCache(LogicalSide direction) {
        return this.cache.getData(direction).orElse(Collections.emptyMap());
    }

    @Nullable
    public GatewayCache.GatewayNode getGatewayNode(Level level, LogicalSide direction, BlockPos pos) {
        return this.cache.getData(direction)
                .map(data -> data.get(level.dimension()))
                .orElse(Collections.emptyList())
                .stream()
                .filter(node -> node.getBlockPos().equals(pos))
                .findFirst()
                .orElse(null);
    }

    @OnlyIn(Dist.CLIENT)
    public void updateClientCache(@Nullable Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions) {
        this.cache.setData(LogicalSide.CLIENT, positions);
    }

    private void loadIntoCache(Level level) {
        GatewayCache cache = DataAS.DOMAIN_AS.getData(level, DataAS.KEY_GATEWAY_CACHE);
        Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> gatewayCache = this.cache.getData(LogicalSide.SERVER).orElse(new HashMap<>());
        gatewayCache.put(level.dimension(), new HashSet<>(cache.getGatewayPositions()));
        this.cache.setData(LogicalSide.SERVER, gatewayCache);
    }

}
