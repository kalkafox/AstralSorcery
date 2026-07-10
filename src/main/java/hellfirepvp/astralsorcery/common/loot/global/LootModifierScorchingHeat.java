/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot.global;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.loot.LootUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.loot.GlobalLootModifierSerializer;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.fml.hooks.BasicEventHooks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LootModifierScorchingHeat
 * Created by HellFirePvP
 * Date: 08.05.2020 / 19:17
 */
public class LootModifierScorchingHeat extends LootModifier {

    private LootModifierScorchingHeat(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected List<ItemStack> run(List<ItemStack> lootTable, LootContext context) {
        if (!LootUtil.doesContextFulfillSet(context, LootContextParamSets.BLOCK)) {
            return lootTable;
        }
        return lootTable.stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    Optional<Tuple<ItemStack, Float>> furnaceResult = RecipeHelper.findSmeltingResult(context.getLevel(), stack);
                    if (context.has(LootContextParams.THIS_ENTITY)) {
                        Entity e = context.get(LootContextParams.THIS_ENTITY);
                        if (e instanceof Player) {
                            furnaceResult.ifPresent(result -> BasicEventHooks.firePlayerSmeltedEvent((Player) e, result.getA()));
                        }
                    }
                    furnaceResult.ifPresent(result -> {
                        ItemStack resultStack = result.getA();
                        float resultExp = result.getB();

                        ItemStack tool = context.get(LootContextParams.TOOL);
                        if (!tool.isEmpty() && !(resultStack.getItem() instanceof BlockItem)) {
                            int silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool);
                            if (silkTouch <= 0) {
                                int addedCount = 0;
                                int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
                                if (fortuneLevel > 0) {
                                    addedCount = Math.max(context.getRandom().nextInt(fortuneLevel + 2) - 1, 0);
                                    resultStack.setCount(resultStack.getCount() * (addedCount + 1));
                                }

                                resultExp *= (addedCount + 1);
                                if (resultExp > 0) {
                                    int iExp = (int) resultExp;
                                    float partialExp = resultExp - iExp;
                                    if (partialExp > 0 && partialExp > context.getRandom().nextFloat()) {
                                        iExp += 1;
                                    }
                                    if (iExp >= 1) {
                                        Vec3 blockPos = context.get(LootContextParams.ORIGIN);
                                        if (blockPos != null) {
                                            ServerLevel level = context.getLevel();
                                            level.addEntity(new ExperienceOrb(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), iExp));
                                        }
                                    }
                                }
                            }
                        }
                    });
                    return furnaceResult.map(Tuple::getA).orElse(stack);
                })
                .collect(Collectors.toList());
    }

    public static class Serializer extends GlobalLootModifierSerializer<LootModifierScorchingHeat> {

        @Override
        public LootModifierScorchingHeat read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            return new LootModifierScorchingHeat(conditions);
        }

        @Override
        public JsonObject write(LootModifierScorchingHeat instance) {
            return this.makeConditions(instance.conditions);
        }
    }
}
