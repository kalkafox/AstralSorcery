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
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Set;
import java.util.stream.Collectors;

import static hellfirepvp.astralsorcery.common.lib.BlocksAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockLootTableProvider
 * Created by HellFirePvP
 * Date: 06.03.2020 / 21:50
 */
public class BlockLootTableProvider extends BlockLootSubProvider {

    public BlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        this.dropSelf(MARBLE_ARCH);
        this.dropSelf(MARBLE_BRICKS);
        this.dropSelf(MARBLE_CHISELED);
        this.dropSelf(MARBLE_ENGRAVED);
        this.dropSelf(MARBLE_PILLAR);
        this.dropSelf(MARBLE_RAW);
        this.dropSelf(MARBLE_RUNED);
        this.dropSelf(MARBLE_STAIRS);
        this.add(MARBLE_SLAB, this::createSlabItemTable);
        this.dropSelf(BLACK_MARBLE_ARCH);
        this.dropSelf(BLACK_MARBLE_BRICKS);
        this.dropSelf(BLACK_MARBLE_CHISELED);
        this.dropSelf(BLACK_MARBLE_ENGRAVED);
        this.dropSelf(BLACK_MARBLE_PILLAR);
        this.dropSelf(BLACK_MARBLE_RAW);
        this.dropSelf(BLACK_MARBLE_RUNED);
        this.dropSelf(BLACK_MARBLE_STAIRS);
        this.add(BLACK_MARBLE_SLAB, this::createSlabItemTable);
        this.dropSelf(INFUSED_WOOD);
        this.dropSelf(INFUSED_WOOD_ARCH);
        this.dropSelf(INFUSED_WOOD_COLUMN);
        this.dropSelf(INFUSED_WOOD_ENGRAVED);
        this.dropSelf(INFUSED_WOOD_ENRICHED);
        this.dropSelf(INFUSED_WOOD_INFUSED);
        this.dropSelf(INFUSED_WOOD_PLANKS);
        this.dropSelf(INFUSED_WOOD_STAIRS);
        this.add(INFUSED_WOOD_SLAB, this::createSlabItemTable);

        this.add(AQUAMARINE_SAND_ORE, (block) -> {
            return createSilkTouchDispatchTable(block,
                    (LootPoolEntryContainer.Builder<?>) LootItem.lootTableItem(ItemsAS.AQUAMARINE)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1F, 3F)))
                            .apply(LinearLuckBonus.builder())
                            .apply(ApplyExplosionDecay.explosionDecay())
            );
        });
        this.add(ROCK_CRYSTAL_ORE, (block) -> {
            return LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(2F, 5F))
                            .add(LootItem.lootTableItem(ItemsAS.ROCK_CRYSTAL)
                                    .apply(RandomCrystalProperty.builder())
                                    .apply(ApplyExplosionDecay.explosionDecay())
                            )
                    );
        });
        this.dropSelf(STARMETAL_ORE);
        this.dropSelf(STARMETAL);
        this.add(GLOW_FLOWER, (block) -> {
            return LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .when(HAS_SHEARS)
                            .add(LootItem.lootTableItem(Items.GLOWSTONE_DUST)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2F, 4F)))
                                    .apply(LinearLuckBonus.builder())
                                    .apply(ApplyExplosionDecay.explosionDecay())
                            )
                    );
        });

        this.dropSelf(SPECTRAL_RELAY);
        this.dropSelf(ALTAR_DISCOVERY);
        this.dropSelf(ALTAR_ATTUNEMENT);
        this.dropSelf(ALTAR_CONSTELLATION);
        this.dropSelf(ALTAR_RADIANCE);
        this.dropSelf(ATTUNEMENT_ALTAR);

        this.add(CELESTIAL_CRYSTAL_CLUSTER, (block) -> {
            return LootTable.lootTable()
                    .apply(ApplyExplosionDecay.explosionDecay())
                    .apply(CopyCrystalProperties.builder())
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.CELESTIAL_CRYSTAL)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockCelestialCrystalCluster.STAGE, 4)))
                            )
                    )
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockCelestialCrystalCluster.STAGE, 1)))
                            )
                    )
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1F, 2F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockCelestialCrystalCluster.STAGE, 2)))
                            )
                    )
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1F, 2F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockCelestialCrystalCluster.STAGE, 3)))
                            )
                    )
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(2.0F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(CELESTIAL_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockCelestialCrystalCluster.STAGE, 4)))
                            )
                    );
        });
        this.add(GEM_CRYSTAL_CLUSTER, (block) -> {
            return LootTable.lootTable()
                    .apply(ApplyExplosionDecay.explosionDecay())
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.PERK_GEM_DAY)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(GEM_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockGemCrystalCluster.STAGE, BlockGemCrystalCluster.GrowthStageType.STAGE_2_DAY)))
                            )
                    )
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.PERK_GEM_NIGHT)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(GEM_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockGemCrystalCluster.STAGE, BlockGemCrystalCluster.GrowthStageType.STAGE_2_NIGHT)))
                            )
                    )
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.PERK_GEM_SKY)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(GEM_CRYSTAL_CLUSTER)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(BlockGemCrystalCluster.STAGE, BlockGemCrystalCluster.GrowthStageType.STAGE_2_SKY)))
                            )
                    );
        });
        this.add(ROCK_COLLECTOR_CRYSTAL, this.createSingleItemTable(ROCK_COLLECTOR_CRYSTAL)
                .apply(CopyCrystalProperties.builder())
                .apply(CopyConstellation.builder()));
        this.add(CELESTIAL_COLLECTOR_CRYSTAL, this.createSingleItemTable(CELESTIAL_COLLECTOR_CRYSTAL)
                .apply(CopyCrystalProperties.builder())
                .apply(CopyConstellation.builder()));
        this.add(LENS, this.createSingleItemTable(LENS)
                .apply(CopyCrystalProperties.builder()));
        this.add(PRISM, this.createSingleItemTable(PRISM)
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
        this.add(TREE_BEACON_COMPONENT, LootTable.lootTable());
        this.add(GATEWAY, this.createNameableBlockEntityTable(GATEWAY)
                .apply(CopyGatewayColor.builder()));
        this.dropSelf(FOUNTAIN);
        this.dropSelf(FOUNTAIN_PRIME_LIQUID);
        this.dropSelf(FOUNTAIN_PRIME_VORTEX);
        this.dropSelf(FOUNTAIN_PRIME_ORE);

        this.add(FLARE_LIGHT, LootTable.lootTable());
        this.add(TRANSLUCENT_BLOCK, LootTable.lootTable());
        this.add(VANISHING, LootTable.lootTable());
        this.add(STRUCTURAL, LootTable.lootTable());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(Mods.ASTRAL_SORCERY::owns)
                .collect(Collectors.toList());
    }
}
