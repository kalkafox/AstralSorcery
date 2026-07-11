/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelArmorMantle
 * Created by HellFirePvP
 * Date: 17.02.2020 / 21:21
 */
public class ModelArmorMantle extends CustomArmorModel<LivingEntity> {

    //TODO adjust arms at some point

    // 1.21 port: HumanoidModel parts are baked and final; instead of swapping the body/arm/head
    // fields for replacement parts at render time, the humanoid skeleton is built with empty
    // (cube-less) parts and the mantle geometry attached as their children.
    public ModelArmorMantle() {
        super(createLayer().bakeRoot());
    }

    private static LayerDefinition createLayer() {
        CubeDeformation s = new CubeDeformation(0.01F);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // empty humanoid skeleton (vanilla pivots), no cubes of its own
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        head.addOrReplaceChild("cowl", CubeListBuilder.create()
                .texOffs(0, 33)
                .addBox(-4.5F, -4.0F, -4.0F, 9, 5, 9, s),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));

        PartDefinition bodyAnchor = body.addOrReplaceChild("body_anchor", CubeListBuilder.create()
                .texOffs(0, 41)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, s), PartPose.ZERO);
        PartDefinition torso = bodyAnchor.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.5F, -0.5F, -3.0F, 9, 6, 6, s), PartPose.ZERO);
        torso.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(0, 12)
                .addBox(-3.5F, -0.5F, -1.0F, 7, 7, 2, s),
                PartPose.offsetAndRotation(0.0F, 1.0F, -3.0F, 0.08726646259971647F, 0.0F, 0.0F));
        torso.addOrReplaceChild("mantle_l", CubeListBuilder.create()
                .texOffs(0, 47)
                .mirror()
                .addBox(-8.0F, -3.5F, 1.0F, 9, 21, 5, s),
                PartPose.offsetAndRotation(6.25F, 2.0F, 0.0F, 0.08726646259971647F, 0.2617993877991494F, 0.0F));
        torso.addOrReplaceChild("mantle_r", CubeListBuilder.create()
                .texOffs(0, 47)
                .addBox(-1.0F, -3.5F, 1.0F, 9, 21, 5, s),
                PartPose.offsetAndRotation(-6.25F, 2.0F, 0.0F, 0.08726646259971647F, -0.2617993877991494F, 0.0F));

        PartDefinition armLAnchor = leftArm.addOrReplaceChild("arm_l_anchor", CubeListBuilder.create()
                .texOffs(0, 41)
                .mirror()
                .addBox(-6.0F, -2.0F, -1.0F, 2, 2, 2, s), PartPose.offset(4.0F, 2.0F, 0.0F));
        PartDefinition armLpauldron = armLAnchor.addOrReplaceChild("arm_l_pauldron", CubeListBuilder.create()
                .texOffs(0, 21)
                .mirror()
                .addBox(-5.45F, -4.0F, -3.0F, 5, 6, 6, s), PartPose.ZERO);
        armLpauldron.addOrReplaceChild("fitting_l", CubeListBuilder.create()
                .texOffs(18, 12)
                .addBox(-6.0F, -2.0F, -1.0F, 4, 1, 2, s),
                PartPose.offsetAndRotation(0.5F, -3.0F, 0.0F, 0.0F, 0.0F, 0.08726646259971647F));

        PartDefinition armRAnchor = rightArm.addOrReplaceChild("arm_r_anchor", CubeListBuilder.create()
                .texOffs(0, 41)
                .mirror()
                .addBox(4.0F, -2.0F, -1.0F, 2, 2, 2, s), PartPose.offset(-4.0F, 2.0F, 0.0F));
        PartDefinition armRpauldron = armRAnchor.addOrReplaceChild("arm_r_pauldron", CubeListBuilder.create()
                .texOffs(0, 21)
                .addBox(0.45F, -4.0F, -3.0F, 5, 6, 6, s), PartPose.ZERO);
        armRpauldron.addOrReplaceChild("fitting_r", CubeListBuilder.create()
                .texOffs(18, 12)
                .addBox(1.5F, -2.0F, -1.0F, 4, 1, 2, s),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.08726646259971647F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
        this.head.visible = true;
        this.body.visible = true;
        this.leftArm.visible = true;
        this.rightArm.visible = true;

        this.hat.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
    }
}
