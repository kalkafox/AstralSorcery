/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.thread.EffectiveSide;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeHelper
 * Created by HellFirePvP
 * Date: 11.10.2019 / 22:30
 */
public class RecipeHelper {

    @Nullable
    public static SimpleAltarRecipe findAltarRecipeResult(Predicate<ItemStack> match) {
        for (SimpleAltarRecipe recipe : RecipeTypesAS.TYPE_ALTAR.getAllRecipes()) {
            if (match.test(recipe.getOutputForRender(Collections.emptyList()))) {
                return recipe;
            }
        }
        return null;
    }

    @Nonnull
    public static Optional<Tuple<ItemStack, Float>> findSmeltingResult(Level level, BlockState from) {
        ItemStack stack = ItemUtils.createBlockStack(from);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return findSmeltingResult(level, stack);
    }

    @Nonnull
    public static Optional<Tuple<ItemStack, Float>> findSmeltingResult(Level level, ItemStack from) {
        RecipeManager mgr = level.getRecipeManager();
        Container inv = new SimpleContainer(from);
        Optional<Recipe<Container>> optRecipe = (Optional<Recipe<Container>>) ObjectUtils.firstNonNull(
                mgr.getRecipe(RecipeType.SMELTING, inv, level),
                mgr.getRecipe(RecipeType.CAMPFIRE_COOKING, inv, level),
                mgr.getRecipe(RecipeType.SMOKING, inv, level),
                Optional.empty());
        return optRecipe.map(recipe -> {
            ItemStack smeltResult = recipe.assemble(inv).copy();
            float futureXp = 0;
            if (recipe instanceof AbstractCookingRecipe) {
                futureXp = ((AbstractCookingRecipe) recipe).getExperience();
            }
            return new Tuple<>(smeltResult, futureXp);
        });
    }

    @Nullable
    public static RecipeManager getRecipeManager() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            return getClientManager();
        } else {
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                return srv.getRecipeManager();
            }
        }
        return null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private static RecipeManager getClientManager() {
        ClientPacketListener conn;
        if ((conn = Minecraft.getInstance().getConnection()) != null) {
            return conn.getRecipeManager();
        }
        return null;
    }

}
