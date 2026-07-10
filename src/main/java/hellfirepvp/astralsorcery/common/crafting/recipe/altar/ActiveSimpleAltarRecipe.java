/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.helper.CraftingFocusStack;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.tile.TileSpectralRelay;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.VoidFluidHandler;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveSimpleAltarRecipe
 * Created by HellFirePvP
 * Date: 13.08.2019 / 20:59
 */
public class ActiveSimpleAltarRecipe {

    private static final Random random = new Random();

    private Map<Integer, Object> clientEffectContainer = new HashMap<>();

    private final SimpleAltarRecipe recipeToCraft;
    private final UUID playerCraftingUUID;
    private int ticksCrafting = 0;
    private int totalCraftingTime;

    private CraftingState state;
    private CompoundTag craftingData = new CompoundTag();

    private List<CraftingFocusStack> focusStacks = new LinkedList<>();

    private ActiveSimpleAltarRecipe(SimpleAltarRecipe recipeToCraft, UUID playerCraftingUUID) {
        this(recipeToCraft, 1, playerCraftingUUID);
    }

    public ActiveSimpleAltarRecipe(SimpleAltarRecipe recipeToCraft, int durationDivisor, UUID playerCraftingUUID) {
        Objects.requireNonNull(recipeToCraft);

        this.recipeToCraft = recipeToCraft;
        this.playerCraftingUUID = playerCraftingUUID;
        this.state = CraftingState.ACTIVE;
        this.totalCraftingTime = recipeToCraft.getDuration() / durationDivisor;
    }

    private void recoverContainedEffects(@Nullable ActiveSimpleAltarRecipe cameFrom) {
        if (cameFrom != null && cameFrom.getRecipeToCraft().getId().equals(this.recipeToCraft.getId())) {
            this.clientEffectContainer.putAll(cameFrom.clientEffectContainer);
        }
    }

    public CompoundTag getCraftingData() {
        return craftingData;
    }

    public UUID getPlayerCraftingUUID() {
        return playerCraftingUUID;
    }

    public void setState(CraftingState state) {
        this.state = state;
    }

    public CraftingState getState() {
        return state;
    }

    public List<CraftingFocusStack> getFocusStacks() {
        return focusStacks;
    }

    @Nullable
    public Player tryGetCraftingPlayerServer() {
        MinecraftServer srv = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return srv.getPlayerList().getPlayerByUUID(this.getPlayerCraftingUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public <T> T getEffectContained(int index, Function<Integer, T> provider) {
        return (T) clientEffectContainer.computeIfAbsent(index, provider);
    }

    public void createItemOutputs(TileAltar altar, Consumer<ItemStack> output) {
        Consumer<ItemStack> informer = stack -> ResearchManager.informCraftedAltar(altar, this, stack);

        Consumer<ItemStack> handleCrafted = informer.andThen(output);
        this.getRecipeToCraft().getOutputs(altar).forEach(handleCrafted);
        this.getRecipeToCraft().onRecipeCompletion(altar, this);
    }

    public void consumeInputs(TileAltar altar) {
        TileInventory inv = altar.getItems();
        AltarRecipeGrid grid = this.recipeToCraft.getInputs();

        for (int slot = 0; slot < AltarRecipeGrid.MAX_INVENTORY_SIZE; slot++) {
            Ingredient from = grid.getIngredient(slot);
            if (from instanceof FluidIngredient) {
                ItemStack stack = inv.getStackInSlot(slot);
                FluidActionResult far = FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, FluidAttributes.BUCKET_VOLUME, null, true);
                if (far.shouldSwing()) {
                    inv.setStackInSlot(slot, far.getObject());
                }
            } else {
                ItemUtils.decrementItem(inv, slot, altar::dropItemOnTop);
            }
        }

        for (CraftingFocusStack from : this.focusStacks) {
            TileSpectralRelay tar = MiscUtils.getTileAt(altar.getLevel(), from.getRealPosition(), TileSpectralRelay.class, true);
            if (tar != null) {
                TileInventory tarInventory = tar.getItems();
                if (from.getInput() != null && from.getInput().getIngredient() instanceof FluidIngredient) {
                    ItemStack stack = tarInventory.getStackInSlot(0);
                    FluidActionResult far = FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, FluidAttributes.BUCKET_VOLUME, null, true);
                    if (far.shouldSwing()) {
                        tarInventory.setStackInSlot(0, far.getObject());
                    }
                } else {
                    ItemUtils.decrementItem(tarInventory, 0, altar::dropItemOnTop);
                }
            }
        }
    }

    public boolean matches(TileAltar altar, boolean ignoreStarlightRequirement, boolean testNecessaryRelayInputs) {
        if (!this.getRecipeToCraft().matches(LogicalSide.SERVER, this.tryGetCraftingPlayerServer(), altar, ignoreStarlightRequirement)) {
            return false;
        }

        List<WrappedIngredient> listIngredients = this.getRecipeToCraft().getRelayInputs();
        for (CraftingFocusStack stack : this.focusStacks) {
            if (stack.getStackIndex() >= 0 && stack.getStackIndex() < listIngredients.size()) {
                TileSpectralRelay relay = MiscUtils.getTileAt(altar.getLevel(), stack.getRealPosition(), TileSpectralRelay.class, true);
                if (relay == null) {
                    return false;
                }
                if (testNecessaryRelayInputs) {
                    WrappedIngredient ingredient = listIngredients.get(stack.getStackIndex());
                    if (!ingredient.getIngredient().test(relay.getItems().getStackInSlot(0))) {
                        return false;
                    }
                }
            } else {
                //The recipe changed?
                return false;
            }
        }
        return true;
    }
    //True if the recipe progressed, false if it's stuck

