/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.advancements;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.AltarRecipeInstance;
import hellfirepvp.astralsorcery.common.advancement.instance.ConstellationInstance;
import hellfirepvp.astralsorcery.common.advancement.instance.PerkLevelInstance;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AstralAdvancementProvider
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:11
 */
public class AstralAdvancementProvider extends AdvancementProvider {

    public AstralAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(output, registries, fileHelper, List.of(AstralAdvancementProvider::registerAdvancements));
    }

    private static MutableComponent title(String key) {
        return Component.translatable(String.format("advancements.astralsorcery.%s.title", key));
    }

    private static MutableComponent description(String key) {
        return Component.translatable(String.format("advancements.astralsorcery.%s.desc", key));
    }

    private static void registerAdvancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> registrar, ExistingFileHelper fileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ItemsAS.TOME, title("root"), description("root"),
                        AstralSorcery.key("textures/block/black_marble_raw.png"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("astralsorcery_present", PlayerTrigger.TriggerInstance.tick())
                .save(registrar, AstralSorcery.key("root").toString());

        AdvancementHolder foundRockCrystals = Advancement.Builder.advancement()
                .parent(root)
                .display(ItemsAS.ROCK_CRYSTAL, title("rock_crystals"), description("rock_crystals"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("rock_crystal_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(ItemsAS.ROCK_CRYSTAL))
                .save(registrar, AstralSorcery.key("rock_crystals").toString());
        AdvancementHolder foundCelestialCrystals = Advancement.Builder.advancement()
                .parent(foundRockCrystals)
                .display(ItemsAS.CELESTIAL_CRYSTAL, title("celestial_crystals"), description("celestial_crystals"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("celestial_crystal_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(ItemsAS.CELESTIAL_CRYSTAL))
                .save(registrar, AstralSorcery.key("celestial_crystals").toString());

        AdvancementHolder craftAltarT2 = Advancement.Builder.advancement()
                .parent(foundRockCrystals)
                .display(BlocksAS.ALTAR_ATTUNEMENT, title("craft_t2_altar"), description("craft_t2_altar"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("altar_craft_t2_altar", AltarRecipeInstance.withOutput(BlocksAS.ALTAR_ATTUNEMENT))
                .save(registrar, AstralSorcery.key("craft_t2_altar").toString());
        AdvancementHolder craftAltarT3 = Advancement.Builder.advancement()
                .parent(craftAltarT2)
                .display(BlocksAS.ALTAR_CONSTELLATION, title("craft_t3_altar"), description("craft_t3_altar"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("altar_craft_t3_altar", AltarRecipeInstance.withOutput(BlocksAS.ALTAR_CONSTELLATION))
                .save(registrar, AstralSorcery.key("craft_t3_altar").toString());
        AdvancementHolder craftAltarT4 = Advancement.Builder.advancement()
                .parent(craftAltarT3)
                .display(BlocksAS.ALTAR_CONSTELLATION, title("craft_t4_altar"), description("craft_t4_altar"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("altar_craft_t4_altar", AltarRecipeInstance.withOutput(BlocksAS.ALTAR_RADIANCE))
                .save(registrar, AstralSorcery.key("craft_t4_altar").toString());

        AdvancementHolder findAnyConstellation = Advancement.Builder.advancement()
                .parent(root)
                .display(BlocksAS.TELESCOPE, title("find_constellation"), description("find_constellation"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("any_constellation_discovered", ConstellationInstance.any(AdvancementsAS.DISCOVER_CONSTELLATION))
                .save(registrar, AstralSorcery.key("find_constellation").toString());
        AdvancementHolder findWeakConstellation = Advancement.Builder.advancement()
                .parent(findAnyConstellation)
                .display(BlocksAS.TELESCOPE, title("find_weak_constellation"), description("find_weak_constellation"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("weak_constellation_discovered", ConstellationInstance.anyWeak(AdvancementsAS.DISCOVER_CONSTELLATION))
                .save(registrar, AstralSorcery.key("find_weak_constellation").toString());
        AdvancementHolder findMinorConstellation = Advancement.Builder.advancement()
                .parent(findWeakConstellation)
                .display(BlocksAS.OBSERVATORY, title("find_minor_constellation"), description("find_minor_constellation"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("minor_constellation_discovered", ConstellationInstance.anyMinor(AdvancementsAS.DISCOVER_CONSTELLATION))
                .save(registrar, AstralSorcery.key("find_minor_constellation").toString());

        AdvancementHolder attuneSelf = Advancement.Builder.advancement()
                .parent(findAnyConstellation)
                .display(BlocksAS.ATTUNEMENT_ALTAR, title("attune_self"), description("attune_self"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("attune_self", ConstellationInstance.any(AdvancementsAS.ATTUNE_SELF))
                .save(registrar, AstralSorcery.key("attune_self").toString());
        AdvancementHolder attuneCrystal = Advancement.Builder.advancement()
                .parent(attuneSelf)
                .display(BlocksAS.RITUAL_PEDESTAL, title("attune_crystal"), description("attune_crystal"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("attune_crystal", ConstellationInstance.anyWeak(AdvancementsAS.ATTUNE_CRYSTAL))
                .save(registrar, AstralSorcery.key("attune_crystal").toString());
        AdvancementHolder attuneCrystalTrait = Advancement.Builder.advancement()
                .parent(attuneCrystal)
                .display(BlocksAS.RITUAL_PEDESTAL, title("attune_trait"), description("attune_trait"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("attune_trait", ConstellationInstance.anyMinor(AdvancementsAS.ATTUNE_CRYSTAL))
                .save(registrar, AstralSorcery.key("attune_trait").toString());

        AdvancementHolder perkLevelSmall = Advancement.Builder.advancement()
                .parent(attuneSelf)
                .display(BlocksAS.SPECTRAL_RELAY, title("perk_level_small"), description("perk_level_small"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("gain_perk_level_small", PerkLevelInstance.reachLevel(10))
                .save(registrar, AstralSorcery.key("perk_level_small").toString());
        AdvancementHolder perkLevelMedium = Advancement.Builder.advancement()
                .parent(perkLevelSmall)
                .display(BlocksAS.SPECTRAL_RELAY, title("perk_level_medium"), description("perk_level_medium"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("gain_perk_level_medium", PerkLevelInstance.reachLevel(25))
                .save(registrar, AstralSorcery.key("perk_level_medium").toString());
        AdvancementHolder perkLevelLarge = Advancement.Builder.advancement()
                .parent(perkLevelMedium)
                .display(BlocksAS.SPECTRAL_RELAY, title("perk_level_large"), description("perk_level_large"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("gain_perk_level_large", PerkLevelInstance.reachLevel(40))
                .save(registrar, AstralSorcery.key("perk_level_large").toString());
    }
}
