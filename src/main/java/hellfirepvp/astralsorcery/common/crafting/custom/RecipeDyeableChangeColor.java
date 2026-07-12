/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.custom;

import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialGateway;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeDyeableChangeColor
 * Created by HellFirePvP
 * Date: 29.11.2019 / 13:24
 *
 * Unlike the rest of this package, this is a real 3x3-grid vanilla {@link CustomRecipe} (not one
 * of the handler-based recipes routed through {@code IHandlerRecipe}), so it follows vanilla's own
 * 1.21 shape: {@link CraftingInput} instead of {@code CraftingContainer}, a
 * {@link CraftingBookCategory} instead of a self-carried id (recipes don't self-report an id
 * anymore - see {@code BaseHandlerRecipe#getId()} for the other family of recipes in this mod),
 * and {@link SimpleCraftingRecipeSerializer} (vanilla's replacement for the old
 * {@code SpecialRecipeSerializer}) instead of a hand-rolled one.
 */
public class RecipeDyeableChangeColor extends CustomRecipe {

    private final Supplier<RecipeSerializer<?>> serializer;
    private final Item targetItem;
    private final BiConsumer<ItemStack, DyeColor> colorFn;

    public RecipeDyeableChangeColor(CraftingBookCategory category, Supplier<RecipeSerializer<?>> serializer, Item targetItem, BiConsumer<ItemStack, DyeColor> colorFn) {
        super(category);
        this.serializer = serializer;
        this.targetItem = targetItem;
        this.colorFn = colorFn;
    }

    @Override
    public boolean matches(CraftingInput input, Level worldIn) {
        return tryFindValidRecipeAndDye(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Tuple<DyeColor, ItemStack> itemColorTpl = tryFindValidRecipeAndDye(input);
        if (itemColorTpl == null) {
            return ItemStack.EMPTY;
        }
        ItemStack out = ItemUtils.copyStackWithSize(itemColorTpl.getB(), 1);
        this.colorFn.accept(out, itemColorTpl.getA());
        return out;
    }

    @Nullable
    private Tuple<DyeColor, ItemStack> tryFindValidRecipeAndDye(CraftingInput input) {
        ItemStack itemFound = ItemStack.EMPTY;
        DyeColor dyeColorFound = null;
        int nonEmptyItemsFound = 0;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack in = input.getItem(slot);
            if (!in.isEmpty()) {
                nonEmptyItemsFound++;

                if (in.getItem().equals(this.targetItem)) {
                    itemFound = in;
                } else {
                    DyeColor color = DyeColor.getColor(in);
                    if (color != null) {
                        dyeColorFound = color;
                    }
                }
            }
        }

        if (itemFound.isEmpty() || dyeColorFound == null || nonEmptyItemsFound != 2) {
            return null;
        } else {
            return new Tuple<>(dyeColorFound, itemFound);
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer.get();
    }

    public static RecipeDyeableChangeColor wandColorRecipe(CraftingBookCategory category) {
        return new RecipeDyeableChangeColor(category, () -> RecipeSerializersAS.CUSTOM_CHANGE_WAND_COLOR_SERIALIZER,
                ItemsAS.ILLUMINATION_WAND, ItemIlluminationWand::setConfiguredColor);
    }

    public static RecipeDyeableChangeColor gatewayColorRecipe(CraftingBookCategory category) {
        return new RecipeDyeableChangeColor(category, () -> RecipeSerializersAS.CUSTOM_CHANGE_GATEWAY_COLOR_SERIALIZER,
                Item.byBlock(BlocksAS.GATEWAY), BlockCelestialGateway::setColor);
    }

    public static class IlluminationWandColorSerializer extends SimpleCraftingRecipeSerializer<RecipeDyeableChangeColor> {

        public IlluminationWandColorSerializer() {
            super(RecipeDyeableChangeColor::wandColorRecipe);
        }
    }

    public static class CelestialGatewayColorSerializer extends SimpleCraftingRecipeSerializer<RecipeDyeableChangeColor> {

        public CelestialGatewayColorSerializer() {
            super(RecipeDyeableChangeColor::gatewayColorRecipe);
        }
    }
}
