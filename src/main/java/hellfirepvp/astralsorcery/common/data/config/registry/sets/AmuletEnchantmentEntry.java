/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigDataSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AmuletEnchantmentEntry
 * Created by HellFirePvP
 * Date: 11.08.2019 / 20:42
 */
public class AmuletEnchantmentEntry implements ConfigDataSet, Comparable<AmuletEnchantmentEntry> {

    private final ResourceKey<Enchantment> enchantment;
    private final int weight;

    public AmuletEnchantmentEntry(ResourceKey<Enchantment> ench, int weight) {
        this.enchantment = ench;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public ResourceKey<Enchantment> getEnchantment() {
        return enchantment;
    }

    @Override
    public int compareTo(AmuletEnchantmentEntry o) {
        return Integer.compare(this.weight, o.weight);
    }

    @Nonnull
    @Override
    public String serialize() {
        return this.enchantment.location().toString() + ";" + weight;
    }

    @Nullable
    public static AmuletEnchantmentEntry deserialize(String str) {
        String[] spl = str.split(";");
        if (spl.length < 2) {
            return null;
        }
        String enchantmentKey = spl[0];
        String weight = spl[1];

        //TODO find a better solution than hardcoding (duh)
        ResourceLocation registryName = ResourceLocation.parse(enchantmentKey);
        if (registryName.toString().equalsIgnoreCase("cofhcore:holding")) {
            AstralSorcery.log.info("Auto-ignoring amulet enchantment 'cofhcore:holding' as it's prone to cause issues.");
            return null;
        }

        int w;
        try {
            w = Integer.parseInt(weight);
        } catch (NumberFormatException exc) {
            AstralSorcery.log.info("Ignoring whitelist entry " + str + " for amulet enchantments - last :-separated argument is not a number!");
            return null;
        }
        return new AmuletEnchantmentEntry(ResourceKey.create(Registries.ENCHANTMENT, registryName), w);
    }
}
