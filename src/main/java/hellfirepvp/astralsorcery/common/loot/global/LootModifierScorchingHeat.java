/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot.global;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.loot.LootUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
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
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LootModifierScorchingHeat
 * Created by HellFirePvP
 * Date: 08.05.2020 / 19:17
 */
public class LootModifierScorchingHeat extends LootModifier {

    public static final MapCodec<LootModifierScorchingHeat> CODEC = RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).apply(inst, LootModifierScorchingHeat::new)
    );

    private LootModifierScorchingHeat(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> lootTable, LootContext context) {
        if (!LootUtil.doesContextFulfillSet(context, LootContextParamSets.BLOCK)) {
            return lootTable;
        }
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        for (ItemStack stack : lootTable) {
            if (stack.isEmpty()) {
                continue;
            }
            Optional<Tuple<ItemStack, Float>> furnaceResult = RecipeHelper.findSmeltingResult(context.getLevel(), stack);
            if (context.hasParam(LootContextParams.THIS_ENTITY)) {
                Entity e = context.getParam(LootContextParams.THIS_ENTITY);
                if (e instanceof Player) {
                    furnaceResult.ifPresent(smeltResult -> NeoForge.EVENT_BUS.post(new PlayerEvent.ItemSmeltedEvent((Player) e, smeltResult.getA())));
                }
            }
            furnaceResult.ifPresent(smeltResult -> {
                ItemStack resultStack = smeltResult.getA();
                float resultExp = smeltResult.getB();

                ItemStack tool = context.getParam(LootContextParams.TOOL);
                if (!tool.isEmpty() && !(resultStack.getItem() instanceof BlockItem)) {
                    Holder<Enchantment> silkTouchEnch = context.getLevel().registryAccess()
                            .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH);
                    int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(silkTouchEnch, tool);
                    if (silkTouch <= 0) {
                        int addedCount = 0;
                        Holder<Enchantment> fortuneEnch = context.getLevel().registryAccess()
                                .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE);
                        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(fortuneEnch, tool);
                        if (fortuneLevel > 0) {
                            addedCount = Math.max(context.getRandom().nextInt(fortuneLevel + 2) - 1, 0);
                            resultStack.setCount(resultStack.getCount() * (addedCount + 1));
                        }

                        float scaledExp = resultExp * (addedCount + 1);
                        if (scaledExp > 0) {
                            int iExp = (int) scaledExp;
                            float partialExp = scaledExp - iExp;
                            if (partialExp > 0 && partialExp > context.getRandom().nextFloat()) {
                                iExp += 1;
                            }
                            if (iExp >= 1) {
                                Vec3 blockPos = context.getParamOrNull(LootContextParams.ORIGIN);
                                if (blockPos != null) {
                                    ServerLevel level = context.getLevel();
                                    level.addFreshEntity(new ExperienceOrb(level, blockPos.x(), blockPos.y(), blockPos.z(), iExp));
                                }
                            }
                        }
                    }
                }
            });
            result.add(furnaceResult.map(Tuple::getA).orElse(stack));
        }
        return result;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
