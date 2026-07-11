/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.helper.IngredientIO;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeTypeHandler;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleAltarRecipeSerializer
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:45
 */
public class SimpleAltarRecipeSerializer extends CustomRecipeSerializer<SimpleAltarRecipe> {

    public SimpleAltarRecipeSerializer() {
        super(RecipeSerializersAS.SIMPLE_ALTAR_CRAFTING);
    }

    @Override
    public SimpleAltarRecipe read(JsonObject json) {
        ResourceLocation recipeId = generateDynamicId();
        int id = GsonHelper.getAsInt(json, "altar_type");
        AltarType type = MiscUtils.getEnumEntry(AltarType.class, id);
        int duration = GsonHelper.getAsInt(json, "duration");
        int starlightRequirement = GsonHelper.getAsInt(json, "starlight");

        AltarRecipeGrid grid = AltarRecipeGrid.deserialize(type, json);
        grid.validate(type);

        SimpleAltarRecipe recipe = new SimpleAltarRecipe(recipeId, type, duration, starlightRequirement, grid);
        if (json.has("recipe_class")) {
            ResourceLocation key = ResourceLocation.parse(GsonHelper.getAsString(json, "recipe_class"));
            recipe = AltarRecipeTypeHandler.convert(recipe, key);
            recipe.setCustomRecipeType(key);
        }

        if (GsonHelper.isArrayNode(json, "output")) {
            JsonArray outputArray = GsonHelper.getAsJsonArray(json, "output");
            for (int i = 0; i < outputArray.size(); i++) {
                recipe.addOutput(JsonHelper.getItemStack(outputArray.get(i), String.format("output[%s]", i)));
            }
        } else {
            recipe.addOutput(JsonHelper.getItemStack(json, "output"));
        }

        JsonObject recipeOptions = new JsonObject();
        if (json.has("options")) {
            recipeOptions = GsonHelper.getAsJsonObject(json, "options");
        }
        recipe.deserializeAdditionalJson(recipeOptions);

        if (json.has("focus_constellation")) {
            ResourceLocation key = ResourceLocation.parse(GsonHelper.getAsString(json, "focus_constellation"));
            IConstellation cst = RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(key);
            if (cst == null) {
                throw new JsonSyntaxException("Unknown constellation " + key.toString());
            }
            recipe.setFocusConstellation(cst);
        }

        if (json.has("relay_inputs")) {
            JsonArray relayIngredients = GsonHelper.getAsJsonArray(json, "relay_inputs");
            for (int i = 0; i < relayIngredients.size(); i++) {
                JsonElement value = relayIngredients.get(i);
                Ingredient ingredient = IngredientIO.deserialize(value);
                if (!ingredient.isEmpty()) {
                    recipe.addRelayInput(ingredient);
                } else {
                    AstralSorcery.log.warn("Skipping relay_inputs[" + i + "] for recipe " + recipeId + " as the ingredient has no matching items!");
                    AstralSorcery.log.warn("Ingredient skipped: " + value);
                }
            }
        }

        if (json.has("effects")) {
            JsonArray effectNames = GsonHelper.getAsJsonArray(json, "effects");
            for (int i = 0; i < effectNames.size(); i++) {
                JsonElement value = effectNames.get(i);
                ResourceLocation effectKey = ResourceLocation.parse(GsonHelper.convertToString(value, "effects[" + i + "]"));
                AltarRecipeEffect effect = RegistriesAS.REGISTRY_ALTAR_EFFECTS.getValue(effectKey);
                if (effect == null) {
                    throw new JsonSyntaxException("No altar effect for name " + effectKey + "! (Found at: effects[" + i + "])");
                }
                recipe.addAltarEffect(effect);
            }
        }

        return recipe;
    }

    @Override
    public SimpleAltarRecipe read(RegistryFriendlyByteBuf buffer) {
        return SimpleAltarRecipe.read(generateDynamicId(), buffer);
    }

    @Override
    public void write(JsonObject object, SimpleAltarRecipe recipe) {
        recipe.write(object);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer, SimpleAltarRecipe recipe) {
        recipe.write(buffer);
    }
}
