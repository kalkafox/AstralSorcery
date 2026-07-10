/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.data.research.PlayerPerkData;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPerkActivity;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.source.AttributeConverterProvider;
import hellfirepvp.astralsorcery.common.perk.source.AttributeModifierProvider;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkEffectHelper
 * Created by HellFirePvP
 * Date: 25.08.2019 / 22:00
 */
public class PerkEffectHelper {

    private PerkEffectHelper() {}

    public static void onPlayerConnectEvent(ServerPlayer player) {
        modifyAllPerks(player, LogicalSide.SERVER, Action.ADD);

        //Restore current overscaled health
        CompoundTag asData = NBTHelper.getPersistentData(player);
        if (asData.contains("health", Constants.NBT.TAG_FLOAT)) {
            player.setHealth(asData.getFloat("health"));
        }

        PacketChannel.CHANNEL.sendToPlayer(player, new PktSyncPerkActivity(PktSyncPerkActivity.Type.UNLOCKALL));
    }

    public static void onPlayerDisconnectEvent(ServerPlayer player) {
        modifyAllPerks(player, LogicalSide.SERVER, Action.REMOVE);

        //Store current overscaled health
        NBTHelper.getPersistentData(player).putFloat("health", player.getHealth());
    }

    public static void onPlayerCloneEvent(ServerPlayer original, ServerPlayer newPlayer) {
        modifyAllPerks(original, LogicalSide.SERVER, Action.REMOVE);
        modifyAllPerks(newPlayer, LogicalSide.SERVER, Action.ADD);
        PerkCooldownHelper.removeAllCooldowns(original, LogicalSide.SERVER);

        PacketChannel.CHANNEL.sendToPlayer(newPlayer, new PktSyncPerkActivity(PktSyncPerkActivity.Type.UNLOCKALL));
    }

    /* ****************************************************************************************************
                                       INTERNAL PERK SYNCHRONIZATION
     **************************************************************************************************** */

