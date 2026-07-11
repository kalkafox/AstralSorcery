/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LegacyRecipeCodecs
 * Created by HellFirePvP
 * Date: porting/1.21.1
 *
 * Bridges the hand-written, imperative {@link JsonObject}-based (de)serialization logic that
 * predates Mojang's {@link com.mojang.serialization.Codec} recipe format onto a {@link MapCodec}
 * so it can back a {@link net.minecraft.world.item.crafting.RecipeSerializer#codec()}, without
 * having to rewrite every recipe parser as a combinator chain.
 *
 * The wrapped codec round-trips through {@link JsonOps#INSTANCE} internally: on decode, whatever
 * {@link DynamicOps} was actually supplied (JSON while loading datapacks, NBT if ever used
 * elsewhere) is first converted into a {@link JsonObject}, handed to the legacy decode function,
 * and vice versa for encoding. This is safe for our use (recipes are only ever loaded through
 * {@code JsonOps.INSTANCE}) but is not a fully general {@link Codec}.
 */
public final class LegacyRecipeCodecs {

    private LegacyRecipeCodecs() {}

    public static <T> MapCodec<T> ofLegacyJson(Function<JsonObject, T> decode, BiConsumer<JsonObject, T> encode) {
        return MapCodec.assumeMapUnsafe(new Codec<T>() {
            @Override
            public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
                try {
                    JsonElement json = ops.convertTo(JsonOps.INSTANCE, input);
                    if (json == null || !json.isJsonObject()) {
                        return DataResult.error(() -> "Expected a JSON object, got: " + json);
                    }
                    T value = decode.apply(json.getAsJsonObject());
                    return DataResult.success(Pair.of(value, input));
                } catch (Exception e) {
                    return DataResult.error(() -> "Failed to parse recipe: " + e.getMessage());
                }
            }

            @Override
            public <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
                try {
                    JsonObject object = new JsonObject();
                    encode.accept(object, input);
                    O encoded = JsonOps.INSTANCE.convertTo(ops, object);
                    return DataResult.success(encoded);
                } catch (Exception e) {
                    return DataResult.error(() -> "Failed to write recipe: " + e.getMessage());
                }
            }
        });
    }
}
