/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.socket;

import net.minecraft.network.chat.Component;

import hellfirepvp.astralsorcery.common.data.research.PerkAllocationType;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.PerkNamesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreeGem;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import java.util.Collection;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GemSocketMajorPerk
 * Created by HellFirePvP
 * Date: 25.08.2019 / 18:22
 */
public class GemSocketMajorPerk extends MajorPerk implements GemSocketPerk {

    public GemSocketMajorPerk(ResourceLocation name, float x, float y) {
        super(name, x, y);
        this.setName(PerkNamesAS.name("gem_socket"));
        this.disableTooltipCaching();
    }

    @Override
    protected PerkTreePoint<? extends GemSocketMajorPerk> initPerkTreePoint() {
        return new PerkTreeGem<>(this, getOffset());
    }

    @Override
    public Collection<PerkAttributeModifier> getModifiers(Player player, LogicalSide direction, boolean ignoreRequirements) {
        Collection<PerkAttributeModifier> mods = super.getModifiers(player, direction, ignoreRequirements);
        ItemStack contained = getContainedItem(player, direction);
        if (!contained.isEmpty() && contained.getItem() instanceof GemSocketItem) {
            mods.addAll(((GemSocketItem) contained.getItem()).getModifiers(contained, this, player, direction));
        }
        return mods;
    }

    @Override
    public void onRemovePerkServer(Player player, PerkAllocationType allocationType, PlayerProgress progress, CompoundTag dataStorage) {
        super.onRemovePerkServer(player, allocationType, progress, dataStorage);

        // Will be removed?
        if (progress.getPerkData().getAllocationTypes(this).size() <= 1) {
            dropItemToPlayer(player, dataStorage);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addLocalizedTooltip(Collection<MutableComponent> tooltip) {
        if (super.addLocalizedTooltip(tooltip)) {
            tooltip.add(Component.literal(""));
        }
        if (canSeeClient()) {
            this.addTooltipInfo(tooltip);
        }
        return true;
    }
}
