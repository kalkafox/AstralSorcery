/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight.network;

import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import hellfirepvp.observerlib.common.util.tick.TickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StarlightTransmissionHandler
 * Created by HellFirePvP
 * Date: 04.08.2016 / 23:24
 */
public class StarlightTransmissionHandler implements ITickHandler {

    private static final StarlightTransmissionHandler instance = new StarlightTransmissionHandler();
    private final Map<ResourceKey<Level>, TransmissionWorldHandler> worldHandlers = new HashMap<>();

    private StarlightTransmissionHandler() {}

    public static StarlightTransmissionHandler getInstance() {
        return instance;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Level level = (Level) context[0];
        if (level.isClientSide() || !(level instanceof ServerLevel)) {
            return;
        }

        worldHandlers.computeIfAbsent(level.dimension(), TransmissionWorldHandler::new).tick((ServerLevel) level);
    }

    public void clearServer() {
        worldHandlers.values().forEach(TransmissionWorldHandler::clear);
        worldHandlers.clear();
    }

    public void informWorldUnload(Level level) {
        ResourceKey<Level> dimKey = level.dimension();
        TransmissionWorldHandler handle = worldHandlers.get(dimKey);
        if (handle != null) {
            handle.clear();
        }
        this.worldHandlers.remove(dimKey);
    }

    @Nullable
    public TransmissionWorldHandler getWorldHandler(Level level) {
        if (level == null) {
            return null;
        }
        return worldHandlers.get(level.dimension());
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.WORLD);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.START;
    }

    @Override
    public String getName() {
        return "Starlight Transmission Handler";
    }

}
