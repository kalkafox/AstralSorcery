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
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.awt.*;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellRecipeSerializer
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:29
 */
public class WellRecipeSerializer extends CustomRecipeSerializer<WellLiquefaction> {

    public WellRecipeSerializer() {
        super(RecipeSerializersAS.WELL_LIQUEFACTION);
    }

    @Override
    public WellLiquefaction read(ResourceLocation recipeId, JsonObject json) {
        Ingredient from = Ingredient.deserialize(GsonHelper.getAsJsonObject(json, "input"));
        String fluidKey = GsonHelper.getAsString(json, "output");
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidKey));
        if (fluid == null) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidKey);
        }
        float productionMultiplier = GsonHelper.getAsFloat(json, "productionMultiplier");
        float shatterMultiplier = GsonHelper.getAsFloat(json, "shatterMultiplier");
        Color color = null;
        if (json.has("color")) {
            color = JsonHelper.getColor(json, "color");
        }
        return new WellLiquefaction(recipeId, from, fluid, color, productionMultiplier, shatterMultiplier);
    }

    @Override
    public WellLiquefaction read(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient from = Ingredient.read(buffer);
        Fluid fluid = ByteBufUtils.readRegistryEntry(buffer);
        float shatter = buffer.readFloat();
        float production = buffer.readFloat();
        Color color = ByteBufUtils.readOptional(buffer, buf -> new Color(buf.readInt(), true));
        return new WellLiquefaction(recipeId, from, fluid, color, production, shatter);
    }

    @Override
    public void write(FriendlyByteBuf buffer, WellLiquefaction recipe) {
        recipe.getInput().write(buffer);
        ByteBufUtils.writeRegistryEntry(buffer, recipe.getFluidOutput());
        buffer.writeFloat(recipe.getShatterMultiplier());
        buffer.writeFloat(recipe.getProductionMultiplier());
        ByteBufUtils.writeOptional(buffer, recipe.getCatalystColor(), (buf, color) -> buf.writeInt(color.getRGB()));
    }

    @Override
    public void write(JsonObject object, WellLiquefaction recipe) {
        object.add("input", recipe.getInput().serialize());
        object.addProperty("output", RegistryHelper.getKey(recipe.getFluidOutput()).toString());
        object.addProperty("productionMultiplier", recipe.getProductionMultiplier());
        object.addProperty("shatterMultiplier", recipe.getShatterMultiplier());
        object.addProperty("color", recipe.getCatalystColor() == null ? Color.WHITE.getRGB() : recipe.getCatalystColor().getRGB());
    }
}
