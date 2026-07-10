/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.research.*;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyTreeConnector
 * Created by HellFirePvP
 * Date: 25.08.2019 / 18:29
 */
public class KeyTreeConnector extends MajorPerk {

    public KeyTreeConnector(ResourceLocation name, float x, float y) {
        super(name, x, y);
        this.setCategory(CATEGORY_EPIPHANY);
    }

    @Override
    public boolean mayUnlockPerk(PlayerProgress progress, Player player) {
        if (!progress.getPerkData().hasFreeAllocationPoint(player, getSide(player)) ||
                !canSee(player, progress)) return false;
        PlayerPerkData perkData = progress.getPerkData();

        LogicalSide direction = getSide(player);
        boolean hasAllAdjacent = true;
        for (AbstractPerk otherPerks : PerkTree.PERK_TREE.getConnectedPerks(direction, this)) {
            if (!perkData.hasPerkAllocation(otherPerks, PerkAllocationType.UNLOCKED)) {
                hasAllAdjacent = false;
                break;
            }
        }
        if (!hasAllAdjacent) {
            return PerkTree.PERK_TREE.getPerkPoints(getSide(player)).stream()
                    .map(PerkTreePoint::getPerk)
                    .filter(perk -> perk instanceof KeyTreeConnector)
                    .anyMatch(perk -> perkData.hasPerkAllocation(perk, PerkAllocationType.UNLOCKED));
        } else {
            return true;
        }
    }

    @Override
    public void onUnlockPerkServer(@Nullable Player player, PerkAllocationType allocationType, PlayerProgress progress, CompoundTag dataStorage) {
        super.onUnlockPerkServer(player, allocationType, progress, dataStorage);

        if (allocationType == PerkAllocationType.UNLOCKED) {
            ListTag listTokens = new ListTag();
            for (AbstractPerk otherPerk : PerkTree.PERK_TREE.getConnectedPerks(LogicalSide.SERVER, this)) {
                if (ResearchManager.forceApplyPerk(player, otherPerk, PlayerPerkAllocation.unlock())) {
                    ResourceLocation accessToken = AstralSorcery.key("connector_tk_" + otherPerk.getRegistryName().getPath());
                    if (ResearchManager.grantFreePerkPoint(player, accessToken)) {
                        listTokens.add(StringTag.valueOf(accessToken.toString()));
                    }
                }
            }
            dataStorage.put("pointtokens", listTokens);
        }
    }

    @Override
    public void onRemovePerkServer(Player player, PerkAllocationType allocationType, PlayerProgress progress, CompoundTag dataStorage) {
        super.onRemovePerkServer(player, allocationType, progress, dataStorage);

        if (allocationType == PerkAllocationType.UNLOCKED) {
            ListTag list = dataStorage.getList("pointtokens", Constants.NBT.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResearchManager.revokeFreePoint(player, ResourceLocation.parse(list.getString(i)));
            }
        }
    }

    @Override
    public void clearCaches(LogicalSide direction) {
        super.clearCaches(direction);
    }
}
