/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.advancements;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.AttuneCrystalTrigger;
import hellfirepvp.astralsorcery.common.advancement.AttuneSelfTrigger;
import hellfirepvp.astralsorcery.common.advancement.DiscoverConstellationTrigger;
import hellfirepvp.astralsorcery.common.advancement.instance.AltarRecipeInstance;
import hellfirepvp.astralsorcery.common.advancement.instance.ConstellationInstance;
import hellfirepvp.astralsorcery.common.advancement.instance.PerkLevelInstance;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.TickTrigger;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AstralAdvancementProvider
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:11
 */
public class AstralAdvancementProvider extends AdvancementProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;

    public AstralAdvancementProvider(DataGenerator generator) {
        super(generator);
        this.generator = generator;
    }

    @Override
    public void run(HashCache cache) {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> registrar = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path outPath = getPath(path, advancement);
                try {
                    DataProvider.save(GSON, cache, advancement.copy().serialize(), outPath);
                } catch (IOException ioexception) {
                    LOGGER.error("Couldn't save advancement {}", outPath, ioexception);
                }

            }
        };

        this.registerAdvancements(registrar);
    }

    private Path getPath(Path base, Advancement advancement) {
        return base.resolve(String.format("data/%s/advancements/%s.json", advancement.getId().getNamespace(), advancement.getId().getPath()));
    }

    private MutableComponent title(String key) {
        return Component.translatable(String.format("advancements.astralsorcery.%s.title", key));
    }

    private MutableComponent description(String key) {
        return Component.translatable(String.format("advancements.astralsorcery.%s.desc", key));
    }

    private void registerAdvancements(Consumer<Advancement> registrar) {
        Advancement root = Advancement.Builder.builder()
                .display(ItemsAS.TOME, title("root"), description("root"),
                        AstralSorcery.key("textures/block/black_marble_raw.png"),
                        AdvancementType.TASK, false, false, false)
                .withCriterion("astralsorcery_present", new TickTrigger.Instance(EntityPredicate.AndPredicate.ANY))
                .register(registrar, AstralSorcery.key("root").toString());

        Advancement foundRockCrystals = Advancement.Builder.builder()
                .serializeToNetwork(root)
                .display(ItemsAS.ROCK_CRYSTAL, title("rock_crystals"), description("rock_crystals"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("rock_crystal_in_inventory", InventoryChangeTrigger.Instance.serializeToJson(ItemsAS.ROCK_CRYSTAL))
                .register(registrar, AstralSorcery.key("rock_crystals").toString());
        Advancement foundCelestialCrystals = Advancement.Builder.builder()
                .serializeToNetwork(foundRockCrystals)
                .display(ItemsAS.CELESTIAL_CRYSTAL, title("celestial_crystals"), description("celestial_crystals"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("celestial_crystal_in_inventory", InventoryChangeTrigger.Instance.serializeToJson(ItemsAS.CELESTIAL_CRYSTAL))
                .register(registrar, AstralSorcery.key("celestial_crystals").toString());

        Advancement craftAltarT2 = Advancement.Builder.builder()
                .serializeToNetwork(foundRockCrystals)
                .display(BlocksAS.ALTAR_ATTUNEMENT, title("craft_t2_altar"), description("craft_t2_altar"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("altar_craft_t2_altar", AltarRecipeInstance.withOutput(BlocksAS.ALTAR_ATTUNEMENT))
                .register(registrar, AstralSorcery.key("craft_t2_altar").toString());
        Advancement craftAltarT3 = Advancement.Builder.builder()
                .serializeToNetwork(craftAltarT2)
                .display(BlocksAS.ALTAR_CONSTELLATION, title("craft_t3_altar"), description("craft_t3_altar"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("altar_craft_t3_altar", AltarRecipeInstance.withOutput(BlocksAS.ALTAR_CONSTELLATION))
                .register(registrar, AstralSorcery.key("craft_t3_altar").toString());
        Advancement craftAltarT4 = Advancement.Builder.builder()
                .serializeToNetwork(craftAltarT3)
                .display(BlocksAS.ALTAR_CONSTELLATION, title("craft_t4_altar"), description("craft_t4_altar"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .withCriterion("altar_craft_t3_altar", AltarRecipeInstance.withOutput(BlocksAS.ALTAR_RADIANCE))
                .register(registrar, AstralSorcery.key("craft_t4_altar").toString());

        Advancement findAnyConstellation = Advancement.Builder.builder()
                .serializeToNetwork(root)
                .display(BlocksAS.TELESCOPE, title("find_constellation"), description("find_constellation"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("any_constellation_discovered", ConstellationInstance.any(DiscoverConstellationTrigger.ID))
                .register(registrar, AstralSorcery.key("find_constellation").toString());
        Advancement findWeakConstellation = Advancement.Builder.builder()
                .serializeToNetwork(findAnyConstellation)
                .display(BlocksAS.TELESCOPE, title("find_weak_constellation"), description("find_weak_constellation"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("weak_constellation_discovered", ConstellationInstance.anyWeak(DiscoverConstellationTrigger.ID))
                .register(registrar, AstralSorcery.key("find_weak_constellation").toString());
        Advancement findMinorConstellation = Advancement.Builder.builder()
                .serializeToNetwork(findWeakConstellation)
                .display(BlocksAS.OBSERVATORY, title("find_minor_constellation"), description("find_minor_constellation"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("minor_constellation_discovered", ConstellationInstance.anyMinor(DiscoverConstellationTrigger.ID))
                .register(registrar, AstralSorcery.key("find_minor_constellation").toString());

        Advancement attuneSelf = Advancement.Builder.builder()
                .serializeToNetwork(findAnyConstellation)
                .display(BlocksAS.ATTUNEMENT_ALTAR, title("attune_self"), description("attune_self"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("attune_self", ConstellationInstance.any(AttuneSelfTrigger.ID))
                .register(registrar, AstralSorcery.key("attune_self").toString());
        Advancement attuneCrystal = Advancement.Builder.builder()
                .serializeToNetwork(attuneSelf)
                .display(BlocksAS.RITUAL_PEDESTAL, title("attune_crystal"), description("attune_crystal"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("attune_crystal", ConstellationInstance.anyWeak(AttuneCrystalTrigger.ID))
                .register(registrar, AstralSorcery.key("attune_crystal").toString());
        Advancement attuneCrystalTrait = Advancement.Builder.builder()
                .serializeToNetwork(attuneCrystal)
                .display(BlocksAS.RITUAL_PEDESTAL, title("attune_trait"), description("attune_trait"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("attune_trait", ConstellationInstance.anyMinor(AttuneCrystalTrigger.ID))
                .register(registrar, AstralSorcery.key("attune_trait").toString());

        Advancement perkLevelSmall = Advancement.Builder.builder()
                .serializeToNetwork(attuneSelf)
                .display(BlocksAS.SPECTRAL_RELAY, title("perk_level_small"), description("perk_level_small"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("gain_perk_level_small", PerkLevelInstance.reachLevel(10))
                .register(registrar, AstralSorcery.key("perk_level_small").toString());
        Advancement perkLevelMedium = Advancement.Builder.builder()
                .serializeToNetwork(perkLevelSmall)
                .display(BlocksAS.SPECTRAL_RELAY, title("perk_level_medium"), description("perk_level_medium"),
                        null, AdvancementType.TASK, true, true, false)
                .withCriterion("gain_perk_level_medium", PerkLevelInstance.reachLevel(25))
                .register(registrar, AstralSorcery.key("perk_level_medium").toString());
        Advancement perkLevelLarge = Advancement.Builder.builder()
                .serializeToNetwork(perkLevelMedium)
                .display(BlocksAS.SPECTRAL_RELAY, title("perk_level_large"), description("perk_level_large"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .withCriterion("gain_perk_level_large", PerkLevelInstance.reachLevel(40))
                .register(registrar, AstralSorcery.key("perk_level_large").toString());
    }
}
