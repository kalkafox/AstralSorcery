/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.screen.ScreenObservatory;
import hellfirepvp.astralsorcery.client.screen.container.*;
import hellfirepvp.astralsorcery.common.container.ContainerObservatory;
import hellfirepvp.astralsorcery.common.container.factory.*;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.IContainerFactory;

import static hellfirepvp.astralsorcery.common.lib.ContainerTypesAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryContainerTypes
 * Created by HellFirePvP
 * Date: 09.08.2019 / 21:15
 */
public class RegistryContainerTypes {

    private RegistryContainerTypes() {}

    public static void init() {
        TOME = register("tome", new ContainerTomeProvider.Factory());
        OBSERVATORY = register("observatory", new ContainerObservatoryProvider.Factory());

        ALTAR_DISCOVERY = register("altar_discovery", new ContainerAltarDiscoveryProvider.Factory());
        ALTAR_ATTUNEMENT = register("altar_attunement", new ContainerAltarAttunementProvider.Factory());
        ALTAR_CONSTELLATION = register("altar_constellation", new ContainerAltarConstellationProvider.Factory());
        ALTAR_RADIANCE = register("altar_radiance", new ContainerAltarRadianceProvider.Factory());
    }

    @OnlyIn(Dist.CLIENT)
    public static void initClient(RegisterMenuScreensEvent event) {
        event.register(TOME, ScreenContainerTome::new);
        event.register(OBSERVATORY, (ContainerObservatory observatory, Inventory inventory, Component name) -> new ScreenObservatory(observatory));
        event.register(ALTAR_DISCOVERY, ScreenContainerAltarDiscovery::new);
        event.register(ALTAR_ATTUNEMENT, ScreenContainerAltarAttunement::new);
        event.register(ALTAR_CONSTELLATION, ScreenContainerAltarConstellation::new);
        event.register(ALTAR_RADIANCE, ScreenContainerAltarRadiance::new);
    }

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> T register(String name, IContainerFactory<C> containerFactory) {
        return register(AstralSorcery.key(name), containerFactory);
    }

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> T register(ResourceLocation name, IContainerFactory<C> containerFactory) {
        MenuType<C> type = new MenuType<>(containerFactory, FeatureFlags.DEFAULT_FLAGS);
        AstralRegistries.register(AstralRegistries.MENU_TYPES, name, type);
        return (T) type;
    }
}
