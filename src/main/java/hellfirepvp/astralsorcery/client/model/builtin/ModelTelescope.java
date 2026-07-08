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
 * Class: ModelTelescope
 * AS_telescope - wiiv
 * Created using Tabula 7.0.0
 */
public class ModelTelescope extends CustomModel {

    private final ModelPart mountpiece;
    private final ModelPart opticalTube;
    private final ModelPart leg;
    private final ModelPart mountpiece_1;
    private final ModelPart aperture;
    private final ModelPart extension;
    private final ModelPart detail;
    private final ModelPart aperture_1;

    public ModelTelescope() {
        super((resKey) -> RenderTypesAS.MODEL_TELESCOPE);
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.leg = new ModelPart(this, 56, 0);
        this.leg.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.leg.addBox(-1.0F, -10.0F, -1.0F, 2, 36, 2, 0.0F);
        this.mountpiece_1 = new ModelPart(this, 32, 0);
        this.mountpiece_1.setRotationPoint(0.0F, 0.0F, -1.0F);
        this.mountpiece_1.addBox(-2.0F, 20.0F, -1.0F, 4, 6, 4, 0.0F);
        this.aperture_1 = new ModelPart(this, 28, 28);
        this.aperture_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.aperture_1.addBox(-1.0F, -3.0F, -6.0F, 6, 6, 2, 0.0F);
        this.aperture = new ModelPart(this, 0, 28);
        this.aperture.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.aperture.addBox(-1.0F, -3.0F, -16.0F, 6, 6, 8, 0.0F);
        this.extension = new ModelPart(this, 0, 12);
        this.extension.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.extension.addBox(-2.0F, -6.0F, 6.0F, 2, 6, 2, 0.0F);
        this.detail = new ModelPart(this, 0, 8);
        this.detail.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.detail.addBox(1.0F, -1.0F, 10.0F, 2, 2, 2, 0.0F);
        this.opticalTube = new ModelPart(this, 0, 0);
        this.opticalTube.setRotationPoint(1.0F, -3.0F, 0.0F);
        this.opticalTube.addBox(0.0F, -2.0F, -14.0F, 4, 4, 24, 0.0F);
        this.setRotateAngle(opticalTube, -0.7853981633974483F, 0.0F, 0.0F);
        this.mountpiece = new ModelPart(this, 0, 0);
        this.mountpiece.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.mountpiece.addBox(-2.0F, 4.0F, -2.0F, 4, 4, 4, 0.0F);

        this.opticalTube.addChild(this.extension);
        this.opticalTube.addChild(this.aperture_1);
        this.opticalTube.addChild(this.aperture);
        this.opticalTube.addChild(this.detail);
        this.mountpiece.addChild(this.leg);
        this.mountpiece.addChild(this.mountpiece_1);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.mountpiece.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.opticalTube.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
