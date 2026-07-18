package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Compatibility helpers for code that still expects Forge's old named
 * registry-entry model.
 */
public final class RegistryHelper {

    private RegistryHelper() {}

    @Nullable
    public static ResourceLocation getKey(Object value) {
        if (value instanceof AstralRegistryEntry<?> entry && entry.getRegistryName() != null) {
            return entry.getRegistryName();
        }
        Registry<?> registry = findRegistry(value);
        return registry == null ? null : getKey(registry, value);
    }

    @Nullable
    public static ResourceLocation getRegistryName(Object value) {
        Registry<?> registry = findRegistry(value);
        return registry == null ? null : registry.key().location();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(ResourceLocation registryName) {
        return (Registry<T>) BuiltInRegistries.REGISTRY.get(registryName);
    }

    @Nullable
    public static <T> T getValue(ResourceLocation registryName, ResourceLocation key) {
        Registry<T> registry = getRegistry(registryName);
        return registry == null ? null : registry.get(key);
    }

    public static boolean containsKey(ResourceLocation registryName, ResourceLocation key) {
        Registry<?> registry = getRegistry(registryName);
        return registry != null && registry.containsKey(key);
    }

    public static <T> Collection<T> getValues(ResourceLocation registryName) {
        Registry<T> registry = getRegistry(registryName);
        return registry == null ? java.util.List.of() : registry.stream().toList();
    }

    @Nullable
    private static Registry<?> findRegistry(Object value) {
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (getKey(registry, value) != null) {
                return registry;
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ResourceLocation getKey(Registry registry, Object value) {
        // getResourceKey instead of getKey: defaulted registries (block, item,
        // fluid, ...) answer getKey with their default key (minecraft:air) for
        // values they don't contain, which breaks the registry probing above.
        return (ResourceLocation) registry.getResourceKey(value)
                .map(key -> ((net.minecraft.resources.ResourceKey<?>) key).location())
                .orElse(null);
    }
}
