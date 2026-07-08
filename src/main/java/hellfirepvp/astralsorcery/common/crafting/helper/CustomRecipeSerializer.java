/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.registry.internal.AbstractAstralRegistryEntry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomRecipeSerializer
 * Created by HellFirePvP
 * Date: 06.07.2019 / 20:38
 */
public abstract class CustomRecipeSerializer<T extends CustomMatcherRecipe> extends AbstractAstralRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {

    public CustomRecipeSerializer(ResourceLocation name) {
        this.setRegistryName(name);
    }

    public abstract void write(JsonObject object, T recipe);
}
