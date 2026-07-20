/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.client.lib.ShadersAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import hellfirepvp.observerlib.common.util.RegistryLookup;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:26
 */
public class RenderingUtils {

    private static final RandomSource random = RandomSource.create();
    private static final ByteBufferBuilder textBuffer = new ByteBufferBuilder(256);
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
        ResourceLocation res = IClientFluidTypeExtensions.of(stack.getFluid()).getStillTexture(stack);
        if (res == null || MissingTextureAtlasSprite.getLocation().equals(res)) {
            return null;
        }
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(res);
    }

    @Nullable
    public static TextureAtlasSprite getParticleIcon(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ItemModelShaper imm = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
        BakedModel mdl = imm.getItemModel(stack);
        if (mdl.equals(imm.getModelManager().getMissingModel())) {
            return null;
        }
        return mdl.getParticleIcon(ModelData.EMPTY);
    }

    @Nullable
    public static TextureAtlasSprite getParticleIcon(BlockState state, @Nullable BlockPos positionHint) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        BlockPos pos = positionHint != null ? positionHint : BlockPos.ZERO;
        try {
            if (state.isAir()) {
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
        ParticleEngine mgr = Minecraft.getInstance().particleEngine;

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

                        mgr.add(new TerrainParticle(level,
                                pos.getX() + d7, pos.getY() + d8, pos.getZ() + d9,
                                d4 - 0.5D, d5 - 0.5D, d6 - 0.5D,
                                particleState, pos));
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
        Entity viewDistance = Minecraft.getInstance().getCameraEntity();
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

    // 1.21 port: the fixed-function pipeline is gone; pick the vanilla core shader matching the
    // vertex format so the immediate-mode draw helpers keep working.
    public static Supplier<ShaderInstance> shaderFor(VertexFormat format) {
        if (format == DefaultVertexFormat.POSITION) {
            return GameRenderer::getPositionShader;
        } else if (format == DefaultVertexFormat.POSITION_COLOR) {
            return GameRenderer::getPositionColorShader;
        } else if (format == DefaultVertexFormat.POSITION_TEX) {
            return GameRenderer::getPositionTexShader;
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR || format == DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL) {
            // Own shader: no alpha-0.1 discard, keeps soft texture falloff (see ShadersAS).
            return () -> ShadersAS.EFFECT_TEX_COLOR;
        } else if (format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
            return GameRenderer::getPositionColorTexLightmapShader;
        } else if (format == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP) {
            return GameRenderer::getPositionColorLightmapShader;
        } else if (format == DefaultVertexFormat.BLOCK) {
            return GameRenderer::getRendertypeTranslucentShader;
        } else if (format == DefaultVertexFormat.NEW_ENTITY) {
            return GameRenderer::getRendertypeEntityTranslucentShader;
        }
        return GameRenderer::getPositionColorShader;
    }

    public static void draw(VertexFormat.Mode mode, VertexFormat format, Consumer<BufferBuilder> fn) {
        draw(mode, format, bufferBuilder -> {
            fn.accept(bufferBuilder);
            return null;
        });
    }

    public static <R> R draw(VertexFormat.Mode mode, VertexFormat format, Function<BufferBuilder, R> fn) {
        RenderSystem.setShader(shaderFor(format));
        BufferBuilder buf = Tesselator.getInstance().begin(mode, format);
        R result = fn.apply(buf);
        end(buf);
        return result;
    }

    public static void draw(VertexFormat format, MeshData data) {
        RenderSystem.setShader(shaderFor(format));
        BufferUploader.drawWithShader(data);
    }

    public static void end(BufferBuilder buf) {
        end(buf, null);
    }

    public static void end(BufferBuilder buf, @Nullable RenderType type) {
        MeshData data = buf.build();
        if (data != null) {
            if (type != null) {
                type.draw(data);
            } else {
                BufferUploader.drawWithShader(data);
            }
        }
    }

    public static int renderInWorldText(FormattedText text, Color color, Vector3 at, PoseStack renderStack, float pTicks, boolean facePlayer) {
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        return renderInWorldText(text, color, 0.02F * (Minecraft.getInstance().options.guiScale().get() / scale), at, renderStack, pTicks, facePlayer);
    }

    public static int renderInWorldText(FormattedText text, Color color, float scale, Vector3 at, PoseStack renderStack, float pTicks, boolean facePlayer) {
        Font fr = Minecraft.getInstance().font;

        renderStack.pushPose();
        renderStack.translate(at.getX(), at.getY(), at.getZ());
        renderStack.scale(scale, -scale, scale);

        if (facePlayer) {
            Entity le = Minecraft.getInstance().getCameraEntity();
            if (le == null) {
                le = Minecraft.getInstance().player;
            }
            float iYaw = RenderingVectorUtils.interpolate(Mth.wrapDegrees(le.yRotO), Mth.wrapDegrees(le.getYRot()), pTicks);
            renderStack.mulPose(Axis.YP.rotationDegrees(-iYaw + 180F));
        }

        Matrix4f matr = renderStack.last().pose();
        int length = fr.width(text);
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(textBuffer);
        FormattedCharSequence processedText = Language.getInstance().getVisualOrder(text);
        int drawnLength = fr.drawInBatch(processedText, -(length / 2F), 0, color.getRGB(), false, matr, buffers, Font.DisplayMode.SEE_THROUGH, 0, LightmapUtil.getPackedFullbrightCoords());
        buffers.endBatch();

        renderStack.popPose();
        return drawnLength;
    }

    public static void renderItemAsEntity(ItemStack stack, PoseStack renderStack, MultiBufferSource buffers, double x, double y, double z, int combinedLight, float pTicks, int age) {
        ItemEntity ei = new ItemEntity(Minecraft.getInstance().level, x, y, z, stack);
        ReflectionHelper.setItemEntityAge(ei, age);
        ReflectionHelper.setItemEntityBobOffset(ei, 0F);
        ReflectionHelper.setSkipItemPhysicsRender(ei);
        Minecraft.getInstance().getEntityRenderDispatcher().render(ei, x, y, z, 0F, pTicks, renderStack, buffers, combinedLight);
    }

    public static void renderItemStackGUI(PoseStack renderStack, ItemStack stack, @Nullable String alternativeText) {
        renderStack.pushPose();
        renderStack.translate(0, 0, 100F);
        Font font = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (font == null) {
            font = Minecraft.getInstance().font;
        }
        renderTranslucentItemStackModelGUI(stack, renderStack, Color.WHITE, Blending.DEFAULT, 255);
        mcdefault_renderItemOverlayIntoGUI(font, renderStack, stack, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true), alternativeText);

        renderStack.popPose();
    }

    public static void renderTranslucentItemStack(ItemStack stack, PoseStack renderStack, MultiBufferSource buffers, float pTicks) {
        // 1.21 port: alpha raised from the 1.16-era 25 - the ghost now draws with standard
        // SRC_ALPHA blending instead of the old premultiplied PREALPHA, which reads much dimmer
        // at the same vertex alpha.
        renderTranslucentItemStack(stack, renderStack, buffers, pTicks, Color.WHITE, 96);
    }

    public static void renderTranslucentItemStack(ItemStack stack, PoseStack renderStack, MultiBufferSource buffers, float pTicks, Color overlayColor, int alpha) {
        renderStack.pushPose();

        // EntityItemRenderer entity bobbing
        float sinBobY = Mth.sin((ClientScheduler.getClientTick() + pTicks) / 10.0F) * 0.1F + 0.1F;
        renderStack.translate(0, sinBobY, 0);
        float ageRotate = ((ClientScheduler.getClientTick() + pTicks) / 20.0F);
        renderStack.mulPose(Axis.YP.rotation(ageRotate));

        renderTranslucentItemStackModelGround(stack, renderStack, buffers, overlayColor, alpha);

        renderStack.popPose();
    }

    // 1.21 port: renders into the pass's own MultiBufferSource. The old implementation drew
    // through the shared renderBuffers() source and force-flushed it mid-BER/entity-pass
    // (same batch-corruption class as the lens render crash); translucency now comes from the
    // translucent item render type + vertex alpha instead of global RenderSystem blend state.
    public static void renderTranslucentItemStackModelGround(ItemStack stack, PoseStack renderStack, MultiBufferSource buffers, Color overlayColor, int alpha) {
        BakedModel bakedModel = ClientHooks.handleCameraTransforms(renderStack, getModel(stack), ItemDisplayContext.GROUND, false);
        renderItemModelWithColor(stack, ItemDisplayContext.GROUND, bakedModel, renderStack, buffers,
                LightmapUtil.getPackedFullbrightCoords(), OverlayTexture.NO_OVERLAY, overlayColor, alpha);
    }

    public static void renderTranslucentItemStackModelGUI(ItemStack stack, PoseStack renderStack, Color overlayColor, Blending blendMode, int alpha) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);

        RenderSystem.enableBlend();
        blendMode.apply();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        renderStack.pushPose();
        renderStack.translate(8.0F, 8.0F, 0.0F);
        renderStack.scale(16.0F, -16.0F, 16.0F);

        BakedModel bakedModel = ClientHooks.handleCameraTransforms(renderStack, getModel(stack), ItemDisplayContext.GUI, false);
        boolean usesBlockLight = bakedModel.usesBlockLight();
        if (!usesBlockLight) {
            Lighting.setupForFlatItems();
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderItemModelWithColor(stack, ItemDisplayContext.GUI, bakedModel, renderStack, buffer,
                LightmapUtil.getPackedFullbrightCoords(), OverlayTexture.NO_OVERLAY, overlayColor, Mth.clamp(alpha, 0, 255));
        buffer.endBatch();

        if (!usesBlockLight) {
            Lighting.setupFor3DItems();
        }

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        renderStack.popPose();
    }

    //TODO wait for mojang to do their work and actually port this method so i don't have to do this myself
    @Deprecated
    public static void mcdefault_renderItemOverlayIntoGUI(Font fr, PoseStack renderStack, ItemStack stack, float pTicks, @Nullable String text) {
        if (stack.isEmpty()) {
            return;
        }
        renderStack.pushPose();
        renderStack.translate(0, 0, 100F);
        if (stack.getCount() > 1 || text != null) {
            FormattedText display = Component.literal(ObjectUtils.firstNonNull(text, String.valueOf(stack.getCount())));
            int length = fr.width(display);

            renderStack.pushPose();
            renderStack.translate(17 - length, 9, 0);
            RenderingDrawUtils.renderStringAt(display, renderStack, fr, 0xFFFFFFFF, true);
            renderStack.popPose();
        }

        if (stack.isBarVisible()) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();

            float barWidth = stack.getBarWidth();
            int color = stack.getBarColor();

            RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 2, 13, 0, 13, 2)
                        .color(0, 0, 0, 255)
                        .draw();
                RenderingGuiUtils.rect(buf, renderStack, 2, 13, 0, barWidth, 1)
                        .color(color >> 16 & 255, color >> 8 & 255, color & 255, 255)
                        .draw();
            });

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        float cooldownPercent = player == null ? 0F : player.getCooldowns().getCooldownPercent(stack.getItem(), pTicks);
        if (cooldownPercent > 0F) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 0, 16F * (1F - cooldownPercent), 0, 16, 16F * cooldownPercent)
                        .color(255, 255, 255, 127)
                        .draw();
            });

            RenderSystem.enableDepthTest();
        }
        renderStack.popPose();
    }

    private static BakedModel getModel(ItemStack stack) {
        return Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);
    }

    private static void renderItemModelWithColor(ItemStack stack, ItemDisplayContext displayContext, BakedModel model, PoseStack renderStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, Color c, int alpha) {
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(-0.5, -0.5, -0.5);

            if (model.isCustomRenderer()) {
                int[] colors = new int[] { c.getRed(), c.getGreen(), c.getBlue(), alpha };
                MultiBufferSource decoratedBuffer = type -> BufferDecoratorBuilder.withColor((r, g, b, a) -> colors).decorate(buffer.getBuffer(type));
                IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, displayContext, renderStack, decoratedBuffer, combinedLight, combinedOverlay);
            } else {
                // 1.21 port: layered rendering goes through NeoForge's getRenderPasses; the old
                // "always translucent" behavior is kept by forcing the fabulous item render type.
                RenderType rType = ItemBlockRenderTypes.getRenderType(stack, true);
                for (BakedModel passModel : model.getRenderPasses(stack, true)) {
                    VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(buffer, rType, true, stack.hasFoil());
                    renderColoredItemModel(stack, passModel, renderStack, vertexBuilder, combinedLight, combinedOverlay, c, alpha);
                }
            }

            renderStack.popPose();
        }
    }

    private static void renderColoredItemModel(ItemStack stack, BakedModel model, PoseStack renderStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Color color, int alpha) {
        Color alphaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        RandomSource renderRand = RandomSource.create();
        ModelData data = ModelData.EMPTY;
        for (Direction dir : Direction.values()) {
            renderRand.setSeed(42);
            renderColoredQuads(buffer, renderStack, model.getQuads(null, dir, renderRand, data, null), alphaColor, combinedLight, combinedOverlay, stack);
        }

        renderRand.setSeed(42);
        renderColoredQuads(buffer, renderStack, model.getQuads(null, null, renderRand, data, null), alphaColor, combinedLight, combinedOverlay, stack);
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

            vb.putBulkData(renderStack.last(), bakedquad, r, g, b, a, combinedLight, combinedOverlay, true);
        }
    }

    public static void renderSimpleBlockModel(BlockState state, PoseStack renderStack, VertexConsumer vb) {
        renderSimpleBlockModel(state, renderStack, vb, BlockPos.ZERO, null, false);
    }

    public static void renderSimpleBlockModel(BlockState state, PoseStack renderStack, VertexConsumer vb, BlockPos pos, @Nullable BlockEntity te, boolean checkRenderSide) {
        if (plainRenderWorld == null) {
            plainRenderWorld = new EmptyRenderWorld(() -> RegistryLookup.client().getValue(Registries.BIOME, Biomes.PLAINS));
        }

        RenderShape brt = state.getRenderShape();
        if (brt == RenderShape.INVISIBLE) {
            return;
        }
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        ModelData data = ModelData.EMPTY;
        if (te != null) {
            data = te.getModelData();
        }
        brd.renderBatched(state, pos, plainRenderWorld, renderStack, vb, checkRenderSide, random, data, null);
    }

    public static void renderSimpleBlockModelCurrentWorld(BlockState state, PoseStack renderStack, VertexConsumer buf, int combinedOverlayIn) {
        renderSimpleBlockModelCurrentWorld(state, renderStack, buf, BlockPos.ZERO, null, combinedOverlayIn, false);
    }

    public static void renderSimpleBlockModelCurrentWorld(BlockState state, PoseStack renderStack, VertexConsumer buf, BlockPos pos, @Nullable BlockEntity te, int combinedOverlayIn, boolean checkRenderSide) {
        RenderShape brt = state.getRenderShape();
        if (brt == RenderShape.INVISIBLE) {
            return;
        }
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        ModelData data = ModelData.EMPTY;
        if (te != null) {
            data = te.getModelData();
        }
        if (brt == RenderShape.MODEL) {
            BakedModel model = brd.getBlockModel(state);
            brd.getModelRenderer().tesselateBlock(Minecraft.getInstance().level, model, state, pos, renderStack, buf, checkRenderSide, random, state.getSeed(pos), combinedOverlayIn, data, null);
        }
    }
}
