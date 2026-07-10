/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectWaterBurst
 * Created by HellFirePvP
 * Date: 25.09.2019 / 19:25
 */
public class EffectLiquidBurst extends AltarRecipeEffect {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(TileAltar altar, ActiveSimpleAltarRecipe.CraftingState state) {
        if (state == ActiveSimpleAltarRecipe.CraftingState.ACTIVE &&
                getClientTick() % 10 == 0 &&
                random.nextBoolean()) {

            float height = 5;

            Vector3 position = new Vector3(altar).add(
                    0.5 + random.nextFloat() * 3 * (random.nextBoolean() ? 1 : -1),
                    0,
                    0.5 + random.nextFloat() * 3 * (random.nextBoolean() ? 1 : -1));
            Vector3 target = position.clone().addY(height);

            EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                    .spawn(position)
                    .setup(target, 0.8F, 0.8F)
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setMaxAge(20);

            for (int i = 0; i < 170; i++) {
                float perc = random.nextFloat();

                Vector3 mot = new Vector3(
                        random.nextFloat() * 0.08 * (random.nextBoolean() ? 1 : -1) * (1 - perc),
                        0,
                        random.nextFloat() * 0.08 * (random.nextBoolean() ? 1 : -1) * (1 - perc)
                );

                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(position.clone().addY(height * perc))
                        .setDeltaMovement(mot)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setAlphaMultiplier(1F)
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                        .setMaxAge(20 + random.nextInt(5));
                if (random.nextBoolean()) {
                     p.color(VFXColorFunction.WHITE);
                } else {
                    p.color(VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE));
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(TileAltar altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(TileAltar altar, boolean isChaining) {}
}
