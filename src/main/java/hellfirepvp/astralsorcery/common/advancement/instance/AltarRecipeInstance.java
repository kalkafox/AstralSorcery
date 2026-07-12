/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement.instance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeInstance
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:28
 */
public record AltarRecipeInstance(Optional<ContextAwarePredicate> player,
                                  List<ResourceLocation> recipeNames,
                                  List<Ingredient> recipeOutputs) implements SimpleCriterionTrigger.SimpleInstance {

    public static final Codec<AltarRecipeInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(AltarRecipeInstance::player),
            ResourceLocation.CODEC.listOf().optionalFieldOf("recipeNames", List.of()).forGetter(AltarRecipeInstance::recipeNames),
            Ingredient.CODEC.listOf().optionalFieldOf("recipeOutputs", List.of()).forGetter(AltarRecipeInstance::recipeOutputs)
    ).apply(inst, AltarRecipeInstance::new));

    public static Criterion<AltarRecipeInstance> craftRecipe(ResourceLocation... recipeIds) {
        return criterion(new AltarRecipeInstance(Optional.empty(), List.of(recipeIds), List.of()));
    }

    public static Criterion<AltarRecipeInstance> craftRecipe(SimpleAltarRecipe... recipes) {
        return criterion(new AltarRecipeInstance(Optional.empty(),
                Arrays.stream(recipes).map(SimpleAltarRecipe::getId).collect(Collectors.toList()),
                List.of()));
    }

    public static Criterion<AltarRecipeInstance> withOutput(ItemLike... outputs) {
        return withOutput(Ingredient.of(outputs));
    }

    public static Criterion<AltarRecipeInstance> withOutput(ItemStack... outputs) {
        return withOutput(Ingredient.of(outputs));
    }

    @SafeVarargs
    public static Criterion<AltarRecipeInstance> withOutput(TagKey<Item>... outputs) {
        return withOutput(Arrays.stream(outputs).map(Ingredient::of).collect(Collectors.toList()));
    }

    public static Criterion<AltarRecipeInstance> withOutput(Ingredient... outputs) {
        return withOutput(Arrays.asList(outputs));
    }

    public static Criterion<AltarRecipeInstance> withOutput(List<Ingredient> outputs) {
        return criterion(new AltarRecipeInstance(Optional.empty(), List.of(), List.copyOf(outputs)));
    }

    private static Criterion<AltarRecipeInstance> criterion(AltarRecipeInstance instance) {
        return AdvancementsAS.ALTAR_CRAFT.createCriterion(instance);
    }

    public boolean test(SimpleAltarRecipe recipe, ItemStack output) {
        if (this.recipeNames.isEmpty() && this.recipeOutputs.isEmpty()) {
            return true;
        }
        if (this.recipeNames.contains(recipe.getId())) {
            return true;
        }
        for (Ingredient i : this.recipeOutputs) {
            if (i.test(output)) {
                return true;
            }
        }
        return false;
    }
}
