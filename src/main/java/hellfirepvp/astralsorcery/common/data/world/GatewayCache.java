/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.PlayerReference;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import hellfirepvp.astralsorcery.common.util.log.LogUtil;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.base.GlobalWorldData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GatewayCache
 * Created by HellFirePvP
 * Date: 30.05.2019 / 14:35
 */
public class GatewayCache extends GlobalWorldData {

    private final Set<GatewayNode> gatewayPositions = new HashSet<>();

    public GatewayCache(WorldCacheDomain.SaveKey<?> key) {
        super(key);
    }

    public Collection<GatewayNode> getGatewayPositions() {
        return Collections.unmodifiableCollection(gatewayPositions);
    }

    public boolean hasGateway(BlockPos pos) {
        return this.gatewayPositions.stream().anyMatch(gateway -> gateway.getPos().equals(pos));
    }

    @Nullable
    public GatewayNode getGatewayNode(BlockPos pos) {
        return this.gatewayPositions.stream()
                .filter(gateway -> gateway.getPos().equals(pos))
                .findFirst()
                .orElse(null);
    }

    public void updateGatewayNode(BlockPos pos, Consumer<GatewayNodeAccess> nodeFn) {
        this.gatewayPositions.stream()
                .filter(gateway -> gateway.getPos().equals(pos))
                .findFirst()
                .ifPresent(gatewayNode -> update(gatewayNode, nodeFn));
    }

    private void update(GatewayNode node, Consumer<GatewayNodeAccess> nodeFn) {
        nodeFn.accept(node.writeAccess());
        this.markDirty();
        CelestialGatewayHandler.INSTANCE.syncToAll();
    }

    public boolean offerPosition(Level world, BlockPos pos) {
        TileCelestialGateway te = MiscUtils.getTileAt(world, pos, TileCelestialGateway.class, false);
        if (te == null) {
            return false;
        }
        GatewayNode node = new GatewayNode(pos);
        if (!gatewayPositions.add(node)) {
            return false;
        }
        markDirty();
        CelestialGatewayHandler.INSTANCE.addPosition(world, node);
        LogUtil.info(LogCategory.GATEWAY_CACHE, () -> "Added new gateway node at: dim=" + world.getDimensionKey().getLocation() + ", " + pos.toString());
        return true;
    }

    public void removePosition(Level world, BlockPos pos) {
        if (gatewayPositions.removeIf(node -> node.getPos().equals(pos))) {
            markDirty();
            CelestialGatewayHandler.INSTANCE.removePosition(world, pos);
            LogUtil.info(LogCategory.GATEWAY_CACHE, () -> "Removed gateway node at: dim=" + world.getDimensionKey().getLocation() + ", " + pos.toString());
        }
    }

    @Override
    public void updateTick(Level world) {}

    @Override
    public void onLoad(Level world) {
        super.onLoad(world);

        LogUtil.info(LogCategory.GATEWAY_CACHE, () -> "Checking GatewayCache integrity for dimension " + world.getDimensionKey().getLocation());
        long msStart = System.currentTimeMillis();

        Iterator<GatewayNode> iterator = gatewayPositions.iterator();
        while (iterator.hasNext()) {
            GatewayNode node = iterator.next();
            TileCelestialGateway gateway;
            try {
                gateway = MiscUtils.getTileAt(world, node.getPos(), TileCelestialGateway.class, true);
            } catch (Exception loadEx) {
                LogUtil.info(LogCategory.GATEWAY_CACHE, () -> "Failed to check gateway for " + node + " skipping");
                continue;
            }
            if (gateway == null) {
                iterator.remove();
                LogUtil.info(LogCategory.GATEWAY_CACHE, () -> "Invalid entry: " + node + " - no gateway tileentity found there!");
            }
        }

        LogUtil.info(LogCategory.GATEWAY_CACHE, () ->
                "GatewayCache checked and fully loaded in " + (System.currentTimeMillis() - msStart) +
                        "ms! Collected and checked " + gatewayPositions.size() + " gateway nodes!");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        ListTag list = new ListTag();
        for (GatewayNode node : gatewayPositions) {
            CompoundTag tag = new CompoundTag();
            node.write(tag);
            list.add(tag);
        }
        compound.put("posList", list);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        ListTag list = compound.getList("posList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            gatewayPositions.add(GatewayNode.read(tag));
        }
    }

    public static class GatewayNode {

        private final BlockPos pos;
        private Component display;
        private DyeColor color;

        private boolean locked = false;
        private PlayerReference owner = null;
        private Map<Integer, PlayerReference> allowedUsers = new HashMap<>();

        private GatewayNode(BlockPos pos) {
            this.pos = pos;
        }

        private GatewayNodeAccess writeAccess() {
            return new GatewayNodeAccess(this);
        }

        @Nonnull
        public final BlockPos getPos() {
            return pos;
        }

        @Nullable
        public Component getDisplayName() {
            return display;
        }

        @Nullable
        public DyeColor getColor() {
            return color;
        }

        public boolean isLocked() {
            return locked;
        }

        @Nullable
        public PlayerReference getOwner() {
            return owner;
        }

