/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.registry.internal.AbstractAstralRegistryEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomRecipeSerializer
 * Created by HellFirePvP
 * Date: 06.07.2019 / 20:38
 *
 * 1.21's {@link RecipeSerializer} dropped the imperative
 * {@code read(ResourceLocation, JsonObject)}/{@code read(ResourceLocation, FriendlyByteBuf)}/
 * {@code write(FriendlyByteBuf, T)} triplet in favor of {@link #codec()} and {@link #streamCodec()}.
 * Recipes also no longer receive their own id during decode - it's attached externally by
 * {@link net.minecraft.world.item.crafting.RecipeManager} via
 * {@link net.minecraft.world.item.crafting.RecipeHolder}. Subclasses keep their existing
 * hand-written {@code read}/{@code write} bodies (just without the id parameter) and this base
 * class wraps them into a codec/stream codec pair via {@link LegacyRecipeCodecs}, synthesizing a
 * throwaway internal id (see {@link BaseHandlerRecipe#getId()}) for each decoded instance.
 */
public abstract class CustomRecipeSerializer<T extends CustomMatcherRecipe> extends AbstractAstralRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {

    private final MapCodec<T> codec = LegacyRecipeCodecs.ofLegacyJson(this::read, this::write);
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.of(this::write, this::read);

    public CustomRecipeSerializer(ResourceLocation name) {
        this.setRegistryName(name);
    }

    /**
     * Synthesizes a placeholder id for a recipe decoded from JSON/network, since neither the
     * codec nor the stream codec are told the real datapack-assigned id anymore (that only lives
     * on the {@link net.minecraft.world.item.crafting.RecipeHolder} the RecipeManager wraps this
     * recipe in afterwards). Only meant to give {@link BaseHandlerRecipe} instances a stable
     * identity of their own for the lifetime of the loaded recipe set.
     */
    protected final ResourceLocation generateDynamicId() {
        return ResourceLocation.fromNamespaceAndPath(AstralSorcery.MODID, "dynamic/" + UUID.randomUUID());
    }

    public abstract T read(JsonObject json);

    public abstract void write(JsonObject object, T recipe);

    public abstract T read(RegistryFriendlyByteBuf buffer);

    public abstract void write(RegistryFriendlyByteBuf buffer, T recipe);

    @Override
    public MapCodec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }
}
