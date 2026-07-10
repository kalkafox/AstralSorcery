/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.neoforged.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityItemHighlighted
 * Created by HellFirePvP
 * Date: 18.08.2019 / 10:25
 */
public class EntityItemHighlighted extends EntityCustomItemReplacement {

    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.createKey(EntityItemHighlighted.class, EntityDataSerializers.INT);
    private static final int NO_COLOR = 0xFF000000;

    public EntityItemHighlighted(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        ReflectionHelper.setSkipItemPhysicsRender(this);
        refreshDimensions();
    }

    public EntityItemHighlighted(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z) {
        this(type, level);
        this.setPosition(x, y, z);
        this.setYRot(this.random.nextFloat() * 360.0F);
        this.setDeltaMovement(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
    }

    public EntityItemHighlighted(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z, ItemStack stack) {
        this(type, level, x, y, z);
        this.setItem(stack);
        this.timeout = stack.isEmpty() ? 6000 : stack.getEntityLifespan(level);
    }

    public static EntityType.IFactory<EntityItemHighlighted> factoryHighlighted() {
        return (spawnEntity, level) -> new EntityItemHighlighted(EntityTypesAS.ITEM_HIGHLIGHT, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().register(DATA_COLOR, NO_COLOR);
    }

    public void applyColor(@Nullable Color color) {
        this.getEntityData().set(DATA_COLOR, color == null ? NO_COLOR : (color.getRGB() & 0x00FFFFFF));
    }

    public boolean hasCustomColor() {
        return this.getEntityData().get(DATA_COLOR) != NO_COLOR;
    }

    @Nullable
    public Color getHighlightColor() {
        if (!hasCustomColor()) {
            return null;
        }
        int colorInt = this.getEntityData().get(DATA_COLOR);
        return new Color(colorInt, false);
    }

    @Override
    public void tick() {
        boolean onGround = this.isOnGround();
        super.tick();
        if (this.isOnGround() != onGround) {
            refreshDimensions();
        }
    }

    @Override
    public void setOnGround(boolean grounded) {
        boolean updateSize = isOnGround() != grounded;
        super.setOnGround(grounded);
        if (updateSize) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getSize(Pose poseIn) {
        if (!this.isOnGround()) {
            return EntityType.ITEM.getSize();
        }
        return this.getType().getSize();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
