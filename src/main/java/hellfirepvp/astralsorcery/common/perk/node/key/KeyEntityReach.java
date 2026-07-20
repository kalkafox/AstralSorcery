/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyEntityReach
 * Created by HellFirePvP
 * Date: 25.08.2020 / 18:31
 */
public class KeyEntityReach extends KeyPerk {

    // 1.21 port: entity reach is now the syncable ENTITY_INTERACTION_RANGE attribute; the 1.16
    // getMouseOver/processUseEntity distance-cap coremods are replaced by extending the entity
    // range to the block interaction range (base 4.5 vs 3.0).
    private static final ResourceLocation REACH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("astralsorcery", "perk_entity_reach");
    private static final double REACH_BONUS = 1.5D;

    public KeyEntityReach(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void applyPerkLogic(Player player, LogicalSide side) {
        super.applyPerkLogic(player, side);

        if (side.isServer()) {
            AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (reach != null && !reach.hasModifier(REACH_MODIFIER_ID)) {
                reach.addTransientModifier(new AttributeModifier(REACH_MODIFIER_ID, REACH_BONUS, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @Override
    public void removePerkLogic(Player player, LogicalSide side) {
        super.removePerkLogic(player, side);

        if (side.isServer()) {
            AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (reach != null) {
                reach.removeModifier(REACH_MODIFIER_ID);
            }
        }
    }
}

