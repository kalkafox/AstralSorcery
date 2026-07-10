/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builder;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeBuilder;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.awt.*;
import net.minecraft.tags.TagKey;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellRecipeBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 17:01
 */
public class WellRecipeBuilder extends CustomRecipeBuilder<WellLiquefaction> {

    private final ResourceLocation id;

    private Ingredient from = Ingredient.EMPTY;
    private Fluid output = Fluids.EMPTY;

    private float productionMultiplier = 0.5F;
    private float shatterMultiplier = 15F;
    private Color catalystColor = Color.WHITE;

    private WellRecipeBuilder(ResourceLocation id) {
        this.id = id;
    }

    public static WellRecipeBuilder builder(ForgeRegistryEntry<?> nameProvider) {
        return new WellRecipeBuilder(AstralSorcery.key(nameProvider.getRegistryName().getPath()));
    }

    public static WellRecipeBuilder builder(ResourceLocation id) {
        return new WellRecipeBuilder(id);
    }

    public WellRecipeBuilder setItemInput(ItemLike item) {
        this.from = Ingredient.valueFromJson(item);
        return this;
    }

    public WellRecipeBuilder setItemInput(TagKey<Item> tag) {
        this.from = Ingredient.fromTag(tag);
        return this;
    }

    public WellRecipeBuilder setItemInput(Ingredient from) {
        this.from = from;
        return this;
    }

    public WellRecipeBuilder setLiquidOutput(Fluid output) {
        this.output = output;
        return this;
    }

    public WellRecipeBuilder color(Color color) {
        this.catalystColor = color;
        return this;
    }

    public WellRecipeBuilder productionMultiplier(float multiplier) {
        this.productionMultiplier = multiplier;
        return this;
    }

    public WellRecipeBuilder shatterMultiplier(float multiplier) {
        this.shatterMultiplier = multiplier;
        return this;
    }

    @Nonnull
    @Override
    protected WellLiquefaction validateAndGet() {
        if (this.from.isEmpty()) {
            throw new IllegalArgumentException("No valid item for input found!");
        }
        if (this.output == Fluids.EMPTY) {
            throw new IllegalArgumentException("No output fluid defined!");
        }
        return new WellLiquefaction(this.id, this.from, this.output, this.catalystColor, this.productionMultiplier, this.shatterMultiplier);
    }

    @Override
    protected CustomRecipeSerializer<WellLiquefaction> getSerializer() {
        return RecipeSerializersAS.WELL_LIQUEFACTION_SERIALIZER;
    }
}
