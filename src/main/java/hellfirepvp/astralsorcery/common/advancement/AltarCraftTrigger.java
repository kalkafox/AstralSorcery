/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.AltarRecipeInstance;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarCraftTrigger
 * Created by HellFirePvP
 * Date: 27.10.2018 / 14:23
 */
public class AltarCraftTrigger extends SimpleCriterionTrigger<AltarRecipeInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("altar_craft");

    @Override
    public Codec<AltarRecipeInstance> codec() {
        return AltarRecipeInstance.CODEC;
    }

    public void trigger(ServerPlayer player, SimpleAltarRecipe recipe, ItemStack output) {
        this.trigger(player, (i) -> i.test(recipe, output));
    }
}
