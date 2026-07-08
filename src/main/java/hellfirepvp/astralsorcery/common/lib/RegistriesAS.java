/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
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
import hellfirepvp.astralsorcery.common.registry.internal.LegacyRegistry;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistriesAS
 * Created by HellFirePvP
 * Date: 02.06.2019 / 09:17
 */
public class RegistriesAS {

    private RegistriesAS() {}

    public static final ResourceLocation REGISTRY_NAME_CONSTELLATIONS = AstralSorcery.key("constellations");
    public static final ResourceLocation REGISTRY_NAME_CONSTELLATION_EFFECTS = AstralSorcery.key("constellation_effect");
    public static final ResourceLocation REGISTRY_NAME_MANTLE_EFFECTS = AstralSorcery.key("mantle_effect");
    public static final ResourceLocation REGISTRY_NAME_ENGRAVING_EFFECT = AstralSorcery.key("engraving_effect");
    public static final ResourceLocation REGISTRY_NAME_STRUCTURE_TYPES = AstralSorcery.key("structure_types");
    public static final ResourceLocation REGISTRY_NAME_PERK_ATTRIBUTE_TYPES = AstralSorcery.key("perk_attribute_types");
    public static final ResourceLocation REGISTRY_NAME_PERK_ATTRIBUTE_CONVERTERS = AstralSorcery.key("perk_attribute_converters");
    public static final ResourceLocation REGISTRY_NAME_PERK_CUSTOM_MODIFIERS = AstralSorcery.key("perk_attribute_custom_modifiers");
    public static final ResourceLocation REGISTRY_NAME_PERK_ATTRIBUTE_READERS = AstralSorcery.key("perk_attribute_readers");
    public static final ResourceLocation REGISTRY_NAME_CRYSTAL_PROPERTIES = AstralSorcery.key("attribute_crystal_properties");
    public static final ResourceLocation REGISTRY_NAME_CRYSTAL_USAGES = AstralSorcery.key("attribute_crystal_usages");
    public static final ResourceLocation REGISTRY_NAME_ALTAR_EFFECTS = AstralSorcery.key("altar_recipe_effects");

    public static final ResourceKey<Registry<IConstellation>> REGISTRY_KEY_CONSTELLATIONS = key(REGISTRY_NAME_CONSTELLATIONS);
    public static final ResourceKey<Registry<ConstellationEffectProvider>> REGISTRY_KEY_CONSTELLATION_EFFECTS = key(REGISTRY_NAME_CONSTELLATION_EFFECTS);
    public static final ResourceKey<Registry<MantleEffect>> REGISTRY_KEY_MANTLE_EFFECTS = key(REGISTRY_NAME_MANTLE_EFFECTS);
    public static final ResourceKey<Registry<EngravingEffect>> REGISTRY_KEY_ENGRAVING_EFFECTS = key(REGISTRY_NAME_ENGRAVING_EFFECT);
    public static final ResourceKey<Registry<StructureType>> REGISTRY_KEY_STRUCTURE_TYPES = key(REGISTRY_NAME_STRUCTURE_TYPES);
    public static final ResourceKey<Registry<PerkAttributeType>> REGISTRY_KEY_PERK_ATTRIBUTE_TYPES = key(REGISTRY_NAME_PERK_ATTRIBUTE_TYPES);
    public static final ResourceKey<Registry<PerkConverter>> REGISTRY_KEY_PERK_ATTRIBUTE_CONVERTERS = key(REGISTRY_NAME_PERK_ATTRIBUTE_CONVERTERS);
    public static final ResourceKey<Registry<PerkAttributeModifier>> REGISTRY_KEY_PERK_CUSTOM_MODIFIERS = key(REGISTRY_NAME_PERK_CUSTOM_MODIFIERS);
    public static final ResourceKey<Registry<PerkAttributeReader>> REGISTRY_KEY_PERK_ATTRIBUTE_READERS = key(REGISTRY_NAME_PERK_ATTRIBUTE_READERS);
    public static final ResourceKey<Registry<CrystalProperty>> REGISTRY_KEY_CRYSTAL_PROPERTIES = key(REGISTRY_NAME_CRYSTAL_PROPERTIES);
    public static final ResourceKey<Registry<PropertyUsage>> REGISTRY_KEY_CRYSTAL_USAGES = key(REGISTRY_NAME_CRYSTAL_USAGES);
    public static final ResourceKey<Registry<AltarRecipeEffect>> REGISTRY_KEY_ALTAR_EFFECTS = key(REGISTRY_NAME_ALTAR_EFFECTS);

    public static LegacyRegistry<IConstellation> REGISTRY_CONSTELLATIONS;
    public static LegacyRegistry<ConstellationEffectProvider> REGISTRY_CONSTELLATION_EFFECT;
    public static LegacyRegistry<MantleEffect> REGISTRY_MANTLE_EFFECT;
    public static LegacyRegistry<EngravingEffect> REGISTRY_ENGRAVING_EFFECT;
    public static LegacyRegistry<StructureType> REGISTRY_STRUCTURE_TYPES;
    public static LegacyRegistry<PerkAttributeType> REGISTRY_PERK_ATTRIBUTE_TYPES;
    public static LegacyRegistry<PerkConverter> REGISTRY_PERK_ATTRIBUTE_CONVERTERS;
    public static LegacyRegistry<PerkAttributeModifier> REGISTRY_PERK_CUSTOM_MODIFIERS;
    public static LegacyRegistry<PerkAttributeReader> REGISTRY_PERK_ATTRIBUTE_READERS;
    public static LegacyRegistry<CrystalProperty> REGISTRY_CRYSTAL_PROPERTIES;
    public static LegacyRegistry<PropertyUsage> REGISTRY_CRYSTAL_USAGES;
    public static LegacyRegistry<AltarRecipeEffect> REGISTRY_ALTAR_EFFECTS;

    private static <T> ResourceKey<Registry<T>> key(ResourceLocation name) {
        return ResourceKey.createRegistryKey(name);
    }

}
