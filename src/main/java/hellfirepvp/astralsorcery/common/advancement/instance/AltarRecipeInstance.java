/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.advancement.AltarCraftTrigger;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeInstance
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:28
 */
public class AltarRecipeInstance extends AbstractCriterionTriggerInstance {

    private final Set<ResourceLocation> recipeNames = new HashSet<>();
    private final List<Ingredient> recipeOutputs = new ArrayList<>();

    private AltarRecipeInstance(ResourceLocation id) {
        super(id, EntityPredicate.AndPredicate.ANY);
    }

    public static AltarRecipeInstance craftRecipe(ResourceLocation... recipeIds) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID);
        instance.recipeNames.addAll(Arrays.asList(recipeIds));
        return instance;
    }

    public static AltarRecipeInstance craftRecipe(SimpleAltarRecipe... recipes) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID);
        Arrays.asList(recipes).forEach(recipe -> instance.recipeNames.add(recipe.getId()));
        return instance;
    }

    public static AltarRecipeInstance withOutput(ItemLike... outputs) {
        return withOutput(Ingredient.valueFromJson(outputs));
    }

    public static AltarRecipeInstance withOutput(ItemStack... outputs) {
        return withOutput(Ingredient.fromStacks(outputs));
    }

    public static AltarRecipeInstance withOutput(Tag<Item>... outputs) {
        return withOutput(Arrays.stream(outputs).map(Ingredient::fromTag).collect(Collectors.toList()));
    }

    public static AltarRecipeInstance withOutput(Ingredient... outputs) {
        return withOutput(Arrays.asList(outputs));
    }

    public static AltarRecipeInstance withOutput(List<Ingredient> outputs) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID);
        instance.recipeOutputs.addAll(outputs);
        return instance;
    }

    @Override
    public JsonObject serialize(SerializationContext conditions) {
        JsonObject out = super.serialize(conditions);
        if (!this.recipeNames.isEmpty()) {
            JsonArray names = new JsonArray();
            for (ResourceLocation name : this.recipeNames) {
                names.add(name.toString());
            }
            out.add("recipeNames", names);
        }
        if (!this.recipeOutputs.isEmpty()) {
            JsonArray outputs = new JsonArray();
            for (Ingredient output : this.recipeOutputs) {
                outputs.add(output.serialize());
            }
            out.add("recipeOutputs", outputs);
        }
        return out;
    }

    public static AltarRecipeInstance deserialize(ResourceLocation id, JsonObject json) {
        AltarRecipeInstance instance = new AltarRecipeInstance(id);
        JsonArray recipeNames = GsonHelper.getAsJsonArray(json, "recipeNames", new JsonArray());
        for (int idx = 0; idx < recipeNames.size(); idx++) {
            JsonElement value = recipeNames.get(idx);
            String key = GsonHelper.getString(value, String.format("recipeNames[%s]", idx));
            instance.recipeNames.add(ResourceLocation.parse(key));
        }
        for (JsonElement value : GsonHelper.getAsJsonArray(json, "recipeOutputs", new JsonArray())) {
            instance.recipeOutputs.add(Ingredient.deserialize(value));
        }
        return instance;
    }

    public boolean test(SimpleAltarRecipe recipe, ItemStack output) {
        if (this.recipeNames.isEmpty() && this.recipeOutputs.isEmpty()) {
            return true;
        }
        ResourceLocation recipeName = recipe.getId();
        if (this.recipeNames.contains(recipeName)) {
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
