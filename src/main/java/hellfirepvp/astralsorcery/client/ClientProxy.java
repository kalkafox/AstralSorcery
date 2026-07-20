/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.data.config.ClientConfig;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.handler.EffectUpdater;
import hellfirepvp.astralsorcery.client.event.*;
import hellfirepvp.astralsorcery.client.event.effect.EffectRenderEventHandler;
import hellfirepvp.astralsorcery.client.event.effect.LightbeamRenderHelper;
import hellfirepvp.astralsorcery.client.registry.RegistryKeyBindings;
import hellfirepvp.astralsorcery.client.render.entity.layer.StarryLayerRenderer;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.AssetPreLoader;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalConstellationOverview;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.screen.journal.bookmark.BookmarkProvider;
import hellfirepvp.astralsorcery.client.util.AreaOfInfluencePreview;
import hellfirepvp.astralsorcery.client.util.ColorizationHelper;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.camera.CameraEventHelper;
import hellfirepvp.astralsorcery.client.util.camera.ClientCameraManager;
import hellfirepvp.astralsorcery.client.util.draw.RenderInfo;
import hellfirepvp.astralsorcery.client.util.word.RandomWordGenerator;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.base.patreon.manager.PatreonManagerClient;
import hellfirepvp.astralsorcery.common.block.tile.BlockStructural;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import hellfirepvp.astralsorcery.common.registry.*;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hellfirepvp.astralsorcery.client.lib.ShadersAS;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientProxy
 * Created by HellFirePvP
 * Date: 19.04.2019 / 18:38
 */
public class ClientProxy extends CommonProxy {

    private ClientScheduler clientScheduler;

    private ClientConfig clientConfig;

    @Override
    public void initialize() {
        this.clientScheduler = new ClientScheduler();

        if (!AstralSorcery.isDoingDataGeneration()) {
            ReloadableResourceManager resMgr = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
            resMgr.registerReloadListener(AssetLibrary.INSTANCE);
            resMgr.registerReloadListener(AssetPreLoader.INSTANCE);
            resMgr.registerReloadListener(ColorizationHelper.onReload());
            // 1.21 port: selective (resource-type-filtered) reloads are gone; clear perk text caches
            // on every resource reload.
            resMgr.registerReloadListener((stage, resourceManager, preparationsProfiler, reloadProfiler, executor, gameExecutor) ->
                    stage.wait(Unit.INSTANCE).thenRunAsync(() ->
                        PerkTree.PERK_TREE.getPerkPoints(LogicalSide.CLIENT).stream()
                                .map(PerkTreePoint::getPerk)
                                .forEach(AbstractPerk::clearClientTextCaches)));
        }

        this.clientConfig = new ClientConfig();

        super.initialize();

        this.addTomeBookmarks();
        RandomWordGenerator.init();

        this.clientConfig.buildConfiguration();
    }

    @Override
    protected void initializeConfigurations() {
        super.initializeConfigurations();

        this.clientConfig.addConfigEntry(RenderingConfig.CONFIG);
    }

