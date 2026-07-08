/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.BlockBreakHelper;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonDataManager;
import hellfirepvp.astralsorcery.common.base.patreon.manager.PatreonManager;
import hellfirepvp.astralsorcery.common.cmd.CommandAstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeTypeHandler;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.data.config.ServerConfig;
import hellfirepvp.astralsorcery.common.data.config.base.BaseConfiguration;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigRegistries;
import hellfirepvp.astralsorcery.common.data.config.entry.*;
import hellfirepvp.astralsorcery.common.data.config.entry.common.CommonGeneralConfig;
import hellfirepvp.astralsorcery.common.data.config.registry.*;
import hellfirepvp.astralsorcery.common.data.research.ResearchIOThread;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletRandomizeHelper;
import hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.event.handler.*;
import hellfirepvp.astralsorcery.common.event.helper.*;
import hellfirepvp.astralsorcery.common.integration.IntegrationCraftTweaker;
import hellfirepvp.astralsorcery.common.integration.IntegrationCurios;
import hellfirepvp.astralsorcery.common.item.armor.ArmorMaterialImbuedLeather;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktOpenGui;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeLimiter;
import hellfirepvp.astralsorcery.common.perk.PerkCooldownHelper;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.data.PerkTreeLoader;
import hellfirepvp.astralsorcery.common.perk.data.PerkTypeHandler;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.tick.PerkTickHelper;
import hellfirepvp.astralsorcery.common.registry.*;
import hellfirepvp.astralsorcery.common.registry.internal.InternalRegistryPrimer;
import hellfirepvp.astralsorcery.common.registry.internal.PrimerEventHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightNetworkRegistry;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionChunkTracker;
import hellfirepvp.astralsorcery.common.tile.TileTreeBeacon;
import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.DamageSourceUtil;
import hellfirepvp.astralsorcery.common.util.ServerLifecycleListener;
import hellfirepvp.astralsorcery.common.util.collision.CollisionManager;
import hellfirepvp.astralsorcery.common.util.time.TimeStopController;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import hellfirepvp.observerlib.common.util.tick.TickManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static hellfirepvp.astralsorcery.common.lib.ItemsAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 19.04.2019 / 18:38
 */
public class CommonProxy {

    public static final UUID FAKEPLAYER_UUID = UUID.fromString("b0c3097f-8391-4b4b-a89a-553ef730b13a");

    public static DamageSource DAMAGE_SOURCE_BLEED   = DamageSourceUtil.newType("astralsorcery.bleed")
            .setDamageBypassesArmor();
    public static DamageSource DAMAGE_SOURCE_STELLAR = DamageSourceUtil.newType("astralsorcery.stellar")
            .setDamageBypassesArmor().setMagicDamage();
    public static DamageSource DAMAGE_SOURCE_REFLECT = DamageSourceUtil.newType("thorns")
            .setDamageBypassesArmor().setDamageIsAbsolute();

