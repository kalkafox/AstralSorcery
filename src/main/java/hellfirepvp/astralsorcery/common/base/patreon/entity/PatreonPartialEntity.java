/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.entity;

import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffectHelper;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PatreonPartialEntity
 * Created by HellFirePvP
 * Date: 30.08.2019 / 17:52
 */
public class PatreonPartialEntity {

    protected static final Random rand = new Random();

    private final UUID ownerUUID;
    private final UUID effectUUID;

    protected Vector3 pos = new Vector3(), prevPos = new Vector3();
    protected Vector3 motion = new Vector3();
    protected boolean removed = false, updatePos = false;

    private ResourceKey<Level> lastTickedDimension = null;

    public PatreonPartialEntity(UUID effectUUID, UUID ownerUUID) {
        this.effectUUID = effectUUID;
        this.ownerUUID = ownerUUID;
    }

    public UUID getEffectUUID() {
        return effectUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public Vector3 getPos() {
        return pos;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Nullable
    public PatreonEffect getEffect() {
        return PatreonEffectHelper.getEffect(this.getEffectUUID());
    }

    @Nullable
    public ResourceKey<Level> getLastTickedDimension() {
        return lastTickedDimension;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {}

    @OnlyIn(Dist.CLIENT)
    public void tickEffects(Level world) {}

    public boolean tick(Level world) {
        boolean changed = lastTickedDimension == null || !lastTickedDimension.equals(world.getDimensionKey());
        lastTickedDimension = world.getDimensionKey();

        if (updateMotion(world)) {
            changed = true;
        }

        if (tryMoveEntity(world)) {
            changed = true;
        }

        if (world.isRemote()) {
            tickEffects(world);
        }

        return changed;
    }

    private boolean updateMotion(LevelAccessor world) {
        Vector3 prevMot = this.motion.clone();

        Player target = findOwner(world);
        if (target == null) {
            this.motion = new Vector3();
        } else {
            Vector3 moveTarget = Vector3.atEntityCenter(target).addY(1.5);
            if (moveTarget.distanceSquared(this.pos) <= 3D) {
                this.motion.multiply(0.95F);
            } else {
                double diffX = (moveTarget.getX() - pos.getX()) / 8;
                double diffY = (moveTarget.getY() - pos.getY()) / 8;
                double diffZ = (moveTarget.getZ() - pos.getZ()) / 8;
                double dist = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
                this.motion = new Vector3(diffX * dist, diffY * dist, diffZ * dist);
            }
        }
        return !this.motion.equals(prevMot);
    }

    private boolean tryMoveEntity(LevelAccessor world) {
        this.prevPos = this.pos.clone();

        Player owner = findOwner(world);
        if (owner != null && this.pos.distance(Vector3.atEntityCenter(owner)) >= 16) {
            placeNear(owner);
            return true;
        }
        this.pos.add(this.motion);
        return !this.pos.equals(this.prevPos);
    }

    public void placeNear(Player player) {
        this.pos = Vector3.atEntityCenter(player)
                .setY(player.getPosY())
                .addY(player.getHeight())
                .add(Vector3.random().setY(0).normalize());
        this.prevPos = this.pos.clone();
        this.motion = new Vector3();
        this.updatePos = true;
    }

    @Nullable
    public Player findOwner(LevelAccessor world) {
        return world.getPlayerByUuid(this.ownerUUID);
    }

    public void readFromNBT(CompoundTag cmp) {
        if (cmp.contains("lastTickedDimension")) {
            ResourceLocation worldKey = new ResourceLocation(cmp.getString("lastTickedDimension"));
            this.lastTickedDimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, worldKey);
        } else {
            this.lastTickedDimension = null;
        }
        if (cmp.contains("pos") && cmp.contains("prevPos")) {
            this.pos = NBTHelper.readVector3(cmp.getCompound("pos"));
            this.prevPos = NBTHelper.readVector3(cmp.getCompound("prevPos"));
        }
    }

    public void writeToNBT(CompoundTag cmp) {
        if (this.lastTickedDimension != null) {
            cmp.putString("lastTickedDimension", this.lastTickedDimension.getLocation().toString());
        }
        if (updatePos) {
            cmp.put("pos", NBTHelper.writeVector3(this.pos));
            cmp.put("prevPos", NBTHelper.writeVector3(this.prevPos));
            updatePos = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatreonPartialEntity that = (PatreonPartialEntity) o;
        return Objects.equals(effectUUID, that.effectUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectUUID);
    }
}
