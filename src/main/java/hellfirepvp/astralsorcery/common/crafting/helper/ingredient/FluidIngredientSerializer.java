/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.IIngredientSerializer;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidIngredientSerializer
 * Created by HellFirePvP
 * Date: 30.05.2019 / 17:39
 */
public class FluidIngredientSerializer implements IIngredientSerializer<FluidIngredient> {

    @Override
    public FluidIngredient parse(JsonObject json) {
        if (!json.has("fluid")) {
            throw new JsonSyntaxException("Expected an array at 'fluid' or a single fluid defined at key 'fluid'.");
        }

        List<FluidStack> foundFluids = new ArrayList<>();
        JsonElement value = json.get("fluid");
        if (value.convertToDouble()) {
            value.getAsJsonArray().forEach(e -> {
                if (e.isJsonObject()) {
                    JsonObject object = e.getAsJsonObject();
                    ResourceLocation key = ResourceLocation.parse(GsonHelper.getString(object, "fluid"));
                    if (!BuiltInRegistries.FLUID.containsKey(key)) {
                        throw new JsonSyntaxException("Unknown fluid '" + key + "'");
                    }
                    int amount = FluidAttributes.BUCKET_VOLUME;
                    if (object.has("amount")) {
                        amount = GsonHelper.getInt(object, "amount");
                    }
                    Fluid fluid = BuiltInRegistries.FLUID.get(key);
                    foundFluids.add(new FluidStack(fluid, amount));
                } else if (e.convertToLong()) {
                    ResourceLocation key = ResourceLocation.parse(GsonHelper.getString(value, "fluid"));
                    if (!BuiltInRegistries.FLUID.containsKey(key)) {
                        throw new JsonSyntaxException("Unknown fluid '" + key + "'");
                    }
                    Fluid fluid = BuiltInRegistries.FLUID.get(key);
                    foundFluids.add(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
                } else {
                    throw new JsonSyntaxException("Value at key 'fluid' has to be a fluid name or an array of fluid names or objects containing 'fluid'.");
                }
            });
        } else if (value.convertToLong()) {
            ResourceLocation key = ResourceLocation.parse(GsonHelper.getString(value, "fluid"));
            if (!BuiltInRegistries.FLUID.containsKey(key)) {
                throw new JsonSyntaxException("Unknown fluid '" + key + "'");
            }
            int amount = FluidAttributes.BUCKET_VOLUME;
            if (json.has("amount")) {
                amount = GsonHelper.getInt(json, "amount");
            }
            Fluid fluid = BuiltInRegistries.FLUID.get(key);
            foundFluids.add(new FluidStack(fluid, amount));
        } else {
            throw new JsonSyntaxException("Value at key 'fluid' has to be a fluid name or an array of fluid names or objects containing 'fluid'.");
        }
        return new FluidIngredient(foundFluids);
    }

    @Override
    public FluidIngredient parse(FriendlyByteBuf buffer) {
        return new FluidIngredient(ByteBufUtils.readList(buffer, ByteBufUtils::readFluidStack));
    }

    @Override
    public void write(FriendlyByteBuf buffer, FluidIngredient ingredient) {
        ByteBufUtils.writeCollection(buffer, ingredient.getFluids(), ByteBufUtils::writeFluidStack);
    }

}
