/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ArmorMaterialImbuedLeather
 * Created by HellFirePvP
 * Date: 17.02.2020 / 19:16
 */
public final class ArmorMaterialImbuedLeather {

    private ArmorMaterialImbuedLeather() {}

    public static ArmorMaterial create() {
        return new ArmorMaterial(
                Map.of(ArmorItem.Type.CHESTPLATE, 7),
                24,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(ItemsAS.STARDUST),
                List.of(new ArmorMaterial.Layer(AstralSorcery.key("imbued_leather"))),
                1.5F,
                0F);
    }
}
