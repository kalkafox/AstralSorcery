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
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.source.FXSourceOrbital;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;

import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FXOrbitalArmara
 * Created by HellFirePvP
 * Date: 19.07.2019 / 10:27
 */
public class FXOrbitalArmara extends FXSourceOrbital<FXFacingParticle, BatchRenderContext<FXFacingParticle>> {

    private static final VFXColorFunction<FXFacingParticle> armaraPrimary =
            VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_ARMARA);
    private static final VFXColorFunction<FXFacingParticle> armaraSecondary =
            VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_ARMARA_SECONDARY);

    private int count = 2 + random.nextInt(2);

    public FXOrbitalArmara(Vector3 pos) {
        super(pos, EffectTemplatesAS.GENERIC_PARTICLE);
        this.refresh(fx -> {
            count--;
            return count > 0;
        });
    }

    @Override
    public void tick() {
        super.tick();

        this.move(new Vector3(0, 0.05, 0));
    }

    @Override
    public void spawnOrbitalParticle(Vector3 pos, Function<Vector3, FXFacingParticle> effectRegistrar) {
        if (random.nextInt(2) == 0) {
            effectRegistrar.apply(pos)
                    .color(armaraPrimary)
                    .setScaleMultiplier(0.25F)
                    .addPosition(new Vector3(
                            (random.nextFloat() * 0.01F) * (random.nextBoolean() ? 1 : -1),
                            (random.nextFloat() * 0.01F) * (random.nextBoolean() ? 1 : -1),
                            (random.nextFloat() * 0.01F) * (random.nextBoolean() ? 1 : -1)
                    ))
                    .setMaxAge(45);
        }
        if (random.nextInt(3) == 0) {
            effectRegistrar.apply(pos)
                    .color(armaraSecondary)
                    .setScaleMultiplier(0.25F)
                    .setDeltaMovement(new Vector3(
                            (random.nextFloat() * 0.025F) * (random.nextBoolean() ? 1 : -1),
                            (random.nextFloat() * 0.025F) * (random.nextBoolean() ? 1 : -1),
                            (random.nextFloat() * 0.025F) * (random.nextBoolean() ? 1 : -1)
                    ))
                    .setMaxAge(35);
        }
    }

    @Override
    public void populateProperties(EffectProperties<FXFacingParticle> properties) {}
}
