/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.registry.RegistryEnchantments;
import hellfirepvp.astralsorcery.common.registry.RegistryWorldGeneration;
import hellfirepvp.astralsorcery.datagen.assets.AstralBlockStateMappingProvider;
import hellfirepvp.astralsorcery.datagen.data.advancements.AstralAdvancementProvider;
import hellfirepvp.astralsorcery.datagen.data.loot.AstralLootTableProvider;
import hellfirepvp.astralsorcery.datagen.data.perks.AstralPerkTreeProvider;
import hellfirepvp.astralsorcery.datagen.data.recipes.AstralRecipeProvider;
import hellfirepvp.astralsorcery.datagen.data.tags.AstralBiomeTagsProvider;
import hellfirepvp.astralsorcery.datagen.data.tags.AstralBlockTagsProvider;
import hellfirepvp.astralsorcery.datagen.data.tags.AstralItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AstralDataGenerator
 * Created by HellFirePvP
 * Date: 06.03.2020 / 20:11
 */
//Annotation used to separate this code initialization cleanly from everything else.
@EventBusSubscriber(modid = AstralSorcery.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AstralDataGenerator {

    @SubscribeEvent
    public static void createFileDeletedCheck(GatherDataEvent event) {
        if (!AstralSorcery.isDoingDataGeneration()) {
            return;
        }

        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        // Enchantments and the worldgen content are datapack registries since 1.21 -
        // this emits their definition JSONs from the registry classes' bootstrap
        // methods and exposes a lookup provider that includes them for the
        // downstream providers.
        DatapackBuiltinEntriesProvider datapackEntries = gen.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(),
                        new RegistrySetBuilder()
                                .add(Registries.ENCHANTMENT, RegistryEnchantments::bootstrap)
                                .add(Registries.CONFIGURED_FEATURE, RegistryWorldGeneration::bootstrapConfiguredFeatures)
                                .add(Registries.PLACED_FEATURE, RegistryWorldGeneration::bootstrapPlacedFeatures)
                                .add(Registries.STRUCTURE, RegistryWorldGeneration::bootstrapStructures)
                                .add(Registries.STRUCTURE_SET, RegistryWorldGeneration::bootstrapStructureSets)
                                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RegistryWorldGeneration::bootstrapBiomeModifiers),
                        Set.of(AstralSorcery.MODID)));
        CompletableFuture<HolderLookup.Provider> lookupProvider = datapackEntries.getRegistryProvider();

        gen.addProvider(event.includeServer(), new AstralAdvancementProvider(output, lookupProvider, fileHelper));
        AstralBlockTagsProvider blockTagGen = gen.addProvider(event.includeServer(),
                new AstralBlockTagsProvider(output, lookupProvider, fileHelper));
        gen.addProvider(event.includeServer(), new AstralItemTagsProvider(output, lookupProvider, blockTagGen.contentsGetter(), fileHelper));
        gen.addProvider(event.includeServer(), new AstralBiomeTagsProvider(output, lookupProvider, fileHelper));
        gen.addProvider(event.includeServer(), AstralLootTableProvider.create(output, lookupProvider));
        gen.addProvider(event.includeServer(), new AstralRecipeProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), new AstralPerkTreeProvider(output));

        gen.addProvider(event.includeClient(), new AstralBlockStateMappingProvider(output, fileHelper));
    }
}
