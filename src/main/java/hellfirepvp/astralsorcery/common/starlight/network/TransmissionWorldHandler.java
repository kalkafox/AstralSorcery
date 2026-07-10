/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight.network;

import com.google.common.collect.ImmutableList;
import hellfirepvp.astralsorcery.common.block.base.BlockStarlightRecipient;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.server.DataLightBlockEndpoints;
import hellfirepvp.astralsorcery.common.data.sync.server.DataLightConnections;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TransmissionWorldHandler
 * Created by HellFirePvP
 * Date: 05.08.2016 / 11:02
 */
public class TransmissionWorldHandler {

    private static final Random random = new Random();

    //If a source looses all chunks/all chunks in its network get unloaded it doesn't need to broadcast starlight anymore
    //This map exists to associate a certain chunkPosition to the involved networks in it.
    private final Map<ChunkPos, List<IIndependentStarlightSource>> involvedSourceMap = new HashMap<>();

    //The counterpart to check faster
    //Removing a source here will also stop production!
    private final Map<IIndependentStarlightSource, List<ChunkPos>> activeChunkMap = new HashMap<>();

    private final Map<IIndependentStarlightSource, TransmissionChain> cachedSourceChain = new HashMap<>(); //The distribution source chain.

    private final Map<BlockPos, List<IIndependentStarlightSource>> posToSourceMap = new HashMap<>();

    //Contains a list of source positions whose sources currently calculate their network.
    private final Set<BlockPos> sourcePosBuilding = new HashSet<>();

    private final ResourceKey<Level> dim;

    public TransmissionWorldHandler(ResourceKey<Level> dimKey) {
        this.dim = dimKey;
    }

    public void tick(ServerLevel level) {
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(level);

        for (Tuple<BlockPos, IIndependentStarlightSource> sourceTuple : handler.getAllSources()) {
            BlockPos at = sourceTuple.getA();
            IIndependentStarlightSource source = sourceTuple.getB();

            if (!cachedSourceChain.containsKey(source)) {
                if (!sourcePosBuilding.contains(at)) {
                    sourcePosBuilding.add(at);
                    buildNetworkChain(level, source, handler, at);
                }
            }

            List<ChunkPos> activeChunks = activeChunkMap.get(source);
            if (activeChunks == null || activeChunks.isEmpty()) {
                continue; //Not producing anything as no part of this chain is loaded.
            }

            TransmissionChain chain = cachedSourceChain.get(source);
            float starlight = source.produceStarlightTick(level, at);
            IWeakConstellation type = source.getStarlightType();
            if (type == null) {
                continue;
            }

            Map<BlockPos, Float> lossMultipliers = chain.getLossMultipliers();
            for (ITransmissionReceiver rec : chain.getEndpointsNodes()) {
                BlockPos pos = rec.getLocationPos();
                Float multiplier = lossMultipliers.get(pos);
                if (multiplier != null) {
                    rec.onStarlightReceive(level, type, starlight * multiplier);
                }
            }

            if (starlight > 0.01F) {
                chain.getTransmissionUpdates().forEach((node, multiplier) -> {
                    if (multiplier >= 0.01F) {
                        node.onTransmissionTick(level, starlight * multiplier, type);
                    }
                });
            }

            for (BlockPos endPointPos : chain.getUncheckedEndpointsBlock()) {
                MiscUtils.executeWithChunk(level, endPointPos, () -> {
                    BlockState endState = level.getBlockState(endPointPos);
                    Block b = endState.getBlock();
                    if (b instanceof BlockStarlightRecipient) {
                        Float multiplier = lossMultipliers.get(endPointPos);
                        if (multiplier != null) {
                            ((BlockStarlightRecipient) b).receiveStarlight(level, random, endPointPos, type, starlight * multiplier);
                        }
                    } else {
                        StarlightNetworkRegistry.IStarlightBlockHandler handle = StarlightNetworkRegistry.getStarlightHandler(level, endPointPos, endState, type);
                        if (handle != null) {
                            Float multiplier = lossMultipliers.get(endPointPos);
                            if (multiplier != null) {
                                handle.receiveStarlight(level, random, endPointPos, endState, type, starlight * multiplier);
                            }
                        } else {
                            chain.updatePosAsResolved(level, endPointPos);
                        }
                    }
                });
            }
        }
    }

    private void buildNetworkChain(Level level, IIndependentStarlightSource source, WorldNetworkHandler handler, BlockPos pos) {
        TransmissionChain.buildNetworkChain(level, this, source, handler, pos);
    }

