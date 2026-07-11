/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.crafting.custom.RecipeDyeableChangeColor;
import hellfirepvp.astralsorcery.common.crafting.serializer.*;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRecipeSerializers
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:32
 */
public class RegistryRecipeSerializers {

    private RegistryRecipeSerializers() {}

    public static void init() {
        WELL_LIQUEFACTION_SERIALIZER = register(new WellRecipeSerializer());
        LIQUID_INFUSION_SERIALIZER = register(new LiquidInfusionSerializer());
        BLOCK_TRANSMUTATION_SERIALIZER = register(new BlockTransmutationSerializer());
        ALTAR_RECIPE_SERIALIZER = register(new SimpleAltarRecipeSerializer());
        LIQUID_INTERACTION_SERIALIZER = register(new LiquidInteractionSerializer());

        // These two don't carry an AstralRegistryEntry name of their own (they're built on
        // vanilla's category-only SimpleCraftingRecipeSerializer), so register by explicit id.
        CUSTOM_CHANGE_WAND_COLOR_SERIALIZER = AstralRegistries.register(AstralRegistries.RECIPE_SERIALIZERS,
                CUSTOM_CHANGE_WAND_COLOR, new RecipeDyeableChangeColor.IlluminationWandColorSerializer());
        CUSTOM_CHANGE_GATEWAY_COLOR_SERIALIZER = AstralRegistries.register(AstralRegistries.RECIPE_SERIALIZERS,
                CUSTOM_CHANGE_GATEWAY_COLOR, new RecipeDyeableChangeColor.CelestialGatewayColorSerializer());
    }

    private static <T extends RecipeSerializer<?>> T register(T serializer) {
        return AstralRegistries.register(AstralRegistries.RECIPE_SERIALIZERS, serializer);
    }

}
