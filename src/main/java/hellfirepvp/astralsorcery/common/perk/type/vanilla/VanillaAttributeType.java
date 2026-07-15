/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type.vanilla;

import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: VanillaAttributeType
 * Created by HellFirePvP
 * Date: 09.08.2019 / 08:04
 */
public abstract class VanillaAttributeType extends PerkAttributeType implements VanillaPerkAttributeType {

    public VanillaAttributeType(ResourceLocation name) {
        super(name);
    }

    @Override
    public void onApply(Player player, LogicalSide direction, ModifierSource source) {
        super.onApply(player, direction, source);

        refreshAttribute(player);
    }

    @Override
    public void onRemove(Player player, LogicalSide direction, boolean removedCompletely, ModifierSource source) {
        super.onRemove(player, direction, removedCompletely, source);

        refreshAttribute(player);
    }

    @Override
    public void onModeApply(Player player, ModifierType mode, LogicalSide direction) {
        super.onModeApply(player, mode, direction);

        AttributeInstance attr = player.getAttribute(getAttribute());
        if (attr == null) {
            return;
        }

        ResourceLocation modifierId = getModifierId(mode);
        attr.removeModifier(modifierId);
        attr.addTransientModifier(createModifier(modifierId, mode, player, direction));
    }

    @Override
    public void onModeRemove(Player player, ModifierType mode, LogicalSide direction, boolean removedCompletely) {
        super.onModeRemove(player, mode, direction, removedCompletely);

        AttributeInstance attr = player.getAttribute(getAttribute());
        if (attr == null) {
            return;
        }

        attr.removeModifier(getModifierId(mode));
    }

    public void refreshAttribute(Player player) {
        AttributeInstance attr = player.getAttribute(getAttribute());
        if (attr == null) {
            return;
        }

        LogicalSide direction = player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
        for (ModifierType mode : ModifierType.values()) {
            ResourceLocation modifierId = getModifierId(mode);
            if (attr.hasModifier(modifierId)) {
                attr.removeModifier(modifierId);
                attr.addTransientModifier(createModifier(modifierId, mode, player, direction));
            }
        }
    }

    private ResourceLocation getModifierId(ModifierType mode) {
        return ResourceLocation.fromNamespaceAndPath("astralsorcery", getID(mode).toString());
    }

    private AttributeModifier createModifier(ResourceLocation id, ModifierType mode, Player player, LogicalSide direction) {
        double amount = PerkAttributeHelper.getOrCreateMap(player, direction)
                .getAttributeInstance(player, ResearchHelper.getProgress(player, direction), this, mode) - 1;
        return new AttributeModifier(id, amount, mode.getVanillaAttributeOperation());
    }

    public abstract UUID getID(ModifierType mode);

    public abstract String getDescription();

    @Nonnull
    public abstract Holder<Attribute> getAttribute();

}
