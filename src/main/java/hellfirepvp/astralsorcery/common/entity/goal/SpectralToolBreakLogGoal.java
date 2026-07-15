/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.goal;

import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectPelotrio;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SpectralToolBreakLogGoal
 * Created by HellFirePvP
 * Date: 22.02.2020 / 16:54
 */
public class SpectralToolBreakLogGoal extends SpectralToolGoal {

    private BlockPos selectedBreakPos = null;

    public SpectralToolBreakLogGoal(EntitySpectralTool entity, double speedModifier) {
        super(entity, speedModifier);
    }

    private BlockPredicate breakableLogs() {
        return (level, pos, state) -> {
            return MiscUtils.getTileAt(level, pos, BlockEntity.class, false) == null &&
                    pos.getY() >= this.getEntity().getStartPosition().getY() &&
                    !state.isAir() &&
                    state.getDestroySpeed(level, pos) != -1 &&
                    state.getDestroySpeed(level, pos) <= 10 &&
                    (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) &&
                    BlockUtils.canToolBreakBlockWithoutPlayer(level, pos, state, new ItemStack(Items.DIAMOND_AXE));
        };
    }

    @Override
    public boolean canUse() {
        MoveControl ctrl = this.getEntity().getMoveControl();

        if (!ctrl.hasWanted()) {
            return true;
        } else {
            BlockPos validPos = BlockDiscoverer.searchAreaForFirst(
                    this.getEntity().getCommandSenderWorld(),
                    this.getEntity().getStartPosition(),
                    8,
                    Vector3.atEntityCorner(this.getEntity()),
                    this.breakableLogs());
            return validPos != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.selectedBreakPos != null;
    }

    @Override
    public void start() {
        super.start();

        BlockPos validPos = BlockDiscoverer.searchAreaForFirst(
                this.getEntity().getCommandSenderWorld(),
                this.getEntity().getStartPosition(),
                10,
                Vector3.atEntityCorner(this.getEntity()),
                this.breakableLogs());

        if (validPos != null) {
            this.selectedBreakPos = validPos;

            this.getEntity().getMoveControl().setWantedPosition(
                    this.selectedBreakPos.getX() + 0.5,
                    this.selectedBreakPos.getY() + 0.5,
                    this.selectedBreakPos.getZ() + 0.5,
                    this.getSpeedModifier());
        }
    }

    @Override
    public void stop() {
        super.stop();

        this.selectedBreakPos = null;
        this.actionCooldown = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!canContinueToUse()) {
            return;
        }

        if (this.actionCooldown < 0) {
            this.actionCooldown = 0; //lol. wtf.
        }

        Level level = this.getEntity().getCommandSenderWorld();
        boolean resetTimer = false;

        if (level.isEmptyBlock(this.selectedBreakPos)) {
            this.selectedBreakPos = null;
            resetTimer = true;
        } else {
            this.getEntity().getMoveControl().setWantedPosition(
                    this.selectedBreakPos.getX() + 0.5,
                    this.selectedBreakPos.getY() + 0.5,
                    this.selectedBreakPos.getZ() + 0.5,
                    this.getSpeedModifier());

            if (Vector3.atEntityCorner(this.getEntity()).distanceSquared(this.selectedBreakPos) <= 9) {
                this.actionCooldown++;
                if (this.actionCooldown >= MantleEffectPelotrio.CONFIG.ticksPerAxeLogBreak.get() && level instanceof ServerLevel) {
                    LivingEntity owner = this.getEntity().getOwningEntity();
                    if (owner instanceof Player) {
                        BlockDropCaptureAssist.startCapturing();
                    }
                    if (BlockUtils.breakBlockWithoutPlayer(
                            (ServerLevel) level,
                            this.selectedBreakPos,
                            level.getBlockState(this.selectedBreakPos),
                            this.getEntity().getItem(),
                            true,
                            true,
                            true)) {
                        resetTimer = true;
                    }
                    if (owner instanceof Player) {
                        for (ItemStack dropped : BlockDropCaptureAssist.getCapturedStacksAndStop()) {
                            ItemStack remainder = ItemUtils.dropItemToPlayer((Player) owner, dropped);
                            if (!remainder.isEmpty()) {
                                ItemUtils.dropItemNaturally(level, owner.getX(), owner.getY(), owner.getZ(), remainder);
                            }
                        }
                    }
                }
            }
        }

        if (resetTimer) {
            this.actionCooldown = 0;
        }
    }
}
