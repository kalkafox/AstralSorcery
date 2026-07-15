/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.block;

import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlockGemCrystalCluster
 * Created by HellFirePvP
 * Date: 17.05.2020 / 09:26
 */
public class ItemBlockGemCrystalCluster extends ItemBlockCustom {

    public ItemBlockGemCrystalCluster(Block block, Properties itemProperties) {
        super(block, itemProperties);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState toPlace = super.getPlacementState(context);
        if (toPlace != null) {
            return toPlace.setValue(BlockGemCrystalCluster.STAGE, this.getGrowthStage(context.getItemInHand()));
        }
        return null;
    }

    @Nonnull
    private BlockGemCrystalCluster.GrowthStageType getGrowthStage(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlockGemCrystalCluster)) {
            return BlockGemCrystalCluster.GrowthStageType.STAGE_0;
        }
        return MiscUtils.getEnumEntry(BlockGemCrystalCluster.GrowthStageType.class, stack.getDamageValue());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        BlockGemCrystalCluster.GrowthStageType stage = this.getGrowthStage(stack);
        switch (stage) {
            case STAGE_2_SKY:
                return super.getDescriptionId(stack) + ".sky";
            case STAGE_2_DAY:
                return super.getDescriptionId(stack) + ".day";
            case STAGE_2_NIGHT:
                return super.getDescriptionId(stack) + ".night";
        }
        return super.getDescriptionId(stack);
    }
}