    @OnlyIn(Dist.CLIENT)
    public static void clientChangePerkData(AbstractPerk perk, CompoundTag oldData, CompoundTag newData) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.CLIENT);
        PlayerPerkData perkData = progress.getPerkData();

        if (!perkData.hasPerkAllocation(perk)) {
            return;
        }
        perkData.updatePerkData(perk, oldData);
        modifySource(player, LogicalSide.CLIENT, perk, Action.REMOVE);
        perkData.updatePerkData(perk, newData);
        modifySource(player, LogicalSide.CLIENT, perk, Action.ADD);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientClearAllPerks() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.CLIENT);
        if (!progress.isValid()) {
            return;
        }

        PerkAttributeMap attr = PerkAttributeHelper.getOrCreateMap(player, LogicalSide.CLIENT);
        for (ModifierSource source : ModifierManager.getAppliedModifiers(player, LogicalSide.CLIENT)) {
            if (source instanceof AbstractPerk) {
                removeSource(attr, player, LogicalSide.CLIENT, source);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientRefreshAllPerks() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        modifyAllPerks(player, LogicalSide.CLIENT, Action.ADD);
        PerkCooldownHelper.removeAllCooldowns(player, LogicalSide.CLIENT);
    }

    /* ****************************************************************************************************
                                      INTERNAL PERK APPLICATION LOGIC
     **************************************************************************************************** */

    private static void modifyAllPerks(Player player, LogicalSide direction, Action action) {
        ResearchHelper.getProgress(player, direction).getPerkData().getEffectGrantingPerks()
                .forEach(perk -> modifySource(player, direction, perk, action));
    }

    public static void updateSource(Player player, LogicalSide direction, ModifierSource oldSource, ModifierSource newSource) {
        PlayerProgress progress = ResearchHelper.getProgress(player, direction);
        if (!progress.isValid()) {
            return;
        }

        PerkAttributeMap attributeMap = PerkAttributeHelper.getOrCreateMap(player, direction);
        attributeMap.write(() -> {
            if (ModifierManager.isModifierApplied(player, direction, oldSource)) {
                removeSource(attributeMap, player, direction, oldSource);
            }
            if (!ModifierManager.isModifierApplied(player, direction, newSource) && newSource.canApplySource(player, direction)) {
                applySource(attributeMap, player, direction, newSource);
            }
        });
    }

    public static <T extends ModifierSource> void modifySources(Player player, LogicalSide direction, Collection<T> sources, Action action) {
        PlayerProgress progress = ResearchHelper.getProgress(player, direction);
        if (!progress.isValid()) {
            return;
        }

        PerkAttributeMap attributeMap = PerkAttributeHelper.getOrCreateMap(player, direction);
        for (T src : sources) {
            if (action.isRemove()) {
                if (ModifierManager.isModifierApplied(player, direction, src)) {
                    attributeMap.write(() -> removeSource(attributeMap, player, direction, src));
                }
            } else {
                if (!ModifierManager.isModifierApplied(player, direction, src) && src.canApplySource(player, direction)) {
                    attributeMap.write(() -> applySource(attributeMap, player, direction, src));
                }
            }
        }
    }

    public static void modifySource(Player player, LogicalSide direction, ModifierSource source, Action action) {
        PlayerProgress progress = ResearchHelper.getProgress(player, direction);
        if (!progress.isValid()) {
            return;
        }

        PerkAttributeMap attributeMap = PerkAttributeHelper.getOrCreateMap(player, direction);
        if (action.isRemove()) {
            if (ModifierManager.isModifierApplied(player, direction, source)) {
                attributeMap.write(() -> removeSource(attributeMap, player, direction, source));
            }
        } else {
            if (!ModifierManager.isModifierApplied(player, direction, source) && source.canApplySource(player, direction)) {
                attributeMap.write(() -> applySource(attributeMap, player, direction, source));
            }
        }
    }

    private static void applySource(PerkAttributeMap attrMap, Player player, LogicalSide direction, ModifierSource add) {
        //The onlyAdd perk is already on the playerprogress (potentially with other, not-yet-added perks); filter it away.
        Collection<ModifierSource> sources = ModifierManager.getAppliedModifiers(player, direction);
        //List<ModifierSource> sources = new LinkedList<>(prog.getAppliedPerks());
        //sources = sources.stream().filter(attrMap::isModifierApplied).collect(Collectors.toList());

        sources.forEach(source -> {
            removeModifiers(source, attrMap, player, direction);
            ModifierManager.removeModifier(player, direction, source);
        });

        if (add instanceof AttributeConverterProvider) {
            ((AttributeConverterProvider) add).getConverters(player, direction, false)
                    .forEach((c) -> attrMap.applyConverter(player, c));
        }
        Collection<PerkAttributeModifier> newModifiers = applyModifiers(add, attrMap, player, direction);

        sources.forEach(source -> {
            applyModifiers(source, attrMap, player, direction);
            ModifierManager.addModifier(player, direction, source);
        });
        //Add new source.
        ModifierManager.addModifier(player, direction, add);
        newModifiers.forEach(mod -> mod.getAttributeType().onApply(player, direction, add));
    }

    private static Collection<PerkAttributeModifier> applyModifiers(ModifierSource source, PerkAttributeMap attrMap, Player player, LogicalSide direction) {
        Collection<PerkAttributeModifier> addedModifiers = new ArrayList<>();
        if (source instanceof AttributeModifierProvider) {
            for (PerkAttributeModifier modifier : ((AttributeModifierProvider) source).getModifiers(player, direction, false)) {
                addedModifiers.addAll(attrMap.addModifier(player, modifier, source));
            }
        }
        return addedModifiers;
    }

    private static void removeSource(PerkAttributeMap attrMap, Player player, LogicalSide direction, ModifierSource remove) {
        //Drop the old source
        ModifierManager.removeModifier(player, direction, remove);

        Collection<ModifierSource> sources = ModifierManager.getAppliedModifiers(player, direction);
        sources.forEach(source -> {
            removeModifiers(source, attrMap, player, direction);
            ModifierManager.removeModifier(player, direction, source);
        });

        Collection<PerkAttributeModifier> removedModifiers = removeModifiers(remove, attrMap, player, direction);
        if (remove instanceof AttributeConverterProvider) {
            ((AttributeConverterProvider) remove).getConverters(player, direction, false)
                    .forEach((c) -> attrMap.removeConverter(player, c));
        }

        sources.forEach(source -> {
            applyModifiers(source, attrMap, player, direction);
            ModifierManager.addModifier(player, direction, source);
        });

        PerkAttributeMap map = PerkAttributeHelper.getOrCreateMap(player, direction);
        removedModifiers.forEach(mod -> {
            mod.getAttributeType().onRemove(player, direction, !map.hasModifiers(mod.getAttributeType()), remove);
        });
    }

    private static Collection<PerkAttributeModifier> removeModifiers(ModifierSource source, PerkAttributeMap attrMap, Player player, LogicalSide direction) {
        Collection<PerkAttributeModifier> removedModifiers = new ArrayList<>();
        if (source instanceof AttributeModifierProvider) {
            for (PerkAttributeModifier modifier : ((AttributeModifierProvider) source).getModifiers(player, direction, false)) {
                removedModifiers.addAll(attrMap.removeModifier(player, modifier, source));
            }
        }
        return removedModifiers;
    }

    public static enum Action {

        ADD,
        REMOVE;

        private boolean isRemove() {
            return this == REMOVE;
        }

    }
}
