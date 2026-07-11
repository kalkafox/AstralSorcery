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
 * Class: ModelTelescope
 * AS_telescope - wiiv
 * Created using Tabula 7.0.0
 */
public class ModelTelescope extends CustomModel {

    private final ModelPart mountpiece;
    private final ModelPart opticalTube;

    public ModelTelescope() {
        super((resKey) -> RenderTypesAS.MODEL_TELESCOPE);
        ModelPart root = createLayer().bakeRoot();
        this.mountpiece = root.getChild("mountpiece");
        this.opticalTube = root.getChild("optical_tube");
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition mountpiece = root.addOrReplaceChild("mountpiece", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, 4.0F, -2.0F, 4, 4, 4), PartPose.offset(0.0F, -2.0F, 0.0F));
        mountpiece.addOrReplaceChild("leg", CubeListBuilder.create()
                .texOffs(56, 0)
                .addBox(-1.0F, -10.0F, -1.0F, 2, 36, 2), PartPose.offset(0.0F, 8.0F, 0.0F));
        mountpiece.addOrReplaceChild("mountpiece_1", CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-2.0F, 20.0F, -1.0F, 4, 6, 4), PartPose.offset(0.0F, 0.0F, -1.0F));

        PartDefinition opticalTube = root.addOrReplaceChild("optical_tube", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(0.0F, -2.0F, -14.0F, 4, 4, 24),
                PartPose.offsetAndRotation(1.0F, -3.0F, 0.0F, -0.7853981633974483F, 0.0F, 0.0F));
        opticalTube.addOrReplaceChild("extension", CubeListBuilder.create()
                .texOffs(0, 12)
                .addBox(-2.0F, -6.0F, 6.0F, 2, 6, 2), PartPose.ZERO);
        opticalTube.addOrReplaceChild("aperture_1", CubeListBuilder.create()
                .texOffs(28, 28)
                .addBox(-1.0F, -3.0F, -6.0F, 6, 6, 2), PartPose.ZERO);
        opticalTube.addOrReplaceChild("aperture", CubeListBuilder.create()
                .texOffs(0, 28)
                .addBox(-1.0F, -3.0F, -16.0F, 6, 6, 8), PartPose.ZERO);
        opticalTube.addOrReplaceChild("detail", CubeListBuilder.create()
                .texOffs(0, 8)
                .addBox(1.0F, -1.0F, 10.0F, 2, 2, 2), PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        int color = packColor(red, green, blue, alpha);
        this.mountpiece.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        this.opticalTube.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
    }
}
