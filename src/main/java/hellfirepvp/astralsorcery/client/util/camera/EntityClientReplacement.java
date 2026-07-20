/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityClientReplacement
 * Created by HellFirePvP
 * Date: 02.12.2019 / 20:13
 */
public class EntityClientReplacement extends AbstractClientPlayer {

    public EntityClientReplacement() {
        super(Minecraft.getInstance().level, Minecraft.getInstance().player.getGameProfile());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isModelPartShown(part);
    }

    /**
     * This entity's UUID is re-rolled so it can coexist with the real player in the entity
     * lookup; resolve skin/cape info through the real player's UUID instead of our own.
     */
    @Nullable
    @Override
    protected PlayerInfo getPlayerInfo() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().getConnection() == null) {
            return null;
        }
        return Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID());
    }
}
