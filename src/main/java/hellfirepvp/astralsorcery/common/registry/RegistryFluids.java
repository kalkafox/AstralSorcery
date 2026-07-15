/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.fluid.BlockLiquidStarlight;
import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import hellfirepvp.astralsorcery.common.fluid.ItemLiquidStarlightBucket;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.LinkedList;
import java.util.List;

import static hellfirepvp.astralsorcery.common.lib.FluidsAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryFluids
 * Created by HellFirePvP
 * Date: 20.09.2019 / 21:53
 */
public class RegistryFluids {

    static final List<Block> FLUID_BLOCKS = new LinkedList<>();
    static final List<Item> FLUID_HOLDER_ITEMS = new LinkedList<>();

    private RegistryFluids() {}

    public static void registerFluids() {
        FluidsAS.LIQUID_STARLIGHT_FLUID_TYPE = AstralRegistries.register(
                AstralRegistries.FLUID_TYPES,
                AstralSorcery.key("liquid_starlight"),
                new FluidType(FluidType.Properties.create()
                        .rarity(Rarity.EPIC)
                        .lightLevel(15)
                        .density(1001)
                        .viscosity(300)
                        .temperature(40)));

        FluidsAS.LIQUID_STARLIGHT_PROPERTIES = new BaseFlowingFluid.Properties(
                () -> LIQUID_STARLIGHT_FLUID_TYPE,
                () -> LIQUID_STARLIGHT_SOURCE,
                () -> LIQUID_STARLIGHT_FLOWING)
                .block(() -> BlocksAS.FLUID_LIQUID_STARLIGHT)
                .bucket(() -> ItemsAS.BUCKET_LIQUID_STARLIGHT);

        FluidsAS.LIQUID_STARLIGHT_SOURCE = registerFluid(new FluidLiquidStarlight.Source(LIQUID_STARLIGHT_PROPERTIES));
        FluidsAS.LIQUID_STARLIGHT_FLOWING = registerFluid(new FluidLiquidStarlight.Flowing(LIQUID_STARLIGHT_PROPERTIES));

        FLUID_BLOCKS.add(BlocksAS.FLUID_LIQUID_STARLIGHT = new BlockLiquidStarlight(LIQUID_STARLIGHT_SOURCE));
        FLUID_HOLDER_ITEMS.add(ItemsAS.BUCKET_LIQUID_STARLIGHT = new ItemLiquidStarlightBucket(() -> LIQUID_STARLIGHT_SOURCE));
    }

    private static <T extends Fluid> T registerFluid(T fluid) {
        return registerFluid(fluid, NameUtil.fromClass(fluid, "Fluid", "Source"));
    }

    private static <T extends Fluid> T registerFluid(T fluid, ResourceLocation name) {
        return AstralRegistries.register(AstralRegistries.FLUIDS, name, fluid);
    }
}
