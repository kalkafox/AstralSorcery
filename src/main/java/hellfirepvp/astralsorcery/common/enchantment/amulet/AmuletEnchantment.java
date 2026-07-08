/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentType;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AmuletEnchantment
 * Created by HellFirePvP
 * Date: 11.08.2019 / 20:12
 */
public class AmuletEnchantment extends DynamicEnchantment {

    public AmuletEnchantment(DynamicEnchantmentType type, @Nonnull ResourceKey<Enchantment> enchantment, int levelAddition) {
        super(type, enchantment, levelAddition);
    }

    public AmuletEnchantment(DynamicEnchantmentType type, int levelAddition) {
        super(type, levelAddition);
    }

    public MutableComponent getDisplay() {
        String typeStr = this.getType().getDisplayName();
        MutableComponent levels = Component.translatable(String.format("astralsorcery.amulet.enchantment.level.%s", this.levelAddition > 1 ? "more" : "one"));

        if (this.getType().isEnchantmentSpecific()) {
            ResourceKey<Enchantment> enchantment = this.getEnchantment();
            MutableComponent enchantmentName = enchantment == null ?
                    Component.empty() :
                    Component.translatable(Util.makeDescriptionId("enchantment", enchantment.location()));
            return Component.translatable(typeStr,
                    String.valueOf(this.getLevelAddition()), levels, enchantmentName);
        } else {
            return Component.translatable(typeStr, String.valueOf(this.getLevelAddition()), levels);
        }
    }

    public boolean canMerge(AmuletEnchantment other) {
        return this.type.equals(other.type) && (!this.type.isEnchantmentSpecific() || Objects.equals(this.enchantment, other.enchantment));
    }

    public void merge(AmuletEnchantment src) {
        if (canMerge(src)) {
            this.levelAddition += src.levelAddition;
        }
    }

    public CompoundTag serialize() {
        CompoundTag cmp = new CompoundTag();
        cmp.putInt("type", this.type.ordinal());
        cmp.putInt("level", this.levelAddition);
        if (this.type.isEnchantmentSpecific()) { //Enchantment must not be null here anyway as the type requires a ench to begin with
            cmp.putString("ench", this.enchantment.location().toString());
        }
        return cmp;
    }

    @Nullable
    public static AmuletEnchantment deserialize(CompoundTag cmp) {
        int typeId = cmp.getInt("type");
        if (typeId < 0 || typeId >= DynamicEnchantmentType.values().length) {
            return null;
        }
        DynamicEnchantmentType type = DynamicEnchantmentType.values()[typeId];
        int level = Math.max(0, cmp.getInt("level"));
        if (type.isEnchantmentSpecific()) {
            ResourceLocation res = ResourceLocation.parse(cmp.getString("ench"));
            return new AmuletEnchantment(type, ResourceKey.create(Registries.ENCHANTMENT, res), level);
        } else {
            return new AmuletEnchantment(type, level);
        }
    }

}