    public static final CreativeModeTab ITEM_GROUP_AS = new CreativeModeTab(AstralSorcery.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(TOME);
        }
    };
    public static final CreativeModeTab ITEM_GROUP_AS_PAPERS = new CreativeModeTab(AstralSorcery.MODID + ".papers") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(CONSTELLATION_PAPER);
        }
    };
    public static final CreativeModeTab ITEM_GROUP_AS_CRYSTALS = new CreativeModeTab(AstralSorcery.MODID + ".crystals") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ROCK_CRYSTAL);
        }
    };
    public static final Rarity RARITY_CELESTIAL = Rarity.create("AS_CELESTIAL", TextFormatting.BLUE);
    public static final Rarity RARITY_ARTIFACT = Rarity.create("AS_ARTIFACT", TextFormatting.GOLD);
    public static final Rarity RARITY_VESTIGE = Rarity.create("AS_VESTIGE", TextFormatting.RED);

    public static final ArmorMaterial ARMOR_MATERIAL_IMBUED_LEATHER = new ArmorMaterialImbuedLeather();

    private InternalRegistryPrimer registryPrimer;
    private PrimerEventHandler registryEventHandler;
    private CommonScheduler commonScheduler;
    private TickManager tickManager;
    private final List<ServerLifecycleListener> serverLifecycleListeners = Lists.newArrayList();

    private CommonConfig commonConfig;
    private ServerConfig serverConfig;

    public void initialize() {
        this.registryPrimer = new InternalRegistryPrimer();
        this.registryEventHandler = new PrimerEventHandler(this.registryPrimer);
        this.commonScheduler = new CommonScheduler();

        this.commonConfig = new CommonConfig();
        this.serverConfig = new ServerConfig();

        RegistryData.init();
        RegistryMaterials.init();
        RegistryGameRules.init();
        RegistryStructureTypes.init();
        PacketChannel.registerPackets();
        RegistryIngredientTypes.init();
        RegistryAdvancements.init();
        AltarRecipeTypeHandler.init();
        PerkTypeHandler.init();
        ModifierManager.init();
        RegistryConstellations.init();
        RegistryArgumentTypes.init();

        this.initializeConfigurations();
        ConfigRegistries.getRegistries().buildDataRegistries(this.serverConfig);

        this.tickManager = new TickManager();
        this.attachTickListeners(tickManager::register);

        this.serverLifecycleListeners.add(ResearchIOThread.getInstance());
        this.serverLifecycleListeners.add(ServerLifecycleListener.wrap(EventHandlerCache::onServerStart, EventHandlerCache::onServerStop));
        this.serverLifecycleListeners.add(ServerLifecycleListener.wrap(CelestialGatewayHandler.INSTANCE::onServerStart, CelestialGatewayHandler.INSTANCE::onServerStop));
        this.serverLifecycleListeners.add(ServerLifecycleListener.start(PerkTree.PERK_TREE::setupServerPerkTree));
        this.serverLifecycleListeners.add(ServerLifecycleListener.start(PerkLevelManager::loadPerkLevels));
        this.serverLifecycleListeners.add(ServerLifecycleListener.stop(BlockBreakHelper::clearServerCache));
        this.serverLifecycleListeners.add(ServerLifecycleListener.stop(TileTreeBeacon.TreeWatcher::clearServerCache));
        this.serverLifecycleListeners.add(ServerLifecycleListener.stop(PlayerAffectionFlags::clearServerCache));

        SyncDataHolder.initialize();

        this.commonConfig.buildConfiguration();
    }

    public void attachLifecycle(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onEnqueueIMC);
        modEventBus.addListener(BaseConfiguration::refreshConfiguration);

        modEventBus.addListener(RegistryRegistries::buildRegistries);
        modEventBus.addListener(PacketChannel::registerPayloadHandlers);
        modEventBus.addListener(RegistryEntities::initAttributes);
        registryEventHandler.attachEventHandlers(modEventBus);
    }

    public void attachEventHandlers(IEventBus eventBus) {
        eventBus.addListener(this::onRegisterCommands);
        eventBus.addListener(this::onServerStop);
        eventBus.addListener(this::onServerStopping);
        eventBus.addListener(this::onServerStarting);
        eventBus.addListener(this::onServerStarted);
        eventBus.addListener(this::onRegisterReloadListeners);

        EventHandlerInteract.attachListeners(eventBus);
        EventHandlerCache.attachListeners(eventBus);
        EventHandlerBlockStorage.attachListeners(eventBus);
        EventHandlerMisc.attachListeners(eventBus);
        EventHelperSpawnDeny.attachListeners(eventBus);
        EventHelperInvulnerability.attachListeners(eventBus);
        EventHelperEntityFreeze.attachListeners(eventBus);
        EventHelperDamageCancelling.attachListeners(eventBus);
        PerkAttributeLimiter.attachListeners(eventBus);

        eventBus.addListener(RegistryWorldGeneration::loadBiomeFeatures);

        eventBus.addListener(PlayerAmuletHandler::onEnchantmentAdd);
        eventBus.addListener(DynamicEnchantmentHelper::onGetEnchantmentLevel);
        eventBus.addListener(BlockDropCaptureAssist.INSTANCE::onDrop);
        eventBus.addListener(CelestialGatewayHandler.INSTANCE::onWorldInit);
        eventBus.addListener(EventPriority.LOW, TileTreeBeacon.TreeWatcher::onGrow);

        tickManager.attachListeners(eventBus);
        TransmissionChunkTracker.INSTANCE.attachListeners(eventBus);

        BlockChangeNotifier.addListener(new EventHandlerAutoLink());

        Mods.CRAFTTWEAKER.executeIfPresent(() -> () -> IntegrationCraftTweaker.attachListeners(eventBus));
    }

    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(this.commonScheduler);
        registrar.accept(StarlightTransmissionHandler.getInstance());
        registrar.accept(StarlightUpdateHandler.getInstance());
        registrar.accept(SyncDataHolder.getTickInstance());
        registrar.accept(LinkHandler.getInstance());
        registrar.accept(SkyHandler.getInstance());
        registrar.accept(PlayerAmuletHandler.INSTANCE);
        registrar.accept(PerkTickHelper.INSTANCE);
        registrar.accept(PatreonManager.INSTANCE);
        registrar.accept(TimeStopController.INSTANCE);
        registrar.accept(AlignmentChargeHandler.INSTANCE);
        registrar.accept(ModifierManager.INSTANCE);
        registrar.accept(EventHelperEnchantmentTick.INSTANCE);

        EventHelperTemporaryFlight.attachTickListener(registrar);
        EventHelperSpawnDeny.attachTickListener(registrar);
        EventHelperInvulnerability.attachTickListener(registrar);
        EventHelperEntityFreeze.attachTickListener(registrar);
        PerkCooldownHelper.attachTickListeners(registrar);
        PlayerAffectionFlags.attachTickListeners(registrar);
    }

    protected void initializeConfigurations() {
        ConfigRegistries.getRegistries().addDataRegistry(FluidRarityRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(TechnicalEntityRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(TileAccelerationBlacklistRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(AmuletEnchantmentRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(WeightedPerkAttributeRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(OreItemRarityRegistry.VOID_TRASH_REWARD);
        ConfigRegistries.getRegistries().addDataRegistry(OreBlockRarityRegistry.STONE_ENRICHMENT);
        ConfigRegistries.getRegistries().addDataRegistry(OreBlockRarityRegistry.MINERALIS_RITUAL);
        ConfigRegistries.getRegistries().addDataRegistry(EntityTransmutationRegistry.INSTANCE);

        ToolsConfig.CONFIG.newSubSection(WandsConfig.CONFIG);
        MachineryConfig.CONFIG.newSubSection(TileTreeBeacon.Config.CONFIG);

        this.serverConfig.addConfigEntry(GeneralConfig.CONFIG);
        this.serverConfig.addConfigEntry(ToolsConfig.CONFIG);
        this.serverConfig.addConfigEntry(EntityConfig.CONFIG);
        this.serverConfig.addConfigEntry(CraftingConfig.CONFIG);
        this.serverConfig.addConfigEntry(LightNetworkConfig.CONFIG);
        this.serverConfig.addConfigEntry(LogConfig.CONFIG);
        this.serverConfig.addConfigEntry(PerkConfig.CONFIG);
        this.serverConfig.addConfigEntry(AmuletRandomizeHelper.CONFIG);
        this.serverConfig.addConfigEntry(MachineryConfig.CONFIG);

        RegistryPerks.initConfig(PerkConfig.CONFIG::newSubSection);

        this.commonConfig.addConfigEntry(CommonGeneralConfig.CONFIG);
        this.commonConfig.addConfigEntry(WorldGenConfig.CONFIG);

        RegistryWorldGeneration.addConfigEntries(WorldGenConfig.CONFIG::newSubSection);

        ConstellationEffectRegistry.addConfigEntries(this.serverConfig);
        MantleEffectRegistry.addConfigEntries(this.serverConfig);
    }

    public InternalRegistryPrimer getRegistryPrimer() {
        return registryPrimer;
    }

    public TickManager getTickManager() {
        return tickManager;
    }

    // Utils

    public FakePlayer getASFakePlayerServer(ServerLevel world) {
        return FakePlayerFactory.get(world, new GameProfile(FAKEPLAYER_UUID, "AS-FakePlayer"));
    }

    public File getASServerDataDirectory() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        File asDataDir = server.func_240776_a_(new LevelResource(AstralSorcery.MODID)).toFile();
        if (!asDataDir.exists()) {
            asDataDir.mkdirs();
        }
        return asDataDir;
    }

    public void scheduleClientside(Runnable r, int tickDelay) {}

    public void scheduleClientside(Runnable r) {
        this.scheduleClientside(r, 0);
    }

    public void scheduleDelayed(Runnable r, int tickDelay) {
        this.commonScheduler.addRunnable(r, tickDelay);
    }

    public void scheduleDelayed(Runnable r) {
        this.scheduleDelayed(r, 0);
    }

    // GUI stuff

    public void openGuiClient(GuiType type, CompoundTag data) {
        //No-Op
    }

    public void openGui(Player player, GuiType type, Object... data) {
        if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
            PktOpenGui pkt = new PktOpenGui(type, type.serializeArguments(data));
            PacketChannel.CHANNEL.sendToPlayer(player, pkt);
        }
    }

    // Mod events

    private void onCommonSetup(FMLCommonSetupEvent event) {
        this.serverConfig.buildConfiguration();

        RegistryCapabilities.init(NeoForge.EVENT_BUS);
        StarlightNetworkRegistry.setupRegistry();
        CollisionManager.init();

        PatreonDataManager.loadPatreonEffects();

        event.enqueueWork(RegistryWorldGeneration::registerStructureGeneration);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandAstralSorcery.register(event.getDispatcher());
    }

    private void onEnqueueIMC(InterModEnqueueEvent event) {
        Mods.CURIOS.executeIfPresent(() -> IntegrationCurios::initIMC);
    }

    // Generic events

    private void onRegisterReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PerkTreeLoader.INSTANCE);
    }

    private void onServerStarted(ServerStartedEvent event) {
        this.serverLifecycleListeners.forEach(ServerLifecycleListener::onServerStart);
    }

    private void onServerStarting(ServerStartingEvent event) {

    }

    private void onServerStopping(ServerStoppingEvent event) {
        this.serverLifecycleListeners.forEach(ServerLifecycleListener::onServerStop);
    }

    private void onServerStop(ServerStoppedEvent event) {
    }
}
