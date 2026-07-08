/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DefaultWrapperContext
 * Created by HellFirePvP
 * Date: 01.07.2019 / 00:12
 */
public class DefaultWrapperContext extends RecipeCraftingContext<IHandlerRecipe, IItemHandler> {

    private final IItemHandler handler;
    private final Level world;

    public DefaultWrapperContext(IItemHandler handler, Level world) {
        this.handler = handler;
        this.world = world;
    }

    public IItemHandler getHandler() {
        return handler;
    }

    public Level getWorld() {
        return world;
    }
}
