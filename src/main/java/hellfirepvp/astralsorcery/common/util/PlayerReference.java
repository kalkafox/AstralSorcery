/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PlayerReference
 * Created by HellFirePvP
 * Date: 18.10.2020 / 20:29
 */
public class PlayerReference {

    private final UUID playerUUID;
    private final MutableComponent playerName;

    public PlayerReference(UUID playerUUID, MutableComponent playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    public static PlayerReference of(Player player) {
        Component txt = player.getDisplayName();
        if (txt instanceof MutableComponent) {
            return new PlayerReference(player.getUniqueID(), (MutableComponent) txt);
        }
        return new PlayerReference(player.getUniqueID(), Component.literal("").append(txt));
    }

    public boolean isPlayer(Player player) {
        return this.getPlayerUUID().equals(player.getUniqueID());
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public Component getPlayerName() {
        return this.playerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerReference that = (PlayerReference) o;
        return Objects.equals(playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID);
    }

    @Nullable
    public ServerPlayer getOnlinePlayer() {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        if (server == null) {
            throw new IllegalArgumentException("Called getOnlinePlayer on clientside or while no server is running!");
        }
        return server.getPlayerList().getPlayerByUUID(this.playerUUID);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        this.writeToNBT(tag);
        return tag;
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putUniqueId("playerUUID", this.playerUUID);
        tag.putString("playerName", ITextComponent.Serializer.toJson(this.playerName));
    }

    public void write(FriendlyByteBuf buf) {
        ByteBufUtils.writeUUID(buf, this.playerUUID);
        ByteBufUtils.writeTextComponent(buf, this.playerName);
    }

    public static PlayerReference deserialize(CompoundTag tag) {
        return new PlayerReference(tag.getUniqueId("playerUUID"), ITextComponent.Serializer.getComponentFromJson(tag.getString("playerName")));
    }

    public static PlayerReference read(FriendlyByteBuf buf) {
        return new PlayerReference(ByteBufUtils.readUUID(buf), ByteBufUtils.readTextComponent(buf));
    }
}
