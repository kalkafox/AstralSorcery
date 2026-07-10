package hellfirepvp.astralsorcery.common.registry.internal;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Small compatibility view over a modern Minecraft registry.
 */
public final class LegacyRegistry<T> implements Iterable<T> {

    private final ResourceKey<? extends Registry<T>> key;
    private final Registry<T> registry;

    public LegacyRegistry(ResourceKey<? extends Registry<T>> key, Registry<T> registry) {
        this.key = key;
        this.registry = registry;
    }

    public T getValue(ResourceLocation name) {
        return registry.get(name);
    }

    public Collection<T> getValues() {
        return registry.stream().toList();
    }

    public Set<ResourceLocation> getUserList() {
        return registry.keySet();
    }

    public boolean containsKey(ResourceLocation name) {
        return registry.containsKey(name);
    }

    public ResourceLocation getKey(T value) {
        return registry.getKey(value);
    }

    public ResourceKey<? extends Registry<T>> key() {
        return key;
    }

    public Registry<T> unwrap() {
        return registry;
    }

    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        registry.forEach(action);
    }
}
