/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WRItemObject
 * Created by HellFirePvP
 * Date: 07.05.2016 / 15:20
 */
public class WRItemObject<T> implements WeightedEntry {

    private final Weight weight;
    private final T object;

    public WRItemObject(int itemWeightIn, T value) {
        this.weight = Weight.of(itemWeightIn);
        this.object = value;
    }

    @Override
    public Weight getWeight() {
        return this.weight;
    }

    public T getValue() {
        return object;
    }

}
