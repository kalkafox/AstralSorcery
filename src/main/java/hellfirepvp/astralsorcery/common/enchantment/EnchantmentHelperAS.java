/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;

/**
 * Small holder lookup bridge for the 1.21 dynamic enchantment registry.
 */
public class EnchantmentHelperAS {

    private EnchantmentHelperAS() {}

    public static Optional<Holder.Reference<Enchantment>> getHolder(ResourceKey<Enchantment> key) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return Optional.empty();
        }
        return getHolder(server.registryAccess(), key);
    }

    public static Optional<Holder.Reference<Enchantment>> getHolder(HolderLookup.Provider BUILTIN, ResourceKey<Enchantment> key) {
        return BUILTIN.lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(key));
    }
}
