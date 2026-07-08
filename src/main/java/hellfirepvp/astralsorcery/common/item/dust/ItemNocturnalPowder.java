/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.dust;

import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemNocturnalPowder
 * Created by HellFirePvP
 * Date: 17.08.2019 / 10:59
 */
public class ItemNocturnalPowder extends ItemUsableDust {

    @Override
    boolean dispense(BlockSource dispenser) {
        BlockPos at = dispenser.getBlockPos();
        Direction face = dispenser.getBlockState().get(DispenserBlock.FACING);
        EntityNocturnalSpark nocSpark = new EntityNocturnalSpark(at.getX(), at.getY(), at.getZ(), dispenser.getWorld());
        nocSpark.shoot(face.getXOffset(), face.getYOffset() + 0.1F, face.getZOffset(), 0.7F, 0.9F);
        return dispenser.getWorld().addEntity(nocSpark);
    }

    @Override
    boolean rightClickAir(Level world, Player player, ItemStack dust) {
        return world.addEntity(new EntityNocturnalSpark(player, world));
    }

    @Override
    boolean rightClickBlock(UseOnContext ctx) {
        BlockPos pos = ctx.getPos().offset(ctx.getFace());
        EntityNocturnalSpark noc = new EntityNocturnalSpark(ctx.getPlayer(), ctx.getWorld());
        noc.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        noc.setSpawning();
        return ctx.getWorld().addEntity(noc);
    }
}
