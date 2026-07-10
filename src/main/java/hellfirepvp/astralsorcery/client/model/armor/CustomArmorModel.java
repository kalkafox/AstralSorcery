/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomArmorModel
 * Created by HellFirePvP
 * Date: 17.02.2020 / 21:22
 */
//Again, another version, another ripoff of net.minecraft.client.renderer.entity.model.ArmorStandArmorModel
public class CustomArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

    public CustomArmorModel() {
        super(0F);
    }

    @Override
    public void setupAnim(T entity, float animationPosition, float animationSpeed, float ageInTicks, float netHeadYaw, float yHeadRot) {
        if (entity instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) entity;

            this.head.xRot = ((float)Math.PI / 180F) * armorStand.getHeadPose().getX();
            this.head.yRot = ((float)Math.PI / 180F) * armorStand.getHeadPose().getY();
            this.head.zRot = ((float)Math.PI / 180F) * armorStand.getHeadPose().getZ();
            this.head.setPos(0.0F, 1.0F, 0.0F);
            this.body.xRot = ((float)Math.PI / 180F) * armorStand.getBodyPose().getX();
            this.body.yRot = ((float)Math.PI / 180F) * armorStand.getBodyPose().getY();
            this.body.zRot = ((float)Math.PI / 180F) * armorStand.getBodyPose().getZ();
            this.leftArm.xRot = ((float)Math.PI / 180F) * armorStand.getLeftArmPose().getX();
            this.leftArm.yRot = ((float)Math.PI / 180F) * armorStand.getLeftArmPose().getY();
            this.leftArm.zRot = ((float)Math.PI / 180F) * armorStand.getLeftArmPose().getZ();
            this.rightArm.xRot = ((float)Math.PI / 180F) * armorStand.getRightArmPose().getX();
            this.rightArm.yRot = ((float)Math.PI / 180F) * armorStand.getRightArmPose().getY();
            this.rightArm.zRot = ((float)Math.PI / 180F) * armorStand.getRightArmPose().getZ();
            this.leftLeg.xRot = ((float)Math.PI / 180F) * armorStand.getLeftLegPose().getX();
            this.leftLeg.yRot = ((float)Math.PI / 180F) * armorStand.getLeftLegPose().getY();
            this.leftLeg.zRot = ((float)Math.PI / 180F) * armorStand.getLeftLegPose().getZ();
            this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
            this.rightLeg.xRot = ((float)Math.PI / 180F) * armorStand.getRightLegPose().getX();
            this.rightLeg.yRot = ((float)Math.PI / 180F) * armorStand.getRightLegPose().getY();
            this.rightLeg.zRot = ((float)Math.PI / 180F) * armorStand.getRightLegPose().getZ();
            this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
            this.hat.copyModelAngles(this.head);
        } else {
            super.setupAnim(entity, animationPosition, animationSpeed, ageInTicks, netHeadYaw, yHeadRot);
        }
    }

    protected void setRotateAngle(ModelPart modelPart, float x, float y, float z) {
        modelPart.xRot = x;
        modelPart.yRot = y;
        modelPart.zRot = z;
    }
}
