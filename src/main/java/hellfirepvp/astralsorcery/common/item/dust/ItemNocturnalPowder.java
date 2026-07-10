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
        EntityNocturnalSpark nocSpark = new EntityNocturnalSpark(at.getX(), at.getY(), at.getZ(), dispenser.getLevel());
        nocSpark.shoot(face.getXOffset(), face.getMyRidingOffset() + 0.1F, face.getZOffset(), 0.7F, 0.9F);
        return dispenser.getLevel().addEntity(nocSpark);
    }

    @Override
    boolean rightClickAir(Level level, Player player, ItemStack dust) {
        return level.addEntity(new EntityNocturnalSpark(player, level));
    }

    @Override
    boolean rightClickBlock(UseOnContext ctx) {
        BlockPos pos = ctx.getBlockPos().offset(ctx.getFace());
        EntityNocturnalSpark noc = new EntityNocturnalSpark(ctx.getPlayer(), ctx.getLevel());
        noc.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        noc.setSpawning();
        return ctx.getLevel().addEntity(noc);
    }
}
