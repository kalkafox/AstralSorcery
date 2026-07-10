/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.source.orbital;

import hellfirepvp.astralsorcery.client.effect.EffectProperties;
import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.FXSourceOrbital;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingAtlasParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.awt.*;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FXOrbitalInfuser
 * Created by HellFirePvP
 * Date: 10.11.2019 / 16:39
 */
public class FXOrbitalInfuserLiquid extends FXSourceOrbital<FXFacingAtlasParticle, BatchRenderContext<FXFacingAtlasParticle>> {

    private final FluidStack display;
    private final Vector3 infuserTarget;

    public FXOrbitalInfuserLiquid(Vector3 pos, FluidStack displayStack) {
        super(pos, EffectTemplatesAS.GENERIC_ATLAS_PARTICLE);
        this.display = displayStack.copy();
        this.infuserTarget = pos.clone().addY(0.75); //Actually the itemstack hovering above, not the orbital center
    }

    @Override
    public void spawnOrbitalParticle(Vector3 pos, Function<Vector3, FXFacingAtlasParticle> effectRegistrar) {
        Vector3 motion = this.getPosition().subtract(pos).cross(this.getOrbitAxis()).normalize().mul(0.2 + random.nextFloat() * 0.04);
        motion.add(this.getOrbitAxis().normalize().mul(0.2 + random.nextFloat() * 0.05));

        MiscUtils.applyRandomOffset(pos, random, 0.15F);
        if (random.nextInt(4) != 0) {
            effectRegistrar.apply(pos)
                    .pickSprite(RenderingUtils.getParticleIcon(display))
                    .selectFraction(0.2F)
                    .setScaleMultiplier(0.03F)
                    .color((fx, pTicks) -> new Color(ColorUtils.getOverlayColor(display)))
                    .alpha1arg(VFXAlphaFunction.proximity(() -> this.infuserTarget, 2F))
                    .motion(VFXMotionController.target(this.infuserTarget::clone, 0.08F))
                    .setDeltaMovement(motion);
        } else {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .setScaleMultiplier(0.15F)
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.proximity(() -> this.infuserTarget, 2F))
                    .motion(VFXMotionController.target(this.infuserTarget::clone, 0.08F))
                    .setDeltaMovement(motion);
        }
    }

    @Override
    public void populateProperties(EffectProperties<FXFacingAtlasParticle> properties) {}
}
