package hellfirepvp.astralsorcery.common.registry.internal;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Basic named value used by Astral Sorcery's code-defined custom registries.
 */
public abstract class AbstractAstralRegistryEntry<T> implements AstralRegistryEntry<T> {

    private ResourceLocation registryName;

    @Override
    @SuppressWarnings("unchecked")
    public T setRegistryName(ResourceLocation name) {
        this.registryName = name;
        return (T) this;
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
