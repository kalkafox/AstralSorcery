/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.common.crafting.IIngredientSerializer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CrystalIngredientSerializer
 * Created by HellFirePvP
 * Date: 28.09.2019 / 10:06
 */
public class CrystalIngredientSerializer implements IIngredientSerializer<CrystalIngredient> {

    @Override
    public CrystalIngredient parse(JsonObject json) {
        boolean hasToBeAttuned = GsonHelper.getBoolean(json, "hasToBeAttuned", false);
        boolean hasToBeCelestial = GsonHelper.getBoolean(json, "hasToBeCelestial", false);
        boolean canBeAttuned = GsonHelper.getBoolean(json, "canBeAttuned", true);
        boolean canBeCelestialCrystal = GsonHelper.getBoolean(json, "canBeCelestialCrystal", true);
        return new CrystalIngredient(hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestialCrystal);
    }

    @Override
    public CrystalIngredient parse(FriendlyByteBuf buffer) {
        return new CrystalIngredient(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer, CrystalIngredient ingredient) {
        buffer.writeBoolean(ingredient.hasToBeAttuned());
        buffer.writeBoolean(ingredient.hasToBeCelestial());
        buffer.writeBoolean(ingredient.canBeAttuned());
        buffer.writeBoolean(ingredient.canBeCelestialCrystal());
    }
}
