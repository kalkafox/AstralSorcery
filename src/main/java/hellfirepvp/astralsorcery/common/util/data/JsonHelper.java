/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.data;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JsonHelper
 * Created by HellFirePvP
 * Date: 19.07.2019 / 21:02
 */
public class JsonHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void parseMultipleStrings(JsonObject root, String key, Consumer<String> consumer) {
        consumeJsonListConfiguration(root, key, "String", "Strings", JsonElement::isJsonPrimitive, JsonElement::getAsString, consumer);
    }

    public static void parseMultipleJsonPrimitives(JsonObject root, String key, String singular, String plural, Consumer<JsonPrimitive> consumer) {
        consumeJsonListConfiguration(root, key, singular, plural, JsonElement::isJsonPrimitive, JsonElement::getAsJsonPrimitive, consumer);
    }

    public static void parseMultipleJsonObjects(JsonObject root, String key, Consumer<JsonObject> consumer) {
        consumeJsonListConfiguration(root, key, "JsonObject", "JsonObjects", JsonElement::isJsonObject, JsonElement::getAsJsonObject, consumer);
    }

    private static <T> void consumeJsonListConfiguration(JsonObject root, String key,
                                                                             String singular, String plural,
                                                                             Predicate<JsonElement> verifier,
                                                                             Function<JsonElement, T> consumerTransformer,
                                                                             Consumer<T> consumer) {
        if (!root.has(key)) {
            throw new JsonSyntaxException(String.format("Expected '%s' to be a %s or an array of %s!", key, singular, plural));
        }
        JsonElement el = root.get(key);
        if (verifier.test(el)) {
            consumer.accept(consumerTransformer.apply(el));
        } else if (el.isJsonArray()) {
            JsonArray objectArray = el.getAsJsonArray();
            for (JsonElement arrayEl : objectArray) {
                if (!verifier.test(arrayEl)) {
                    throw new JsonSyntaxException(String.format("Expected '%s' to be an array of %s!", key, plural));
                }
                consumer.accept(consumerTransformer.apply(arrayEl));
            }
        } else {
            throw new JsonSyntaxException(String.format("Expected '%s' to be a %s or an array of %s!", key, singular, plural));
        }
    }

    @Nonnull
    public static FluidStack getFluidStack(JsonElement fluidElement, String infoKey) {
        FluidStack fluidStack;
        if (fluidElement.isJsonPrimitive() && fluidElement.getAsJsonPrimitive().isString()) {
            String strKey = fluidElement.getAsString();
            ResourceLocation fluidKey = ResourceLocation.parse(strKey);
            fluidStack = new FluidStack(BuiltInRegistries.FLUID.get(fluidKey), FluidType.BUCKET_VOLUME);
        } else if (fluidElement.isJsonObject()) {
            fluidStack = getFluidStack(fluidElement.getAsJsonObject(), true);
        } else {
            throw new JsonSyntaxException("Missing " + infoKey + ", expected to find a string or object");
        }
        return fluidStack;
    }

    // 1.21 port: FluidStack lost its raw-NBT constructor/loadFluidStackFromNBT (data components
    // replaced free-form NBT tags on stacks); the "nbt" field is parsed-and-discarded here, same
    // fluid+amount-only scope limitation as LiquidInteraction's reactant matching.
    @Nonnull
    public static FluidStack getFluidStack(JsonObject json, boolean readNBT) {
        String fluidName = GsonHelper.getAsString(json, "fluid");
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidName));
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, GsonHelper.getAsInt(json, "amount", FluidType.BUCKET_VOLUME));
    }

    @Nonnull
    public static ItemStack getItemStack(JsonElement itemElement, String infoKey) {
        ItemStack itemstack;
        if (itemElement.isJsonPrimitive() && itemElement.getAsJsonPrimitive().isString()) {
            String strKey = itemElement.getAsString();
            ResourceLocation itemKey = ResourceLocation.parse(strKey);
            itemstack = new ItemStack(BuiltInRegistries.ITEM.get(itemKey));
        } else if (itemElement.isJsonObject()) {
            itemstack = parseItemStackObject(itemElement.getAsJsonObject());
        } else {
            throw new JsonSyntaxException("Missing " + infoKey + ", expected to find a string or object");
        }
        return itemstack;
    }

    @Nonnull
    public static ItemStack getItemStack(JsonObject root, String key) {
        if (!root.has(key)) {
            throw new JsonSyntaxException("Missing " + key + ", expected to find a string or object");
        }
        ItemStack itemstack;
        if (root.get(key).isJsonObject()) {
            itemstack = parseItemStackObject(GsonHelper.getAsJsonObject(root, key));
        } else {
            String strKey = GsonHelper.getAsString(root, key);
            ResourceLocation itemKey = ResourceLocation.parse(strKey);
            itemstack = new ItemStack(BuiltInRegistries.ITEM.get(itemKey));
        }
        return itemstack;
    }

    // 1.21 port: CraftingHelper.getItemStack(JsonObject, boolean) is gone (ItemStack's NBT tag was
    // replaced by data components, which need a HolderLookup.Provider this static utility doesn't
    // have). Item + count only; a "nbt" field is parsed-and-discarded, same scope limitation as
    // getFluidStack's "nbt" field.
    @Nonnull
    private static ItemStack parseItemStackObject(JsonObject object) {
        String itemName = GsonHelper.getAsString(object, "item");
        ResourceLocation itemKey = ResourceLocation.parse(itemName);
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemKey));
        stack.setCount(GsonHelper.getAsInt(object, "count", 1));
        return stack;
    }

    @Nonnull
    public static JsonObject serializeItemStack(ItemStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("item", RegistryHelper.getKey(stack.getItem()).toString());
        object.addProperty("count", stack.getCount());
        return object;
    }

    public static Color getColor(JsonObject object, String key) {
        String value = GsonHelper.getAsString(object, key);
        if (value.startsWith("0x")) { //Assume hex color.
            String hexNbr = value.substring(2);
            try {
                return new Color(Integer.parseInt(hexNbr, 16), true);
            } catch (NumberFormatException exc) {
                throw new JsonParseException("Expected " + hexNbr + " to be a hexadecimal string!", exc);
            }
        } else {
            try {
                return new Color(Integer.parseInt(value), true);
            } catch (NumberFormatException exc) {
                try {
                    return new Color(Integer.parseInt(value, 16), true);
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Expected " + value + " to be a int or hexadecimal-number!", e);
                }
            }
        }
    }

}
