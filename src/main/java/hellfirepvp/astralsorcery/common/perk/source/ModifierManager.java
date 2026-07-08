/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.source;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.event.ASRegistryEvents;
import hellfirepvp.astralsorcery.common.perk.source.provider.PerkSourceProvider;
import hellfirepvp.astralsorcery.common.perk.source.provider.equipment.EquipmentSourceProvider;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModifierManager
 * Created by HellFirePvP
 * Date: 01.04.2020 / 17:27
 */
public class ModifierManager implements ITickHandler {

    public static final ResourceLocation PERK_PROVIDER_KEY = AstralSorcery.key("perks");
    public static final ResourceLocation EQUIPMENT_PROVIDER_KEY = AstralSorcery.key("equipment");

    public static final ModifierManager INSTANCE = new ModifierManager();

    private static Map<ResourceLocation, ModifierSourceProvider<?>> sourceProviders = null;

    private static final Map<UUID, Set<ModifierSource>> modifierCache = new HashMap<>();
    private static final Map<UUID, Set<ModifierSource>> modifierCacheClient = new HashMap<>();

    private ModifierManager() {}

    public static void init() {
        if (sourceProviders == null) {
            sourceProviders = new HashMap<>();

            //Special dummy handler for perks. They're handled differently.. (and i don't wanna refactor AGAIN)
            sourceProviders.put(PERK_PROVIDER_KEY, new PerkSourceProvider());

            sourceProviders.put(EQUIPMENT_PROVIDER_KEY, new EquipmentSourceProvider());

            NeoForge.EVENT_BUS.post(new ASRegistryEvents.ModifierSourceRegister(sourceProvider -> {
                sourceProviders.put(sourceProvider.getKey(), sourceProvider);
            }));
        }
    }

    @Nullable
    public static ModifierSourceProvider<?> getProvider(ResourceLocation key) {
        return sourceProviders.get(key);
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        LogicalSide side = (LogicalSide) context[1];

        if (!side.isServer() || !(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;

        for (ModifierSourceProvider<?> sourceProvider : sourceProviders.values()) {
            sourceProvider.update(serverPlayer);
        }
    }

    @Nonnull
    private static Set<ModifierSource> getModifiers(Player player, LogicalSide side) {
        if (side.isClient()) {
            return modifierCacheClient.computeIfAbsent(player.getUniqueID(), uuid -> new HashSet<>());
        } else {
            return modifierCache.computeIfAbsent(player.getUniqueID(), uuid -> new HashSet<>());
        }
    }

    @Nonnull
    public static Set<ModifierSource> getAppliedModifiers(Player player, LogicalSide side) {
        return new HashSet<>(getModifiers(player, side));
    }

    public static void addModifier(Player player, LogicalSide side, ModifierSource source) {
        Set<ModifierSource> modifiers = getModifiers(player, side);
        if (!modifiers.contains(source) && modifiers.add(source)) {
            source.onApply(player, side);
        }
    }

    public static void removeModifier(Player player, LogicalSide side, ModifierSource source) {
        Set<ModifierSource> modifiers = getModifiers(player, side);
        if (modifiers.remove(source)) {
            source.onRemove(player, side);
        }
    }

    public static boolean isModifierApplied(Player player, LogicalSide side, ModifierSource source) {
        return getModifiers(player, side).contains(source);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clearClientCache() {
        modifierCacheClient.clear();
    }

    public static void onDisconnect(ServerPlayer player) {
        for (ModifierSourceProvider<?> sourceProvider : sourceProviders.values()) {
            sourceProvider.removeModifiers(player);
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Modifier Source Manager";
    }
}
