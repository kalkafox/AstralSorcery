/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.foliage;

import hellfirepvp.astralsorcery.common.block.base.template.BlockFlowerTemplate;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.common.PlantType;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockGlowFlower
 * Created by HellFirePvP
 * Date: 21.07.2019 / 09:23
 */
public class BlockGlowFlower extends BlockFlowerTemplate implements IPlantable {

    private final VoxelShape shape;

    public BlockGlowFlower() {
        super(PropertiesMisc.defaultTickingPlant()
                .isRedstoneConductor(state -> 5));
        this.shape = createShape();
    }

    private VoxelShape createShape() {
        return Block.box(1.5, 0, 1.5, 14.5, 13, 14.5);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Vec3 offset = state.getOffset(level, pos);
        return this.shape.offset(offset.x, offset.y, offset.z);
    }

    @Nonnull
    @Override
    public MobEffect getStewEffect() {
        return MobEffects.LUCK;
    }

    @Override
    public int getEffectDuration() {
        return 40;
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader level, BlockPos pos, int fortune, int silktouch) {
        if (silktouch == 0) {
            return 0;
        }
        if (fortune > 0) {
            return fortune * Mth.nextInt(RANDOM, 2, 5);
        }
        return Mth.nextInt(RANDOM, 1, 2);
    }

    @Override
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.CAVE;
    }

}
