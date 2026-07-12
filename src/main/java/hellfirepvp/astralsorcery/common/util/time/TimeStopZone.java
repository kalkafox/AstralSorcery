/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.time;

import hellfirepvp.astralsorcery.common.data.config.registry.TileAccelerationBlacklistRegistry;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TimeStopZone
 * Created by HellFirePvP
 * Date: 31.08.2019 / 13:30
 */
public class TimeStopZone {

    final EntityTargetController targetController;

    final float range;
    final BlockPos offset;
    private final Level level;
    private int ticksToLive;

    private boolean active = true;

    private final List<BlockEntity> cachedTiles = new LinkedList<>();

    TimeStopZone(EntityTargetController ctrl, float range, BlockPos offset, Level level, int tickLivespan) {
        this.targetController = ctrl;
        this.range = range;
        this.offset = offset;
        this.level = level;
        this.ticksToLive = tickLivespan;
    }

    void onServerTick() {
        if (!active) return;
        this.ticksToLive--;

        int minX = Mth.floor((offset.getX() - range) / 16.0D);
        int maxX = Mth.floor((offset.getX() + range) / 16.0D);
        int minZ = Mth.floor((offset.getZ() - range) / 16.0D);
        int maxZ = Mth.floor((offset.getZ() + range) / 16.0D);

        for (int xx = minX; xx <= maxX; ++xx) {
            for (int zz = minZ; zz <= maxZ; ++zz) {
                LevelChunk ch = level.getChunk(xx, zz);
                if (!ch.isEmpty()) {
                    Map<BlockPos, BlockEntity> map = ch.getBlockEntities();
                    for (Map.Entry<BlockPos, BlockEntity> teEntry : map.entrySet()) {
                        BlockEntity te = teEntry.getValue();
                        if (TileAccelerationBlacklistRegistry.INSTANCE.canBeInfluenced(te) &&
                                te.getBlockPos().distSqr(offset) <= range * range &&
                                !te.isRemoved()) {
                            // 1.21 port: Level#tickableBlockEntities is gone (block-entity ticking now lives
                            // in a private LevelChunk map with no public add/remove accessor). The chunk's
                            // ticker wrapper only invokes tick() while !BlockEntity#isRemoved(), so toggling
                            // that flag directly freezes/resumes ticking without touching the tick registry.
                            te.setRemoved();
                            safeCacheTile(te);
                        }
                    }
                }
            }
        }
    }

    private void safeCacheTile(BlockEntity te) {
        if (te == null) return;

        for (BlockEntity tile : cachedTiles) {
            if (tile.getBlockPos().equals(te.getBlockPos())) {
                return;
            }
        }
        cachedTiles.add(te);
    }

    public void setTicksToLive(int ticksToLive) {
        this.ticksToLive = ticksToLive;
    }

    void stopEffect() {
        for (BlockEntity cached : cachedTiles) {
            BlockState state = level.getBlockState(cached.getBlockPos());
            if (state.getBlock() instanceof EntityBlock) {
                cached.clearRemoved();
            }
        }
        this.cachedTiles.clear();
        this.active = false;
    }

    boolean shouldDespawn() {
        return ticksToLive <= 0 || !active;
    }

    boolean interceptEntityTick(LivingEntity e) {
        return active && e != null && targetController.shouldFreezeEntity(e) && Vector3.atEntityCorner(e).distance(offset) <= range;
    }

    //Mainly because we still want to be able to do damage.
    static void handleImportantEntityTicks(LivingEntity e) {
        if (e.hurtTime > 0) {
            e.hurtTime--;
        }
        if (e.invulnerableTime > 0) {
            e.invulnerableTime--;
        }
        e.xo = e.getX();
        e.yo = e.getY();
        e.zo = e.getZ();
        // 1.21 port: animationSpeed/animationSpeedOld are gone - walking animation interpolation now
        // lives in the encapsulated Entity#walkAnimation (WalkAnimationState), which only advances via
        // its own update(...) call (not invoked here, since movement/AI ticking is what's being frozen).
        e.yBodyRotO = e.yBodyRot;
        e.xRotO = e.getXRot();
        e.yRotO = e.getYRot();
        e.yHeadRotO = e.yHeadRot;
        e.oAttackAnim = e.attackAnim;
        e.walkDistO = e.walkDist;

        if (!e.getCommandSenderWorld().isClientSide()) {
            e.travel(Vec3.ZERO);
        }

        if (e instanceof EnderDragon) {
            DragonPhaseInstance currentPhase = ((EnderDragon) e).getPhaseManager().getCurrentPhase();
            if (currentPhase.getPhase() != EnderDragonPhase.HOLDING_PATTERN &&
                    currentPhase.getPhase() != EnderDragonPhase.DYING) {
                ((EnderDragon) e).getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            }
        }
    }

    public static class EntityTargetController {

        final int ownerId;
        final boolean hasOwner;
        final boolean targetPlayers;

        EntityTargetController(int ownerId, boolean hasOwner, boolean targetPlayers) {
            this.ownerId = ownerId;
            this.hasOwner = hasOwner;
            this.targetPlayers = targetPlayers;
        }

        boolean shouldFreezeEntity(LivingEntity e) {
            if (!e.isAlive() || e.getHealth() <= 0) {
                return false;
            }
            if (e instanceof EnderDragon && ((EnderDragon) e).getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
                return false;
            }
            if (hasOwner && e.getId() == ownerId) {
                return false;
            }
            return targetPlayers || !(e instanceof Player);
        }

        public static EntityTargetController allExcept(Entity entity) {
            return new EntityTargetController(entity.getId(), true, true);
        }

        public static EntityTargetController noPlayers() {
            return new EntityTargetController(-1, false, false);
        }

        @Nonnull
        public CompoundTag serializeNBT() {
            CompoundTag out = new CompoundTag();
            out.putBoolean("targetPlayers", this.targetPlayers);
            out.putBoolean("hasOwner", this.hasOwner);
            out.putInt("ownerEntityId", this.ownerId);
            return out;
        }

        @Nonnull
        public static EntityTargetController deserializeNBT(CompoundTag cmp) {
            boolean targetPlayers = cmp.getBoolean("targetPlayers");
            boolean hasOwner = cmp.getBoolean("hasOwner");
            int ownerId = cmp.getInt("ownerEntityId");
            return new EntityTargetController(ownerId, hasOwner, targetPlayers);
        }

    }

}
