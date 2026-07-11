/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IngredientIO
 * Created by HellFirePvP
 * Date: porting/1.21.1
 *
 * {@link Ingredient} lost its hand-rolled {@code serialize()}/{@code deserialize()}/{@code read()}/
 * {@code write()} methods in 1.21 in favor of {@link Ingredient#CODEC} (JSON) and
 * {@link Ingredient#CONTENTS_STREAM_CODEC} (network). This is a thin adapter back to the old
 * call shape so the many hand-written recipe (de)serializers in this package don't each need to
 * spell out the codec plumbing.
 */
public final class IngredientIO {

    private IngredientIO() {}

    public static JsonElement serialize(Ingredient ingredient) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
    }

    public static Ingredient deserialize(JsonElement element) {
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    public static void write(RegistryFriendlyByteBuf buffer, Ingredient ingredient) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
    }

    public static Ingredient read(RegistryFriendlyByteBuf buffer) {
        return Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
    }
}
