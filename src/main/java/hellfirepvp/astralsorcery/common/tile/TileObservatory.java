/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.entity.technical.EntityObservatoryHelper;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.tile.NamedInventoryTile;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileObservatory
 * Created by HellFirePvP
 * Date: 15.02.2020 / 18:28
 */
public class TileObservatory extends TileEntityTick implements NamedInventoryTile {

    private UUID entityHelperRef;
    private Integer entityIdServerRef = null;

    public float observatoryYaw = 0, prevObservatoryYaw = 0;
    public float observatoryPitch = -45, prevObservatoryPitch = -45;

    public TileObservatory(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.OBSERVATORY, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.astralsorcery.observatory");
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide()) {
            if (this.entityHelperRef == null) {
                this.createNewObservatoryEntity();
            } else {
                Entity helper;
                if ((helper = resolveEntity(this.entityHelperRef)) == null || !helper.isAlive()) {
                    this.createNewObservatoryEntity();
                }
            }
        }
    }

    public boolean isFlyEnabled() {
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                if (xx == 0 && zz == 0) {
                    continue;
                }
                BlockPos other = getBlockPos().offset(xx, 0, zz);
                if (!MiscUtils.canSeeSky(this.getLevel(), other, false, true)) {
                    return false;
                }
            }
        }
        return MiscUtils.canSeeSky(this.getLevel(), this.getBlockPos().above(), true, false);
    }

    private Entity createNewObservatoryEntity() {
        this.setEntityHelperRef(null);
        this.entityIdServerRef = null;

        EntityObservatoryHelper helper = EntityTypesAS.OBSERVATORY_HELPER.create(this.getLevel());
        helper.setFixedObservatoryPos(this.getBlockPos());
        helper.moveTo(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.1, getBlockPos().getZ() + 0.5, 0,0);
        this.getLevel().addFreshEntity(helper);

        this.setEntityHelperRef(helper.getUUID());
        this.entityIdServerRef = helper.getId();
        return helper;
    }

    @Nullable
    private Entity resolveEntity(UUID entityUUID) {
        if (entityUUID == null) {
            return null;
        }
        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(Vec3.atLowerCornerOf(getBlockPos().offset(-3, -1, -3)), Vec3.atLowerCornerOf(getBlockPos().offset(3, 2, 3))))) {
            if (e.getUUID().equals(entityUUID)) {
                this.entityIdServerRef = e.getId();
                return e;
            }
        }
        return null;
    }

    @Nullable
    public Entity findRideableObservatoryEntity() {
        if (this.getEntityHelperRef() == null || this.entityIdServerRef == null) {
            return null;
        }
        return this.getLevel().getEntity(this.entityIdServerRef);
    }

    @Nullable
    public UUID getEntityHelperRef() {
        return entityHelperRef;
    }

    public void setEntityHelperRef(UUID entityHelperRef) {
        this.entityHelperRef = entityHelperRef;
        markForUpdate();
    }

    public void updatePitchYaw(float pitch, float prevPitch, float yRot, float prevYaw) {
        this.observatoryPitch = pitch;
        this.prevObservatoryPitch = prevPitch;
        this.observatoryYaw = yRot;
        this.prevObservatoryYaw = prevYaw;
    }

    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return AABB.INFINITE;
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.entityHelperRef = NBTHelper.getUUID(pattern, "entity", null);
        this.observatoryYaw = pattern.getFloat("oYaw");
        this.observatoryPitch = pattern.getFloat("oPitch");
        this.prevObservatoryYaw = pattern.getFloat("oYawPrev");
        this.prevObservatoryPitch = pattern.getFloat("oPitchPrev");
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        if(this.entityHelperRef != null) {
            pattern.putUUID("entity", this.entityHelperRef);
        }
        pattern.putFloat("oYaw", this.observatoryYaw);
        pattern.putFloat("oPitch", this.observatoryPitch);
        pattern.putFloat("oYawPrev", this.prevObservatoryYaw);
        pattern.putFloat("oPitchPrev", this.prevObservatoryPitch);
    }
}
