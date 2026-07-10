/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinWorld
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 */
@Mixin(Level.class)
public class MixinWorld {

    @Shadow private int skyDarken;

    @Inject(method = "calculateInitialSkylight", at = @At("RETURN"), cancellable = true)
    public void solarEclipseSunBrightnessServer(CallbackInfo ci) {
        Level level = (Level)(Object) this;

        WorldContext ctx = SkyHandler.getContext(level);
        String strDimKey = level.dimension().getLocation().toString();
        if (ctx != null &&
                ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            this.skyDarken = 11 - Math.round(ctx.getCelestialEventHandler().getSolarEclipsePercent() * 11F);
        }
    }

}
