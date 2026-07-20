/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.common.container.ContainerAltarBase;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeGrid;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Recipe-transfer ("+" autofill) mapping for the altar containers.
 *
 * <p>The JEI layout exposes the full 5x5 recipe grid as inputs in grid order; this maps each grid
 * cell to the matching crafting slot of the open container via
 * {@link ContainerAltarBase#translateIndex(int)}. Grid cells a lower-tier container doesn't have
 * only ever carry empty ingredients for recipes it can handle, so those map to a dummy slot that
 * is never filled. Player inventory slots are 0-35, crafting slots start at 36.
 */
public class AltarRecipeTransferInfo<C extends ContainerAltarBase> implements IRecipeTransferInfo<C, SimpleAltarRecipe> {

    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final Class<C> containerClass;
    private final RecipeType<SimpleAltarRecipe> recipeType;

    public AltarRecipeTransferInfo(Class<C> containerClass, RecipeType<SimpleAltarRecipe> recipeType) {
        this.containerClass = containerClass;
        this.recipeType = recipeType;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return this.containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType<SimpleAltarRecipe> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public boolean canHandle(C container, SimpleAltarRecipe recipe) {
        return container.getTileEntity().getAltarType().isThisGEThan(recipe.getAltarType());
    }

    @Override
    public List<Slot> getRecipeSlots(C container, SimpleAltarRecipe recipe) {
        List<Slot> slots = new ArrayList<>(AltarRecipeGrid.MAX_INVENTORY_SIZE);
        for (int gridIndex = 0; gridIndex < AltarRecipeGrid.MAX_INVENTORY_SIZE; gridIndex++) {
            if (recipe.getInputs().getIngredient(gridIndex).isEmpty()) {
                slots.add(container.slots.get(PLAYER_INVENTORY_SIZE));
            } else {
                slots.add(container.slots.get(PLAYER_INVENTORY_SIZE + container.translateIndex(gridIndex)));
            }
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(C container, SimpleAltarRecipe recipe) {
        return new ArrayList<>(container.slots.subList(0, PLAYER_INVENTORY_SIZE));
    }
}
