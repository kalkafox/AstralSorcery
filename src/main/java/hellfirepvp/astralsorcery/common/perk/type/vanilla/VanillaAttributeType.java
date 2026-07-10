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

        AttributeInstance attr = player.getAttributes().createInstanceIfAbsent(getAttribute());
        if (attr == null) {
            return;
        }

        //The attributes don't get written/read from bytebuffer on local connection, but ARE in dedicated connections.
        //Remove minecraft's dummy instances in case we're on a dedicated server.
        if (direction.isClient()) {
            AttributeModifier modifier;
            if ((modifier = attr.getAttributeInstance(getID(mode))) != null) {
                if (!(modifier instanceof DynamicAttributeModifier)) {
                    attr.removeModifier(getID(mode));
                } else {
                    return;
                }
            }
        }

        switch (mode) {
            case ADDITION:
                attr.applyNonPersistentModifier(new DynamicAttributeModifier(getID(mode), getDescription() + " Add", this, mode, player, direction));
                break;
            case ADDED_MULTIPLY:
                attr.applyNonPersistentModifier(new DynamicAttributeModifier(getID(mode), getDescription() + " Multiply Add", this, mode, player, direction));
                break;
            case STACKING_MULTIPLY:
                attr.applyNonPersistentModifier(new DynamicAttributeModifier(getID(mode), getDescription() + " Stack Add", this, mode, player, direction));
                break;
            default:
                break;
        }
    }

    @Override
    public void onModeRemove(Player player, ModifierType mode, LogicalSide direction, boolean removedCompletely) {
        super.onModeRemove(player, mode, direction, removedCompletely);

        AttributeInstance attr = player.getAttributes().createInstanceIfAbsent(getAttribute());
        if (attr == null) {
            return;
        }

        attr.removeModifier(getID(mode));
    }

    public void refreshAttribute(Player player) {
        AttributeInstance attr = player.getAttributes().createInstanceIfAbsent(getAttribute());
        if (attr == null) {
            return;
        }

        double base = attr.getBaseValue();
        if (base == 0) {
            attr.setBaseValue(1);
        } else {
            attr.setBaseValue(0);
        }
        attr.setBaseValue(base);
    }

    public abstract UUID getID(ModifierType mode);

    public abstract String getDescription();

    @Nonnull
    public abstract Attribute getAttribute();

    static class DynamicAttributeModifier extends AttributeModifier {

        private Player player;
        private LogicalSide direction;
        private PerkAttributeType type;

        public DynamicAttributeModifier(UUID idIn, String nameIn, PerkAttributeType type, ModifierType mode, Player player, LogicalSide direction) {
            this(idIn, nameIn, type, mode.getVanillaAttributeOperation(), player, direction);
        }

        public DynamicAttributeModifier(UUID idIn, String nameIn, PerkAttributeType type, Operation operationIn, Player player, LogicalSide direction) {
            super(idIn, nameIn, operationIn == Operation.MULTIPLY_TOTAL ? 1 : 0, operationIn);
            this.player = player;
            this.direction = direction;
            this.type = type;
        }

        @Override
        public double getAmount() {
            ModifierType mode = ModifierType.fromVanillaAttributeOperation(getOperation());
            return PerkAttributeHelper.getOrCreateMap(player, direction)
                    .getAttributeInstance(player, ResearchHelper.getProgress(player, direction), type, mode) - 1;
        }

    }

}
