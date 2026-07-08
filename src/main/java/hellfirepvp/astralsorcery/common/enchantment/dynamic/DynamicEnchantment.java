/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment.dynamic;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicEnchantment
 * Created by HellFirePvP
 * Date: 11.08.2019 / 19:45
 */
public class DynamicEnchantment {

    protected final DynamicEnchantmentType type;
    @Nullable
    protected final ResourceKey<Enchantment> enchantment;
    protected int levelAddition;

    public DynamicEnchantment(DynamicEnchantmentType type, @Nonnull ResourceKey<Enchantment> enchantment, int levelAddition) {
        if (!type.isEnchantmentSpecific()) {
            throw new IllegalArgumentException("Tried to create dynamic enchantment with a type that doesn\'t require an enchantment, but supplied an enchantment!");
        }
        this.type = type;
        this.enchantment = enchantment;
        this.levelAddition = levelAddition;
    }

    public DynamicEnchantment(DynamicEnchantmentType type, int levelAddition) {
        if (type.isEnchantmentSpecific()) {
            throw new IllegalArgumentException("Tried to create dynamic enchantment with a type that requires an enchantment without specifying such an enchantment!");
        }
        this.type = type;
        this.enchantment = null;
        this.levelAddition = levelAddition;
    }

    public DynamicEnchantmentType getType() {
        return type;
    }

    @Nullable
    public ResourceKey<Enchantment> getEnchantment() {
        return enchantment;
    }

    public int getLevelAddition() {
        return levelAddition;
    }

    public void setLevelAddition(int levelAddition) {
        this.levelAddition = levelAddition;
    }

    @Nonnull
    public DynamicEnchantment copy() {
        return this.copy(this.getLevelAddition());
    }

    @Nonnull
    public DynamicEnchantment copy(int level) {
        if (this.getType().isEnchantmentSpecific()) {
            ResourceKey<Enchantment> enchantment = this.getEnchantment();
            if (enchantment == null) {
                throw new IllegalStateException("Missing enchantment key for enchantment-specific dynamic modifier");
            }
            return new DynamicEnchantment(this.getType(), enchantment, level);
        } else {
            return new DynamicEnchantment(this.type, level);
        }
    }
}
