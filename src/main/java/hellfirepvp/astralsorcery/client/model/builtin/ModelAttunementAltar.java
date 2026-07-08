/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.model.builtin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import net.minecraft.client.model.geom.ModelPart;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelAttunementAltar
 * Created by wiiv
 */
public class ModelAttunementAltar extends CustomModel {

    private final ModelPart base;
    private final ModelPart hovering;

    public ModelAttunementAltar() {
        super((resKey) -> RenderTypesAS.MODEL_ATTUNEMENT_ALTAR);
        this.textureWidth = 128;
        this.textureHeight = 32;

        this.base = new ModelPart(this, 0, 0);
        this.base.setRotationPoint(0.0F, 16.0F, 0.0F);
        this.base.addBox(-10.0F, -14.0F, -10.0F, 20, 6, 20, 0.0F);

        this.hovering = new ModelPart(this, 0, 0);
        this.hovering.setRotationPoint(-2.0F, -16.0F, -2.0F); //was -14, -14
        this.hovering.addBox(0.0F, 0.0F, 0.0F, 4, 4, 4, 0.0F);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    public void renderHovering(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, float offX, float offZ, float perc) {
        float distance = 0.9453125F;
        this.hovering.setRotationPoint(-2F + (16F * offX * distance), -16F, -2F + (16F * offZ * distance));
        this.setRotateAngle(this.hovering, offZ * 0.39269908169872414F * perc, 0, offX * -0.39269908169872414F * perc);
        this.hovering.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
