/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.internal;

import hellfirepvp.astralsorcery.common.registry.*;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Builds Astral Sorcery's code-defined values, then publishes each queued
 * value during the corresponding NeoForge RegisterEvent.
 */
public class PrimerEventHandler {

    private final InternalRegistryPrimer registry;
    private boolean prepared;

    public PrimerEventHandler(InternalRegistryPrimer registry) {
        this.registry = registry;
    }

    public void attachEventHandlers(IEventBus eventBus) {
        prepareRegistrations();
        eventBus.addListener(this::register);
    }

    private void prepareRegistrations() {
        if (prepared) {
            return;
        }
        prepared = true;

        RegistryFluids.registerFluids();
        RegistryBlocks.registerBlocks();
        RegistryBlocks.registerFluidBlocks();
        RegistryItems.registerItems();
        RegistryItems.registerItemBlocks();
        RegistryItems.registerFluidContainerItems();

        RegistryTileEntities.registerTiles();
        RegistryEntities.init();
        RegistryEffects.init();
        RegistryEnchantments.init();
        RegistryContainerTypes.init();
        RegistrySounds.init();

        RegistryConstellationEffects.init();
        RegistryMantleEffects.init();
        RegistryEngravingEffects.init();
        RegistryStructures.init();
        RegistryCrystalPropertyUsages.init();
        RegistryCrystalProperties.init();
        RegistryCrystalProperties.initDefaultAttributes();
        RegistryRecipeTypes.init();
        RegistryRecipeTypes.initAltarEffects();
        RegistryRecipeSerializers.init();
        RegistryResearch.init();

        TransmissionClassRegistry.setupRegistry();
        SourceClassRegistry.setupRegistry();

        RegistryPerkAttributeTypes.init();
        RegistryPerkConverters.init();
        RegistryPerkCustomModifiers.init();
        RegistryPerkAttributeReaders.init();
    }

    private void register(RegisterEvent event) {
        registry.fillRegistry(event);
    }
}
