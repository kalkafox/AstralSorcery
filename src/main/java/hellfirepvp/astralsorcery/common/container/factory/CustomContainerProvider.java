/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomContainerProvider
 * Created by HellFirePvP
 * Date: 10.08.2019 / 09:11
 */
public abstract class CustomContainerProvider<C extends AbstractContainerMenu> implements MenuProvider {

    private final MenuType<C> type;

    public CustomContainerProvider(MenuType<C> type) {
        this.type = type;
    }

    @Override
    public Component getDisplayName() {
        ResourceLocation key = RegistryHelper.getKey(this.type);
        return Component.translatable("screen.%s.%s", key.getNamespace(), key.getPath());
    }

    @Nonnull
    @Override
    public abstract C createMenu(int id, Inventory plInventory, Player player);

    protected abstract void writeExtraData(FriendlyByteBuf buf);

    public void openFor(ServerPlayer player) {
        player.openMenu(this, this::writeExtraData);
    }
}
