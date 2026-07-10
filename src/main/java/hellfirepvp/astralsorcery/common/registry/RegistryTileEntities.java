/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.client.render.tile.*;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.common.tile.*;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryTileEntities
 * Created by HellFirePvP
 * Date: 01.06.2019 / 13:35
 */
public class RegistryTileEntities {

    private RegistryTileEntities() {}

    public static void registerTiles() {
        SPECTRAL_RELAY = registerTile(TileSpectralRelay.class, BlocksAS.SPECTRAL_RELAY);
        ALTAR = registerTile(TileAltar.class, BlocksAS.ALTAR_DISCOVERY, BlocksAS.ALTAR_ATTUNEMENT, BlocksAS.ALTAR_CONSTELLATION, BlocksAS.ALTAR_RADIANCE);
        ATTUNEMENT_ALTAR = registerTile(TileAttunementAltar.class, BlocksAS.ATTUNEMENT_ALTAR);
        CELESTIAL_CRYSTAL_CLUSTER = registerTile(TileCelestialCrystals.class, BlocksAS.CELESTIAL_CRYSTAL_CLUSTER);
        GATEWAY = registerTile(TileCelestialGateway.class, BlocksAS.GATEWAY);
        CHALICE = registerTile(TileChalice.class, BlocksAS.CHALICE);
        COLLECTOR_CRYSTAL = registerTile(TileCollectorCrystal.class, BlocksAS.ROCK_COLLECTOR_CRYSTAL, BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL);
        FOUNTAIN = registerTile(TileFountain.class, BlocksAS.FOUNTAIN);
        GEM_CRYSTAL_CLUSTER = registerTile(TileGemCrystals.class, BlocksAS.GEM_CRYSTAL_CLUSTER);
        ILLUMINATOR = registerTile(TileIlluminator.class, BlocksAS.ILLUMINATOR);
        INFUSER = registerTile(TileInfuser.class, BlocksAS.INFUSER);
        LENS = registerTile(TileLens.class, BlocksAS.LENS);
        OBSERVATORY = registerTile(TileObservatory.class, BlocksAS.OBSERVATORY);
        PRISM = registerTile(TilePrism.class, BlocksAS.PRISM);
        REFRACTION_TABLE = registerTile(TileRefractionTable.class, BlocksAS.REFRACTION_TABLE);
        RITUAL_LINK = registerTile(TileRitualLink.class, BlocksAS.RITUAL_LINK);
        RITUAL_PEDESTAL = registerTile(TileRitualPedestal.class, BlocksAS.RITUAL_PEDESTAL);
        TELESCOPE = registerTile(TileTelescope.class, BlocksAS.TELESCOPE);
        TRANSLUCENT_BLOCK = registerTile(TileTranslucentBlock.class, BlocksAS.TRANSLUCENT_BLOCK);
        TREE_BEACON = registerTile(TileTreeBeacon.class, BlocksAS.TREE_BEACON);
        TREE_BEACON_COMPONENT = registerTile(TileTreeBeaconComponent.class, BlocksAS.TREE_BEACON_COMPONENT);
        VANISHING = registerTile(TileVanishing.class, BlocksAS.VANISHING);
        WELL = registerTile(TileWell.class, BlocksAS.WELL);
    }

    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        BlockEntityRenderers.register(ALTAR, RenderAltar::new);
        BlockEntityRenderers.register(ATTUNEMENT_ALTAR, RenderAttunementAltar::new);
        BlockEntityRenderers.register(CHALICE, RenderChalice::new);
        BlockEntityRenderers.register(COLLECTOR_CRYSTAL, RenderCollectorCrystal::new);
        BlockEntityRenderers.register(INFUSER, RenderInfuser::new);
        BlockEntityRenderers.register(LENS, RenderLens::new);
        BlockEntityRenderers.register(OBSERVATORY, RenderObservatory::new);
        BlockEntityRenderers.register(PRISM, RenderPrism::new);
        BlockEntityRenderers.register(REFRACTION_TABLE, RenderRefractionTable::new);
        BlockEntityRenderers.register(RITUAL_PEDESTAL, RenderRitualPedestal::new);
        BlockEntityRenderers.register(SPECTRAL_RELAY, RenderSpectralRelay::new);
        BlockEntityRenderers.register(TELESCOPE, RenderTelescope::new);
        BlockEntityRenderers.register(TRANSLUCENT_BLOCK, RenderTileFakedState::new);
        BlockEntityRenderers.register(TREE_BEACON_COMPONENT, RenderTileFakedState::new);
        BlockEntityRenderers.register(WELL, RenderWell::new);
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerTile(Class<T> tileClass, Block... validBlocks) {
        ResourceLocation name = NameUtil.fromClass(tileClass, "Tile");
        BlockEntityType.Builder<T> typeBuilder = BlockEntityType.Builder.of((pos, state) -> {
            try {
                return tileClass.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
            } catch (ReflectiveOperationException exc) {
                throw new IllegalArgumentException("Unexpected Constructor for class: " + tileClass.getName(), exc);
            }
        }, validBlocks);

        BlockEntityType<T> type = typeBuilder.build(null);
        return AstralRegistries.register(AstralRegistries.BLOCK_ENTITY_TYPES, name, type);
    }
}
