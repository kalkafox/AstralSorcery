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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.client.registry.RenderingRegistry;

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
                EntityType.Builder.create(EntityNocturnalSpark.factory(), MobCategory.MISC)
                        .noSummon()
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityNocturnalSpark(level))
                        .size(0.1F, 0.1F));
        ILLUMINATION_SPARK = register("illumination_spark",
                EntityType.Builder.create(EntityIlluminationSpark.factory(), MobCategory.MISC)
                        .noSummon()
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityIlluminationSpark(level))
                        .size(0.1F, 0.1F));
        FLARE = register("flare",
                EntityType.Builder.create(EntityFlare.factory(), MobCategory.MISC)
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(64)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityFlare(level))
                        .size(0.4F, 0.4F));
        SPECTRAL_TOOL = register("spectral_tool",
                EntityType.Builder.create(EntitySpectralTool.factory(), MobCategory.MISC)
                        .noSummon()
                        .fireImmune()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32)
                        .setCustomClientFactory((spawnEntity, level) -> new EntitySpectralTool(level))
                        .size(0.6F, 0.8F));

        ITEM_HIGHLIGHT = register("item_highlighted",
                EntityType.Builder.create(EntityItemHighlighted.factoryHighlighted(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityItemHighlighted(ITEM_HIGHLIGHT, level))
                        .size(0.25F, 0.25F));
        ITEM_EXPLOSION_RESISTANT = register("item_explosion_resistant",
                EntityType.Builder.create(EntityItemExplosionResistant.factoryExplosionResistant(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityItemExplosionResistant(ITEM_EXPLOSION_RESISTANT, level))
                        .size(0.25F, 0.25F));
        ITEM_CRYSTAL = register("item_crystal",
                EntityType.Builder.create(EntityCrystal.factoryCrystal(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityCrystal(ITEM_CRYSTAL, level))
                        .size(0.5F, 0.5F));
        ITEM_STARMETAL_INGOT = register("item_starmetal",
                EntityType.Builder.create(EntityStarmetal.factoryStarmetalIngot(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(16)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityStarmetal(ITEM_STARMETAL_INGOT, level))
                        .size(0.5F, 0.5F));
        OBSERVATORY_HELPER = register("observatory_helper",
                EntityType.Builder.create(EntityObservatoryHelper.factory(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(64)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityObservatoryHelper(level))
                        .size(0, 0));
        GRAPPLING_HOOK = register("grappling_hook",
                EntityType.Builder.create(EntityGrapplingHook.factory(), MobCategory.MISC)
                        .noSummon()
                        .setUpdateInterval(1)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(64)
                        .setCustomClientFactory((spawnEntity, level) -> new EntityGrapplingHook(level))
                        .size(0.1F, 0.1F));
    }

    public static void initAttributes(EntityAttributeCreationEvent event) {
        event.put(FLARE, EntityFlare.createAttributes().create());
        event.put(SPECTRAL_TOOL, EntitySpectralTool.createAttributes().create());
    }

    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        RenderingRegistry.registerEntityRenderingHandler(NOCTURNAL_SPARK, new RenderEntityEmpty.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ILLUMINATION_SPARK, new RenderEntityEmpty.Factory());
        RenderingRegistry.registerEntityRenderingHandler(FLARE, new RenderEntityEmpty.Factory());
        RenderingRegistry.registerEntityRenderingHandler(SPECTRAL_TOOL, new RenderEntitySpectralTool.Factory());

        RenderingRegistry.registerEntityRenderingHandler(ITEM_HIGHLIGHT, new RenderEntityItemHighlighted.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ITEM_EXPLOSION_RESISTANT, new RenderEntityItemHighlighted.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ITEM_CRYSTAL, new RenderEntityItemHighlighted.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ITEM_STARMETAL_INGOT, manager -> new ItemEntityRenderer(manager, Minecraft.getInstance().getItemRenderer()));

        RenderingRegistry.registerEntityRenderingHandler(OBSERVATORY_HELPER, new RenderEntityEmpty.Factory());
        RenderingRegistry.registerEntityRenderingHandler(GRAPPLING_HOOK, new RenderEntityGrapplingHook.Factory());
    }

    private static <E extends Entity> EntityType<E> register(String name, EntityType.Builder<E> typeBuilder) {
        EntityType<E> type = typeBuilder.build(AstralSorcery.key(name).toString());
        return AstralRegistries.register(AstralRegistries.ENTITY_TYPES, AstralSorcery.key(name), type);
    }
}
