/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemAttunedRockCrystal
 * Created by HellFirePvP
 * Date: 21.07.2019 / 13:38
 */
public class ItemAttunedRockCrystal extends ItemAttunedCrystalBase {

    public ItemAttunedRockCrystal() {
        super(new Properties()
);
    }

    @Override
    public ItemAttunedCrystalBase getTunedItemVariant() {
        return ItemsAS.ATTUNED_ROCK_CRYSTAL;
    }

    @Override
    public ItemCrystalBase getInertDuplicateItem() {
        return ItemsAS.ROCK_CRYSTAL;
    }
}
