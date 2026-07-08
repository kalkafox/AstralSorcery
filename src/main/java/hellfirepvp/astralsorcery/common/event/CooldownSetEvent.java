/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CooldownSetEvent
 * Created by HellFirePvP
 * Date: 27.07.2020 / 21:05
 */
public class CooldownSetEvent extends Event {

    private final Player player;
    private final int originalCooldown;
    private int cooldown;

    public CooldownSetEvent(Player player, int originalCooldown) {
        this.player = player;
        this.originalCooldown = originalCooldown;
        this.setCooldown(this.getOriginalCooldown());
    }

    public Player getPlayer() {
        return player;
    }

    public int getOriginalCooldown() {
        return originalCooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getResultCooldown() {
        return cooldown;
    }
}
