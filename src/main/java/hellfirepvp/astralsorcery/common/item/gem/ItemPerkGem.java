/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.gem;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.DynamicAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketItem;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketPerk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemPerkGem
 * Created by HellFirePvP
 * Date: 09.08.2019 / 07:25
 */
public abstract class ItemPerkGem extends Item implements GemSocketItem {

    private final GemType type;

    public ItemPerkGem(GemType type) {
        super(new Properties()
                .stacksTo(1)
);
        this.type = type;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }

        if (DynamicModifierHelper.getStaticModifiers(stack).isEmpty()) {
            GemAttributeHelper.rollGem(stack);
        }
    }

    @Nullable
    public static GemType getGemType(ItemStack gem) {
        if (gem.isEmpty() || !(gem.getItem() instanceof ItemPerkGem)) {
            return null;
        }
        return ((ItemPerkGem) gem.getItem()).type;
    }

    @Override
    public <T extends AbstractPerk & GemSocketPerk> boolean canBeInserted(ItemStack stack, T perk, Player player, PlayerProgress progress, LogicalSide direction) {
        return !this.getModifiers(stack, perk, player, direction).isEmpty();
    }

    @Override
    public <T extends AbstractPerk & GemSocketPerk> List<DynamicAttributeModifier> getModifiers(ItemStack stack, T perk, Player player, LogicalSide direction) {
        return DynamicModifierHelper.getStaticModifiers(stack);
    }
}
