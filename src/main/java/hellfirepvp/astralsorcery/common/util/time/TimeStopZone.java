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
    private final Level world;
    private int ticksToLive;

    private boolean active = true;

    private final List<BlockEntity> cachedTiles = new LinkedList<>();

    TimeStopZone(EntityTargetController ctrl, float range, BlockPos offset, Level world, int tickLivespan) {
        this.targetController = ctrl;
        this.range = range;
        this.offset = offset;
        this.world = world;
        this.ticksToLive = tickLivespan;
    }

    void onServerTick() {
        if (!active) return;
        this.ticksToLive--;

        int minX = MathHelper.floor((offset.getX() - range) / 16.0D);
        int maxX = MathHelper.floor((offset.getX() + range) / 16.0D);
        int minZ = MathHelper.floor((offset.getZ() - range) / 16.0D);
        int maxZ = MathHelper.floor((offset.getZ() + range) / 16.0D);

        for (int xx = minX; xx <= maxX; ++xx) {
            for (int zz = minZ; zz <= maxZ; ++zz) {
                LevelChunk ch = world.getChunk(xx, zz);
                if (!ch.isEmpty()) {
                    Map<BlockPos, BlockEntity> map = ch.getTileEntityMap();
                    for (Map.Entry<BlockPos, BlockEntity> teEntry : map.entrySet()) {
                        BlockEntity te = teEntry.getValue();
                        if (TileAccelerationBlacklistRegistry.INSTANCE.canBeInfluenced(te) &&
                                te.getPos().withinDistance(offset, range) &&
                                world.tickableTileEntities.contains(te)) {
                            world.tickableTileEntities.remove(te);
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
            if (tile.getPos().equals(te.getPos())) {
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
            BlockState state = world.getBlockState(cached.getPos());
            if (state.getBlock().hasTileEntity(state)) {
                BlockEntity te = state.getBlock().createTileEntity(state, world);
                if (te != null && te.getClass().isAssignableFrom(cached.getClass())) {
                    world.tickableTileEntities.add(cached);
                }
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
        if (e.hurtResistantTime > 0) {
            e.hurtResistantTime--;
        }
        e.prevPosX = e.getPosX();
        e.prevPosY = e.getPosY();
        e.prevPosZ = e.getPosZ();
        e.prevLimbSwingAmount = e.limbSwingAmount;
        e.prevRenderYawOffset = e.renderYawOffset;
        e.prevRotationPitch = e.rotationPitch;
        e.prevRotationYaw = e.rotationYaw;
        e.prevRotationYawHead = e.rotationYawHead;
        e.prevSwingProgress = e.swingProgress;
        e.prevDistanceWalkedModified = e.distanceWalkedModified;

        if (!e.getEntityWorld().isRemote()) {
            e.travel(Vector3d.ZERO);
        }

        if (e instanceof EnderDragon) {
            DragonPhaseInstance phase = ((EnderDragon) e).getPhaseManager().getCurrentPhase();
            if (phase.getType() != PhaseType.HOLDING_PATTERN &&
                    phase.getType() != PhaseType.DYING) {
                ((EnderDragon) e).getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
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
            if (e instanceof EnderDragon && ((EnderDragon) e).getPhaseManager().getCurrentPhase().getType() == PhaseType.DYING) {
                return false;
            }
            if (hasOwner && e.getEntityId() == ownerId) {
                return false;
            }
            return targetPlayers || !(e instanceof Player);
        }

        public static EntityTargetController allExcept(Entity entity) {
            return new EntityTargetController(entity.getEntityId(), true, true);
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