        public Map<Integer, PlayerReference> getAllowedUsers() {
            return Collections.unmodifiableMap(this.allowedUsers);
        }

        public boolean hasAccess(Player player) {
            PlayerReference owner = this.getOwner();
            if (owner == null || !this.isLocked()) {
                return true;
            }
            return owner.isPlayer(player) || this.getAllowedUsers().values().stream().anyMatch(ref -> ref.isPlayer(player));
        }

        public void write(CompoundTag tag) {
            NBTHelper.writeBlockPosToNBT(this.getPos(), tag);
            if (this.getDisplayName() != null) {
                tag.putString("display", ITextComponent.Serializer.toJson(this.getDisplayName()));
            }
            if (this.getColor() != null) {
                NBTHelper.writeEnum(tag, "color", this.getColor());
            }

            tag.putBoolean("locked", this.isLocked());
            NBTHelper.writeOptional(tag, "owningPlayer", this.getOwner(), (compound, playerRef) -> playerRef.writeToNBT(compound));
            NBTHelper.writeList(tag, "allowedUsers", this.allowedUsers.entrySet(), entry -> {
                CompoundTag compound = new CompoundTag();
                compound.putInt("index", entry.getKey());
                compound.put("player", entry.getValue().serialize());
                return compound;
            });
        }

        public void write(FriendlyByteBuf buf) {
            ByteBufUtils.writePos(buf, this.getPos());
            ByteBufUtils.writeOptional(buf, this.getDisplayName(), ByteBufUtils::writeTextComponent);
            ByteBufUtils.writeOptional(buf, this.getColor(), ByteBufUtils::writeEnumValue);
            buf.writeBoolean(this.isLocked());
            ByteBufUtils.writeOptional(buf, this.getOwner(), (buffer, ref) -> ref.write(buffer));
            ByteBufUtils.writeMap(buf, this.getAllowedUsers(), FriendlyByteBuf::writeInt, (buffer, ref) -> ref.write(buffer));
        }

        public static GatewayNode read(CompoundTag tag) {
            GatewayNode node = new GatewayNode(NBTHelper.readBlockPosFromNBT(tag));
            if (tag.contains("display")) {
                node.display = ITextComponent.Serializer.getComponentFromJson(tag.getString("display"));
            }
            if (tag.contains("color")) {
                node.color = NBTHelper.readEnum(tag, "color", DyeColor.class);
            }

            node.locked = tag.getBoolean("locked");
            node.owner = NBTHelper.readOptional(tag, "owningPlayer", PlayerReference::deserialize);
            NBTHelper.readList(tag, "allowedUsers", Constants.NBT.TAG_COMPOUND, nbt -> {
                CompoundTag compound = (CompoundTag) nbt;
                return new Tuple<>(compound.getInt("index"), PlayerReference.deserialize(compound.getCompound("player")));
            }).forEach(tpl -> node.allowedUsers.put(tpl.getA(), tpl.getB()));
            return node;
        }

        public static GatewayNode read(FriendlyByteBuf buf) {
            GatewayNode node = new GatewayNode(ByteBufUtils.readPos(buf));
            node.display = ByteBufUtils.readOptional(buf, ByteBufUtils::readTextComponent);
            node.color = ByteBufUtils.readOptional(buf, buffer -> ByteBufUtils.readEnumValue(buffer, DyeColor.class));
            node.locked = buf.readBoolean();
            node.owner = ByteBufUtils.readOptional(buf, PlayerReference::read);
            node.allowedUsers = ByteBufUtils.readMap(buf, FriendlyByteBuf::readInt, PlayerReference::read);
            return node;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GatewayNode that = (GatewayNode) o;
            return Objects.equals(getPos(), that.getPos());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPos());
        }
    }

    public static class GatewayNodeAccess extends GatewayNode {

        private final GatewayNode decorated;

        public GatewayNodeAccess(GatewayNode decorated) {
            super(decorated.getPos());
            this.decorated = decorated;
        }

        @Nullable
        @Override
        public DyeColor getColor() {
            return this.decorated.getColor();
        }

        public void setColor(@Nullable DyeColor color) {
            this.decorated.color = color;
        }

        @Nullable
        @Override
        public Component getDisplayName() {
            return this.decorated.getDisplayName();
        }

        public void setDisplayName(@Nullable Component displayName) {
            this.decorated.display = displayName;
        }

        @Override
        public boolean isLocked() {
            return this.decorated.isLocked();
        }

        public void setLocked(boolean locked) {
            this.decorated.locked = locked;
        }

        @Nullable
        @Override
        public PlayerReference getOwner() {
            return this.decorated.getOwner();
        }

        public void setOwner(PlayerReference owner) {
            this.decorated.owner = owner;
        }

        @Override
        public Map<Integer, PlayerReference> getAllowedUsers() {
            return this.decorated.getAllowedUsers();
        }

        public void setAllowedUsers(Map<Integer, PlayerReference> users) {
            this.decorated.allowedUsers.clear();
            this.decorated.allowedUsers.putAll(users);
        }
    }
}
