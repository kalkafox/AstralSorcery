/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IHandlerRecipe
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:39
 */
public interface IHandlerRecipe<I extends IItemHandler> extends Recipe<IHandlerRecipe.NoopInput> {

    boolean matches(I handler, Level level);

    @Override
    default boolean matches(NoopInput input, Level worldIn) {
        return false;
    }

    /**
     * None of these recipes are ever matched against a vanilla crafting grid - real matching
     * happens through {@link #matches(IItemHandler, Level)}. {@link Recipe} requires a
     * {@link RecipeInput} bound in 1.21, so this is a zero-size marker input to satisfy that
     * bound without pretending these recipes participate in container-based crafting.
     */
    interface NoopInput extends RecipeInput {

        NoopInput INSTANCE = new NoopInput() {};

        @Override
        default ItemStack getItem(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        default int size() {
            return 0;
        }
    }
}
