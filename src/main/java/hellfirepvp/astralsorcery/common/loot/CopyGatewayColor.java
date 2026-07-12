/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialGateway;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CopyGatewayColor
 * Created by HellFirePvP
 * Date: 12.09.2020 / 21:35
 */
public class CopyGatewayColor extends LootItemConditionalFunction {

    public static final MapCodec<CopyGatewayColor> CODEC = RecordCodecBuilder.mapCodec(
            inst -> commonFields(inst).apply(inst, CopyGatewayColor::new)
    );

    private CopyGatewayColor(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.newHashSet(LootContextParams.BLOCK_ENTITY);
    }

    @Override
    public LootItemFunctionType<CopyGatewayColor> getType() {
        return LootAS.Functions.COPY_GATEWAY_COLOR;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity tile = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof TileCelestialGateway) {
            ((TileCelestialGateway) tile).getColor().ifPresent(color -> {
                BlockCelestialGateway.setColor(stack, color);
            });
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyGatewayColor::new);
    }
}
