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
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CopyCrystalProperties
 * Created by HellFirePvP
 * Date: 16.08.2019 / 06:18
 */
public class CopyCrystalProperties extends LootItemConditionalFunction {

    public static final MapCodec<CopyCrystalProperties> CODEC = RecordCodecBuilder.mapCodec(
            inst -> commonFields(inst).apply(inst, CopyCrystalProperties::new)
    );

    private CopyCrystalProperties(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public LootItemFunctionType<CopyCrystalProperties> getType() {
        return LootAS.Functions.COPY_CRYSTAL_PROPERTIES;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity tile = context.getParam(LootContextParams.BLOCK_ENTITY);
            if (tile instanceof CrystalAttributeTile && stack.getItem() instanceof CrystalAttributeItem) {
                CrystalAttributes attr = ((CrystalAttributeTile) tile).getAttributes();
                if (attr == null) {
                    attr = ((CrystalAttributeTile) tile).getMissingAttributes();
                }
                ((CrystalAttributeItem) stack.getItem()).setAttributes(stack, attr);
            }
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyCrystalProperties::new);
    }
}
