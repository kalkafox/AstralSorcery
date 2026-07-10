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
 * Class: ModelRefractionTable
 * AS_starmapper - wiiv
 * Created using Tabula 7.0.0
 */
public class ModelRefractionTable extends CustomModel {

    private final ModelPart fitting_l;
    private final ModelPart fitting_r;
    private final ModelPart support_1;
    private final ModelPart support_2;
    private final ModelPart support_3;
    private final ModelPart support_4;
    private final ModelPart platform_l;
    private final ModelPart platform_r;
    private final ModelPart platform_f;
    private final ModelPart platform_b;
    private final ModelPart basin_l;
    private final ModelPart basim_r;
    private final ModelPart basin_f;
    private final ModelPart basin_b;
    private final ModelPart socket;
    private final ModelPart base;
    private final ModelPart leg_1;
    private final ModelPart leg_2;
    private final ModelPart leg_3;
    private final ModelPart leg_4;

    private final ModelPart parchment;
    private final ModelPart black_mirror;

    private final ModelPart treated_glass;

    public ModelRefractionTable() {
        super((resKey) -> RenderTypesAS.MODEL_REFRACTION_TABLE);
        this.textureWidth = 128;
        this.textureHeight = 128;

        this.fitting_l = new ModelPart(this, 0, 48);
        this.fitting_l.setPos(0.0F, 0.0F, 0.0F);
        this.fitting_l.addBox(-14.0F, 0.0F, -12.0F, 4, 4, 24, 0.0F);
        this.fitting_r = new ModelPart(this, 56, 48);
        this.fitting_r.setPos(0.0F, 0.0F, 0.0F);
        this.fitting_r.addBox(10.0F, 0.0F, -12.0F, 4, 4, 24, 0.0F);
        this.support_1 = new ModelPart(this, 24, 76);
        this.support_1.setPos(0.0F, 0.0F, 0.0F);
        this.support_1.addBox(-14.0F, 4.0F, -12.0F, 4, 6, 2, 0.0F);
        this.support_2 = new ModelPart(this, 24, 76);
        this.support_2.setPos(0.0F, 0.0F, 0.0F);
        this.support_2.addBox(10.0F, 4.0F, -12.0F, 4, 6, 2, 0.0F);
        this.support_3 = new ModelPart(this, 24, 76);
        this.support_3.setPos(0.0F, 0.0F, 0.0F);
        this.support_3.addBox(10.0F, 4.0F, 10.0F, 4, 6, 2, 0.0F);
        this.support_4 = new ModelPart(this, 24, 76);
        this.support_4.setPos(0.0F, 0.0F, 0.0F);
        this.support_4.addBox(-14.0F, 4.0F, 10.0F, 4, 6, 2, 0.0F);
        this.platform_l = new ModelPart(this, 0, 0);
        this.platform_l.setPos(0.0F, 16.0F, 0.0F);
        this.platform_l.addBox(-14.0F, -6.0F, -12.0F, 4, 2, 24, 0.0F);
        this.platform_r = new ModelPart(this, 0, 0);
        this.platform_r.setPos(0.0F, 16.0F, 0.0F);
        this.platform_r.addBox(10.0F, -6.0F, -12.0F, 4, 2, 24, 0.0F);
        this.platform_f = new ModelPart(this, 32, 0);
        this.platform_f.setPos(0.0F, 16.0F, 0.0F);
        this.platform_f.addBox(-10.0F, -6.0F, -12.0F, 20, 2, 2, 0.0F);
        this.platform_b = new ModelPart(this, 32, 0);
        this.platform_b.setPos(0.0F, 16.0F, 0.0F);
        this.platform_b.addBox(-10.0F, -6.0F, 10.0F, 20, 2, 2, 0.0F);
        this.basin_l = new ModelPart(this, 84, 76);
        this.basin_l.setPos(0.0F, 16.0F, 0.0F);
        this.basin_l.addBox(-10.0F, -8.0F, -10.0F, 2, 6, 20, 0.0F);
        this.basim_r = new ModelPart(this, 84, 102);
        this.basim_r.setPos(0.0F, 16.0F, 0.0F);
        this.basim_r.addBox(8.0F, -8.0F, -10.0F, 2, 6, 20, 0.0F);
        this.basin_f = new ModelPart(this, 36, 84);
        this.basin_f.setPos(0.0F, 16.0F, 0.0F);
        this.basin_f.addBox(-8.0F, -8.0F, -10.0F, 16, 6, 2, 0.0F);
        this.basin_b = new ModelPart(this, 36, 76);
        this.basin_b.setPos(0.0F, 16.0F, 0.0F);
        this.basin_b.addBox(-8.0F, -8.0F, 8.0F, 16, 6, 2, 0.0F);
        this.socket = new ModelPart(this, 0, 76);
        this.socket.setPos(0.0F, 16.0F, 0.0F);
        this.socket.addBox(-3.0F, -4.0F, -3.0F, 6, 2, 6, 0.0F);
        this.base = new ModelPart(this, 0, 26);
        this.base.setPos(0.0F, 16.0F, 0.0F);
        this.base.addBox(-10.0F, -2.0F, -10.0F, 20, 2, 20, 0.0F);
        this.leg_1 = new ModelPart(this, 0, 76);
        this.leg_1.setPos(0.0F, 16.0F, 0.0F);
        this.leg_1.addBox(-10.0F, 0.0F, -10.0F, 6, 8, 6, 0.0F);
        this.leg_2 = new ModelPart(this, 0, 76);
        this.leg_2.setPos(0.0F, 16.0F, 0.0F);
        this.leg_2.addBox(4.0F, 0.0F, -10.0F, 6, 8, 6, 0.0F);
        this.leg_3 = new ModelPart(this, 0, 76);
        this.leg_3.setPos(0.0F, 16.0F, 0.0F);
        this.leg_3.addBox(4.0F, 0.0F, 4.0F, 6, 8, 6, 0.0F);
        this.leg_4 = new ModelPart(this, 0, 76);
        this.leg_4.setPos(0.0F, 16.0F, 0.0F);
        this.leg_4.addBox(-10.0F, 0.0F, 4.0F, 6, 8, 6, 0.0F);

        this.parchment = new ModelPart(this, 66, 28);
        this.parchment.setPos(0.0F, 16.0F, 0.0F);
        this.parchment.addBox(-7.0F, -8.5F, -7.0F, 14, 0, 14, 0.0F);
        this.black_mirror = new ModelPart(this, 64, 12);
        this.black_mirror.setPos(0.0F, 16.0F, 0.0F);
        this.black_mirror.addBox(-8.0F, -8.0F, -8.0F, 16, 0, 16, 0.0F);

        this.treated_glass = new ModelPart(this, 0, 107);
        this.treated_glass.setPos(0.0F, 16.0F, 0.0F);
        this.treated_glass.addBox(-10.0F, -15.0F, -10.0F, 20, 1, 20, 0.0F);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {}

    public void renderFrame(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, boolean hasParchment) {
        this.fitting_l.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.fitting_r.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.support_1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.support_2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.support_3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.support_4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.platform_l.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.platform_r.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.platform_f.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.platform_b.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.basin_l.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.basim_r.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.basin_f.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.basin_b.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.socket.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.leg_1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.leg_2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.leg_3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.leg_4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (hasParchment) {
            this.parchment.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            this.black_mirror.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }



    public void renderGlass(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.treated_glass.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}