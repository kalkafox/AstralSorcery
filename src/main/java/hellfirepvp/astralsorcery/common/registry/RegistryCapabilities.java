/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.capability.ChunkFluidEntry;
import hellfirepvp.astralsorcery.common.lib.CapabilitiesAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import hellfirepvp.astralsorcery.common.tile.TileFountain;
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.tile.TileSpectralRelay;
import hellfirepvp.astralsorcery.common.tile.TileWell;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryCapabilities
 * Created by HellFirePvP
 * Date: 19.07.2019 / 13:59
 */
public class RegistryCapabilities {

    private RegistryCapabilities() {}

    public static void init() {
        CapabilitiesAS.CHUNK_FLUID = AstralRegistries.ATTACHMENT_TYPES.register(
                CapabilitiesAS.CHUNK_FLUID_KEY.getPath(),
                () -> AttachmentType.serializable(ChunkFluidEntry::new).build());
    }

    public static void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TileEntityTypesAS.WELL, TileWell::getExposedItemHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TileEntityTypesAS.WELL, TileWell::getExposedFluidHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TileEntityTypesAS.CHALICE, TileChalice::getExposedFluidHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TileEntityTypesAS.FOUNTAIN, TileFountain::getExposedFluidHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TileEntityTypesAS.INFUSER, TileInfuser::getExposedItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TileEntityTypesAS.RITUAL_PEDESTAL, TileRitualPedestal::getExposedItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TileEntityTypesAS.SPECTRAL_RELAY, TileSpectralRelay::getExposedItemHandler);

        // NeoForge only auto-registers the bucket wrapper for BucketItem itself, not subclasses
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), ItemsAS.BUCKET_LIQUID_STARLIGHT);
    }

}
