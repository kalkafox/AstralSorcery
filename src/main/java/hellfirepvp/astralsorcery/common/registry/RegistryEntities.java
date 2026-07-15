/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityEmpty;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityGrapplingHook;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityItemHighlighted;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntitySpectralTool;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.entity.EntityIlluminationSpark;
import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.entity.item.EntityCrystal;
import hellfirepvp.astralsorcery.common.entity.item.EntityItemExplosionResistant;
import hellfirepvp.astralsorcery.common.entity.item.EntityItemHighlighted;
import hellfirepvp.astralsorcery.common.entity.item.EntityStarmetal;
import hellfirepvp.astralsorcery.common.entity.technical.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.entity.technical.EntityObservatoryHelper;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import static hellfirepvp.astralsorcery.common.lib.EntityTypesAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryEntities
 * Created by HellFirePvP
 * Date: 17.08.2019 / 08:47
 */
public class RegistryEntities {

    private RegistryEntities() {}

    public static void init() {
        NOCTURNAL_SPARK = register("nocturnal_spark",
                EntityType.Builder.of(EntityNocturnalSpark.factory(), MobCategory.MISC)
                        .noSummon()
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32)
                        .sized(0.1F, 0.1F));
        ILLUMINATION_SPARK = register("illumination_spark",
                EntityType.Builder.of(EntityIlluminationSpark.factory(), MobCategory.MISC)
                        .noSummon()
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32)
                        .sized(0.1F, 0.1F));
        FLARE = register("flare",
                EntityType.Builder.of(EntityFlare.factory(), MobCategory.MISC)
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(64)
                        .sized(0.4F, 0.4F));
        SPECTRAL_TOOL = register("spectral_tool",
                EntityType.Builder.of(EntitySpectralTool.factory(), MobCategory.MISC)
                        .noSummon()
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32)
                        .sized(0.6F, 0.8F));

        ITEM_HIGHLIGHT = register("item_highlighted",
                EntityType.Builder.of(EntityItemHighlighted.factoryHighlighted(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .sized(0.25F, 0.25F));
        ITEM_EXPLOSION_RESISTANT = register("item_explosion_resistant",
                EntityType.Builder.of(EntityItemExplosionResistant.factoryExplosionResistant(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .sized(0.25F, 0.25F));
        ITEM_CRYSTAL = register("item_crystal",
                EntityType.Builder.of(EntityCrystal.factoryCrystal(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .sized(0.5F, 0.5F));
        ITEM_STARMETAL_INGOT = register("item_starmetal",
                EntityType.Builder.of(EntityStarmetal.factoryStarmetalIngot(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .sized(0.5F, 0.5F));
        OBSERVATORY_HELPER = register("observatory_helper",
                EntityType.Builder.of(EntityObservatoryHelper.factory(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(64)
                        .sized(0, 0));
        GRAPPLING_HOOK = register("grappling_hook",
                EntityType.Builder.of(EntityGrapplingHook.factory(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(64)
                        .sized(0.1F, 0.1F));
    }

    public static void initAttributes(EntityAttributeCreationEvent event) {
        event.put(FLARE, EntityFlare.createAttributes().build());
        event.put(SPECTRAL_TOOL, EntitySpectralTool.createAttributes().build());
    }

    @OnlyIn(Dist.CLIENT)
    public static void initClient(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NOCTURNAL_SPARK, RenderEntityEmpty::new);
        event.registerEntityRenderer(ILLUMINATION_SPARK, RenderEntityEmpty::new);
        event.registerEntityRenderer(FLARE, RenderEntityEmpty::new);
        event.registerEntityRenderer(SPECTRAL_TOOL, RenderEntitySpectralTool::new);

        event.registerEntityRenderer(ITEM_HIGHLIGHT, RenderEntityItemHighlighted::new);
        event.registerEntityRenderer(ITEM_EXPLOSION_RESISTANT, RenderEntityItemHighlighted::new);
        event.registerEntityRenderer(ITEM_CRYSTAL, RenderEntityItemHighlighted::new);
        event.registerEntityRenderer(ITEM_STARMETAL_INGOT, ItemEntityRenderer::new);

        event.registerEntityRenderer(OBSERVATORY_HELPER, RenderEntityEmpty::new);
        event.registerEntityRenderer(GRAPPLING_HOOK, RenderEntityGrapplingHook::new);
    }

    private static <E extends Entity> EntityType<E> register(String name, EntityType.Builder<E> typeBuilder) {
        EntityType<E> type = typeBuilder.build(AstralSorcery.key(name).toString());
        return AstralRegistries.register(AstralRegistries.ENTITY_TYPES, AstralSorcery.key(name), type);
    }
}
