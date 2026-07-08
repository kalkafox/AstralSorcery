/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RandomCrystalProperty
 * Created by HellFirePvP
 * Date: 21.07.2019 / 08:50
 */
public class RandomCrystalProperty extends LootItemConditionalFunction {

    private RandomCrystalProperty(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getFunctionType() {
        return LootAS.Functions.RANDOM_CRYSTAL_PROPERTIES;
    }

    @Override
    protected ItemStack doApply(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() instanceof CrystalAttributeGenItem) {
            CrystalAttributes attr = CrystalGenerator.generateNewAttributes(itemStack);
            ((CrystalAttributeGenItem) itemStack.getItem()).setAttributes(itemStack, attr);
        }
        return itemStack;
    }

    public static LootFunction.Builder<?> builder() {
        return builder(RandomCrystalProperty::new);
    }

    public static class Serializer extends LootFunction.Serializer<RandomCrystalProperty> {

        @Override
        public RandomCrystalProperty deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] iLootConditions) {
            return new RandomCrystalProperty(iLootConditions);
        }
    }
}
