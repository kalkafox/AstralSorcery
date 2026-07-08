package hellfirepvp.astralsorcery.common.registry.internal;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Compatibility contract for Astral Sorcery's named custom registry values.
 *
 * <p>Minecraft no longer stores registry names on registry values themselves.
 * Astral Sorcery still uses those names extensively for serialization and
 * display logic, so custom values retain their name through this interface.</p>
 */
public interface AstralRegistryEntry<T> {

    T setRegistryName(ResourceLocation name);

    @Nullable
    ResourceLocation getRegistryName();
}