    void updateNetworkChainData(Level level, TransmissionChain chain, IIndependentStarlightSource source, WorldNetworkHandler handle, BlockPos pos) {
        sourcePosBuilding.remove(pos);

        cachedSourceChain.put(source, chain);
        List<ChunkPos> activeChunks = new LinkedList<>();
        for (ChunkPos pos : chain.getInvolvedChunks()) {
            List<IIndependentStarlightSource> sources = involvedSourceMap.computeIfAbsent(pos, k -> new LinkedList<>());
            sources.add(source);
            MiscUtils.executeWithChunk(level, pos, () -> activeChunks.add(pos));
        }
        if (!activeChunks.isEmpty()) {
            activeChunkMap.put(source, activeChunks);
        }
        for (BlockPos pos : chain.getLossMultipliers().keySet()) {
            List<IIndependentStarlightSource> sources = posToSourceMap.computeIfAbsent(pos, k -> new LinkedList<>());
            sources.add(source);
        }
        List<IIndependentStarlightSource> sources = posToSourceMap.computeIfAbsent(pos, k -> new LinkedList<>());
        sources.add(source);
    }

    public Collection<TransmissionChain> getTransmissionChains() {
        return ImmutableList.copyOf(this.cachedSourceChain.values());
    }

    //Fired if the node's state related to the network changes.
    //Break all networks associated with that node to trigger recalculations as needed.
    public void notifyTransmissionNodeChange(IPrismTransmissionNode node) {
        BlockPos pos = node.getLocationPos();
        List<IIndependentStarlightSource> sources = posToSourceMap.get(pos);
        if (sources != null) {
            new ArrayList<>(sources).forEach(this::breakSourceNetwork);
        }
    }

    public TransmissionChain getSourceChain(IIndependentStarlightSource source) {
        return cachedSourceChain.get(source);
    }

    //Remove a source from the network to trigger recalculation!
    public void breakSourceNetwork(IIndependentStarlightSource source) {
        TransmissionChain knownChain = cachedSourceChain.get(source);
        if (knownChain != null) {
            for (ChunkPos chPos : knownChain.getInvolvedChunks()) {
                List<IIndependentStarlightSource> sources = involvedSourceMap.get(chPos);
                if (sources != null) {
                    sources.remove(source);
                    if (sources.isEmpty()) {
                        involvedSourceMap.remove(chPos);
                    }
                }
            }
            for (BlockPos pos : knownChain.getLossMultipliers().keySet()) {
                List<IIndependentStarlightSource> sources = posToSourceMap.get(pos);
                if (sources != null) {
                    sources.remove(source);
                    if (sources.isEmpty()) {
                        posToSourceMap.remove(pos);
                    }
                }
            }
            SyncDataHolder.executeServer(SyncDataHolder.DATA_LIGHT_CONNECTIONS, DataLightConnections.class, data -> {
                data.removeOldConnectionsThreaded(dim, knownChain.getFoundConnections());
            });
            SyncDataHolder.executeServer(SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS, DataLightBlockEndpoints.class, data -> {
                data.removeEndpoints(dim, knownChain.getResolvedNormalBlockPositions());
            });
        }
        activeChunkMap.remove(source);
        cachedSourceChain.remove(source);
    }

    public void informChunkUnload(ChunkPos pos) {
        List<IIndependentStarlightSource> sources = involvedSourceMap.get(pos);
        if (sources != null) {
            for (IIndependentStarlightSource source : sources) {
                List<ChunkPos> activeChunks = activeChunkMap.get(source);
                if (activeChunks != null) {
                    activeChunks.remove(pos);
                    if (activeChunks.isEmpty()) {
                        activeChunkMap.remove(source);
                    }
                }
            }
        }
    }

    public void informChunkLoad(ChunkPos pos) {
        List<IIndependentStarlightSource> sources = involvedSourceMap.get(pos);
        if (sources != null) {
            for (IIndependentStarlightSource source : sources) {
                TransmissionChain chain = cachedSourceChain.get(source);
                if (chain != null) {
                    if (chain.getInvolvedChunks().contains(pos)) {
                        if (activeChunkMap.containsKey(source)) {
                            List<ChunkPos> positions = activeChunkMap.get(source);
                            if (!positions.contains(pos)) positions.add(pos);
                        } else {
                            List<ChunkPos> positions = new LinkedList<>();
                            positions.add(pos);
                            activeChunkMap.put(source, positions);
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        this.activeChunkMap.clear();
        this.cachedSourceChain.clear();
        this.involvedSourceMap.clear();
        this.posToSourceMap.clear();
    }

}