    @Override
    public void attachLifecycle(IEventBus modEventBus) {
        super.attachLifecycle(modEventBus);

        modEventBus.addListener(RegistryItems::registerColors);
        modEventBus.addListener(RegistryBlocks::registerColors);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterRenderers);
        modEventBus.addListener(this::onAddLayers);
        modEventBus.addListener(this::onRegisterClientExtensions);
        modEventBus.addListener(this::onRegisterShaders);
        modEventBus.addListener(RegistryContainerTypes::initClient);
        modEventBus.addListener(RegistryKeyBindings::init);
    }

    private void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(
                            event.getResourceProvider(),
                            AstralSorcery.key("effect_tex_color"),
                            DefaultVertexFormat.POSITION_TEX_COLOR),
                    shader -> ShadersAS.EFFECT_TEX_COLOR = shader);
        } catch (java.io.IOException exc) {
            throw new RuntimeException("Failed to load Astral Sorcery core shaders", exc);
        }
    }

    private void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Suppress vanilla break/hit particles for the invisible flare light.
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                return true;
            }
        }, BlocksAS.FLARE_LIGHT);

        // Also play break particles for the telescope's structural top half.
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                RenderingUtils.playBlockBreakParticles(pos.above(), BlocksAS.TELESCOPE.defaultBlockState(), BlocksAS.TELESCOPE.defaultBlockState());
                return false;
            }
        }, BlocksAS.TELESCOPE);

        // Redirect break/hit particles of structural dummies to the block they support.
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                EventFlags.PLAY_BLOCK_BREAK_EFFECTS.executeWithFlag(() -> {
                    switch (state.getValue(BlockStructural.BLOCK_TYPE)) {
                        case TELESCOPE:
                            manager.destroy(pos.below(), BlocksAS.TELESCOPE.defaultBlockState());
                            break;
                    }
                });
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                if (target instanceof BlockHitResult blockHit) {
                    EventFlags.PLAY_BLOCK_BREAK_EFFECTS.executeWithFlag(() -> {
                        switch (state.getValue(BlockStructural.BLOCK_TYPE)) {
                            case TELESCOPE:
                                manager.destroy(blockHit.getBlockPos().below(), BlocksAS.TELESCOPE.defaultBlockState());
                                break;
                        }
                    });
                }
                return true;
            }
        }, BlocksAS.STRUCTURAL);

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return AstralSorcery.key("fluid/liquid_starlight_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return AstralSorcery.key("fluid/liquid_starlight_flowing");
            }
        }, FluidsAS.LIQUID_STARLIGHT_FLUID_TYPE);
    }

    @Override
    public void attachEventHandlers(IEventBus eventBus) {
        super.attachEventHandlers(eventBus);

        EffectRenderEventHandler.getInstance().attachEventListeners(eventBus);
        AlignmentChargeRenderer.INSTANCE.attachEventListeners(eventBus);
        PerkExperienceRenderer.INSTANCE.attachEventListeners(eventBus);
        ItemHeldEffectRenderer.INSTANCE.attachEventListeners(eventBus);
        OverlayRenderer.INSTANCE.attachEventListeners(eventBus);

        CameraEventHelper.attachEventListeners(eventBus);
        GatewayInteractionHandler.attachEventListeners(eventBus);

        eventBus.addListener(EventPriority.LOWEST, SkyRenderEventHandler::onRender);
        eventBus.addListener(EventPriority.LOWEST, SkyRenderEventHandler::onFog);

        eventBus.addListener((RecipesUpdatedEvent event) ->
                RecipeHelper.rebindDynamicIds(event.getRecipeManager()));
    }

    @Override
    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        super.attachTickListeners(registrar);

        registrar.accept(this.clientScheduler);
        registrar.accept(RenderInfo.getInstance());
        registrar.accept(EffectUpdater.getInstance());
        registrar.accept(PatreonManagerClient.INSTANCE);
        registrar.accept(ClientCameraManager.INSTANCE);
        registrar.accept(TimeStopEffectHandler.INSTANCE);
        registrar.accept(AlignmentChargeRenderer.INSTANCE);
        registrar.accept(PerkExperienceRenderer.INSTANCE);
        registrar.accept(AreaOfInfluencePreview.INSTANCE);

        LightbeamRenderHelper.attachTickListener(registrar);
        EffectRenderEventHandler.getInstance().attachTickListeners(registrar);
    }

    @Override
    public void scheduleClientside(Runnable r, int tickDelay) {
        this.clientScheduler.addRunnable(r, tickDelay);
    }

    @Override
    public void openGuiClient(GuiType type, CompoundTag data) {
        Screen toOpen = type.deserialize(data);
        if (toOpen != null) {
            Minecraft.getInstance().setScreen(toOpen);
        }
    }

    @Override
    public void openGui(Player player, GuiType type, Object... data) {
        if (player instanceof AbstractClientPlayer) {
            openGuiClient(type, type.serializeArguments(data));
            return;
        }
        super.openGui(player, type, data);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        RegistryBlockRenderTypes.initBlocks();
        RegistryBlockRenderTypes.initFluids();
        RegistryItems.registerItemProperties();
    }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        RegistryEntities.initClient(event);
        RegistryTileEntities.initClient();
    }

    private void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new StarryLayerRenderer<>(renderer, skin == PlayerSkin.Model.SLIM));
            }
        }
    }

    private void addTomeBookmarks() {
        ScreenJournal.addBookmark(new BookmarkProvider("screen.astralsorcery.tome.progression", 10,
                ScreenJournalProgression::getJournalInstance,
                () -> true));
        ScreenJournal.addBookmark(new BookmarkProvider("screen.astralsorcery.tome.constellations", 20,
                ScreenJournalConstellationOverview::getConstellationScreen,
                () -> !ResearchHelper.getClientProgress().getSeenConstellations().isEmpty()));
        ScreenJournal.addBookmark(new BookmarkProvider("screen.astralsorcery.tome.perks", 30,
                ScreenJournalPerkTree::new,
                () -> ResearchHelper.getClientProgress().isAttuned()));
    }

}