    public CraftingState tick(TileAltar altar) {
        if (recipeToCraft instanceof AltarCraftingProgress) {
            if (!((AltarCraftingProgress) recipeToCraft).tryProcess(altar, this, craftingData, ticksCrafting, totalCraftingTime)) {
                return CraftingState.WAITING;
            }
        }

        List<WrappedIngredient> iIngredients = this.getRecipeToCraft().getRelayInputs();

        if (!iIngredients.isEmpty()) {
            boolean shouldWait = tickCraftingRelayInputs(altar, iIngredients);
            if (shouldWait) {
                return CraftingState.WAITING;
            }
        }

        ticksCrafting++;
        return CraftingState.ACTIVE;
    }

    private boolean tickCraftingRelayInputs(TileAltar altar, List<WrappedIngredient> iIngredients) {
        //Start the surrounding stack intake past 66%
        int part = totalCraftingTime / 3;
        //Every input is evenly spread
        int cttPart = part / (iIngredients.size()) + 1;


        boolean waitMissingInputs = false;
        for (int i = 0; i < iIngredients.size(); i++) {
            WrappedIngredient from = iIngredients.get(i);
            int index = i;

            int offset = part + (index * cttPart);
            if (this.ticksCrafting >= offset) {
                CraftingFocusStack found = MiscUtils.iterativeSearch(this.focusStacks, fStack -> fStack.getStackIndex() == index);
                if (found == null) {
                    Set<BlockPos> relays = altar.nearbyRelays();
                    relays.removeIf(pos -> MiscUtils.contains(this.focusStacks, fs -> fs.getRealPosition().equals(pos)));
                    //No unused relay found for a new focus-stack..
                    if (relays.isEmpty()) {
                        waitMissingInputs = true;
                        continue;
                    }
                    BlockPos at = MiscUtils.getRandomEntry(relays, random);
                    TileSpectralRelay tar = MiscUtils.getTileAt(altar.getLevel(), at, TileSpectralRelay.class, true);
                    if (tar == null) { // We were lied to. lol.
                        waitMissingInputs = true;
                        continue;
                    }
                    found = new CraftingFocusStack(index, from, at);
                    this.focusStacks.add(found);
                }
                TileSpectralRelay tar = MiscUtils.getTileAt(altar.getLevel(), found.getRealPosition(), TileSpectralRelay.class, true);
                if (tar == null) {
                    waitMissingInputs = true;
                    continue;
                }
                if (!from.getIngredient().test(tar.getItems().getStackInSlot(0))) {
                    waitMissingInputs = true;
                }
            }
        }
        return waitMissingInputs;
    }

    public int getTicksCrafting() {
        return ticksCrafting;
    }

    public int getTotalCraftingTime() {
        return totalCraftingTime;
    }

    public SimpleAltarRecipe getRecipeToCraft() {
        return recipeToCraft;
    }

    public boolean isFinished() {
        return ticksCrafting >= totalCraftingTime;
    }

    @Nullable
    public static ActiveSimpleAltarRecipe deserialize(CompoundTag pattern, @Nullable ActiveSimpleAltarRecipe cameFrom) {
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return null;
        }

        ResourceLocation recipeKey = ResourceLocation.parse(pattern.getString("recipeToCraft"));
        Optional<?> recipe = mgr.getRecipe(recipeKey);
        if (!recipe.isPresent() || !(recipe.get() instanceof SimpleAltarRecipe)) {
            AstralSorcery.log.info("Recipe with unknown/invalid name found: " + recipeKey);
            return null;
        }
        SimpleAltarRecipe altarRecipe = (SimpleAltarRecipe) recipe.get();
        UUID uuidCraft = pattern.getUniqueId("playerCraftingUUID");
        int tick = pattern.getInt("ticksCrafting");
        int total = pattern.getInt("totalCraftingTime");
        CraftingState state = CraftingState.values()[pattern.getInt("state")];
        List<CraftingFocusStack> stacks = new LinkedList<>();
        ListTag listStacks = pattern.getList("focusStacks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listStacks.size(); i++) {
            stacks.add(new CraftingFocusStack(listStacks.getCompound(i)));
        }

        ActiveSimpleAltarRecipe task = new ActiveSimpleAltarRecipe(altarRecipe, uuidCraft);
        task.ticksCrafting = tick;
        task.totalCraftingTime = total;
        task.setState(state);
        task.craftingData = pattern.getCompound("craftingData");
        task.focusStacks = stacks;
        task.recoverContainedEffects(cameFrom);
        return task;
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag pattern = new CompoundTag();
        pattern.putString("recipeToCraft", getRecipeToCraft().getId().toString());
        pattern.putUniqueId("playerCraftingUUID", getPlayerCraftingUUID());
        pattern.putInt("ticksCrafting", getTicksCrafting());
        pattern.putInt("totalCraftingTime", getTotalCraftingTime());
        pattern.putInt("state", getState().ordinal());
        pattern.put("craftingData", craftingData);

        ListTag list = new ListTag();
        for (CraftingFocusStack stack : this.focusStacks) {
            list.add(stack.serialize());
        }
        pattern.put("focusStacks", list);
        return pattern;
    }

    public static enum CraftingState {

        ACTIVE, //All valid, continuing to craft.

        WAITING //Potentially waiting for user interaction. Recipe itself is fully valid.

    }

}
