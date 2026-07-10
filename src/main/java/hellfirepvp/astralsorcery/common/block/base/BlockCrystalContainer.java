/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCrystalContainer
 * Created by HellFirePvP
 * Date: 30.09.2019 / 18:02
 */
public abstract class BlockCrystalContainer extends BaseEntityBlock {

    protected BlockCrystalContainer(Properties builder) {
        super(builder);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = super.getPickBlock(state, target, level, pos, player);
        if (stack.getItem() instanceof CrystalAttributeItem) {
            CrystalAttributeTile cat = MiscUtils.getTileAt(level, pos, CrystalAttributeTile.class, true);
            if (cat != null) {
                ((CrystalAttributeItem) stack.getItem()).setAttributes(stack, cat.getAttributes());
            }
        }
        if (stack.getItem() instanceof ConstellationItem) {
            ConstellationTile ct = MiscUtils.getTileAt(level, pos, ConstellationTile.class, true);
            if (ct != null) {
                ((ConstellationItem) stack.getItem()).setAttunedConstellation(stack, ct.getAttunedConstellation());
                ((ConstellationItem) stack.getItem()).setTraitConstellation(stack, ct.getTraitConstellation());
            }
        }
        return stack;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Item i = stack.getItem();
        if (i instanceof CrystalAttributeItem) {
            CrystalAttributeTile cat = MiscUtils.getTileAt(level, pos, CrystalAttributeTile.class, true);
            if (cat != null) {
                cat.setAttributes(((CrystalAttributeItem) i).getAttributes(stack));
            }
        }
        if (i instanceof ConstellationItem) {
            ConstellationTile ct = MiscUtils.getTileAt(level, pos, ConstellationTile.class, true);
            if (ct != null) {
                ct.setAttunedConstellation(((ConstellationItem) i).getAttunedConstellation(stack));
                ct.setTraitConstellation(((ConstellationItem) i).getTraitConstellation(stack));
            }
        }
    }

}
