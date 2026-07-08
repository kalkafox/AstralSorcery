/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.registry.RegistryRenderTypes;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StarryLayerRenderer
 * Created by HellFirePvP
 * Date: 06.01.2021 / 16:00
 */
public class StarryLayerRenderer<E extends LivingEntity, M extends HumanoidModel<E>> extends HumanoidArmorLayer<E, M, HumanoidModel<E>> {

    private static final List<CacheReference<RenderType>> RENDER_TYPES = IntStream.range(0, 2)
            .mapToObj((i) -> new CacheReference<>(() -> RegistryRenderTypes.createDepthProjectionType(i)))
            .collect(Collectors.toList());
    private static final HumanoidModel MODEL_HEAD = new PlayerModel<>(-0.5F, false);
    private static final HumanoidModel MODEL_ARMOR = new PlayerModel<>(0F, false);
    private static final HumanoidModel MODEL_ARMOR_SMALL = new PlayerModel<>(0F, true);

    private static BiPredicate<Player, EquipmentSlot> renderTest = (p, type) -> false;

    private final boolean slimRender;

    public StarryLayerRenderer(RenderLayerParent<E, M> entityRendererIn, boolean slimRender) {
        super(entityRendererIn, MODEL_ARMOR, MODEL_ARMOR);
        this.slimRender = slimRender;
    }

    public static void addRender(BiPredicate<Player, EquipmentSlot> render) {
        renderTest = renderTest.or(render);
    }

    @Override
    public void render(PoseStack renderStack, MultiBufferSource buffer, int light, E entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Player)) {
            return;
        }

        for (EquipmentSlot slotType : EquipmentSlotType.values()) {
            if (slotType.getSlotType() == EquipmentSlotType.Group.ARMOR) {
                if (renderTest.test((Player) entity, slotType)) {
                    HumanoidModel<E> model = slotType == EquipmentSlotType.HEAD ? MODEL_HEAD : this.slimRender ? MODEL_ARMOR_SMALL : MODEL_ARMOR;
                    this.renderArmorPart(renderStack, buffer, slotType, light, model);
                }
            }
        }
    }

    private void renderArmorPart(PoseStack renderStack, MultiBufferSource buffer, EquipmentSlot slotType, int light, HumanoidModel<E> model) {
        this.getEntityModel().setModelAttributes(model);
        this.setModelSlotVisible(model, slotType);
        for (CacheReference<RenderType> renderType : RENDER_TYPES) {
            model.render(renderStack, buffer.getBuffer(renderType.get()), light, OverlayTexture.NO_OVERLAY, 0.4F, 0.4F, 1F, 0.1F);
        }
    }
}
