/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BaseHandlerRecipe
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:42
 */
public abstract class BaseHandlerRecipe<I extends IItemHandler> implements IHandlerRecipe<I> {

    private final ResourceLocation recipeId;
    private String group = "";

    protected BaseHandlerRecipe(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public void group(String group) {
        this.group = group;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    /**
     * {@link Recipe#getId()} no longer exists in 1.21 - recipes don't self-report their own id
     * anymore, {@link net.minecraft.world.item.crafting.RecipeManager} attaches it externally via
     * {@link net.minecraft.world.item.crafting.RecipeHolder} when loading recipe JSON. This id is
     * kept purely as this object's own internal identity (equals/hashCode, NBT round-tripping of
     * in-progress crafts) - see {@link hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer}
     * for how it's synthesized for JSON/network-decoded recipes.
     */
    public final ResourceLocation getId() {
        return this.recipeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseHandlerRecipe<?> that = (BaseHandlerRecipe<?>) o;
        return recipeId.equals(that.recipeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeId);
    }
}
