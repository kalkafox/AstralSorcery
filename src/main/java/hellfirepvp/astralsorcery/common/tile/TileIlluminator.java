/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileIlluminator
 * Created by HellFirePvP
 * Date: 31.08.2019 / 22:44
 */
public class TileIlluminator extends TileEntityTick {

    public static final LightCheck ILLUMINATOR_CHECK = new LightCheck();

    public static final int SEARCH_RADIUS = 64;
    public static final int STEP_WIDTH = 2;

    private List<List<BlockPos>> layerPositions = null;
    private boolean doRecalculation = false;
    private int ticksUntilNextPlacement = 180;
    private boolean playerPlaced = false;
    private int boostedTicks = 0;

    private DyeColor color = DyeColor.YELLOW;

    public TileIlluminator(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.ILLUMINATOR, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isPlayerPlaced()) {
            return; //Don't do anything if it's not specifically made as player-placed
        }

        if (!level.isClientSide()) {
            if (layerPositions == null) {
                refresh();
            }
            placeFlare();
            placeFlare();
            placeFlare();
            if (random.nextInt(3) == 0 && placeFlare()) {
                doRecalculation = true;
            }
            if (boostedTicks > 0) {
                boostedTicks--;
            }
            ticksUntilNextPlacement--;
            if (ticksUntilNextPlacement <= 0) {
                ticksUntilNextPlacement = boostedTicks > 0 ? 30 : 180;
                if (doRecalculation) {
                    doRecalculation = false;
                    refresh();
                }
            }
        }

        if (level.isClientSide()) {
            this.tickEffects();
        }
    }

    private void refresh() {
        int height = Math.max(0, getBlockPos().getY() - 7);
        int parts = height / 7;
        layerPositions = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            int yPart = 3 + i * 7;
            List<BlockPos> positions = new ArrayList<>();
            generatePositions(positions, new BlockPos(getBlockPos().getX(), yPart, getBlockPos().getZ()));
            layerPositions.add(positions);
        }
    }

    private void generatePositions(List<BlockPos> positions, BlockPos center) {
        int x = center.getX();
        int y = center.getY();
        int zPos = center.getZ();
        BlockPos currentPos = center;
        if (!positions.contains(currentPos)) {
            positions.add(currentPos);
        }

        Direction dir = Direction.NORTH;
        while (Math.abs(currentPos.getX() - x) <= SEARCH_RADIUS &&
                Math.abs(currentPos.getY() - y) <= SEARCH_RADIUS &&
                Math.abs(currentPos.getZ() - zPos) <= SEARCH_RADIUS) {
            currentPos = currentPos.relative(dir, STEP_WIDTH);
            if (!positions.contains(currentPos)) {
                positions.add(currentPos);
            }
            Direction tryDirNext = dir.getClockWise();
            if (!positions.contains(currentPos.relative(tryDirNext, STEP_WIDTH))) {
                dir = tryDirNext;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffects() {
        if (!this.doesSeeSky() && this.boostedTicks <= 0) {
            return;
        }
        FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(this).add(0.5, 0.5, 0.5))
                .setScaleMultiplier(0.25F)
                .setDeltaMovement(new Vector3(random.nextFloat() * 0.025F * (random.nextBoolean() ? 1 : -1),
                        random.nextFloat() * 0.025F * (random.nextBoolean() ? 1 : -1),
                        random.nextFloat() * 0.025F * (random.nextBoolean() ? 1 : -1)));
        Color c = ColorUtils.flareColorFromDye(this.getColor());
        p.color(VFXColorFunction.constant(MiscUtils.eitherOf(random,
                Color.WHITE, c.brighter().brighter(), c)));

        if (this.boostedTicks > 0) {
            if (this.tickCount % 4 == 0) {
                Collection<Vector3> positions = MiscUtils.getCirclePositions(
                        new Vector3(this).add(0.5, 0.5, 0.5),
                        Vector3.RotAxis.Y_AXIS, 0.8F + random.nextFloat() * 0.1F, 20 + random.nextInt(10));

                for (Vector3 v : positions) {
                    p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                            .spawn(v)
                            .setScaleMultiplier(0.15F)
                            .setDeltaMovement(new Vector3(0, (random.nextBoolean() ? 1 : -1) * random.nextFloat() * 0.01, 0));
                    p.color(VFXColorFunction.constant(MiscUtils.eitherOf(random,
                            Color.WHITE, c.brighter().brighter(), c)));
                }
            }
        }
    }

    private boolean placeFlare() {
        boolean recalc = false;
        for (List<BlockPos> list : layerPositions) {
            if (list.isEmpty()) {
                recalc = true;
                continue;
            }

            int index = random.nextInt(list.size());
            BlockPos at = list.remove(index);
            if (!recalc && list.isEmpty()) {
                recalc = true;
            }
            at = at.offset(random.nextInt(5) - 2, random.nextInt(13) - 6, random.nextInt(5) - 2);
            MiscUtils.executeWithChunk(level, at, at, (pos) -> {
                if (this.doesSeeSky() && TileIlluminator.ILLUMINATOR_CHECK.test(level, pos, level.getBlockState(pos))) {
                    DyeColor color = this.getColor();
                    BlockState toPlace = BlocksAS.FLARE_LIGHT.defaultBlockState().setValue(BlockFlareLight.COLOR, color);
                    if (level.setBlockAndUpdate(pos, toPlace)) {
                        EntityFlare.spawnAmbientFlare(level, this.getBlockPos());
                    }
                }
            });
        }
        return recalc;
    }

    public void setColor(DyeColor color) {
        this.color = color;
        this.markForUpdate();
    }

    public DyeColor getColor() {
        return color;
    }

    public void setPlayerPlaced(boolean playerPlaced) {
        this.playerPlaced = playerPlaced;
        this.markForUpdate();
    }

    public boolean isPlayerPlaced() {
        return playerPlaced;
    }

    public void onWandUsed(ItemStack stack) {
        this.boostedTicks = 10 * 60 * 20;
        this.setColor(ItemIlluminationWand.getConfiguredColor(stack));
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        pattern.putBoolean("playerPlaced", this.playerPlaced);
        pattern.putInt("color", this.color.getId());
        pattern.putInt("boostedTicks", this.boostedTicks);
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.playerPlaced = pattern.getBoolean("playerPlaced");
        this.color = DyeColor.byId(pattern.getInt("color"));
        this.boostedTicks = pattern.getInt("boostedTicks");
    }

    public static class LightCheck implements BlockPredicate {

        @Override
        public boolean test(Level level, BlockPos pos, BlockState state) {
            return level.isEmptyBlock(pos) &&
                    !MiscUtils.canSeeSky(level, pos, false, false) &&
                    level.getMaxLocalRawBrightness(pos) < 8 &&
                    level.getBrightness(LightLayer.SKY, pos) < 4;
        }

    }
}
