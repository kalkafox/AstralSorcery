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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomModel
 * Created by HellFirePvP
 * Date: 21.09.2019 / 15:31
 */
public abstract class CustomModel extends Model {

    public CustomModel(Function<ResourceLocation, RenderType> renderTypeIn) {
        super(renderTypeIn);
    }

    public final RenderType getGeneralType() {
        return this.renderType(TextureAtlas.LOCATION_BLOCKS);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource buffer, int packedLightIn, int packedOverlayIn) {
        this.render(matrixStackIn, buffer.getBuffer(this.getGeneralType()), packedLightIn, packedOverlayIn, 1F, 1F, 1F, 1F);
    }

    // 1.21 port: Model.renderToBuffer takes a packed ARGB color now; keep the old float-color
    // entry point for the mod's render paths and bridge to the packed variant.
    public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {}

    protected static int packColor(float red, float green, float blue, float alpha) {
        return FastColor.ARGB32.colorFromFloat(alpha, red, green, blue);
    }

    protected void setRotateAngle(ModelPart modelPart, float x, float y, float z) {
        modelPart.xRot = x;
        modelPart.yRot = y;
        modelPart.zRot = z;
    }
}
