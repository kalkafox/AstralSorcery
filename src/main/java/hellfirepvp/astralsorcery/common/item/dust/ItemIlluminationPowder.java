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
        BlockPos at = dispenser.getBlockPos();
        Direction face = dispenser.getBlockState().get(DispenserBlock.FACING);
        EntityIlluminationSpark nocSpark = new EntityIlluminationSpark(at.getX(), at.getY(), at.getZ(), dispenser.getWorld());
        nocSpark.shoot(face.getXOffset(), face.getYOffset() + 0.1F, face.getZOffset(), 0.7F, 0.9F);
        return dispenser.getWorld().addEntity(nocSpark);
    }

    @Override
    boolean rightClickAir(Level world, Player player, ItemStack dust) {
        return world.addEntity(new EntityIlluminationSpark(player, world));
    }

    @Override
    boolean rightClickBlock(UseOnContext ctx) {
        Level world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        Player player = ctx.getPlayer();
        if (player == null) {
            return false;
        }

        if (!BlockUtils.isReplaceable(world, pos)) {
            pos = pos.offset(ctx.getFace());
        }

        if (!BlockUtils.isReplaceable(world, pos)) {
            return false;
        }

        if (player.canPlayerEdit(pos, ctx.getFace(), ctx.getItem()) && !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(world.getDimensionKey(), world, pos), ctx.getFace())) {
            return world.setBlockState(pos, BlocksAS.FLARE_LIGHT.getDefaultState());
        }
        return false;
    }
}
