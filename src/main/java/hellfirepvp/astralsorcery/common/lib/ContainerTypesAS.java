/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.container.*;
import net.minecraft.world.inventory.MenuType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerTypesAS
 * Created by HellFirePvP
 * Date: 09.08.2019 / 21:22
 */
public class ContainerTypesAS {

    private ContainerTypesAS() {}

    public static MenuType<ContainerTome> TOME;
    public static MenuType<ContainerObservatory> OBSERVATORY;

    public static MenuType<ContainerAltarDiscovery> ALTAR_DISCOVERY;
    public static MenuType<ContainerAltarAttunement> ALTAR_ATTUNEMENT;
    public static MenuType<ContainerAltarConstellation> ALTAR_CONSTELLATION;
    public static MenuType<ContainerAltarTrait> ALTAR_RADIANCE;

}
