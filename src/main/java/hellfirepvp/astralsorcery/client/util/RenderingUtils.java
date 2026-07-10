/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import hellfirepvp.observerlib.client.util.RenderTypeDecorator;
import hellfirepvp.observerlib.common.util.RegistryLookup;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.*;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.client.ForgeHooksClient;
import net.neoforged.neoforge.client.model.data.EmptyModelData;
import net.neoforged.neoforge.client.model.data.IModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.ObjectUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:26
 */
public class RenderingUtils {

    private static final Random random = new Random();
    private static BlockAndTintGetter plainRenderWorld = null;

    public static long getPositionSeed(BlockPos pos) {
        long seed = 1553015L;
        seed ^= pos.getX();
        seed ^= pos.getY();
        seed ^= pos.getZ();
        return seed;
    }

    @Nullable
    public static TextureAtlasSprite getParticleIcon(FluidStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation res = stack.getType().getAttributes().getStillTexture(stack);
        if (MissingTextureAtlasSprite.getLocation().equals(res)) {
            return null;
        }
        return Minecraft.getInstance().getModelManager().getAtlasTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE).getSprite(res);
    }

    @Nullable
    public static TextureAtlasSprite getParticleIcon(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ItemModelShaper imm = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
        BakedModel mdl = imm.getIndex(stack);
        if (mdl.equals(imm.getModelManager().getMissingModel())) {
            return null;
        }
        return mdl.getParticleIcon(EmptyModelData.INSTANCE);
    }

    @Nullable
    public static TextureAtlasSprite getParticleIcon(BlockState state, @Nullable BlockPos positionHint) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        BlockPos pos = positionHint != null ? positionHint : BlockPos.ZERO;
        try {
            if (state.isAir(level, pos)) {
                return null;
            }
        } catch (Exception exc) {
            return null;
        }
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(state, level, pos);
    }

    //Straight up ripped off of MC code.
    public static void playBlockBreakParticles(BlockPos pos, @Nullable BlockState actualState, BlockState particleState) {
        ClientLevel level = Minecraft.getInstance().level;
        ParticleEngine mgr = Minecraft.getInstance().particles;

        VoxelShape voxelshape;
        try {
            voxelshape = actualState == null ? Shapes.block() : actualState.getShape(level, pos);
        } catch (Exception exc) {
            voxelshape = Shapes.block();
        }
        voxelshape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double xDist = Math.min(1, maxX - minX);
            double yDist = Math.min(1, maxY - minY);
            double zDist = Math.min(1, maxZ - minZ);
            double i = Math.max(2, Mth.ceil(xDist / 0.25D));
            double j = Math.max(2, Mth.ceil(yDist / 0.25D));
            double k = Math.max(2, Mth.ceil(zDist / 0.25D));

            for (int xx = 0; xx < i; ++xx) {
                for (int yy = 0; yy < j; ++yy) {
                    for (int zz = 0; zz < k; ++zz) {

                        double d4 = (xx + 0.5D) / i;
                        double d5 = (yy + 0.5D) / j;
                        double d6 = (zz + 0.5D) / k;
                        double d7 = d4 * xDist + minX;
                        double d8 = d5 * yDist + minY;
                        double d9 = d6 * zDist + minZ;

                        TerrainParticle p = (new TerrainParticle(level,
                                pos.getX() + d7, pos.getY() + d8, pos.getZ() + d9,
                                d4 - 0.5D, d5 - 0.5D, d6 - 0.5D,
                                particleState));
                        p.init();
                        p.getLightColor(pos);
                        mgr.addEffect(p);
                    }
                }
            }

        });
    }

    public static Color clampToColor(int rgb) {
        return clampToColorWithMultiplier(rgb, 1F);
    }

    public static Color clampToColorWithMultiplier(int rgb, float mul) {
        int r = ((rgb >> 16) & 0xFF);
        int g = ((rgb >> 8)  & 0xFF);
        int b = ((rgb >> 0)  & 0xFF);
        return new Color(
                Mth.clamp((int) (((float) r) * mul), 0, 255),
                Mth.clamp((int) (((float) g) * mul), 0, 255),
                Mth.clamp((int) (((float) b) * mul), 0, 255));
    }

    public static Color clampToColor(int r, int g, int b) {
        return new Color(
                Mth.clamp((int) (((float) r)), 0, 255),
                Mth.clamp((int) (((float) g)), 0, 255),
                Mth.clamp((int) (((float) b)), 0, 255));
    }

    public static boolean canEffectExist(EntityComplexFX fx) {
        Entity viewDistance = Minecraft.getInstance().getRenderViewEntity();
        if (viewDistance == null) {
            viewDistance = Minecraft.getInstance().player;
        }
        if (viewDistance == null) {
            return false;
        }
        return fx.getPosition().distanceSquared(viewDistance) <= RenderingConfig.CONFIG.getMaxEffectRenderDistanceSq();
    }

    public static void translate(PoseStack renderStack, float x, float y, float z, Consumer<PoseStack> fn) {
        renderStack.pushPose();
        renderStack.translate(x, y, z);
        fn.accept(renderStack);
        renderStack.popPose();
    }

    public static void draw(int mode, VertexFormat format, Consumer<BufferBuilder> fn) {
        draw(mode, format, bufferBuilder -> {
            fn.accept(bufferBuilder);
            return null;
        });
    }

    public static <R> R draw(int mode, VertexFormat format, Function<BufferBuilder, R> fn) {
        BufferBuilder buf = Tesselator.getInstance().getBuffer();
        buf.begin(mode, format);
        R result = fn.apply(buf);
        end(buf);
        return result;
    }

    public static void end(BufferBuilder buf) {
        end(buf, null);
    }

    public static void end(BufferBuilder buf, @Nullable RenderType type) {
        if (buf.building()) {
            if (type != null) {
                type.finish(buf, 0, 0, 0);
            } else {
                buf.end();
                BufferUploader.draw(buf);
            }
        }
    }

    public static void refreshDrawing(VertexConsumer vb, RenderType type) {
        if (vb instanceof BufferBuilder) {
            type.finish((BufferBuilder) vb, 0, 0, 0);
            ((BufferBuilder) vb).begin(type.mode(), type.format());
        }
    }

    public static int renderInWorldText(FormattedText text, Color color, Vector3 at, PoseStack renderStack, float pTicks, boolean facePlayer) {
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        return renderInWorldText(text, color, 0.02F * (Minecraft.getInstance().options.guiScale / scale), at, renderStack, pTicks, facePlayer);
    }

    public static int renderInWorldText(FormattedText text, Color color, float scale, Vector3 at, PoseStack renderStack, float pTicks, boolean facePlayer) {
        Font fr = Minecraft.getInstance().font;

        renderStack.pushPose();
        renderStack.translate(at.getX(), at.getY(), at.getZ());
        renderStack.scale(scale, -scale, scale);

        if (facePlayer) {
            Entity le = Minecraft.getInstance().renderViewEntity;
            if (le == null) {
                le = Minecraft.getInstance().player;
            }
            float iYaw = RenderingVectorUtils.interpolate(Mth.wrapDegrees(le.yRotO), Mth.wrapDegrees(le.getYRot()), pTicks);
            renderStack.mirror(Axis.YP.rotationDegrees(-iYaw + 180F));
        }

        Matrix4f matr = renderStack.last().pose();
        int length = fr.getStringPropertyWidth(text);
        MultiBufferSource.Impl buffers = MultiBufferSource.getImpl(Tesselator.getInstance().getBuffer());
        FormattedCharSequence processedText = Language.getInstance().getVisualOrder(text);
        int drawnLength = fr.func_238416_a_(processedText, -(length / 2F), 0, color.getRGB(), false, matr, buffers, true, 0, LightmapUtil.getPackedFullbrightCoords());
        buffers.finish();

        renderStack.popPose();
        return drawnLength;
    }

    public static void renderItemAsEntity(ItemStack stack, PoseStack renderStack, MultiBufferSource buffers, double x, double y, double z, int combinedLight, float pTicks, int age) {
        ItemEntity ei = new ItemEntity(Minecraft.getInstance().level, x, y, z, stack);
        ei.age = age;
        ei.bobOffs = 0;
        ReflectionHelper.setSkipItemPhysicsRender(ei);
        Minecraft.getInstance().getRenderManager().renderEntityStatic(ei, x, y, z, 0F, pTicks, renderStack, buffers, combinedLight);
    }

    public static void renderItemStackGUI(PoseStack renderStack, ItemStack stack, @Nullable String alternativeText) {
        renderStack.pushPose();
        renderStack.translate(0, 0, 100F);
        Font font = stack.getItem().getFont(stack);
        if (font == null) {
            font = Minecraft.getInstance().font;
        }
        renderTranslucentItemStackModelGUI(stack, renderStack, Color.WHITE, Blending.DEFAULT, 255);
        mcdefault_renderItemOverlayIntoGUI(font, renderStack, stack, Minecraft.getInstance().getFrameTime(), alternativeText);

        renderStack.popPose();
    }

    public static void renderTranslucentItemStack(ItemStack stack, PoseStack renderStack, float pTicks) {
        renderTranslucentItemStack(stack, renderStack, pTicks, Color.WHITE, 25);
    }

    public static void renderTranslucentItemStack(ItemStack stack, PoseStack renderStack, float pTicks, Color overlayColor, int alpha) {
        renderStack.pushPose();

        // EntityItemRenderer entity bobbing
        float sinBobY = Mth.sin((ClientScheduler.getClientTick() + pTicks) / 10.0F) * 0.1F + 0.1F;
        renderStack.translate(0, sinBobY, 0);
        float ageRotate = ((ClientScheduler.getClientTick() + pTicks) / 20.0F);
        renderStack.mirror(Axis.YP.rotation(ageRotate));

        renderTranslucentItemStackModelGround(stack, renderStack, overlayColor, Blending.PREALPHA, alpha);

        renderStack.popPose();
    }

    public static void renderTranslucentItemStackModelGround(ItemStack stack, PoseStack renderStack, Color overlayColor, Blending blendMode, int alpha) {
        BakedModel bakedModel = getIndex(stack);
        ForgeHooksClient.handleCameraTransforms(renderStack, bakedModel, ItemTransforms.TransformType.GROUND, false);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        textureManager.bindTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        MultiBufferSource.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderItemModelWithColor(stack, ItemTransforms.TransformType.GROUND, bakedModel, renderStack, (renderType) -> {
            RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(renderType, () -> {
                RenderSystem.enableBlend();
                blendMode.apply();
            }, () -> {
                Blending.DEFAULT.apply();
                RenderSystem.disableBlend();
            });
            return buffer.getBuffer(decorated);
        }, LightmapUtil.getPackedFullbrightCoords(), OverlayTexture.NO_OVERLAY, overlayColor, alpha);
        buffer.finish();
    }

    public static void renderTranslucentItemStackModelGUI(ItemStack stack, PoseStack renderStack, Color overlayColor, Blending blendMode, int alpha) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        blendMode.apply();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        renderStack.pushPose();
        renderStack.translate(8.0F, 8.0F, 0.0F);
        renderStack.scale(16.0F, -16.0F, 16.0F);

        BakedModel bakedModel = ForgeHooksClient.handleCameraTransforms(renderStack, getIndex(stack), ItemTransforms.TransformType.GUI, false);
        boolean usesBlockLight = bakedModel.usesBlockLight();
        if (!usesBlockLight) {
            Lighting.setupGuiFlatDiffuseLighting();
        }

        MultiBufferSource.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderItemModelWithColor(stack, ItemTransforms.TransformType.GUI, bakedModel, renderStack, buffer,
                LightmapUtil.getPackedFullbrightCoords(), OverlayTexture.NO_OVERLAY, overlayColor, Mth.clamp(alpha, 0, 255));
        buffer.finish();

        if (!usesBlockLight) {
            Lighting.setupGui3DDiffuseLighting();
        }

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.enableDepthTest();
        renderStack.popPose();
    }

    //TODO wait for mojang to do their work and actually port this method so i don't have to do this myself
    @Deprecated
    public static void mcdefault_renderItemOverlayIntoGUI(Font fr, PoseStack renderStack, ItemStack stack, float pTicks, @Nullable String text) {
        if (stack.isEmpty()) {
            return;
        }
        //TODO ugh.
        RenderSystem.disableLighting();

        renderStack.pushPose();
        renderStack.translate(0, 0, 100F);
        if (stack.getCount() > 1 || text != null) {
            FormattedText display = Component.literal(ObjectUtils.firstNonNull(text, String.valueOf(stack.getCount())));
            int length = fr.getStringPropertyWidth(display);

            renderStack.pushPose();
            renderStack.translate(17 - length, 9, 0);
            RenderingDrawUtils.renderStringAt(display, renderStack, fr, 0xFFFFFFFF, true);
            renderStack.popPose();
        }

        if (stack.getItem().showDurabilityBar(stack)) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();

            float health = (float) stack.getItem().getDurabilityForDisplay(stack);
            float durabilityPercent = 13F - health * 13F;
            int color = stack.getItem().getRGBDurabilityForDisplay(stack);

            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 2, 13, 0, 13, 2)
                        .color(0, 0, 0, 255)
                        .draw();
                RenderingGuiUtils.rect(buf, renderStack, 2, 13, 0, durabilityPercent, 1)
                        .color(color >> 16 & 255, color >> 8 & 255, color & 255, 255)
                        .draw();
            });

            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        float cooldownPercent = player == null ? 0F : player.getCooldowns().getCooldown(stack.getItem(), pTicks);
        if (cooldownPercent > 0F) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 0, 16F * (1F - cooldownPercent), 0, 16, 16F * cooldownPercent)
                        .color(255, 255, 255, 127)
                        .draw();
            });

            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
        renderStack.popPose();
    }

    private static BakedModel getIndex(ItemStack stack) {
        return Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack, Minecraft.getInstance().level, Minecraft.getInstance().player);
    }

    private static void renderItemModelWithColor(ItemStack stack, ItemTransforms.TransformType transformType, BakedModel model, PoseStack renderStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, Color c, int alpha) {
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(-0.5, -0.5, -0.5);

            boolean renderThirdPersonView = transformType == ItemTransforms.TransformType.GUI ||
                    transformType == ItemTransforms.TransformType.GROUND ||
                    transformType == ItemTransforms.TransformType.FIXED;

            if (model.isCustomRenderer() || (stack.getItem() == Items.TRIDENT && !renderThirdPersonView)) {
                int[] colors = new int[] { c.getRed(), c.getGreen(), c.getBlue(), alpha };
                MultiBufferSource decoratedBuffer = type -> BufferDecoratorBuilder.withColor((r, g, b, a) -> colors).decorate(buffer.getBuffer(type));
                stack.getItem().getItemStackTileEntityRenderer().renderByItem(stack, transformType, renderStack, decoratedBuffer, combinedLight, combinedOverlay);
            } else if (model.isLayered()) {
                for (Pair<BakedModel, RenderType> layerModel : model.getLayerModels(stack, true)) {
                    BakedModel layeringState = layerModel.getFirst();
                    RenderType rType = layerModel.getSecond();
                    ForgeHooksClient.setRenderLayer(rType);
                    try {
                        VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(buffer, rType, true, stack.isFoil());
                        renderColoredItemModel(stack, layeringState, renderStack, vertexBuilder,combinedLight, combinedOverlay, c, alpha);
                    } finally {
                        ForgeHooksClient.setRenderLayer(null);
                    }
                }
            } else {
                //Always get translucent renderType
                RenderType rType = ItemBlockRenderTypes.func_239219_a_(stack, true);
                VertexConsumer vertexBuilder;

                //Wth are you doing here mojang. Taken from ItemEntityRenderer#renderItem
                if (stack.getItem() instanceof CompassItem && stack.isFoil()) {
                    renderStack.pushPose();
                    PoseStack.Entry topEntry = renderStack.last();

                    if (transformType == ItemTransforms.TransformType.GUI) {
                        topEntry.pose().mul(0.5F);
                    } else if (transformType.firstPerson()) {
                        topEntry.pose().mul(0.75F);
                    }
                    vertexBuilder = ItemRenderer.renderAndDecorateItem(buffer, rType, topEntry);
                    renderStack.popPose();
                } else {
                    vertexBuilder = ItemRenderer.getFoilBufferDirect(buffer, rType, true, stack.isFoil());
                }

                renderColoredItemModel(stack, model, renderStack, vertexBuilder, combinedLight, combinedOverlay, c, alpha);
            }

            renderStack.popPose();
        }
    }

    private static void renderColoredItemModel(ItemStack stack, BakedModel model, PoseStack renderStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Color color, int alpha) {
        Color alphaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        Random renderRand = new Random();
        IModelData data = EmptyModelData.INSTANCE;
        for (Direction dir : Direction.values()) {
            renderRand.initNoise(42);
            renderColoredQuads(buffer, renderStack, model.getQuads(null, dir, renderRand, data), alphaColor, combinedLight, combinedOverlay, stack);
        }

        renderRand.initNoise(42);
        renderColoredQuads(buffer, renderStack, model.getQuads(null, null, renderRand, data), alphaColor, combinedLight, combinedOverlay, stack);
    }

    private static void renderColoredQuads(VertexConsumer vb, PoseStack renderStack, List<BakedQuad> polygons, Color color, int combinedLight, int combinedOverlay, ItemStack stack) {
        boolean useOverlayColors = (color.getRGB() & 0xFFFFFF) == 0xFFFFFF && !stack.isEmpty();
        int i = 0;

        ItemColors itemColors = Minecraft.getInstance().getItemColors();
        for (int j = polygons.size(); i < j; ++i) {
            BakedQuad bakedquad = polygons.get(i);
            int col = color.getRGB();
            if (useOverlayColors && bakedquad.isTinted()) {
                col = itemColors.getColor(stack, bakedquad.getTintIndex());
            }

            float r = (col >> 16 & 255) / 255F;
            float g = (col >> 8 & 255) / 255F;
            float b = (col & 255) / 255F;
            float a = color.getAlpha() / 255F;

            vb.addVertexData(renderStack.last(), bakedquad, r, g, b, a, combinedLight, combinedOverlay, true);
        }
    }

    public static void renderSimpleBlockModel(BlockState state, PoseStack renderStack, VertexConsumer vb) {
        renderSimpleBlockModel(state, renderStack, vb, BlockPos.ZERO, null, false);
    }

    public static void renderSimpleBlockModel(BlockState state, PoseStack renderStack, VertexConsumer vb, BlockPos pos, @Nullable BlockEntity te, boolean checkRenderSide) {
        if (plainRenderWorld == null) {
            plainRenderWorld = new EmptyRenderWorld(() -> RegistryLookup.client().getValue(Registry.BIOME_REGISTRY, Biomes.PLAINS));
        }

        RenderShape brt = state.getRenderType();
        if (brt == RenderShape.INVISIBLE) {
            return;
        }
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        IModelData data = EmptyModelData.INSTANCE;
        if (te != null) {
            data = te.getModelData();
        }
        brd.renderModel(state, pos, plainRenderWorld, renderStack, vb, checkRenderSide, random, data);
    }

    public static void renderSimpleBlockModelCurrentWorld(BlockState state, PoseStack renderStack, VertexConsumer buf, int combinedOverlayIn) {
        renderSimpleBlockModelCurrentWorld(state, renderStack, buf, BlockPos.ZERO, null, combinedOverlayIn, false);
    }

    public static void renderSimpleBlockModelCurrentWorld(BlockState state, PoseStack renderStack, VertexConsumer buf, BlockPos pos, @Nullable BlockEntity te, int combinedOverlayIn, boolean checkRenderSide) {
        RenderShape brt = state.getRenderType();
        if (brt == RenderShape.INVISIBLE) {
            return;
        }
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        IModelData data = EmptyModelData.INSTANCE;
        if (te != null) {
            data = te.getModelData();
        }
        if (brt == RenderShape.MODEL) {
            BakedModel model = brd.onResourceManagerReload(state);
            brd.getModelRenderer().renderModel(Minecraft.getInstance().level, model, state, pos, renderStack, buf, checkRenderSide, random, state.getSeed(pos), combinedOverlayIn, data);
        }
    }
}
