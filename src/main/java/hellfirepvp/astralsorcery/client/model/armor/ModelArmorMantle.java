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

    private final ModelPart bodyReplacement, lArm, rArm, headReplacement;

    private final ModelPart cowl;
    private final ModelPart mantle_l;
    private final ModelPart mantle_r;

    private final ModelPart bodyAnchor;
    private final ModelPart body;
    private final ModelPart plate;

    private final ModelPart armLAnchor;
    private final ModelPart armLpauldron;
    private final ModelPart fitting_l;

    private final ModelPart armRAnchor;
    private final ModelPart armRpauldron;
    private final ModelPart fitting_r;

    public ModelArmorMantle() {
        float s = 0.01F;
        this.textureWidth = 64;
        this.textureHeight = 128;

        this.cowl = new ModelPart(this, 0, 33);
        this.cowl.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cowl.addBox(-4.5F, -4.0F, -4.0F, 9, 5, 9, s);
        this.setRotateAngle(cowl, 0.2617993877991494F, 0.0F, 0.0F);
        this.mantle_l = new ModelPart(this, 0, 47);
        this.mantle_l.mirror = true;
        this.mantle_l.setRotationPoint(6.25F, 2.0F, 0.0F);
        this.mantle_l.addBox(-8.0F, -3.5F, 1.0F, 9, 21, 5, s);
        this.setRotateAngle(mantle_l, 0.08726646259971647F, 0.2617993877991494F, 0.0F);
        this.mantle_r = new ModelPart(this, 0, 47);
        this.mantle_r.setRotationPoint(-6.25F, 2.0F, 0.0F);
        this.mantle_r.addBox(-1.0F, -3.5F, 1.0F, 9, 21, 5, s);
        this.setRotateAngle(mantle_r, 0.08726646259971647F, -0.2617993877991494F, 0.0F);

        this.bodyAnchor = new ModelPart(this, 0, 41);
        this.bodyAnchor.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bodyAnchor.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, s);
        this.body = new ModelPart(this, 0, 0);
        this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.body.addBox(-4.5F, -0.5F, -3.0F, 9, 6, 6, s);
        this.plate = new ModelPart(this, 0, 12);
        this.plate.setRotationPoint(0.0F, 1.0F, -3.0F);
        this.plate.addBox(-3.5F, -0.5F, -1.0F, 7, 7, 2, s);
        this.setRotateAngle(plate, 0.08726646259971647F, 0.0F, 0.0F);

        this.armLAnchor = new ModelPart(this, 0, 41);
        this.armLAnchor.mirror = true;
        this.armLAnchor.setRotationPoint(4.0F, 2.0F, 0.0F);
        this.armLAnchor.addBox(-6F, -2.0F, -1.0F, 2, 2, 2, s);
        this.armLpauldron = new ModelPart(this, 0, 21);
        this.armLpauldron.mirror = true;
        this.armLpauldron.setRotationPoint(0.0F, 0.0F, -0.0F);
        this.armLpauldron.addBox(-5.45F, -4.0F, -3.0F, 5, 6, 6, s);
        this.fitting_l = new ModelPart(this, 18, 12);
        this.fitting_l.setRotationPoint(0.5F, -3.0F, 0.0F);
        this.fitting_l.addBox(-6.0F, -2.0F, -1.0F, 4, 1, 2, s);
        this.setRotateAngle(fitting_l, 0.0F, 0.0F, 0.08726646259971647F);

        this.armRAnchor = new ModelPart(this, 0, 41);
        this.armRAnchor.mirror = true;
        this.armRAnchor.setRotationPoint(-4.0F, 2.0F, 0.0F);
        this.armRAnchor.addBox(4F, -2.0F, -1.0F, 2, 2, 2, s);
        this.armRpauldron = new ModelPart(this, 0, 21);
        this.armRpauldron.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.armRpauldron.addBox(0.45F, -4.0F, -3.0F, 5, 6, 6, s);
        this.fitting_r = new ModelPart(this, 18, 12);
        this.fitting_r.setRotationPoint(0.0F, -3.0F, 0.0F);
        this.fitting_r.addBox(1.5F, -2.0F, -1.0F, 4, 1, 2, s);
        this.setRotateAngle(fitting_r, 0.0F, 0.0F, -0.08726646259971647F);

        this.bodyReplacement = new ModelPart(this);
        this.bodyReplacement.addChild(this.bodyAnchor);
        this.bodyAnchor.addChild(this.body);
        this.body.addChild(this.plate);
        this.body.addChild(this.mantle_l);
        this.body.addChild(this.mantle_r);

        this.headReplacement = new ModelPart(this);
        this.headReplacement.addChild(this.cowl);

        this.lArm = new ModelPart(this);
        this.lArm.addChild(this.armLAnchor);
        this.armLAnchor.addChild(this.armLpauldron);
        this.armLpauldron.addChild(this.fitting_l);

        this.rArm = new ModelPart(this);
        this.rArm.addChild(this.armRAnchor);
        this.armRAnchor.addChild(this.armRpauldron);
        this.armRpauldron.addChild(this.fitting_r);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        bodyAnchor.showModel = true;
        armRAnchor.showModel = true;
        armLAnchor.showModel = true;
        bipedHead.showModel = true;

        bipedHeadwear.showModel = false;
        bipedRightLeg.showModel = false;
        bipedLeftLeg.showModel = false;

        bipedBody = this.bodyReplacement;
        bipedRightArm = this.rArm;
        bipedLeftArm = this.lArm;
        bipedHead = this.headReplacement;

        super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
