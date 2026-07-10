/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFakedState
 * Created by HellFirePvP
 * Date: 04.09.2020 / 19:19
 */
public abstract class TileFakedState extends TileEntityTick {

    private BlockState fakedState = Blocks.AIR.defaultBlockState();
    private Color overlayColor = Color.WHITE;

    protected TileFakedState(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public boolean revert() {
        if (this.getLevel().isClientSide()) {
            return false;
        }
        return this.getLevel().setBlock(this.getBlockPos(), this.getFakedState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    @Nonnull
    public BlockState getFakedState() {
        return fakedState;
    }

    @Nonnull
    public Color getOverlayColor() {
        return overlayColor;
    }

    public void setFakedState(@Nonnull BlockState fakedState) {
        this.fakedState = fakedState;
        this.markForUpdate();
    }

    public void setOverlayColor(@Nonnull Color overlayColor) {
        this.overlayColor = overlayColor;
        this.markForUpdate();
    }

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);

        this.fakedState = NBTHelper.getBlockStateFromTag(pattern.getCompound("fakedState"), Blocks.AIR.defaultBlockState());
        this.overlayColor = new Color(pattern.getInt("color"), false);
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        NBTHelper.setBlock(pattern, "fakedState", this.fakedState);
        pattern.putInt("color", this.overlayColor.getRGB());
    }

    @OnlyIn(Dist.CLIENT)
    public double getViewDistance() {
        return 65536.0D;
    }
}
