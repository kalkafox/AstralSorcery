/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.loot;

import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.loot.*;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.world.item.Items;
import net.minecraft.loot.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.stream.Collectors;

import static hellfirepvp.astralsorcery.common.lib.BlocksAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockLootTableProvider
 * Created by HellFirePvP
 * Date: 06.03.2020 / 21:50
 */
public class BlockLootTableProvider extends BlockLoot {

    @Override
    protected void addTables() {
        this.dropSelf(MARBLE_ARCH);
        this.dropSelf(MARBLE_BRICKS);
        this.dropSelf(MARBLE_CHISELED);
        this.dropSelf(MARBLE_ENGRAVED);
        this.dropSelf(MARBLE_PILLAR);
        this.dropSelf(MARBLE_RAW);
        this.dropSelf(MARBLE_RUNED);
        this.dropSelf(MARBLE_STAIRS);
        this.add(MARBLE_SLAB, BlockLoot::createSlabItemTable);
        this.dropSelf(BLACK_MARBLE_ARCH);
        this.dropSelf(BLACK_MARBLE_BRICKS);
        this.dropSelf(BLACK_MARBLE_CHISELED);
        this.dropSelf(BLACK_MARBLE_ENGRAVED);
        this.dropSelf(BLACK_MARBLE_PILLAR);
        this.dropSelf(BLACK_MARBLE_RAW);
        this.dropSelf(BLACK_MARBLE_RUNED);
        this.dropSelf(BLACK_MARBLE_STAIRS);
        this.add(BLACK_MARBLE_SLAB, BlockLoot::createSlabItemTable);
        this.dropSelf(INFUSED_WOOD);
        this.dropSelf(INFUSED_WOOD_ARCH);
        this.dropSelf(INFUSED_WOOD_COLUMN);
        this.dropSelf(INFUSED_WOOD_ENGRAVED);
        this.dropSelf(INFUSED_WOOD_ENRICHED);
        this.dropSelf(INFUSED_WOOD_INFUSED);
        this.dropSelf(INFUSED_WOOD_PLANKS);
        this.dropSelf(INFUSED_WOOD_STAIRS);
        this.add(INFUSED_WOOD_SLAB, BlockLoot::createSlabItemTable);

        this.add(AQUAMARINE_SAND_ORE, (block) -> {
            return droppingWithSilkTouch(block,
                    LootItem.builder(ItemsAS.AQUAMARINE)
                            .apply(SetItemCountFunction.builder(RandomValueBounds.of(1F, 3F)))
                            .apply(LinearLuckBonus.builder())
                            .apply(ApplyExplosionDecay.builder())
            );
        });
        this.add(ROCK_CRYSTAL_ORE, (block) -> {
            return LootTable.builder()
                    .addLootPool(LootPool.builder()
                            .rolls(RandomValueBounds.of(2F, 5F))
                            .addEntry(LootItem.builder(ItemsAS.ROCK_CRYSTAL)
                                    .apply(RandomCrystalProperty.builder())
                                    .apply(ApplyExplosionDecay.builder())
                            )
                    );
        });
        this.dropSelf(STARMETAL_ORE);
        this.dropSelf(STARMETAL);
        this.add(GLOW_FLOWER, (block) -> {
            return createDoublePlantWithSeedDrops(block,
                    LootItem.builder(Items.GLOWSTONE_DUST)
                            .apply(SetItemCountFunction.builder(RandomValueBounds.of(2F, 4F)))
                            .apply(LinearLuckBonus.builder())
                            .apply(ApplyExplosionDecay.builder())
            );
        });

        this.dropSelf(SPECTRAL_RELAY);
        this.dropSelf(ALTAR_DISCOVERY);
        this.dropSelf(ALTAR_ATTUNEMENT);
        this.dropSelf(ALTAR_CONSTELLATION);
        this.dropSelf(ALTAR_RADIANCE);
        this.dropSelf(ATTUNEMENT_ALTAR);

        this.add(CELESTIAL_CRYSTAL_CLUSTER, (block) -> {
            return LootTable.builder()
                    .apply(ApplyExplosionDecay.builder())
                    .apply(CopyCrystalProperties.builder())
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantIntValue.of(1))
                            .addEntry(LootItem.builder(ItemsAS.CELESTIAL_CRYSTAL)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withIntProp(BlockCelestialCrystalCluster.STAGE, 4)))
                            )
                    )
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantIntValue.of(1))
                            .addEntry(LootItem.builder(ItemsAS.STARDUST)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withIntProp(BlockCelestialCrystalCluster.STAGE, 1)))
                            )
                    )
                    .addLootPool(LootPool.builder()
                            .rolls(RandomValueBounds.of(1F, 2F))
                            .addEntry(LootItem.builder(ItemsAS.STARDUST)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withIntProp(BlockCelestialCrystalCluster.STAGE, 2)))
                            )
                    )
                    .addLootPool(LootPool.builder()
                            .rolls(RandomValueBounds.of(1F, 2F))
                            .addEntry(LootItem.builder(ItemsAS.STARDUST)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withIntProp(BlockCelestialCrystalCluster.STAGE, 3)))
                            )
                    )
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantIntValue.of(2))
                            .addEntry(LootItem.builder(ItemsAS.STARDUST)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withIntProp(BlockCelestialCrystalCluster.STAGE, 4)))
                            )
                    );
        });
        this.add(GEM_CRYSTAL_CLUSTER, (block) -> {
            return LootTable.builder()
                    .apply(ApplyExplosionDecay.builder())
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantIntValue.of(1))
                            .addEntry(LootItem.builder(ItemsAS.PERK_GEM_DAY)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(GEM_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withProp(BlockGemCrystalCluster.STAGE, BlockGemCrystalCluster.GrowthStageType.STAGE_2_DAY)))
                            )
                    )
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantIntValue.of(1))
                            .addEntry(LootItem.builder(ItemsAS.PERK_GEM_NIGHT)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(GEM_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withProp(BlockGemCrystalCluster.STAGE, BlockGemCrystalCluster.GrowthStageType.STAGE_2_NIGHT)))
                            )
                    )
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantIntValue.of(1))
                            .addEntry(LootItem.builder(ItemsAS.PERK_GEM_SKY)
                                    .acceptCondition(LootItemBlockStatePropertyCondition.builder(GEM_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .withProp(BlockGemCrystalCluster.STAGE, BlockGemCrystalCluster.GrowthStageType.STAGE_2_SKY)))
                            )
                    );
        });
        this.add(ROCK_COLLECTOR_CRYSTAL, createLeavesDrops(ROCK_COLLECTOR_CRYSTAL)
                .apply(CopyCrystalProperties.builder())
                .apply(CopyConstellation.builder()));
        this.add(CELESTIAL_COLLECTOR_CRYSTAL, createLeavesDrops(CELESTIAL_COLLECTOR_CRYSTAL)
                .apply(CopyCrystalProperties.builder())
                .apply(CopyConstellation.builder()));
        this.add(LENS, createLeavesDrops(LENS)
                .apply(CopyCrystalProperties.builder()));
        this.add(PRISM, createLeavesDrops(PRISM)
                .apply(CopyCrystalProperties.builder()));
        this.dropSelf(RITUAL_LINK);
        this.dropSelf(RITUAL_PEDESTAL);
        this.dropSelf(ILLUMINATOR);
        this.dropSelf(INFUSER);
        this.dropSelf(CHALICE);
        this.dropSelf(WELL);
        this.dropSelf(TELESCOPE);
        this.dropSelf(OBSERVATORY);
        this.dropSelf(REFRACTION_TABLE);
        this.dropSelf(TREE_BEACON);
        this.add(TREE_BEACON_COMPONENT, LootTable.builder());
        this.add(GATEWAY, BlockLoot.createNameableBlockEntityTable(GATEWAY)
                .apply(CopyGatewayColor.builder()));
        this.dropSelf(FOUNTAIN);
        this.dropSelf(FOUNTAIN_PRIME_LIQUID);
        this.dropSelf(FOUNTAIN_PRIME_VORTEX);
        this.dropSelf(FOUNTAIN_PRIME_ORE);

        this.add(FLARE_LIGHT, LootTable.builder());
        this.add(TRANSLUCENT_BLOCK, LootTable.builder());
        this.add(VANISHING, LootTable.builder());
        this.add(STRUCTURAL, LootTable.builder());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.getValues().stream()
                .filter(Mods.ASTRAL_SORCERY::owns)
                .collect(Collectors.toList());
    }
}
