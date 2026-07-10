/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.world;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestSeed;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldSeedCache
 * Created by HellFirePvP
 * Date: 02.06.2019 / 14:21
 */
public class WorldSeedCache {

    private static long lastServerQuery = 0L;
    private static int activeSession = 0;

    private static final Map<ResourceKey<Level>, Long> cacheSeedLookup = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void clearClient() {
        activeSession++;
        cacheSeedLookup.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateSeedCache(ResourceKey<Level> dim, int user, long seed) {
        if (activeSession == user) {
            cacheSeedLookup.put(dim, seed);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<Long> getSeedIfPresent(ResourceKey<Level> dim) {
        if (dim == null) {
            return Optional.empty();
        }
        if (!cacheSeedLookup.containsKey(dim)) {
            long current = System.currentTimeMillis();
            if (current - lastServerQuery > 5_000) {
                lastServerQuery = current;
                activeSession++;
                PktRequestSeed req = new PktRequestSeed(activeSession, dim);
                PacketChannel.CHANNEL.sendToServer(req);
            }
            return Optional.empty();
        }
        return Optional.of(cacheSeedLookup.get(dim));
    }

}
