/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.registry.internal.LegacyRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static hellfirepvp.astralsorcery.common.lib.RegistriesAS.*;

/**
 * Creates Astral Sorcery's code-defined registries using NeoForge's modern
 * registry lifecycle.
 */
public final class RegistryRegistries {

    private RegistryRegistries() {}

    public static void buildRegistries(NewRegistryEvent event) {
        REGISTRY_CONSTELLATIONS = create(event, REGISTRY_KEY_CONSTELLATIONS,
                new RegistryBuilder<>(REGISTRY_KEY_CONSTELLATIONS)
                        .onAdd((registry, id, key, value) -> ConstellationRegistry.addConstellation(value)));
        REGISTRY_CONSTELLATION_EFFECT = create(event, REGISTRY_KEY_CONSTELLATION_EFFECTS);
        REGISTRY_MANTLE_EFFECT = create(event, REGISTRY_KEY_MANTLE_EFFECTS);
        REGISTRY_ENGRAVING_EFFECT = create(event, REGISTRY_KEY_ENGRAVING_EFFECTS);
        REGISTRY_STRUCTURE_TYPES = create(event, REGISTRY_KEY_STRUCTURE_TYPES);
        REGISTRY_PERK_ATTRIBUTE_TYPES = create(event, REGISTRY_KEY_PERK_ATTRIBUTE_TYPES);
        REGISTRY_PERK_ATTRIBUTE_CONVERTERS = create(event, REGISTRY_KEY_PERK_ATTRIBUTE_CONVERTERS);
        REGISTRY_PERK_CUSTOM_MODIFIERS = create(event, REGISTRY_KEY_PERK_CUSTOM_MODIFIERS);
        REGISTRY_PERK_ATTRIBUTE_READERS = create(event, REGISTRY_KEY_PERK_ATTRIBUTE_READERS);
        REGISTRY_CRYSTAL_PROPERTIES = create(event, REGISTRY_KEY_CRYSTAL_PROPERTIES);
        REGISTRY_CRYSTAL_USAGES = create(event, REGISTRY_KEY_CRYSTAL_USAGES);
        REGISTRY_ALTAR_EFFECTS = create(event, REGISTRY_KEY_ALTAR_EFFECTS);
    }

    private static <T> LegacyRegistry<T> create(NewRegistryEvent event,
                                                 ResourceKey<Registry<T>> key) {
        return create(event, key, new RegistryBuilder<>(key));
    }

    private static <T> LegacyRegistry<T> create(NewRegistryEvent event,
                                                 ResourceKey<Registry<T>> key,
                                                 RegistryBuilder<T> builder) {
        Registry<T> registry = builder.sync(true).create();
        event.register(registry);
        return new LegacyRegistry<>(key, registry);
    }
}
