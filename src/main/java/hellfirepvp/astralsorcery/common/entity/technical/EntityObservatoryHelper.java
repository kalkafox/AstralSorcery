/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.technical;

import com.google.common.collect.Iterables;
import hellfirepvp.astralsorcery.common.container.ContainerObservatory;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityObservatoryHelper
 * Created by HellFirePvP
 * Date: 16.02.2020 / 09:00
 */
public class EntityObservatoryHelper extends Entity {

    private static final EntityDataAccessor<BlockPos> FIXED = SynchedEntityData.defineId(EntityObservatoryHelper.class, EntityDataSerializers.BLOCK_POS);

    public EntityObservatoryHelper(Level worldIn) {
        super(EntityTypesAS.OBSERVATORY_HELPER, worldIn);
    }

    public static EntityType.EntityFactory<EntityObservatoryHelper> factory() {
        return (spawnEntity, level) -> new EntityObservatoryHelper(level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FIXED, BlockPos.ZERO);
    }

    public void setFixedObservatoryPos(BlockPos pos) {
        this.entityData.set(FIXED, pos);
    }

    public BlockPos getFixedObservatoryPos() {
        return this.entityData.get(FIXED);
    }

    @Nullable
    public TileObservatory getAssociatedObservatory() {
        BlockPos at = this.getFixedObservatoryPos();
        TileObservatory observatory = MiscUtils.getTileAt(this.level(), at, TileObservatory.class, true);
        if (observatory == null) {
            return null;
        }
        UUID helperRef = observatory.getEntityHelperRef();
        if (helperRef == null || !helperRef.equals(this.getUUID())) {
            return null;
        }
        return observatory;
    }

    @Override
    public void tick() {
        super.tick();

        this.noPhysics = true;

        TileObservatory observatory;
        if ((observatory = this.getAssociatedObservatory()) == null) {
            if (!this.level().isClientSide()) {
                this.remove(RemovalReason.DISCARDED);
            }
            return;
        }

        Entity wasRiding = Iterables.getFirst(this.getPassengers(), null);
        if (wasRiding instanceof Player) {
            this.applyObservatoryRotationsFrom(observatory, (Player) wasRiding, true);
        } else {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        if (!observatory.isFlyEnabled()) {
            this.ejectPassengers();
        }
    }

    public void applyObservatoryRotationsFrom(TileObservatory to, Player wasRiding, boolean updateTile) {
        if (wasRiding.containerMenu instanceof ContainerObservatory) {
            //Adjust observatory pitch and jaw to player head
            this.setYRot(wasRiding.yHeadRot);
            this.yRotO = wasRiding.yHeadRotO;
            this.setXRot(wasRiding.getXRot());
            this.xRotO = wasRiding.xRotO;
        } else  {
            //Adjust observatory to player-body
            this.setYRot(wasRiding.yBodyRot);
            this.yRotO = wasRiding.yBodyRotO;
        }

        to.updatePitchYaw(this.getXRot(), this.xRotO, this.getYRot(), this.yRotO);
        if (updateTile) {
            to.markForUpdate();
        }

        double xOffset = -0.85;
        double zDist = 0.15;
        double yawRad = -Math.toRadians(to.observatoryYaw);
        double xComp = 0.5F + Math.sin(yawRad) * xOffset - Math.cos(yawRad) * zDist;
        double zComp = 0.5F + Math.cos(yawRad) * xOffset + Math.sin(yawRad) * zDist;
        Vector3 pos = new Vector3(to.getBlockPos()).add(xComp, 0.4F, zComp);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
        this.setOldPosAndRot();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if (!super.canAddPassenger(passenger)) {
            return false;
        }
        TileObservatory observatory = this.getAssociatedObservatory();
        return observatory != null && observatory.isFlyEnabled();
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(BlocksAS.OBSERVATORY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pattern) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag pattern) {}
}
