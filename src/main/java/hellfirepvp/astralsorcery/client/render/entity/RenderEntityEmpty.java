/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.client.registry.IRenderFactory;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderEntityEmpty
 * Created by HellFirePvP
 * Date: 17.08.2019 / 13:08
 */
public class RenderEntityEmpty extends EntityRenderer<Entity> {

    public RenderEntityEmpty(EntityRenderDispatcher mgr) {
        super(mgr);
    }

    @Override
    public void render(Entity entity, float entityYaw, float a, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {}

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS_TEXTURE;
    }

    public static class Factory implements IRenderFactory<Entity> {

        @Override
        public EntityRenderer<? super Entity> createRenderFor(EntityRenderDispatcher manager) {
            return new RenderEntityEmpty(manager);
        }
    }

}
