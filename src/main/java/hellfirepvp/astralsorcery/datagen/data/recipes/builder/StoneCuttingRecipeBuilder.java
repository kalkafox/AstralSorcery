/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StoneCuttingRecipeBuilder
 * Created by HellFirePvP
 * Date: 22.08.2020 / 16:00
 */
public class StoneCuttingRecipeBuilder {

    private final Ingredient from;
    private final ItemLike output;
    private final int count;

    private StoneCuttingRecipeBuilder(Ingredient from, ItemLike output, int count) {
        this.from = from;
        this.output = output;
        this.count = count;
    }

    public static StoneCuttingRecipeBuilder stoneCuttingRecipe(Ingredient from, ItemLike output) {
        return stoneCuttingRecipe(from, output, 1);
    }

    public static StoneCuttingRecipeBuilder stoneCuttingRecipe(Ingredient from, ItemLike output, int count) {
        return new StoneCuttingRecipeBuilder(from, output, count);
    }

    public void build(RecipeOutput recipeOutput) {
        this.build(recipeOutput, BuiltInRegistries.ITEM.getKey(this.output.asItem()));
    }

    public void build(RecipeOutput recipeOutput, ResourceLocation id) {
        id = NameUtil.prefixPath(id, "stonecutting/");
        recipeOutput.accept(id, new StonecutterRecipe("", this.from, new ItemStack(this.output.asItem(), this.count)), null);
    }
}
