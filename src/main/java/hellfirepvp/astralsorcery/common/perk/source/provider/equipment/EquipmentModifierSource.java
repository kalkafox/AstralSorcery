/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.source.provider.equipment;

import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.source.AttributeModifierProvider;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.neoforged.fml.LogicalSide;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EquipmentModifierSource
 * Created by HellFirePvP
 * Date: 02.04.2020 / 19:54
 */
public class EquipmentModifierSource implements ModifierSource, AttributeModifierProvider {

    final EquipmentSlot slot;
    final ItemStack itemStack;

    EquipmentModifierSource(EquipmentSlot slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemStack = itemStack;
    }

    @Override
    public boolean canApplySource(Player player, LogicalSide dist) {
        return true;
    }

    @Override
    public void onRemove(Player player, LogicalSide dist) {}

    @Override
    public void onApply(Player player, LogicalSide dist) {}

    @Override
    public Collection<PerkAttributeModifier> getModifiers(Player player, LogicalSide side, boolean ignoreRequirements) {
        if (this.itemStack.isEmpty()) {
            return Collections.emptyList();
        }
        return DynamicModifierHelper.getDynamicModifiers(this.itemStack, player, side, ignoreRequirements);
    }

    @Override
    public boolean isEqual(ModifierSource other) {
        return this.equals(other);
    }

    @Override
    public ResourceLocation getProviderName() {
        return ModifierManager.EQUIPMENT_PROVIDER_KEY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentModifierSource that = (EquipmentModifierSource) o;
        return slot == that.slot &&
                NBTHelper.getUUID(NBTHelper.getPersistentData(itemStack), EquipmentSourceProvider.KEY_MOD_IDENTIFIER, Util.DUMMY_UUID)
                        .equals(NBTHelper.getUUID(NBTHelper.getPersistentData(that.itemStack), EquipmentSourceProvider.KEY_MOD_IDENTIFIER, Util.DUMMY_UUID));
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, NBTHelper.getUUID(NBTHelper.getPersistentData(itemStack), EquipmentSourceProvider.KEY_MOD_IDENTIFIER, Util.DUMMY_UUID));
    }
}
