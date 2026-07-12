/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CrystalIngredient
 * Created by HellFirePvP
 * Date: 28.09.2019 / 10:03
 */
public class CrystalIngredient implements ICustomIngredient {

    public static final MapCodec<CrystalIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("hasToBeAttuned", false).forGetter(CrystalIngredient::hasToBeAttuned),
            Codec.BOOL.optionalFieldOf("hasToBeCelestial", false).forGetter(CrystalIngredient::hasToBeCelestial),
            Codec.BOOL.optionalFieldOf("canBeAttuned", true).forGetter(CrystalIngredient::canBeAttuned),
            Codec.BOOL.optionalFieldOf("canBeCelestialCrystal", true).forGetter(CrystalIngredient::canBeCelestialCrystal)
    ).apply(inst, CrystalIngredient::new));

    private final boolean hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestialCrystal;

    public CrystalIngredient(boolean hasToBeAttuned, boolean hasToBeCelestial) {
        this(hasToBeAttuned, hasToBeCelestial, true, true);
    }

    public CrystalIngredient(boolean hasToBeAttuned, boolean hasToBeCelestial, boolean canBeAttuned, boolean canBeCelestialCrystal) {
        this.hasToBeAttuned = hasToBeAttuned;
        this.hasToBeCelestial = hasToBeCelestial;
        this.canBeAttuned = canBeAttuned;
        this.canBeCelestialCrystal = canBeCelestialCrystal;
    }

    private List<ItemStack> getMatchingStacks() {
        boolean canBeAttuned = this.canBeAttuned;
        boolean canBeCelestialCrystal = this.canBeCelestialCrystal;
        if (this.hasToBeAttuned) {
            canBeAttuned = true;
        }
        if (this.hasToBeCelestial) {
            canBeCelestialCrystal = true;
        }

        List<ItemStack> stacks = new ArrayList<>();
        if (this.hasToBeAttuned) {
            if (this.hasToBeCelestial) {
                stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL));
            } else {
                stacks.add(new ItemStack(ItemsAS.ATTUNED_ROCK_CRYSTAL));
                if (canBeCelestialCrystal) {
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL));
                }
            }
        } else {
            if (this.hasToBeCelestial) {
                stacks.add(new ItemStack(ItemsAS.CELESTIAL_CRYSTAL));
                if (canBeAttuned) {
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL));
                }
            } else {
                stacks.add(new ItemStack(ItemsAS.ROCK_CRYSTAL));
                if (canBeCelestialCrystal) {
                    stacks.add(new ItemStack(ItemsAS.CELESTIAL_CRYSTAL));
                }
                if (canBeAttuned) {
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_ROCK_CRYSTAL));
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL));
                }
            }
        }
        return stacks;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return this.getMatchingStacks().stream();
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return this.getMatchingStacks().stream().anyMatch(match -> match.getItem() == stack.getItem());
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return IngredientSerializersAS.CRYSTAL_INGREDIENT_TYPE;
    }

    public boolean hasToBeAttuned() {
        return hasToBeAttuned;
    }

    public boolean hasToBeCelestial() {
        return hasToBeCelestial;
    }

    public boolean canBeAttuned() {
        return canBeAttuned;
    }

    public boolean canBeCelestialCrystal() {
        return canBeCelestialCrystal;
    }
}
