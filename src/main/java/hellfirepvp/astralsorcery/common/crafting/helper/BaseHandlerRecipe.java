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

    private ResourceLocation recipeId;
    private String group = "";

    protected BaseHandlerRecipe(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    /**
     * Rebinds the synthesized decode-time id to the RecipeManager holder id once recipes are
     * loaded/synced (see RecipeHelper#rebindDynamicIds). The holder id is stable across
     * server/client and across reloads, so ids serialized into tiles or packets resolve again.
     */
    public final void bindId(ResourceLocation holderId) {
        this.recipeId = holderId;
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
     * {@link net.minecraft.world.item.crafting.RecipeHolder} when loading recipe JSON. Decode
     * synthesizes a throwaway id ({@link CustomRecipeSerializer#generateDynamicId()}), which is
     * rebound to the holder id right after recipes load/sync via {@link #bindId(ResourceLocation)},
     * so this matches the holder id during gameplay.
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
