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
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationTile;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CopyConstellation
 * Created by HellFirePvP
 * Date: 16.08.2019 / 06:38
 */
public class CopyConstellation extends LootItemConditionalFunction {

    private CopyConstellation(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.COPY_CONSTELLATION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.has(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity tile = context.get(LootContextParams.BLOCK_ENTITY);
            if (tile instanceof ConstellationTile && stack.getItem() instanceof ConstellationItem) {
                IWeakConstellation main = ((ConstellationTile) tile).getAttunedConstellation();
                IMinorConstellation trait = ((ConstellationTile) tile).getTraitConstellation();

                ((ConstellationItem) stack.getItem()).setAttunedConstellation(stack, main);
                ((ConstellationItem) stack.getItem()).setTraitConstellation(stack, trait);
            }
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return builder(CopyConstellation::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyConstellation> {

        @Override
        public CopyConstellation deserialize(JsonObject jsonObject, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new CopyConstellation(conditions);
        }
    }
}
