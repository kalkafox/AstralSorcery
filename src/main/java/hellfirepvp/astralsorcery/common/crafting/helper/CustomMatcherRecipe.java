/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomMatcherRecipe
 * Created by HellFirePvP
 * Date: 01.07.2019 / 00:23
 */
public abstract class CustomMatcherRecipe extends BaseHandlerRecipe<IItemHandler> {

    protected CustomMatcherRecipe(ResourceLocation recipeId) {
        super(recipeId);
    }

    @Override
    public final boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public final boolean matches(IItemHandler handler, Level level) {
        return false;
    }

    @Override
    public final ItemStack assemble(Container inv) {
        return getResultItem();
    }

    @Override
    public final ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public abstract CustomRecipeSerializer<?> getSerializer();
}
