/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.level.material.Fluid;

import java.util.Arrays;
import java.util.List;

import static hellfirepvp.astralsorcery.common.lib.BlocksAS.*;
import static hellfirepvp.astralsorcery.common.lib.FluidsAS.LIQUID_STARLIGHT_FLOWING;
import static hellfirepvp.astralsorcery.common.lib.FluidsAS.LIQUID_STARLIGHT_SOURCE;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryBlockRenderTypes
 * Created by HellFirePvP
 * Date: 10.06.2020 / 21:06
 */
public class RegistryBlockRenderTypes {

    private RegistryBlockRenderTypes() {}

    public static void initBlocks() {
        setRenderLayer(MARBLE_ARCH,           RenderType.solid());
        setRenderLayer(MARBLE_BRICKS,         RenderType.solid());
        setRenderLayer(MARBLE_CHISELED,       RenderType.solid());
        setRenderLayer(MARBLE_ENGRAVED,       RenderType.solid());
        setRenderLayer(MARBLE_PILLAR,         RenderType.solid());
        setRenderLayer(MARBLE_RAW,            RenderType.solid());
        setRenderLayer(MARBLE_RUNED,          RenderType.solid());
        setRenderLayer(MARBLE_STAIRS,         RenderType.solid());
        setRenderLayer(MARBLE_SLAB,           RenderType.solid());
        setRenderLayer(BLACK_MARBLE_ARCH,     RenderType.solid());
        setRenderLayer(BLACK_MARBLE_BRICKS,   RenderType.solid());
        setRenderLayer(BLACK_MARBLE_CHISELED, RenderType.solid());
        setRenderLayer(BLACK_MARBLE_ENGRAVED, RenderType.solid());
        setRenderLayer(BLACK_MARBLE_PILLAR,   RenderType.solid());
        setRenderLayer(BLACK_MARBLE_RAW,      RenderType.solid());
        setRenderLayer(BLACK_MARBLE_RUNED,    RenderType.solid());
        setRenderLayer(BLACK_MARBLE_STAIRS,   RenderType.solid());
        setRenderLayer(BLACK_MARBLE_SLAB,     RenderType.solid());
        setRenderLayer(INFUSED_WOOD,          RenderType.solid());
        setRenderLayer(INFUSED_WOOD_ARCH,     RenderType.solid());
        setRenderLayer(INFUSED_WOOD_COLUMN,   RenderType.solid());
        setRenderLayer(INFUSED_WOOD_ENGRAVED, RenderType.solid());
        setRenderLayer(INFUSED_WOOD_ENRICHED, RenderType.solid());
        setRenderLayer(INFUSED_WOOD_INFUSED,  RenderType.solid());
        setRenderLayer(INFUSED_WOOD_PLANKS,   RenderType.solid());
        setRenderLayer(INFUSED_WOOD_STAIRS,   RenderType.solid());
        setRenderLayer(INFUSED_WOOD_SLAB,     RenderType.solid());

        setRenderLayer(AQUAMARINE_SAND_ORE, RenderType.solid(), RenderType.translucent());
        setRenderLayer(ROCK_CRYSTAL_ORE,    RenderType.solid(), RenderType.translucent());
        setRenderLayer(STARMETAL_ORE,       RenderType.solid(), RenderType.translucent());
        setRenderLayer(STARMETAL,           RenderType.solid());
        setRenderLayer(GLOW_FLOWER,         RenderType.cutout());

        setRenderLayer(SPECTRAL_RELAY,              RenderType.solid(), RenderType.translucent());
        setRenderLayer(ALTAR_DISCOVERY,             RenderType.solid());
        setRenderLayer(ALTAR_ATTUNEMENT,            RenderType.solid());
        setRenderLayer(ALTAR_CONSTELLATION,         RenderType.solid());
        setRenderLayer(ALTAR_RADIANCE,              RenderType.solid());
        setRenderLayer(ATTUNEMENT_ALTAR,            RenderType.solid());
        setRenderLayer(CELESTIAL_CRYSTAL_CLUSTER,   RenderType.solid(), RenderType.translucent());
        setRenderLayer(GEM_CRYSTAL_CLUSTER,         RenderType.solid(), RenderType.translucent());
        setRenderLayer(ROCK_COLLECTOR_CRYSTAL,      RenderType.solid(), RenderType.translucent());
        setRenderLayer(CELESTIAL_COLLECTOR_CRYSTAL, RenderType.solid(), RenderType.translucent());
        setRenderLayer(LENS,                        RenderType.solid(), RenderType.translucent());
        setRenderLayer(PRISM,                       RenderType.solid(), RenderType.translucent());
        setRenderLayer(RITUAL_LINK,                 RenderType.solid(), RenderType.translucent());
        setRenderLayer(RITUAL_PEDESTAL,             RenderType.solid(), RenderType.translucent());
        setRenderLayer(INFUSER,                     RenderType.solid(), RenderType.translucent());
        setRenderLayer(CHALICE,                     RenderType.solid());
        setRenderLayer(WELL,                        RenderType.solid());
        setRenderLayer(ILLUMINATOR,                 RenderType.solid(), RenderType.translucent());
        setRenderLayer(TELESCOPE,                   RenderType.solid());
        setRenderLayer(TELESCOPE,                   RenderType.solid());
        setRenderLayer(OBSERVATORY,                 RenderType.solid());
        setRenderLayer(REFRACTION_TABLE,            RenderType.solid());
        setRenderLayer(TREE_BEACON,                 RenderType.solid());
        setRenderLayer(TREE_BEACON_COMPONENT,       RenderType.translucent());
        setRenderLayer(GATEWAY,                     RenderType.solid(), RenderType.translucent());
        setRenderLayer(FOUNTAIN,                    RenderType.solid());
        setRenderLayer(FOUNTAIN_PRIME_LIQUID,       RenderType.solid(), RenderType.translucent());
        setRenderLayer(FOUNTAIN_PRIME_VORTEX,       RenderType.solid(), RenderType.translucent());
        setRenderLayer(FOUNTAIN_PRIME_ORE,          RenderType.solid(), RenderType.translucent());

        setRenderLayer(FLARE_LIGHT,       RenderType.translucent());
        setRenderLayer(TRANSLUCENT_BLOCK, RenderType.translucent());
        setRenderLayer(VANISHING,         RenderType.translucent());
        setRenderLayer(STRUCTURAL,        RenderType.translucent());
    }

    public static void initFluids() {
        RegistryFluids.FLUID_BLOCKS.forEach((fluidBlock) -> setRenderLayer(fluidBlock, RenderType.translucent()));

        setRenderLayer(LIQUID_STARLIGHT_SOURCE, RenderType.translucent());
        setRenderLayer(LIQUID_STARLIGHT_FLOWING, RenderType.translucent());
    }

    private static void setRenderLayer(Block block, RenderType... types) {
        List<RenderType> typeList = Arrays.asList(types);
        ItemBlockRenderTypes.setRenderLayer(block, typeList::contains);
    }

    private static void setRenderLayer(Fluid fluid, RenderType... types) {
        List<RenderType> typeList = Arrays.asList(types);
        ItemBlockRenderTypes.setRenderLayer(fluid, typeList.get(0));
    }
}
