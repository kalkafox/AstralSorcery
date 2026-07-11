/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.infusion.ActiveLiquidInfusionRecipe;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktProgressionUpdate;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncModifierSource;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPerkActivity;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkEffectHelper;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResearchManager
 * Created by HellFirePvP
 * Date: 07.05.2016 / 13:33
 */
public class ResearchManager {

    public static void unsafeForceGiveResearch(ServerPlayer player, ResearchProgression prog) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return;

        ProgressionTier reqTier = prog.getRequiredProgress();
        if (!progress.getTierReached().isThisLaterOrEqual(reqTier)) {
            progress.setTierReached(reqTier);
        }

        LinkedList<ResearchProgression> progToGive = new LinkedList<>();
        progToGive.add(prog);
        while (!progToGive.isEmpty()) {
            ResearchProgression give = progToGive.popPose();
            if (!progress.hasResearch(give)) {
                progress.forceGainResearch(give);
            }
            progToGive.addAll(give.getPreConditions());
        }

        PktProgressionUpdate pkt = new PktProgressionUpdate();
        PacketChannel.CHANNEL.sendToPlayer(player, pkt);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
    }

    public static boolean grantResearch(Player player, ResearchProgression prog) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        ProgressionTier tier = prog.getRequiredProgress();
        if (!progress.getTierReached().isThisLaterOrEqual(tier)) return false;
        for (ResearchProgression other : prog.getPreConditions()) {
            if (!progress.hasResearch(other)) return false;
        }

        if (progress.forceGainResearch(prog)) {
            PktProgressionUpdate pkt = new PktProgressionUpdate(prog);
            PacketChannel.CHANNEL.sendToPlayer(player, pkt);
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean grantProgress(Player player, ProgressionTier tier) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        ProgressionTier t = progress.getTierReached();
        if (!t.hasNextTier()) return false; //No higher tier available anyway.
        ProgressionTier next = t.next();
        if (!next.equals(tier)) return false; //Given one is not the next step.

        progress.setTierReached(next);
        PktProgressionUpdate pkt = new PktProgressionUpdate(next);
        PacketChannel.CHANNEL.sendToPlayer(player, pkt);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean discoverConstellations(Collection<IConstellation> constellations, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        for (IConstellation cst : constellations) {
            progress.discoverConstellation(cst.getRegistryName());
            AdvancementsAS.DISCOVER_CONSTELLATION.trigger((ServerPlayer) player, cst);
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean discoverConstellation(IConstellation constellation, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.discoverConstellation(constellation.getRegistryName());

        AdvancementsAS.DISCOVER_CONSTELLATION.trigger((ServerPlayer) player, constellation);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean memorizeConstellation(IConstellation c, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.memorizeConstellation(c.getRegistryName());

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean updateConstellationPapers(List<IConstellation> papers, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.setStoredConstellationPapers(papers.stream().map(IForgeRegistryEntry::getRegistryName).collect(Collectors.toList()));

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean maximizeTier(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.setTierReached(ProgressionTier.values()[ProgressionTier.values().length - 1]);

        PktProgressionUpdate pkt = new PktProgressionUpdate();
        PacketChannel.CHANNEL.sendToPlayer(player, pkt);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setAttunedBefore(Player player, boolean wasAttunedBefore) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.setAttunedBefore(wasAttunedBefore);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setAttunedConstellation(Player player, @Nullable IMajorConstellation constellation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        if (constellation != null && !progress.hasConstellationDiscovered(constellation)) {
            return false;
        }
        PlayerPerkData perkData = progress.getPerkData();
        removeAllAllocatedPerks(progress, player);

        perkData.setExp(0);
        progress.setAttunedConstellation(constellation);
        AbstractPerk root;
        if (constellation != null && (root = PerkTree.PERK_TREE.getRootPerk(LogicalSide.SERVER, constellation)) != null) {
            doApplyPerk(progress, perkData, player, root, PlayerPerkAllocation.unlock());
        }

        AdvancementsAS.ATTUNE_SELF.trigger((ServerPlayer) player, constellation);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setPerkData(Player player, @Nonnull AbstractPerk perk, CompoundTag prevoiusData, CompoundTag newData) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.hasPerkAllocation(perk)) return false;

        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.REMOVE);
        progress.getPerkData().updatePerkData(perk, newData);
        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.ADD);

        PacketChannel.CHANNEL.sendToPlayer(player, new PktSyncPerkActivity(perk, prevoiusData, newData));

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean applyPerk(Player player, @Nonnull AbstractPerk perk, PlayerPerkAllocation allocation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        PlayerPerkData perkData = progress.getPerkData();

        if (allocation.getType() == PerkAllocationType.UNLOCKED) {
            if (!perkData.hasFreeAllocationPoint(player, LogicalSide.SERVER)) return false;
        }

        if (!doApplyPerk(progress, perkData, player, perk, allocation)) {
            return false;
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean applyPerkSeal(Player player, @Nonnull AbstractPerk perk) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.hasPerkAllocation(perk)) return false;
        if (perkData.isPerkSealed(perk)) return false;

        if (!perkData.canSealPerk(perk)) {
            return false;
        }

        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.REMOVE);
        PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.remove(perk));

        if (!perkData.sealPerk(perk)) {
            return false;
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean breakPerkSeal(Player player, @Nonnull AbstractPerk perk) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.isPerkSealed(perk)) return false;

        if (!perkData.breakSeal(perk)) {
            return false;
        }

        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.ADD);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);

        PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.add(perk));
        return true;
    }

    public static boolean grantFreePerkPoint(Player player, ResourceLocation accessToken) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        if (!progress.getPerkData().grantFreeAllocationPoint(accessToken)) {
            return false;
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean revokeFreePoint(Player player, ResourceLocation accessToken) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        if (!progress.getPerkData().tryRevokeAllocationPoint(accessToken)) {
            return false;
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean forceApplyPerk(Player player, @Nonnull AbstractPerk perk, PlayerPerkAllocation allocation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        PlayerPerkData perkData = progress.getPerkData();

        if (!doApplyPerk(progress, perkData, player, perk, allocation)) {
            return false;
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    @Deprecated
    public static boolean removePerk(Player player, AbstractPerk perk, PlayerPerkAllocation allocation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.hasPerkAllocation(perk, allocation.getType())) return false;

        if (!doRemovePerk(progress, player, LogicalSide.SERVER, perk, allocation, true)) {
            return false;
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean resetPerks(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        removeAllAllocatedPerks(progress, player);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    private static void removeAllAllocatedPerks(PlayerProgress progress, Player player) {
        PlayerPerkData perkData = progress.getPerkData();
        List<AbstractPerk> allocatedPerks = new ArrayList<>(perkData.getAllocatedPerks(PerkAllocationType.UNLOCKED));
        List<AbstractPerk> syncRemovable = new ArrayList<>();
        for (AbstractPerk perk : allocatedPerks) {
            if (doRemovePerk(progress, player, LogicalSide.SERVER, perk, PlayerPerkAllocation.unlock(), false)) {
                syncRemovable.add(perk);
            }
        }
        List<ResourceLocation> removals = syncRemovable.stream().map(AbstractPerk::getRegistryName).collect(Collectors.toList());
        PacketChannel.CHANNEL.sendToPlayer(player, new PktSyncPerkActivity(removals));
    }

    private static boolean doRemovePerk(PlayerProgress progress, Player player, LogicalSide direction, AbstractPerk perk, PlayerPerkAllocation allocation, boolean sync) {
        PlayerPerkData perkData = progress.getPerkData();
        if (perkData.hasPerkAllocation(perk, allocation.getType())) {
            CompoundTag data = perkData.getData(perk);
            if (data != null) {
                PerkRemovalResult removeResult = perkData.removePerkAllocation(perk, allocation, true);
                if (removeResult.isFailure()) {
                    return false;
                }
                if (removeResult.removesPerk()) {
                    PerkEffectHelper.modifySource(player, direction, perk, PerkEffectHelper.Action.REMOVE);
                }
                if (removeResult.removesAllocationType()) {
                    perk.onRemovePerkServer(player, allocation.getType(), progress, data);
                }
                PerkRemovalResult actualResult = perkData.removePerkAllocation(perk, allocation, false);
                if (actualResult.removesPerk()) {
                    if (sync) {
                        PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.remove(perk));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static boolean doApplyPerk(PlayerProgress progress, PlayerPerkData perkData, Player player, AbstractPerk perk, PlayerPerkAllocation allocation) {
        if (!perkData.applyPerkAllocation(perk, allocation, true)) {
            return false;
        }
        if (perkData.hasPerkAllocation(perk)) {
            if (!perkData.hasPerkAllocation(perk, allocation.getType())) {
                CompoundTag data = perkData.getData(perk);
                perk.onUnlockPerkServer(player, allocation.getType(), progress, data);
            }
            return perkData.applyPerkAllocation(perk, allocation, false);
        } else {
            CompoundTag data = new CompoundTag();
            perk.onUnlockPerkServer(player, allocation.getType(), progress, data);
            perkData.applyPerkAllocation(perk, allocation, false);
            perkData.updatePerkData(perk, data);

            PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.ADD);
            PacketChannel.CHANNEL.sendToPlayer(player, PktSyncModifierSource.add(perk));
            return true;
        }
    }

    public static boolean setTomeReceived(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.setTomeReceived();

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean togglePerkAbilities(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.setUsePerkAbilities(!progress.doPerkAbilities());

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setExp(Player player, long futureXp) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.getPerkData().setExp(futureXp);

        AdvancementsAS.PERK_LEVEL.trigger((ServerPlayer) player);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean modifyExp(Player player, double futureXp) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;

        progress.getPerkData().modifyExp(futureXp, player);

        AdvancementsAS.PERK_LEVEL.trigger((ServerPlayer) player);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean forceMaximizeAll(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        ProgressionTier before = progress.getTierReached();

        ResearchManager.discoverConstellations(ConstellationRegistry.getAllConstellations(), player);
        ResearchManager.maximizeTier(player);
        ResearchManager.forceMaximizeResearch(player);
        ResearchManager.setAttunedBefore(player, true);

        if (progress.getTierReached().isThisLater(before)) {
            PktProgressionUpdate pkt = new PktProgressionUpdate(progress.getTierReached());
            PacketChannel.CHANNEL.sendToPlayer(player, pkt);
        }

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean forceMaximizeResearch(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) return false;
        for (ResearchProgression progression : ResearchProgression.values()) {
            progress.forceGainResearch(progression);
        }

        PktProgressionUpdate pkt = new PktProgressionUpdate();
        PacketChannel.CHANNEL.sendToPlayer(player, pkt);

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static void informCraftedInfuser(@Nonnull TileInfuser infuser, @Nonnull ActiveLiquidInfusionRecipe recipe, @Nonnull ItemStack crafted) {
        Player crafter = recipe.tryGetCraftingPlayerServer();
        if (!(crafter instanceof ServerPlayer)) {
            AstralSorcery.log.warn("Infusion finished, player that initialized crafting could not be found!");
            AstralSorcery.log.warn("Affected tile: " + infuser.getBlockPos() + " in dim " + infuser.getLevel().dimension().location());
            return;
        }

        informCrafted(crafter, crafted);
    }

    public static void informCraftedAltar(@Nonnull TileAltar altar, @Nonnull ActiveSimpleAltarRecipe recipe, @Nonnull ItemStack crafted) {
        Player crafter = recipe.tryGetCraftingPlayerServer();
        if (!(crafter instanceof ServerPlayer)) {
            AstralSorcery.log.warn("Crafting finished, player that initialized crafting could not be found!");
            AstralSorcery.log.warn("Affected tile: " + altar.getBlockPos() + " in dim " + altar.getLevel().dimension().location());
            return;
        }

        informCrafted(crafter, crafted);

        AdvancementsAS.ALTAR_CRAFT.trigger((ServerPlayer) crafter, recipe.getRecipeToCraft(), crafted);
    }

    public static void informCrafted(@Nonnull Player player, @Nonnull ItemStack out) {
        if (!out.isEmpty()) {
            informCraftCompletion(player, out, out.getItem(), Block.getBlockFromItem(out.getItem()));
        }
    }

    private static void informCraftCompletion(@Nonnull Player crafter, @Nonnull ItemStack crafted, @Nonnull Item itemCrafted, @Nonnull Block blockCrafted) {
        if (blockCrafted instanceof BlockAltar) {
            grantProgress(crafter, ProgressionTier.BASIC_CRAFT);
            grantResearch(crafter, ResearchProgression.BASIC_CRAFT);

            //Fallthrough switch to lower tiers
            switch (((BlockAltar) blockCrafted).getAltarType()) {
                case RADIANCE:
                    grantProgress(crafter, ProgressionTier.TRAIT_CRAFT);
                    grantResearch(crafter, ResearchProgression.RADIANCE);
                case CONSTELLATION:
                    grantProgress(crafter, ProgressionTier.CONSTELLATION_CRAFT);
                    grantResearch(crafter, ResearchProgression.CONSTELLATION);
                case ATTUNEMENT:
                    grantProgress(crafter, ProgressionTier.ATTUNEMENT);
                    grantResearch(crafter, ResearchProgression.ATTUNEMENT);
                default:
                    break;
            }
        }
    }

}
