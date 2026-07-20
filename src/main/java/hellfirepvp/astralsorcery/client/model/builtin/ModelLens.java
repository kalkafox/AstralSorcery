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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

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
        ModelPart root = createLayer().bakeRoot();
        this.base = root.getChild("base");
        this.frame1 = root.getChild("frame1");
        this.frame2 = root.getChild("frame2");
        this.lens = root.getChild("lens");
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 13)
                .addBox(-6.0F, 4.0F, -6.0F, 12, 2, 12), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("frame1", CubeListBuilder.create()
                .texOffs(0, 13)
                .addBox(-8.0F, -4.0F, -1.0F, 2, 10, 2), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("frame2", CubeListBuilder.create()
                .texOffs(0, 13)
                .mirror()
                .addBox(6.0F, -4.0F, -1.0F, 2, 10, 2), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("lens", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, -6.0F, -0.5F, 12, 12, 1), PartPose.offset(0.0F, 14.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void renderFrame(PoseStack matrixStackIn, MultiBufferSource buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.MODEL_LENS_SOLID);
        int color = packColor(red, green, blue, alpha);
        this.base.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, color);
        this.frame1.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, color);
        this.frame2.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, color);
    }

    public void renderGlass(PoseStack matrixStackIn, MultiBufferSource buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.MODEL_LENS_GLASS);
        this.lens.render(matrixStackIn, vb, packedLightIn, packedOverlayIn, packColor(red, green, blue, alpha));

        this.lens.xRot = 0;
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
