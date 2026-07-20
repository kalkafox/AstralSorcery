/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarAttunement;
import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
import hellfirepvp.astralsorcery.common.container.ContainerAltarTrait;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomMatcherRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.integration.jei.*;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationJEI
 * Created by HellFirePvP
 * Date: 25.07.2020 / 09:23
 */
@JeiPlugin
public class IntegrationJEI implements IModPlugin {

    public static final List<JEICategory<?>> CATEGORIES = new ArrayList<>();

    public static final RecipeType<SimpleAltarRecipe> TYPE_ALTAR_DISCOVERY = RecipeType.create(AstralSorcery.MODID, "altar_discovery", SimpleAltarRecipe.class);
    public static final RecipeType<SimpleAltarRecipe> TYPE_ALTAR_ATTUNEMENT = RecipeType.create(AstralSorcery.MODID, "altar_attunement", SimpleAltarRecipe.class);
    public static final RecipeType<SimpleAltarRecipe> TYPE_ALTAR_CONSTELLATION = RecipeType.create(AstralSorcery.MODID, "altar_constellation", SimpleAltarRecipe.class);
    public static final RecipeType<SimpleAltarRecipe> TYPE_ALTAR_TRAIT = RecipeType.create(AstralSorcery.MODID, "altar_trait", SimpleAltarRecipe.class);
    public static final RecipeType<LiquidInfusion> TYPE_INFUSER = RecipeType.create(AstralSorcery.MODID, "infuser", LiquidInfusion.class);
    public static final RecipeType<LiquidInteraction> TYPE_LIQUID_INTERACTION = RecipeType.create(AstralSorcery.MODID, "interaction", LiquidInteraction.class);
    public static final RecipeType<BlockTransmutation> TYPE_TRANSMUTATION = RecipeType.create(AstralSorcery.MODID, "transmutation", BlockTransmutation.class);
    public static final RecipeType<WellLiquefaction> TYPE_WELL = RecipeType.create(AstralSorcery.MODID, "well", WellLiquefaction.class);

    public static IJeiRuntime runtime = null;

    private static final ISubtypeInterpreter<ItemStack> PERSISTENT_DATA_SUBTYPE = new ISubtypeInterpreter<>() {
        @Nullable
        @Override
        public Object getSubtypeData(ItemStack stack, UidContext context) {
            return NBTHelper.hasPersistentData(stack) ? NBTHelper.getPersistentData(stack.copy()) : null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
            return NBTHelper.hasPersistentData(stack) ? NBTHelper.getPersistentData(stack.copy()).toString() : "";
        }
    };

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        for (Item item : new Item[] {
                ItemsAS.ATTUNED_ROCK_CRYSTAL,
                ItemsAS.ATTUNED_CELESTIAL_CRYSTAL,
                BlocksAS.ROCK_COLLECTOR_CRYSTAL.asItem(),
                BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.asItem(),
                BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.asItem(),
                BlocksAS.GEM_CRYSTAL_CLUSTER.asItem()
        }) {
            registry.registerSubtypeInterpreter(item, PERSISTENT_DATA_SUBTYPE);
        }

        registry.registerSubtypeInterpreter(ItemsAS.RESONATOR, stringSubtype(stack -> ItemResonator.getUpgrades(stack)
                .stream()
                .map(ItemResonator.ResonatorUpgrade::getAppendix)
                .collect(Collectors.joining(","))));
        registry.registerSubtypeInterpreter(ItemsAS.MANTLE, stringSubtype(stack -> Optional.ofNullable(ItemsAS.MANTLE.getConstellation(stack))
                .map(IConstellation::getName)
                .orElse("none")));
    }

    private static ISubtypeInterpreter<ItemStack> stringSubtype(java.util.function.Function<ItemStack, String> fn) {
        return new ISubtypeInterpreter<>() {
            @Nullable
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return fn.apply(stack);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                return fn.apply(stack);
            }
        };
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        CATEGORIES.clear();
        CATEGORIES.add(new CategoryAltar(TYPE_ALTAR_DISCOVERY, "altar_discovery", BlocksAS.ALTAR_DISCOVERY, guiHelper));
        CATEGORIES.add(new CategoryAltar(TYPE_ALTAR_ATTUNEMENT, "altar_attunement", BlocksAS.ALTAR_ATTUNEMENT, guiHelper));
        CATEGORIES.add(new CategoryAltar(TYPE_ALTAR_CONSTELLATION, "altar_constellation", BlocksAS.ALTAR_CONSTELLATION, guiHelper));
        CATEGORIES.add(new CategoryAltar(TYPE_ALTAR_TRAIT, "altar_trait", BlocksAS.ALTAR_RADIANCE, guiHelper));
        CATEGORIES.add(new CategoryInfuser(guiHelper));
        CATEGORIES.add(new CategoryLiquidInteraction(guiHelper));
        CATEGORIES.add(new CategoryTransmutation(guiHelper));
        CATEGORIES.add(new CategoryWell(guiHelper));

        CATEGORIES.forEach(registry::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        CATEGORIES.forEach(category -> registerCategoryRecipes(registry, category));
    }

    private static <T extends CustomMatcherRecipe> void registerCategoryRecipes(IRecipeRegistration registry, JEICategory<T> category) {
        List<T> recipes = new ArrayList<>(category.getRecipes());
        recipes.sort(Comparator.comparing(recipe -> recipe.getId().toString()));
        registry.addRecipes(category.getRecipeType(), recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.ALTAR_DISCOVERY), TYPE_ALTAR_DISCOVERY);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.ALTAR_ATTUNEMENT), TYPE_ALTAR_ATTUNEMENT);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.ALTAR_CONSTELLATION), TYPE_ALTAR_CONSTELLATION);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.ALTAR_RADIANCE), TYPE_ALTAR_TRAIT);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.INFUSER), TYPE_INFUSER);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.CHALICE), TYPE_LIQUID_INTERACTION);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.LENS), TYPE_TRANSMUTATION);
        registry.addRecipeCatalyst(new ItemStack(BlocksAS.WELL), TYPE_WELL);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        // Every altar container can craft its own tier's recipes and every tier below it;
        // canHandle() on the transfer info re-checks the tier of the concrete recipe.
        for (RecipeType<SimpleAltarRecipe> altarRecipeType : List.of(
                TYPE_ALTAR_DISCOVERY, TYPE_ALTAR_ATTUNEMENT, TYPE_ALTAR_CONSTELLATION, TYPE_ALTAR_TRAIT)) {
            registry.addRecipeTransferHandler(new AltarRecipeTransferInfo<>(ContainerAltarDiscovery.class, altarRecipeType));
            registry.addRecipeTransferHandler(new AltarRecipeTransferInfo<>(ContainerAltarAttunement.class, altarRecipeType));
            registry.addRecipeTransferHandler(new AltarRecipeTransferInfo<>(ContainerAltarConstellation.class, altarRecipeType));
            registry.addRecipeTransferHandler(new AltarRecipeTransferInfo<>(ContainerAltarTrait.class, altarRecipeType));
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return AstralSorcery.key("jei_integration");
    }
}
