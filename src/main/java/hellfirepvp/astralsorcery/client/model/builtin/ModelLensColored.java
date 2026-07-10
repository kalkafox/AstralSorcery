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
 * Class: ModelLensColored
 * Created by wiiv
 * Created using Tabula 4.1.1
 * Date: 21.09.2019 / 15:18
 */
public class ModelLensColored extends CustomModel {

    public final ModelPart glass;
    public final ModelPart detail1;
    public final ModelPart detail1_1;
    public final ModelPart fitting2;
    public final ModelPart fitting1;

    public ModelLensColored() {
        super((resKey) -> RenderTypesAS.MODEL_LENS_COLORED_SOLID);
        this.textureWidth = 32;
        this.textureHeight = 16;
        this.glass = new ModelPart(this, 0, 0);
        this.glass.setPos(0.0F, 14.0F, 0.0F);
        this.glass.addBox(-5.0F, -5.0F, -1.51F, 10, 10, 1, 0.0F);
        this.fitting1 = new ModelPart(this, 22, 0);
        this.fitting1.setPos(0.0F, 14.0F, 0.0F);
        this.fitting1.addBox(-5.0F, -7.0F, -1.5F, 2, 1, 2, 0.0F);
        this.detail1_1 = new ModelPart(this, 22, 3);
        this.detail1_1.setPos(0.0F, 14.0F, 0.0F);
        this.detail1_1.addBox(3.0F, -6.0F, -1.5F, 2, 1, 1, 0.0F);
        this.fitting2 = new ModelPart(this, 22, 0);
        this.fitting2.setPos(0.0F, 14.0F, 0.0F);
        this.fitting2.addBox(3.0F, -7.0F, -1.5F, 2, 1, 2, 0.0F);
        this.detail1 = new ModelPart(this, 22, 3);
        this.detail1.setPos(0.0F, 14.0F, 0.0F);
        this.detail1.addBox(-5.0F, -6.0F, -1.5F, 2, 1, 1, 0.0F);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.fitting1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.detail1_1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.fitting2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.detail1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    public void renderGlass(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.glass.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
