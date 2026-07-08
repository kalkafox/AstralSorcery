/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.AltarRecipeInstance;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarCraftTrigger
 * Created by HellFirePvP
 * Date: 27.10.2018 / 14:23
 */
public class AltarCraftTrigger extends ListenerCriterionTrigger<AltarRecipeInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("altar_craft");

    public AltarCraftTrigger() {
        super(ID);
    }

    @Override
    public AltarRecipeInstance deserialize(JsonObject object, ConditionArrayParser conditions) {
        return AltarRecipeInstance.deserialize(getId(), object);
    }

    public void trigger(ServerPlayer player, SimpleAltarRecipe recipe, ItemStack output) {
        Listeners<AltarRecipeInstance> listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) {
            listeners.trigger((i) -> i.test(recipe, output));
        }
    }

}
