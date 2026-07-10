/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.camera;

import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import hellfirepvp.observerlib.common.util.tick.TickEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.TreeSet;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientCameraManager
 * Created by HellFirePvP
 * Date: 02.12.2019 / 19:50
 */
public class ClientCameraManager implements ITickHandler {

    public static final ClientCameraManager INSTANCE = new ClientCameraManager();

    private final TreeSet<ICameraTransformer> filters = new TreeSet<>(Comparator.comparingInt(ICameraTransformer::getPriority));
    private ICameraTransformer lastTransformer = null;

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        if (type == TickEvent.Type.RENDER) {
            //Render Tick
            float pTicks = (float) context[0];
            if (this.hasActiveTransformer()) {
                ICameraTransformer prio = this.getActiveTransformer();
                if (!prio.equals(lastTransformer)) {
                    if (lastTransformer != null) {
                        lastTransformer.onStopTransforming(pTicks);
                    }
                    prio.onStartTransforming(pTicks);
                    lastTransformer = prio;
                }
                prio.transformRenderView(Minecraft.getInstance().isPaused() ? 0F : pTicks);
                if (prio.getPersistencyFunction().timedOut()) {
                    prio.onStopTransforming(pTicks);
                    filters.remove(prio);
                }
            } else {
                //Clean up remaining transformer
                if (lastTransformer != null) {
                    lastTransformer.onStopTransforming(pTicks);
                    lastTransformer = null;
                }
            }
        } else if (!Minecraft.getInstance().isPaused()) {
            //Client Tick
            if (this.hasActiveTransformer()) {
                this.getActiveTransformer().onClientTick();
            }
        }
    }

    public void removeAllAndCleanup() {
        if (this.hasActiveTransformer()) {
            filters.last().onStopTransforming(0);
        }
        this.filters.clear();
    }

    public void run(ICameraTransformer transformer) {
        this.filters.add(transformer);
    }

    public void removeTransformer(ICameraTransformer transformer) {
        this.filters.remove(transformer);
    }

    @Nullable
    public ICameraTransformer getActiveTransformer() {
        if (this.hasActiveTransformer()) {
            return this.filters.last();
        }
        return null;
    }

    public boolean hasActiveTransformer() {
        return !this.filters.isEmpty();
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.RENDER, TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.START;
    }

    @Override
    public String getName() {
        return "Client Camera Manager";
    }
}
