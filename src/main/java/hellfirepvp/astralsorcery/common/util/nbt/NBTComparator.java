/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTComparator
 * Created by HellFirePvP
 * Date: 28.03.2019 / 19:37
 */
public class NBTComparator {

    public static boolean contains(@Nonnull CompoundTag thisCompound, @Nonnull CompoundTag otherCompound) {
        for (String key : thisCompound.getAllKeys()) {
            if (!otherCompound.contains(key)) {
                return false;
            }

            Tag thisNBT = thisCompound.get(key);
            Tag otherNBT = otherCompound.get(key);
            if (!compare(thisNBT, otherNBT)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containList(ListTag base, ListTag other) {
        if (base.size() > other.size()) {
            return false;
        }

        List<Integer> matched = new ArrayList<>();
        lblMatching:
        for (Tag thisNbt : base) {
            for (int matchIndex = 0; matchIndex < other.size(); matchIndex++) {
                Tag matchNBT = other.get(matchIndex);

                if (!matched.contains(matchIndex)) {
                    if (compare(thisNbt, matchNBT)) {
                        matched.add(matchIndex);
                        continue lblMatching;
                    }
                }
            }

            return false;
        }

        return true;
    }

    private static boolean compare(Tag thisEntry, Tag thatEntry) {
        if (thisEntry instanceof CompoundTag && thatEntry instanceof CompoundTag) {
            return contains((CompoundTag) thisEntry, (CompoundTag) thatEntry);
        } else if (thisEntry instanceof ListTag && thatEntry instanceof ListTag) {
            return containList((ListTag) thisEntry, (ListTag) thatEntry);
        } else {
            return thisEntry.equals(thatEntry);
        }
    }

}
