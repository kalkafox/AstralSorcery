/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.internal;

import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.PerkConverter;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.item.ArmorMaterial;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Consumer;

/**
 * Central home of every {@link DeferredRegister} Astral Sorcery publishes
 * entries through, replacing the old registry primer.
 *
 * <p>Values are still constructed eagerly during mod construction so the
 * static fields in the {@code common.lib} classes keep their concrete types;
 * the suppliers handed to the registers simply return those instances when
 * the corresponding RegisterEvent fires.</p>
 */
public final class AstralRegistries {

    // Vanilla registries
    public static final DeferredRegister<Block> BLOCKS = create(Registries.BLOCK);
    public static final DeferredRegister<Item> CREATIVE_NAMES = create(Registries.ITEM);
    public static final DeferredRegister<Fluid> FLUIDS = create(Registries.FLUID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = create(Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = create(Registries.ENTITY_TYPE);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = create(Registries.MOB_EFFECT);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = create(Registries.MENU);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = create(Registries.SOUND_EVENT);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = create(Registries.RECIPE_TYPE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = create(Registries.RECIPE_SERIALIZER);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = create(Registries.CREATIVE_MODE_TAB);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = create(Registries.COMMAND_ARGUMENT_TYPE);
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES = create(Registries.LOOT_FUNCTION_TYPE);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = create(Registries.ARMOR_MATERIAL);

    // NeoForge registries
    public static final DeferredRegister<FluidType> FLUID_TYPES = create(NeoForgeRegistries.Keys.FLUID_TYPES);
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS);
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES);

    // ObserverLib's provider registry; Astral Sorcery registers its structure matchers into it.
    public static final DeferredRegister<ObserverProvider<?>> OBSERVER_PROVIDERS = create(RegistryProviders.REGISTRY_KEY);

    // Astral Sorcery's own registries
    public static final DeferredRegister<IConstellation> CONSTELLATIONS = create(RegistriesAS.REGISTRY_KEY_CONSTELLATIONS);
    public static final DeferredRegister<ConstellationEffectProvider> CONSTELLATION_EFFECTS = create(RegistriesAS.REGISTRY_KEY_CONSTELLATION_EFFECTS);
    public static final DeferredRegister<MantleEffect> MANTLE_EFFECTS = create(RegistriesAS.REGISTRY_KEY_MANTLE_EFFECTS);
    public static final DeferredRegister<EngravingEffect> ENGRAVING_EFFECTS = create(RegistriesAS.REGISTRY_KEY_ENGRAVING_EFFECTS);
    public static final DeferredRegister<StructureType> STRUCTURE_TYPES = create(RegistriesAS.REGISTRY_KEY_STRUCTURE_TYPES);
    public static final DeferredRegister<PerkAttributeType> PERK_ATTRIBUTE_TYPES = create(RegistriesAS.REGISTRY_KEY_PERK_ATTRIBUTE_TYPES);
    public static final DeferredRegister<PerkConverter> PERK_ATTRIBUTE_CONVERTERS = create(RegistriesAS.REGISTRY_KEY_PERK_ATTRIBUTE_CONVERTERS);
    public static final DeferredRegister<PerkAttributeModifier> PERK_CUSTOM_MODIFIERS = create(RegistriesAS.REGISTRY_KEY_PERK_CUSTOM_MODIFIERS);
    public static final DeferredRegister<PerkAttributeReader> PERK_ATTRIBUTE_READERS = create(RegistriesAS.REGISTRY_KEY_PERK_ATTRIBUTE_READERS);
    public static final DeferredRegister<CrystalProperty> CRYSTAL_PROPERTIES = create(RegistriesAS.REGISTRY_KEY_CRYSTAL_PROPERTIES);
    public static final DeferredRegister<PropertyUsage> CRYSTAL_USAGES = create(RegistriesAS.REGISTRY_KEY_CRYSTAL_USAGES);
    public static final DeferredRegister<AltarRecipeEffect> ALTAR_EFFECTS = create(RegistriesAS.REGISTRY_KEY_ALTAR_EFFECTS);

    static {
        RegistriesAS.REGISTRY_CONSTELLATIONS = makeRegistry(CONSTELLATIONS, builder ->
                builder.onAdd((registry, id, key, value) -> ConstellationRegistry.addConstellation(value)));
        RegistriesAS.REGISTRY_CONSTELLATION_EFFECT = makeRegistry(CONSTELLATION_EFFECTS, builder -> {});
        RegistriesAS.REGISTRY_MANTLE_EFFECT = makeRegistry(MANTLE_EFFECTS, builder -> {});
        RegistriesAS.REGISTRY_ENGRAVING_EFFECT = makeRegistry(ENGRAVING_EFFECTS, builder -> {});
        RegistriesAS.REGISTRY_STRUCTURE_TYPES = makeRegistry(STRUCTURE_TYPES, builder -> {});
        RegistriesAS.REGISTRY_PERK_ATTRIBUTE_TYPES = makeRegistry(PERK_ATTRIBUTE_TYPES, builder -> {});
        RegistriesAS.REGISTRY_PERK_ATTRIBUTE_CONVERTERS = makeRegistry(PERK_ATTRIBUTE_CONVERTERS, builder -> {});
        RegistriesAS.REGISTRY_PERK_CUSTOM_MODIFIERS = makeRegistry(PERK_CUSTOM_MODIFIERS, builder -> {});
        RegistriesAS.REGISTRY_PERK_ATTRIBUTE_READERS = makeRegistry(PERK_ATTRIBUTE_READERS, builder -> {});
        RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES = makeRegistry(CRYSTAL_PROPERTIES, builder -> {});
        RegistriesAS.REGISTRY_CRYSTAL_USAGES = makeRegistry(CRYSTAL_USAGES, builder -> {});
        RegistriesAS.REGISTRY_ALTAR_EFFECTS = makeRegistry(ALTAR_EFFECTS, builder -> {});
    }

    private AstralRegistries() {}

    /**
     * Attaches every deferred register to the mod event bus. Must run during
     * mod construction, after the mod's registry content has been built.
     */
    public static void subscribe(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        CREATIVE_NAMES.register(modEventBus);
        FLUIDS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        LOOT_FUNCTION_TYPES.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);

        FLUID_TYPES.register(modEventBus);
        ENTITY_DATA_SERIALIZERS.register(modEventBus);
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);

