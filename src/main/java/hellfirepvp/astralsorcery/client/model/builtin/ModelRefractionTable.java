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
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelRefractionTable
 * AS_starmapper - wiiv
 * Created using Tabula 7.0.0
 */
public class ModelRefractionTable extends CustomModel {

    private final ModelPart frame;
    private final ModelPart parchment;
    private final ModelPart black_mirror;
    private final ModelPart treated_glass;

    public ModelRefractionTable() {
        super((resKey) -> RenderTypesAS.MODEL_REFRACTION_TABLE);
        ModelPart root = createLayer().bakeRoot();
        this.frame = root.getChild("frame");
        this.parchment = root.getChild("parchment");
        this.black_mirror = root.getChild("black_mirror");
        this.treated_glass = root.getChild("treated_glass");
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition frame = root.addOrReplaceChild("frame", CubeListBuilder.create(), PartPose.ZERO);
        frame.addOrReplaceChild("fitting_l", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-14.0F, 0.0F, -12.0F, 4, 4, 24), PartPose.ZERO);
        frame.addOrReplaceChild("fitting_r", CubeListBuilder.create()
                .texOffs(56, 48).addBox(10.0F, 0.0F, -12.0F, 4, 4, 24), PartPose.ZERO);
        frame.addOrReplaceChild("support_1", CubeListBuilder.create()
                .texOffs(24, 76).addBox(-14.0F, 4.0F, -12.0F, 4, 6, 2), PartPose.ZERO);
        frame.addOrReplaceChild("support_2", CubeListBuilder.create()
                .texOffs(24, 76).addBox(10.0F, 4.0F, -12.0F, 4, 6, 2), PartPose.ZERO);
        frame.addOrReplaceChild("support_3", CubeListBuilder.create()
                .texOffs(24, 76).addBox(10.0F, 4.0F, 10.0F, 4, 6, 2), PartPose.ZERO);
        frame.addOrReplaceChild("support_4", CubeListBuilder.create()
                .texOffs(24, 76).addBox(-14.0F, 4.0F, 10.0F, 4, 6, 2), PartPose.ZERO);
        frame.addOrReplaceChild("platform_l", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-14.0F, -6.0F, -12.0F, 4, 2, 24), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("platform_r", CubeListBuilder.create()
                .texOffs(0, 0).addBox(10.0F, -6.0F, -12.0F, 4, 2, 24), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("platform_f", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-10.0F, -6.0F, -12.0F, 20, 2, 2), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("platform_b", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-10.0F, -6.0F, 10.0F, 20, 2, 2), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("basin_l", CubeListBuilder.create()
                .texOffs(84, 76).addBox(-10.0F, -8.0F, -10.0F, 2, 6, 20), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("basin_r", CubeListBuilder.create()
                .texOffs(84, 102).addBox(8.0F, -8.0F, -10.0F, 2, 6, 20), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("basin_f", CubeListBuilder.create()
                .texOffs(36, 84).addBox(-8.0F, -8.0F, -10.0F, 16, 6, 2), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("basin_b", CubeListBuilder.create()
                .texOffs(36, 76).addBox(-8.0F, -8.0F, 8.0F, 16, 6, 2), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("socket", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-3.0F, -4.0F, -3.0F, 6, 2, 6), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-10.0F, -2.0F, -10.0F, 20, 2, 20), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("leg_1", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-10.0F, 0.0F, -10.0F, 6, 8, 6), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("leg_2", CubeListBuilder.create()
                .texOffs(0, 76).addBox(4.0F, 0.0F, -10.0F, 6, 8, 6), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("leg_3", CubeListBuilder.create()
                .texOffs(0, 76).addBox(4.0F, 0.0F, 4.0F, 6, 8, 6), PartPose.offset(0.0F, 16.0F, 0.0F));
        frame.addOrReplaceChild("leg_4", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-10.0F, 0.0F, 4.0F, 6, 8, 6), PartPose.offset(0.0F, 16.0F, 0.0F));

        root.addOrReplaceChild("parchment", CubeListBuilder.create()
                .texOffs(66, 28).addBox(-7.0F, -8.5F, -7.0F, 14, 0, 14), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("black_mirror", CubeListBuilder.create()
                .texOffs(64, 12).addBox(-8.0F, -8.0F, -8.0F, 16, 0, 16), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("treated_glass", CubeListBuilder.create()
                .texOffs(0, 107).addBox(-10.0F, -15.0F, -10.0F, 20, 1, 20), PartPose.offset(0.0F, 16.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {}

    public void renderFrame(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, boolean hasParchment) {
        int color = packColor(red, green, blue, alpha);
        this.frame.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);

        if (hasParchment) {
            this.parchment.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
            this.black_mirror.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        }
    }

    public void renderGlass(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.treated_glass.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, packColor(red, green, blue, alpha));
    }
}
