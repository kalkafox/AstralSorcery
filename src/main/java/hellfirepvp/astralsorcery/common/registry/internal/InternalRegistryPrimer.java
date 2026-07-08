/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.internal;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import hellfirepvp.astralsorcery.common.perk.PerkConverter;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static hellfirepvp.astralsorcery.common.lib.RegistriesAS.*;

/**
 * Queues code-defined values until NeoForge emits the matching RegisterEvent.
 */
public class InternalRegistryPrimer {

    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, Object>> primed = new LinkedHashMap<>();
    private final Map<Object, ResourceLocation> names = new IdentityHashMap<>();

    public <T> T register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation name, T entry) {
        primed.computeIfAbsent(registryKey, key -> new LinkedHashMap<>()).put(name, entry);
        names.put(entry, name);
        return entry;
    }

    public <T extends AstralRegistryEntry<?>> T register(T entry) {
        ResourceLocation name = entry.getRegistryName();
        if (name == null) {
            throw new IllegalStateException("Cannot register unnamed value " + entry.getClass().getName());
        }
        return register(resolveRegistryKey(entry), name, entry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void fillRegistry(RegisterEvent event) {
        Map<ResourceLocation, Object> entries = primed.get(event.getRegistryKey());
        if (entries == null) {
            return;
        }
        ResourceKey registryKey = event.getRegistryKey();
        entries.forEach((name, value) -> event.register(registryKey, name, () -> value));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCached(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
        return (T) primed.getOrDefault(registryKey, Map.of()).get(key);
    }

    @Nullable
    public ResourceLocation getName(Object entry) {
        if (entry instanceof AstralRegistryEntry<?> named) {
            return named.getRegistryName();
        }
        return names.get(entry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ResourceKey<? extends Registry<T>> resolveRegistryKey(T entry) {
        if (entry instanceof IConstellation) return (ResourceKey) REGISTRY_KEY_CONSTELLATIONS;
        if (entry instanceof ConstellationEffectProvider) return (ResourceKey) REGISTRY_KEY_CONSTELLATION_EFFECTS;
        if (entry instanceof MantleEffect) return (ResourceKey) REGISTRY_KEY_MANTLE_EFFECTS;
        if (entry instanceof EngravingEffect) return (ResourceKey) REGISTRY_KEY_ENGRAVING_EFFECTS;
        if (entry instanceof StructureType) return (ResourceKey) REGISTRY_KEY_STRUCTURE_TYPES;
        if (entry instanceof PerkAttributeType) return (ResourceKey) REGISTRY_KEY_PERK_ATTRIBUTE_TYPES;
        if (entry instanceof PerkConverter) return (ResourceKey) REGISTRY_KEY_PERK_ATTRIBUTE_CONVERTERS;
        if (entry instanceof PerkAttributeModifier) return (ResourceKey) REGISTRY_KEY_PERK_CUSTOM_MODIFIERS;
        if (entry instanceof PerkAttributeReader) return (ResourceKey) REGISTRY_KEY_PERK_ATTRIBUTE_READERS;
        if (entry instanceof CrystalProperty) return (ResourceKey) REGISTRY_KEY_CRYSTAL_PROPERTIES;
        if (entry instanceof PropertyUsage) return (ResourceKey) REGISTRY_KEY_CRYSTAL_USAGES;
        if (entry instanceof AltarRecipeEffect) return (ResourceKey) REGISTRY_KEY_ALTAR_EFFECTS;
        if (entry instanceof RecipeSerializer<?>) return (ResourceKey) Registries.RECIPE_SERIALIZER;
        throw new IllegalArgumentException("No registry route for " + entry.getClass().getName());
    }
}
