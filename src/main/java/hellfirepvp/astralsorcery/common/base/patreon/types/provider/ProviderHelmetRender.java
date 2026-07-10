/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.types.provider;

import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffectProvider;
import hellfirepvp.astralsorcery.common.base.patreon.types.TypeHelmetRender;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ProviderHelmetRender
 * Created by HellFirePvP
 * Date: 05.04.2020 / 11:37
 */
public class ProviderHelmetRender implements PatreonEffectProvider<TypeHelmetRender> {

    @Override
    public TypeHelmetRender buildEffect(UUID playerUUID, List<String> effectParameters) throws Exception {
        UUID effectUniqueId = UUID.fromString(effectParameters.get(0));

        if (effectParameters.get(1).equals("astralsorcery:blockaltar;3")) {
            effectParameters.set(1, "astralsorcery:altar_radiance");
        }

        String[] itemInfo = effectParameters.get(1).split(";");
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemInfo[0]));
        if (item == null || item == Items.AIR) {
            throw new IllegalArgumentException("Unknown item: " + itemInfo[0]);
        }
        ItemStack stack = new ItemStack(item);
        if (itemInfo.length > 1) {
            int data = Integer.parseInt(itemInfo[1]);
            stack.setBaseDamage(data);
        }
        FlareColor flColor = effectParameters.size() > 2 ?
                FlareColor.valueOf(effectParameters.get(2)) : null;
        return new TypeHelmetRender(effectUniqueId, flColor, playerUUID, stack);
    }
}
