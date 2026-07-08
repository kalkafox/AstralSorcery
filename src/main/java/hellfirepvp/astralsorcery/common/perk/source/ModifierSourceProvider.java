/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.source;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncModifierSource;
import hellfirepvp.astralsorcery.common.perk.PerkEffectHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModifierSourceProvider
 * Created by HellFirePvP
 * Date: 01.04.2020 / 18:20
 */
public abstract class ModifierSourceProvider<T extends ModifierSource> {

    private final ResourceLocation key;

    private final Map<UUID, Map<ResourceLocation, T>> cachedSources = new HashMap<>();

    protected ModifierSourceProvider(ResourceLocation key) {
        this.key = key;
    }

    protected abstract void update(ServerPlayer playerEntity);

    protected abstract void removeModifiers(ServerPlayer playerEntity);

    public abstract void serialize(T source, FriendlyByteBuf buf);

    public abstract T deserialize(FriendlyByteBuf buf);

    @Nullable
    private T getModifier(ServerPlayer player, ResourceLocation identifier) {
        Map<ResourceLocation, T> playerModifiers = cachedSources.computeIfAbsent(player.getUniqueID(), uuid -> new HashMap<>());
        return playerModifiers.get(identifier);
    }

    private void setModifier(ServerPlayer player, ResourceLocation identifier, @Nullable T source) {
        Map<ResourceLocation, T> playerModifiers = cachedSources.computeIfAbsent(player.getUniqueID(), uuid -> new HashMap<>());
        if (source != null) {
            playerModifiers.put(identifier, source);
        } else {
            playerModifiers.remove(identifier);
        }
    }

    protected void updateSource(ServerPlayer player, ResourceLocation identifier, @Nullable T source) {
        boolean needsRemoval = false, needsAddition = false;

        T existing = this.getModifier(player, identifier);
        if (existing != null) {
            if (!existing.isEqual(source)) {
                needsRemoval = true;
            } else {
                return; //Nothing to do
            }
        }
        if (source != null) {
            needsAddition = true;
        }

        if (needsRemoval) {
            if (needsAddition) {
                PerkEffectHelper.updateSource(player, LogicalSide.SERVER, existing, source);
                PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.update(existing, source));
            } else {
                PerkEffectHelper.modifySource(player, LogicalSide.SERVER, existing, PerkEffectHelper.Action.REMOVE);
                PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.remove(existing));
            }
        } else if (needsAddition) {
            PerkEffectHelper.modifySource(player, LogicalSide.SERVER, source, PerkEffectHelper.Action.ADD);
            PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.add(source));
        }
        this.setModifier(player, identifier, source);
    }

    public final ResourceLocation getKey() {
        return key;
    }
}