        OBSERVER_PROVIDERS.register(modEventBus);

        CONSTELLATIONS.register(modEventBus);
        CONSTELLATION_EFFECTS.register(modEventBus);
        MANTLE_EFFECTS.register(modEventBus);
        ENGRAVING_EFFECTS.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        PERK_ATTRIBUTE_TYPES.register(modEventBus);
        PERK_ATTRIBUTE_CONVERTERS.register(modEventBus);
        PERK_CUSTOM_MODIFIERS.register(modEventBus);
        PERK_ATTRIBUTE_READERS.register(modEventBus);
        CRYSTAL_PROPERTIES.register(modEventBus);
        CRYSTAL_USAGES.register(modEventBus);
        ALTAR_EFFECTS.register(modEventBus);
    }

    /**
     * Queues an eagerly-created value for registration and returns it, so
     * call sites can keep assigning concrete-typed static fields.
     */
    public static <T, I extends T> I register(DeferredRegister<T> target, ResourceLocation name, I value) {
        if (!AstralSorcery.MODID.equals(name.getNamespace())) {
            throw new IllegalArgumentException("Cannot register " + name + " through Astral Sorcery's " +
                    target.getRegistryName() + " register; only " + AstralSorcery.MODID + " names are supported");
        }
        target.register(name.getPath(), () -> value);
        return value;
    }

    /**
     * Variant of {@link #register(DeferredRegister, ResourceLocation, Object)} for
     * values that carry their own name through {@link AstralRegistryEntry}.
     */
    public static <T, I extends T> I register(DeferredRegister<T> target, I value) {
        if (!(value instanceof AstralRegistryEntry<?> named)) {
            throw new IllegalArgumentException(value.getClass().getName() + " does not provide a registry name");
        }
        ResourceLocation name = named.getRegistryName();
        if (name == null) {
            throw new IllegalStateException("Cannot register unnamed value " + value.getClass().getName());
        }
        return register(target, name, value);
    }

    private static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey) {
        return DeferredRegister.create(registryKey, AstralSorcery.MODID);
    }

    private static <T> LegacyRegistry<T> makeRegistry(DeferredRegister<T> deferredRegister,
                                                      Consumer<RegistryBuilder<T>> customize) {
        Registry<T> registry = deferredRegister.makeRegistry(builder -> {
            builder.sync(true);
            customize.accept(builder);
        });
        return new LegacyRegistry<>(deferredRegister.getRegistryKey(), registry);
    }
}
