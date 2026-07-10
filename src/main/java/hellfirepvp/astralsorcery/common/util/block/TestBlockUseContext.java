/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TestBlockUseContext
 * Created by HellFirePvP
 * Date: 29.02.2020 / 12:07
 */
public class TestBlockUseContext extends BlockPlaceContext {

    private final Entity entity;

    private TestBlockUseContext(Level worldIn, @Nullable Entity usingEntity, InteractionHand hand, ItemStack stack, BlockPos at, Direction direction) {
        super(worldIn, null, hand, stack, new BlockHitResult(Vec3.copyCentered(at), direction, at, false));
        this.entity = usingEntity;
    }

    public static BlockPlaceContext getHandContext(Level worldIn, @Nullable Entity usingEntity, InteractionHand usedHand, BlockPos at, Direction direction) {
        return getHandContextWithItem(worldIn, usingEntity, usedHand, ItemStack.EMPTY, at, direction);
    }

    public static BlockPlaceContext getHandContextWithItem(Level worldIn, @Nullable Entity usingEntity, InteractionHand usedHand, ItemStack stack, BlockPos at, Direction direction) {
        return new TestBlockUseContext(worldIn, usingEntity, usedHand, stack, at, direction);
    }

    @Override
    public Direction getHorizontalDirection() {
        return this.entity == null ? Direction.NORTH : Direction.fromAngle(this.entity.getYRot());
    }

    @Override
    public Direction getNearestLookingDirection() {
        return Direction.getFacingDirections(this.entity)[0];
    }

    @Override
    public Direction[] getNearestLookingDirections() {
        Direction[] adirection = Direction.getFacingDirections(this.entity);
        if (this.replaceClicked) {
            return adirection;
        } else {
            Direction direction = this.getFace();

            int i;
            i = 0;
            while (i < adirection.length && adirection[i] != direction.getOpposite()) {
                ++i;
            }

            if (i > 0) {
                System.arraycopy(adirection, 0, adirection, 1, i);
                adirection[0] = direction.getOpposite();
            }
            return adirection;
        }
    }

    @Override
    public boolean isSecondaryUseActive() {
        return false;
    }

    @Override
    public float getRotation() {
        return 0F;
    }
}
