/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight.network;

import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import hellfirepvp.observerlib.common.util.tick.TickEvent;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StarlightUpdateHandler
 * Created by HellFirePvP
 * Date: 01.10.2016 / 01:41
 */
public class StarlightUpdateHandler implements ITickHandler {

    private static final StarlightUpdateHandler instance = new StarlightUpdateHandler();
    private static final Map<ResourceKey<Level>, List<IPrismTransmissionNode>> updateRequired = new HashMap<>();
    private static final Object accessLock = new Object();

    private StarlightUpdateHandler() {}

    public static StarlightUpdateHandler getInstance() {
        return instance;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Level level = (Level) context[0];
        if (level.isClientSide()) {
            return;
        }

        List<IPrismTransmissionNode> nodes = getNodes(level);
        synchronized (accessLock) {
            for (IPrismTransmissionNode node : nodes) {
                node.update(level);
            }
        }
    }

    private List<IPrismTransmissionNode> getNodes(Level level) {
        return updateRequired.computeIfAbsent(level.dimension(), k -> new LinkedList<>());
    }

    public void removeNode(Level level, IPrismTransmissionNode node) {
        synchronized (accessLock) {
            getNodes(level).remove(node);
        }
    }

    public void addNode(Level level, IPrismTransmissionNode node) {
        synchronized (accessLock) {
            getNodes(level).add(node);
        }
    }

    public void informWorldLoad(Level level) {
        synchronized (accessLock) {
            updateRequired.remove(level.dimension());
        }
    }

    public void clearServer() {
        synchronized (accessLock) {
            updateRequired.clear();
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.WORLD);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Starlight Update Handler";
    }

}
