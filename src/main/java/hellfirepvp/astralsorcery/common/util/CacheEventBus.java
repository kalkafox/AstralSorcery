/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.neoforged.bus.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CacheEventBus
 * Created by HellFirePvP
 * Date: 14.11.2020 / 15:00
 */
public class CacheEventBus implements IEventBus {

    private final List<Object> registeredListeners = new ArrayList<>();
    private final IEventBus wrapped;

    private CacheEventBus(IEventBus wrapped) {
        this.wrapped = wrapped;
    }

    public static CacheEventBus of(IEventBus bus) {
        return new CacheEventBus(bus);
    }

    public void unregisterAll() {
        registeredListeners.forEach(wrapped::unregister);
        registeredListeners.clear();
    }

    @Override
    public void register(Object target) {
        wrapped.register(target);
        registeredListeners.add(target);
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> consumer) {
        wrapped.addListener(consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(Class<T> event, Consumer<T> consumer) {
        wrapped.addListener(event, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
        wrapped.addListener(priority, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Class<T> event, Consumer<T> consumer) {
        wrapped.addListener(priority, event, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {
        wrapped.addListener(priority, receiveCancelled, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> event, Consumer<T> consumer) {
        wrapped.addListener(priority, receiveCancelled, event, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCancelled, Consumer<T> consumer) {
        wrapped.addListener(receiveCancelled, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCancelled, Class<T> event, Consumer<T> consumer) {
        wrapped.addListener(receiveCancelled, event, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public void unregister(Object object) {
        wrapped.unregister(object);
        registeredListeners.remove(object);
    }

    @Override
    public <T extends Event> T post(T event) {
        return wrapped.post(event);
    }

    @Override
    public <T extends Event> T post(EventPriority currentPhase, T event) {
        return wrapped.post(currentPhase, event);
    }

    @Override
    public void start() {
        wrapped.start();
    }
}
