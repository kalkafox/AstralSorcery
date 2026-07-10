/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.registries.ForgeRegistries;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInfusionSerializer
 * Created by HellFirePvP
 * Date: 26.07.2019 / 21:30
 */
public class LiquidInfusionSerializer extends CustomRecipeSerializer<LiquidInfusion> {

    public LiquidInfusionSerializer() {
        super(RecipeSerializersAS.LIQUID_INFUSION);
    }

    @Override
    public LiquidInfusion read(ResourceLocation recipeId, JsonObject json) {
        ResourceLocation fluidKey = ResourceLocation.parse(GsonHelper.getString(json, "fluidInput"));
        Fluid fluidInput = BuiltInRegistries.FLUID.get(fluidKey);
        if (fluidInput == null || fluidInput == Fluids.EMPTY) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidKey);
        }

        Ingredient from = CraftingHelper.getIngredient(json.get("input"));
        ItemStack output = JsonHelper.getItemStack(json.get("output"), "output");
        float consumptionChance = GsonHelper.getFloat(json, "consumptionChance");
        int duration = GsonHelper.getInt(json, "duration");

        boolean consumeMultipleFluids = GsonHelper.getBoolean(json, "consumeMultipleFluids", false);
        boolean acceptChaliceInput = GsonHelper.getBoolean(json, "acceptChaliceInput", true);
        boolean copyNBTToOutputs = GsonHelper.getBoolean(json, "copyNBTToOutputs", false);
        return new LiquidInfusion(recipeId, duration, fluidInput, from, output, consumptionChance, consumeMultipleFluids, acceptChaliceInput, copyNBTToOutputs);
    }

    @Override
    public LiquidInfusion read(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return LiquidInfusion.read(recipeId, buffer);
    }

    @Override
    public void write(JsonObject object, LiquidInfusion recipe) {
        recipe.write(object);
    }

    @Override
    public void write(FriendlyByteBuf buffer, LiquidInfusion recipe) {
        recipe.write(buffer);
    }
}
