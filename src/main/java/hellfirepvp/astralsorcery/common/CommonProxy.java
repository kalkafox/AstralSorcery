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
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.common.data.world.LightNetworkBuffer;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
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
import net.minecraft.network.chat.Component;
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
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.registries.RegisterEvent;
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
import java.util.function.Supplier;

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

    // 1.21 port: bypassArmor/setMagic/bypassMagic were per-instance DamageSource flags in 1.16;
    // that behavior is now driven by DamageType datapack tags, which DamageSourceUtil.newType's
    // unregistered Holder can't participate in - see DamageSourceUtil's class javadoc caveat.
    public static DamageSource DAMAGE_SOURCE_BLEED   = DamageSourceUtil.newType("astralsorcery.bleed");
    public static DamageSource DAMAGE_SOURCE_STELLAR = DamageSourceUtil.newType("astralsorcery.stellar");
    public static DamageSource DAMAGE_SOURCE_REFLECT = DamageSourceUtil.newType("thorns");

    // 1.21 port: tab contents show every registered item in the main tab for now;
    // the old per-item group assignments (papers/crystals) still need re-curation.
    public static final Supplier<CreativeModeTab> ITEM_GROUP_AS = AstralRegistries.CREATIVE_MODE_TABS.register(
            AstralSorcery.MODID, () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + AstralSorcery.MODID))
                    .icon(() -> new ItemStack(TOME))
                    .displayItems((params, out) -> {
                        AstralRegistries.CREATIVE_NAMES.getEntries().forEach(holder -> out.accept(holder.get()));
                        RegistryItems.addCreativeVariants(out);
                    })
                    .build());
    public static final Supplier<CreativeModeTab> ITEM_GROUP_AS_PAPERS = AstralRegistries.CREATIVE_MODE_TABS.register(
            AstralSorcery.MODID + "_papers", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + AstralSorcery.MODID + ".papers"))
                    .icon(() -> new ItemStack(CONSTELLATION_PAPER))
                    .build());
    public static final Supplier<CreativeModeTab> ITEM_GROUP_AS_CRYSTALS = AstralRegistries.CREATIVE_MODE_TABS.register(
            AstralSorcery.MODID + "_crystals", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + AstralSorcery.MODID + ".crystals"))
                    .icon(() -> new ItemStack(ROCK_CRYSTAL))
                    .build());
    // 1.21 port: Rarity.create is gone (enum extension is JSON-driven now);
    // mapped to nearest vanilla rarities until enumextensions.json is added.
    public static final Rarity RARITY_CELESTIAL = Rarity.RARE;
    public static final Rarity RARITY_ARTIFACT = Rarity.EPIC;
    public static final Rarity RARITY_VESTIGE = Rarity.EPIC;

    public static final ArmorMaterial ARMOR_MATERIAL_IMBUED_LEATHER = AstralRegistries.register(
            AstralRegistries.ARMOR_MATERIALS, AstralSorcery.key("imbued_leather"), ArmorMaterialImbuedLeather.create());

    private boolean registryContentBuilt = false;
    private boolean intrusiveContentBuilt = false;
    private boolean itemDependentContentBuilt = false;
    private CommonScheduler commonScheduler;
    private TickManager tickManager;
    private final List<ServerLifecycleListener> serverLifecycleListeners = Lists.newArrayList();

    private CommonConfig commonConfig;
    private ServerConfig worldData;

    public void initialize() {
        this.commonScheduler = new CommonScheduler();

        this.commonConfig = new CommonConfig();
        this.worldData = new ServerConfig();

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
        ConfigRegistries.getRegistries().buildDataRegistries(this.worldData);

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

        modEventBus.addListener(PacketChannel::registerPayloadHandlers);
        modEventBus.addListener(RegistryEntities::initAttributes);
        modEventBus.addListener(RegistryCapabilities::attachCapabilities);

        this.buildRegistryContent();
        modEventBus.addListener(EventPriority.HIGHEST, this::buildIntrusiveRegistryContent);
        modEventBus.addListener(EventPriority.LOWEST, this::buildItemDependentRegistryContent);
        AstralRegistries.subscribe(modEventBus);
    }

    /**
     * Blocks, items, fluids and entity types create intrusive holders in
     * their constructors, which the built-in registries only permit while
     * NeoForge has them unfrozen - i.e. inside the RegisterEvent window, not
     * during mod construction. This builds that content on the first
     * RegisterEvent at HIGHEST priority, so the values exist (and their
     * suppliers are queued) before the deferred registers add their entries.
     */
    protected void buildIntrusiveRegistryContent(RegisterEvent event) {
        if (intrusiveContentBuilt) {
            return;
        }
        intrusiveContentBuilt = true;

        RegistryFluids.registerFluids();
        RegistryBlocks.registerBlocks();
        RegistryBlocks.registerFluidBlocks();
        RegistryItems.registerItems();
        RegistryItems.registerItemBlocks();
        RegistryItems.registerFluidContainerItems();

        RegistryTileEntities.registerTiles();
        RegistryEntities.init();
    }

    /**
     * Content that dereferences the registered block/item instances - notably
     * anything calling {@code Block.asItem()}, which permanently caches the
     * air item if invoked before the item registry is populated. Runs at
     * LOWEST priority on the item RegisterEvent, i.e. right after the item
     * entries were added; the custom-registry events this queues content for
     * all fire later.
     */
    protected void buildItemDependentRegistryContent(RegisterEvent event) {
        if (!Registries.ITEM.equals(event.getRegistryKey()) || itemDependentContentBuilt) {
            return;
        }
        itemDependentContentBuilt = true;

        RegistryStructures.init();
        RegistryResearch.init();
    }

    /**
     * Eagerly builds the (non-intrusive-holder) registry content in
     * dependency order, queueing everything on the deferred registers before
     * their RegisterEvents fire.
     */
    protected void buildRegistryContent() {
        if (registryContentBuilt) {
            return;
        }
        registryContentBuilt = true;

        RegistryEffects.init();
        RegistryContainerTypes.init();
        RegistrySounds.init();

        RegistryConstellationEffects.init();
        RegistryMantleEffects.init();
        RegistryEngravingEffects.init();
        RegistryWorldGeneration.init();
        RegistryLoot.init();
        RegistryCrystalPropertyUsages.init();
        RegistryCrystalProperties.init();
        RegistryCrystalProperties.initDefaultAttributes();
        RegistryRecipeTypes.init();
        RegistryRecipeTypes.initAltarEffects();
        RegistryRecipeSerializers.init();

        TransmissionClassRegistry.setupRegistry();
        SourceClassRegistry.setupRegistry();

        RegistryPerkAttributeTypes.init();
        RegistryPerkConverters.init();
        RegistryPerkCustomModifiers.init();
        RegistryPerkAttributeReaders.init();

        RegistryCapabilities.init();
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

        // 1.21 port: worldgen biome injection is datapack-driven (biome modifier
        // JSONs emitted by datagen); the old BiomeLoadingEvent hook is gone.

        eventBus.addListener(PlayerAmuletHandler::onEnchantmentAdd);
        eventBus.addListener(DynamicEnchantmentHelper::onGetEnchantmentLevel);
        eventBus.addListener(BlockDropCaptureAssist.INSTANCE::onDrop);
        eventBus.addListener(CelestialGatewayHandler.INSTANCE::onWorldInit);
        eventBus.addListener(EventPriority.LOW, TileTreeBeacon.TreeWatcher::onGrow);

        tickManager.attachListeners(eventBus);
        TransmissionChunkTracker.INSTANCE.attachListeners(eventBus);

        BlockChangeNotifier.addListener(new EventHandlerAutoLink());

        // 1.21 port: CraftTweaker integration is excluded from the build for now.
        //Mods.CRAFTTWEAKER.executeIfPresent(() -> () -> IntegrationCraftTweaker.attachListeners(eventBus));
    }

    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(this.commonScheduler);
        registrar.accept(StarlightTransmissionHandler.getInstance());
        registrar.accept(StarlightUpdateHandler.getInstance());
        registrar.accept(LightNetworkBuffer.NETWORK_TICK_HANDLER);
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

        this.worldData.addConfigEntry(GeneralConfig.CONFIG);
        this.worldData.addConfigEntry(ToolsConfig.CONFIG);
        this.worldData.addConfigEntry(EntityConfig.CONFIG);
        this.worldData.addConfigEntry(CraftingConfig.CONFIG);
        this.worldData.addConfigEntry(LightNetworkConfig.CONFIG);
        this.worldData.addConfigEntry(LogConfig.CONFIG);
        this.worldData.addConfigEntry(PerkConfig.CONFIG);
        this.worldData.addConfigEntry(AmuletRandomizeHelper.CONFIG);
        this.worldData.addConfigEntry(MachineryConfig.CONFIG);

        RegistryPerks.initConfig(PerkConfig.CONFIG::newSubSection);

        this.commonConfig.addConfigEntry(CommonGeneralConfig.CONFIG);
        this.commonConfig.addConfigEntry(WorldGenConfig.CONFIG);

        RegistryWorldGeneration.addConfigEntries(WorldGenConfig.CONFIG::newSubSection);

        ConstellationEffectRegistry.addConfigEntries(this.worldData);
        MantleEffectRegistry.addConfigEntries(this.worldData);
    }

    public TickManager getTickManager() {
        return tickManager;
    }

    // Utils

    public FakePlayer getASFakePlayerServer(ServerLevel level) {
        return FakePlayerFactory.get(level, new GameProfile(FAKEPLAYER_UUID, "AS-FakePlayer"));
    }

    public File getASServerDataDirectory() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        File asDataDir = server.getWorldPath(new LevelResource(AstralSorcery.MODID)).toFile();
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
        this.worldData.buildConfiguration();

        StarlightNetworkRegistry.setupRegistry();
        CollisionManager.init();

        PatreonDataManager.loadPatreonEffects();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandAstralSorcery.register(event.getDispatcher());
    }

    private void onEnqueueIMC(InterModEnqueueEvent event) {
        // 1.21 port: Curios integration is excluded from the build for now.
        //Mods.CURIOS.executeIfPresent(() -> IntegrationCurios::initIMC);
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
