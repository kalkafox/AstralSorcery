/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidIngredient
 * Created by HellFirePvP
 * Date: 30.05.2019 / 17:27
 */
public class FluidIngredient implements ICustomIngredient {

    public static final MapCodec<FluidIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FluidStack.CODEC.listOf().fieldOf("fluid").forGetter(FluidIngredient::getFluids)
    ).apply(inst, FluidIngredient::new));

    private final List<FluidStack> fluids;

    public FluidIngredient(List<FluidStack> fluidStacks) {
        this.fluids = fluidStacks;
    }

    public FluidIngredient(FluidStack... fluidStacks) {
        this.fluids = Arrays.asList(fluidStacks);
    }

    public List<FluidStack> getFluids() {
        return fluids;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return this.fluids.stream().map(FluidUtil::getFilledBucket);
    }

    @Override
    public boolean test(ItemStack from) {
        if (from == null || from.isEmpty()) {
            return false;
        }

        FluidStack contained = FluidUtil.getFluidContained(from).orElse(FluidStack.EMPTY);
        if (contained.isEmpty()) {
            return false;
        }

        for (FluidStack target : this.fluids) {
            if (FluidStack.isSameFluidSameComponents(contained, target) && contained.getAmount() >= target.getAmount()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return IngredientSerializersAS.FLUID_INGREDIENT_TYPE;
    }
}
