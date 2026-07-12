/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RandomCrystalProperty
 * Created by HellFirePvP
 * Date: 21.07.2019 / 08:50
 */
public class RandomCrystalProperty extends LootItemConditionalFunction {

    public static final MapCodec<RandomCrystalProperty> CODEC = RecordCodecBuilder.mapCodec(
            inst -> commonFields(inst).apply(inst, RandomCrystalProperty::new)
    );

    private RandomCrystalProperty(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType<RandomCrystalProperty> getType() {
        return LootAS.Functions.RANDOM_CRYSTAL_PROPERTIES;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext compositePredicates) {
        if (itemStack.getItem() instanceof CrystalAttributeGenItem) {
            CrystalAttributes attr = CrystalGenerator.generateNewAttributes(itemStack);
            ((CrystalAttributeGenItem) itemStack.getItem()).setAttributes(itemStack, attr);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(RandomCrystalProperty::new);
    }
}
