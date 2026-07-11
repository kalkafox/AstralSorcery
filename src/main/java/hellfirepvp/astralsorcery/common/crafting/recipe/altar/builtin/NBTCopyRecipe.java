/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.block.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.crafting.helper.IngredientIO;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.ItemLike;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import net.minecraft.tags.TagKey;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTCopyRecipe
 * Created by HellFirePvP
 * Date: 26.04.2020 / 08:02
 */
public class NBTCopyRecipe extends SimpleAltarRecipe {

    private static final String KEY_SEARCH_ITEMS = "copy_nbt_from_items_matching";

    private List<Ingredient> searchIngredients = Lists.newArrayList();

    public NBTCopyRecipe(ResourceLocation recipeId, AltarType altarType, int duration, int starlightRequirement, AltarRecipeGrid recipeGrid) {
        super(recipeId, altarType, duration, starlightRequirement, recipeGrid);
    }

    public static NBTCopyRecipe convertToThis(SimpleAltarRecipe other) {
        return new NBTCopyRecipe(other.getId(), other.getAltarType(), other.getDuration(), other.getStarlightRequirement(), other.getInputs());
    }

    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(TagKey<Item> tag) {
        return this.addNBTCopyMatchIngredient(Ingredient.of(tag));
    }

    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(ItemStack... items) {
        return this.addNBTCopyMatchIngredient(Ingredient.of(items));
    }

    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(ItemLike... items) {
        return this.addNBTCopyMatchIngredient(Ingredient.of(items));
    }

    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(Ingredient ingredient) {
        this.searchIngredients.add(ingredient);
        return (T) this;
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);

        JsonArray list = GsonHelper.getAsJsonArray(recipeObject, KEY_SEARCH_ITEMS, new JsonArray());
        for (JsonElement value : list) {
            this.searchIngredients.add(IngredientIO.deserialize(value));
        }
    }

    @Override
    public void serializeAdditionalJson(JsonObject recipeObject) {
        super.serializeAdditionalJson(recipeObject);

        JsonArray list = new JsonArray();
        for (Ingredient ingredient : this.searchIngredients) {
            list.add(IngredientIO.serialize(ingredient));
        }
        recipeObject.add(KEY_SEARCH_ITEMS, list);
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputs(TileAltar altar) {
        List<ItemStack> outputs = super.getOutputs(altar);

        List<CompoundTag> foundTags = Lists.newArrayList();
        for (ItemStack existing : altar.getItems()) {
            for (Ingredient match : this.searchIngredients) {
                if (match.test(existing) && existing.has(DataComponents.CUSTOM_DATA)) {
                    foundTags.add(existing.get(DataComponents.CUSTOM_DATA).copyTag());
                }
            }
        }
        for (ItemStack output : outputs) {
            for (CompoundTag foundTag : foundTags) {
                CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> NBTHelper.deepMerge(tag, foundTag, true));
            }
        }
        return outputs;
    }

    @Override
    public void readRecipeSync(RegistryFriendlyByteBuf buf) {
        super.readRecipeSync(buf);

        this.searchIngredients = ByteBufUtils.readList(buf, b -> IngredientIO.read((RegistryFriendlyByteBuf) b));
    }

    @Override
    public void writeRecipeSync(RegistryFriendlyByteBuf buf) {
        super.writeRecipeSync(buf);

        ByteBufUtils.writeCollection(buf, this.searchIngredients, (buffer, ingredient) -> IngredientIO.write((RegistryFriendlyByteBuf) buffer, ingredient));
    }
}
