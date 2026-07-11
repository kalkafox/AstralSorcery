/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.dust;

import hellfirepvp.astralsorcery.common.entity.EntityIlluminationSpark;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.ForgeEventFactory;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemIlluminationPowder
 * Created by HellFirePvP
 * Date: 17.08.2019 / 13:54
 */
public class ItemIlluminationPowder extends ItemUsableDust {

    @Override
    boolean dispense(BlockSource dispenser) {
        BlockPos at = dispenser.pos();
        Direction face = dispenser.getBlockState().get(DispenserBlock.FACING);
        EntityIlluminationSpark nocSpark = new EntityIlluminationSpark(at.getX(), at.getY(), at.getZ(), dispenser.level());
        nocSpark.shoot(face.getXOffset(), face.getMyRidingOffset() + 0.1F, face.getZOffset(), 0.7F, 0.9F);
        return dispenser.level().addFreshEntity(nocSpark);
    }

    @Override
    boolean rightClickAir(Level level, Player player, ItemStack dust) {
        return level.addEntity(new EntityIlluminationSpark(player, level));
    }

    @Override
    boolean rightClickBlock(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (player == null) {
            return false;
        }

        if (!BlockUtils.isReplaceable(level, pos)) {
            pos = pos.offset(ctx.getClickedFace());
        }

        if (!BlockUtils.isReplaceable(level, pos)) {
            return false;
        }

        if (player.mayUseItemAt(pos, ctx.getClickedFace(), ctx.getItemInHand()) && !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), ctx.getClickedFace())) {
            return level.setBlock(pos, BlocksAS.FLARE_LIGHT.defaultBlockState());
        }
        return false;
    }
}
