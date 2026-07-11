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
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelObservatory
 * Created by wiiv
 * Created using Tabula 7.0.0
 */
public class ModelObservatory extends CustomModel {

    private final ModelPart base;
    private final ModelPart seat;
    private final ModelPart tube;

    public ModelObservatory() {
        super((resKey) -> RenderTypesAS.MODEL_OBSERVATORY);
        ModelPart root = createLayer().bakeRoot();
        this.base = root.getChild("base");
        this.seat = root.getChild("seat");
        this.tube = this.seat.getChild("tube");
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        //base
        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 82)
                .addBox(-12.0F, 18.0F, -16.0F, 24, 6, 28, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -4.0F, 0.0F));
        base.addOrReplaceChild("base1", CubeListBuilder.create()
                .texOffs(120, 82).addBox(-14.0F, 4.0F, -18.0F, 6, 18, 12), PartPose.ZERO);
        base.addOrReplaceChild("base2", CubeListBuilder.create()
                .texOffs(224, 52).addBox(-7.0F, 4.0F, -18.0F, 2, 18, 12), PartPose.ZERO);
        base.addOrReplaceChild("base3", CubeListBuilder.create()
                .texOffs(224, 52).addBox(-4.0F, 4.0F, -18.0F, 2, 18, 12), PartPose.ZERO);
        base.addOrReplaceChild("base4", CubeListBuilder.create()
                .texOffs(224, 52).addBox(-1.0F, 4.0F, -18.0F, 2, 18, 12), PartPose.ZERO);
        base.addOrReplaceChild("base5", CubeListBuilder.create()
                .texOffs(180, 52).addBox(2.0F, 4.0F, -18.0F, 10, 18, 12), PartPose.ZERO);
        base.addOrReplaceChild("base6", CubeListBuilder.create()
                .texOffs(192, 0).addBox(12.0F, -18.0F, -18.0F, 8, 40, 12), PartPose.ZERO);
        base.addOrReplaceChild("base7", CubeListBuilder.create()
                .texOffs(156, 82).addBox(8.0F, 4.0F, -6.0F, 8, 18, 20), PartPose.ZERO);
        base.addOrReplaceChild("base8", CubeListBuilder.create()
                .texOffs(192, 82).addBox(-8.0F, 28.0F, -8.0F, 16, 4, 16), PartPose.offset(0.0F, -4.0F, 0.0F));

        //seat
        PartDefinition seat = root.addOrReplaceChild("seat", CubeListBuilder.create()
                .texOffs(144, 28)
                .addBox(-9.0F, 16.0F, 6.0F, 12, 4, 10), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat1", CubeListBuilder.create()
                .texOffs(144, 42).addBox(-9.0F, 16.0F, 0.0F, 12, 2, 4), PartPose.ZERO);
        seat.addOrReplaceChild("seat2", CubeListBuilder.create()
                .texOffs(144, 10).addBox(-9.0F, 6.0F, 16.0F, 12, 14, 4), PartPose.ZERO);
        seat.addOrReplaceChild("seat3", CubeListBuilder.create()
                .texOffs(144, 0).addBox(-7.0F, 2.0F, 16.0F, 8, 6, 4), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat4", CubeListBuilder.create()
                .texOffs(140, 82).addBox(-1.0F, 18.0F, 12.0F, 2, 4, 8), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat5", CubeListBuilder.create()
                .texOffs(156, 82).addBox(-1.0F, 22.0F, 12.0F, 2, 4, 8), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat6", CubeListBuilder.create()
                .texOffs(156, 82).addBox(-7.0F, 22.0F, 12.0F, 2, 4, 8), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat7", CubeListBuilder.create()
                .texOffs(232, 0).addBox(-1.0F, -2.0F, 20.0F, 2, 28, 2), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat8", CubeListBuilder.create()
                .texOffs(232, 0).addBox(-7.0F, -2.0F, 20.0F, 2, 28, 2), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat9", CubeListBuilder.create()
                .texOffs(232, 2).addBox(2.0F, -2.0F, 20.0F, 2, 22, 2), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat10", CubeListBuilder.create()
                .texOffs(232, 30).addBox(-4.0F, -4.0F, 20.0F, 2, 20, 2), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat11", CubeListBuilder.create()
                .texOffs(232, 2).addBox(-10.0F, -2.0F, 20.0F, 2, 22, 2), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat12", CubeListBuilder.create()
                .texOffs(240, 0).addBox(2.0F, -6.0F, 20.0F, 2, 4, 4), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat13", CubeListBuilder.create()
                .texOffs(240, 0).addBox(-4.0F, -8.0F, 20.0F, 2, 4, 4), PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat14", CubeListBuilder.create()
                .texOffs(240, 0).addBox(-10.0F, -6.0F, 20.0F, 2, 4, 4), PartPose.offset(0.0F, -4.0F, 0.0F));

        //tube
        PartDefinition tube = seat.addOrReplaceChild("tube", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-2.0F, -4.0F, -4.0F, 14, 8, 8),
                PartPose.offsetAndRotation(0.0F, -12.0F, -12.0F, -0.7853981633974483F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube1", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-2.0F, -4.0F, -36.0F, 14, 6, 6), PartPose.ZERO);
        tube.addOrReplaceChild("tube2", CubeListBuilder.create()
                .texOffs(78, 90).addBox(2.0F, -2.0F, -30.0F, 2, 2, 26), PartPose.ZERO);
        tube.addOrReplaceChild("tube3", CubeListBuilder.create()
                .texOffs(78, 90).addBox(6.0F, -2.0F, -30.0F, 2, 2, 26), PartPose.ZERO);
        tube.addOrReplaceChild("tube4", CubeListBuilder.create()
                .texOffs(92, 28).addBox(-2.0F, -16.0F, -2.0F, 14, 8, 2), PartPose.ZERO);
        tube.addOrReplaceChild("tube5", CubeListBuilder.create()
                .texOffs(92, 12).addBox(-2.0F, -7.0F, -2.0F, 14, 2, 2), PartPose.ZERO);
        tube.addOrReplaceChild("tube6", CubeListBuilder.create()
                .texOffs(92, 12).addBox(-2.0F, -16.0F, -34.0F, 14, 8, 2), PartPose.ZERO);
        tube.addOrReplaceChild("tube7", CubeListBuilder.create()
                .texOffs(92, 12).addBox(-2.0F, -7.0F, -34.0F, 14, 2, 2), PartPose.ZERO);
        tube.addOrReplaceChild("tube8", CubeListBuilder.create()
                .texOffs(92, 12).addBox(-2.0F, -16.0F, -40.0F, 14, 14, 2), PartPose.ZERO);
        tube.addOrReplaceChild("tube9", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -16.0F, -60.0F, 14, 14, 18), PartPose.ZERO);
        tube.addOrReplaceChild("tube10", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, -14.0F, -56.0F, 10, 10, 68), PartPose.ZERO);
        tube.addOrReplaceChild("tube11", CubeListBuilder.create()
                .texOffs(92, 50).addBox(-4.0F, -10.0F, 2.0F, 4, 4, 12), PartPose.ZERO);
        tube.addOrReplaceChild("tube12", CubeListBuilder.create()
                .texOffs(44, 32).addBox(-4.0F, -10.0F, 14.0F, 2, 2, 6), PartPose.ZERO);
        tube.addOrReplaceChild("tube13", CubeListBuilder.create()
                .texOffs(0, 48).addBox(2.0F, -12.0F, 12.0F, 6, 6, 4), PartPose.ZERO);
        tube.addOrReplaceChild("tube14", CubeListBuilder.create()
                .texOffs(92, 0).addBox(6.0F, -18.0F, -44.0F, 2, 2, 48), PartPose.ZERO);
        tube.addOrReplaceChild("tube15", CubeListBuilder.create()
                .texOffs(92, 0).addBox(2.0F, -18.0F, -44.0F, 2, 2, 48), PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        int color = packColor(red, green, blue, alpha);
        this.seat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
    }

    public void setupRotations(float iYawDegree, float iPitchDegree) {
        float yawRad = (float) Math.toRadians(iYawDegree);
        float pitchRad = (float) Math.toRadians(iPitchDegree);

        this.seat.yRot = yawRad;
        this.base.yRot = yawRad;
        this.tube.xRot = pitchRad;
    }
}
