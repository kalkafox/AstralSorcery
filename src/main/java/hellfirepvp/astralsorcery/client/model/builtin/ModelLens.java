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
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelLens
 * Created by wiiv
 * Created using Tabula 4.1.1
 * Date: 21.09.2019 / 15:16
 */
public class ModelLens extends CustomModel {

    public final ModelPart base;
    public final ModelPart frame1;
    public final ModelPart lens;
    public final ModelPart frame2;

    public ModelLens() {
        super((resKey) -> RenderTypesAS.MODEL_LENS_SOLID);
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.base = new ModelPart(this, 0, 13);
        this.base.setRotationPoint(0.0F, 16.0F, 0.0F);
        this.base.addBox(-6.0F, 4.0F, -6.0F, 12, 2, 12, 0.0F);
        this.frame1 = new ModelPart(this, 0, 13);
        this.frame1.setRotationPoint(0.0F, 16.0F, 0.0F);
        this.frame1.addBox(-8.0F, -4.0F, -1.0F, 2, 10, 2, 0.0F);
        this.frame2 = new ModelPart(this, 0, 13);
        this.frame2.mirror = true;
        this.frame2.setRotationPoint(0.0F, 16.0F, 0.0F);
        this.frame2.addBox(6.0F, -4.0F, -1.0F, 2, 10, 2, 0.0F);
        this.lens = new ModelPart(this, 0, 0);
        this.lens.setRotationPoint(0.0F, 14.0F, 0.0F);
        this.lens.addBox(-6.0F, -6.0F, -0.5F, 12, 12, 1, 0.0F);
    }

    public void renderFrame(PoseStack matrixStackIn, MultiBufferSource buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.MODEL_LENS_SOLID);
        this.base.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.frame1.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.frame2.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        RenderingUtils.refreshDrawing(vb, RenderTypesAS.MODEL_LENS_SOLID);
    }

    public void renderGlass(PoseStack matrixStackIn, MultiBufferSource buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.MODEL_LENS_GLASS);
        this.lens.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        this.lens.rotateAngleX = 0;
        RenderingUtils.refreshDrawing(vb, RenderTypesAS.MODEL_LENS_GLASS);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource buffer, int packedLightIn, int packedOverlayIn) {
        super.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn);
        this.renderFrame(matrixStackIn, buffer, packedLightIn, packedOverlayIn, 1F, 1F, 1F, 1F);
        this.renderGlass(matrixStackIn, buffer, packedLightIn, packedOverlayIn, 1F, 1F, 1F, 1F);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {}
}
