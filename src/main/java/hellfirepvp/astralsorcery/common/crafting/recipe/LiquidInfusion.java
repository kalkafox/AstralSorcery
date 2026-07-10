/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomMatcherRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInfusion
 * Created by HellFirePvP
 * Date: 26.07.2019 / 21:29
 */
public class LiquidInfusion extends CustomMatcherRecipe implements GatedRecipe.Progression {

    private final int craftingTickTime;
    private final Fluid liquidInput;
    private final Ingredient itemInput;
    private final ItemStack output;

    private final float consumptionChance;
    private final boolean consumeMultipleFluids;
    private final boolean acceptChaliceInput;
    private final boolean copyNBTToOutputs;

    public LiquidInfusion(ResourceLocation recipeId, int craftingTickTime, Fluid liquidInput, Ingredient itemInput, ItemStack itemOutput, float consumptionChance, boolean consumeMultipleFluids, boolean acceptChaliceInput, boolean copyNBTToOutputs) {
        super(recipeId);
        this.craftingTickTime = craftingTickTime;
        this.liquidInput = liquidInput;
        this.itemInput = itemInput;
        this.output = itemOutput;
        this.consumptionChance = consumptionChance;
        this.consumeMultipleFluids = consumeMultipleFluids;
        this.acceptChaliceInput = acceptChaliceInput;
        this.copyNBTToOutputs = copyNBTToOutputs;
    }

    public boolean matches(TileInfuser infuser, Player crafter, LogicalSide direction) {
        if (crafter == null) {
            return false;
        }
        boolean hasProgress;
        if (direction.isClient()) {
            hasProgress = this.hasProgressionClient();
        } else {
            hasProgress = this.hasProgressionServer(crafter);
        }
        if (!hasProgress) {
            return false;
        }
        boolean hasFluidInputs = MapStream.of(infuser.getLiquids())
                .mapKey(pos -> pos.offset(infuser.getBlockPos()))
                .allMatch(tpl -> this.liquidInput.equals(tpl.getB()));

        if (!hasFluidInputs) {
            return false;
        }

        return this.itemInput.test(infuser.getItemInput());
    }

    @Nonnull
    @Override
    public ResearchProgression getRequiredProgression() {
        return ResearchProgression.CONSTELLATION;
    }

    public int getCraftingTickTime() {
        return craftingTickTime;
    }

    @Nonnull
    public Fluid getLiquidInput() {
        return liquidInput;
    }

    @Nonnull
    public Ingredient getItemInput() {
        return itemInput;
    }

    public void onRecipeCompletion(TileInfuser infuser) {}

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack getOutputForRender(Iterable<ItemStack> items) {
        return ItemUtils.copyStackWithSize(this.output, this.output.getCount());
    }

    @Nonnull
    public ItemStack getOutput(ItemStack itemInput) {
        return ItemUtils.copyStackWithSize(this.output, this.output.getCount());
    }

    public float getConsumptionChance() {
        return Mth.clamp(consumptionChance, 0F, 1F);
    }

    public boolean doesConsumeMultipleFluids() {
        return consumeMultipleFluids;
    }

    public boolean acceptsChaliceInput() {
        return acceptChaliceInput;
    }

    public boolean doesCopyNBTToOutputs() {
        return copyNBTToOutputs;
    }

    public static LiquidInfusion read(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Fluid fluidIn = ByteBufUtils.readRegistryEntry(buffer);
        Ingredient itemIn = Ingredient.read(buffer);
        ItemStack output = ByteBufUtils.readItem(buffer);
        float consumptionChance = buffer.readFloat();
        int duration = buffer.readInt();
        boolean consumeMultiple = buffer.readBoolean();
        boolean acceptChalice = buffer.readBoolean();
        boolean copyNBTToOutputs = buffer.readBoolean();
        return new LiquidInfusion(recipeId, duration, fluidIn, itemIn, output, consumptionChance, consumeMultiple, acceptChalice, copyNBTToOutputs);
    }

    public final void write(FriendlyByteBuf buffer) {
        ByteBufUtils.writeRegistryEntry(buffer, this.getLiquidInput());
        this.getItemInput().write(buffer);
        ByteBufUtils.writeItemStack(buffer, this.output);
        buffer.writeFloat(this.getConsumptionChance());
        buffer.writeInt(this.getCraftingTickTime());
        buffer.writeBoolean(this.doesConsumeMultipleFluids());
        buffer.writeBoolean(this.acceptsChaliceInput());
        buffer.writeBoolean(this.doesCopyNBTToOutputs());
    }

    public void write(JsonObject object) {
        object.addProperty("fluidInput", RegistryHelper.getKey(this.getLiquidInput()).toString());
        object.add("input", this.getItemInput().serialize());
        object.add("output", JsonHelper.serializeItemStack(this.output));
        object.addProperty("consumptionChance", this.getConsumptionChance());
        object.addProperty("duration", this.getCraftingTickTime());
        object.addProperty("consumeMultipleFluids", this.doesConsumeMultipleFluids());
        object.addProperty("acceptChaliceInput", this.acceptsChaliceInput());
        object.addProperty("copyNBTToOutputs", this.doesCopyNBTToOutputs());
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.TYPE_INFUSION.getType();
    }

    @Override
    public CustomRecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.LIQUID_INFUSION_SERIALIZER;
    }
}
