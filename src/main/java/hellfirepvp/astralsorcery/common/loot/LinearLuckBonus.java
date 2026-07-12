/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LinearLuckBonus
 * Created by HellFirePvP
 * Date: 20.07.2019 / 22:07
 */
public class LinearLuckBonus extends LootItemConditionalFunction {

    public static final MapCodec<LinearLuckBonus> CODEC = RecordCodecBuilder.mapCodec(
            inst -> commonFields(inst).apply(inst, LinearLuckBonus::new)
    );

    private LinearLuckBonus(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public LootItemFunctionType<LinearLuckBonus> getType() {
        return LootAS.Functions.LINEAR_LUCK_BONUS;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext compositePredicates) {
        ItemStack tool = compositePredicates.getParamOrNull(LootContextParams.TOOL);
        if (tool != null) {
            int luck = 0;
            Entity e = compositePredicates.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (e instanceof Player && ((Player) e).hasEffect(MobEffects.LUCK)) {
                luck += ((Player) e).getEffect(MobEffects.LUCK).getAmplifier() + 1;
            }

            Holder<Enchantment> fortune = compositePredicates.getLevel().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE);
            Holder<Enchantment> looting = compositePredicates.getLevel().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING);
            luck += EnchantmentHelper.getItemEnchantmentLevel(fortune, tool);
            luck += EnchantmentHelper.getItemEnchantmentLevel(looting, tool);

            var random = compositePredicates.getRandom();
            int size = 0;
            for (int i = 0; i < luck; i++) {
                size += random.nextInt(3) + 1;
            }
            itemStack.setCount(itemStack.getCount() + size);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(LinearLuckBonus::new);
    }
}
