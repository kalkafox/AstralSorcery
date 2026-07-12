/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryIngredientTypes
 * Created by HellFirePvP
 * Date: 30.05.2019 / 17:48
 */
public class RegistryIngredientTypes {

    private RegistryIngredientTypes() {}

    public static void init() {
        IngredientSerializersAS.FLUID_INGREDIENT_TYPE = AstralRegistries.register(
                AstralRegistries.INGREDIENT_TYPES, AstralSorcery.key("fluid"), new IngredientType<>(FluidIngredient.CODEC));
        IngredientSerializersAS.CRYSTAL_INGREDIENT_TYPE = AstralRegistries.register(
                AstralRegistries.INGREDIENT_TYPES, AstralSorcery.key("crystal"), new IngredientType<>(CrystalIngredient.CODEC));
    }

}
