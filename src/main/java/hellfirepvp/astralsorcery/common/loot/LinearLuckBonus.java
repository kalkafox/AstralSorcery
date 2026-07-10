/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Random;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LinearLuckBonus
 * Created by HellFirePvP
 * Date: 20.07.2019 / 22:07
 */
public class LinearLuckBonus extends LootItemConditionalFunction {

    private LinearLuckBonus(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.LINEAR_LUCK_BONUS;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext compositePredicates) {
        ItemStack tool = compositePredicates.get(LootContextParams.TOOL);
        if (tool != null) {
            int luck = 0;
            Entity e = compositePredicates.get(LootContextParams.THIS_ENTITY);
            if (e instanceof Player && ((Player) e).isPotionActive(MobEffects.LUCK)) {
                luck += ((Player) e).getActivePotionEffect(MobEffects.LUCK).getAmplifier() + 1;
            }
            luck += EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
            luck += EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, tool);

            Random random = compositePredicates.getRandom();
            int size = 0;
            for (int i = 0; i < luck; i++) {
                size += random.nextInt(3) + 1;
            }
            itemStack.setCount(itemStack.getCount() + size);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return builder(LinearLuckBonus::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LinearLuckBonus> {

        @Override
        public LinearLuckBonus deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] iLootConditions) {
            return new LinearLuckBonus(iLootConditions);
        }
    }

}
