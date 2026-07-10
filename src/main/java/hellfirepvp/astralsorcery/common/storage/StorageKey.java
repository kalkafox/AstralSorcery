/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StorageKey
 * Created by HellFirePvP
 * Date: 30.05.2019 / 14:45
 */
public class StorageKey {

    @Nonnull
    private final ItemStack stack;

    private StorageKey(@Nonnull ItemStack stack) {
        this.stack = stack;
    }

    public static StorageKey from(ItemStack stack) {
        return new StorageKey(stack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageKey that = (StorageKey) o;
        Item thisItem = this.stack.getItem();
        Item thatItem = that.stack.getItem();
        return Objects.equals(RegistryHelper.getKey(thisItem), RegistryHelper.getKey(thatItem));
    }

    @Override
    public int hashCode() {
        return Objects.hash(RegistryHelper.getKey(stack.getItem()));
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag keyTag = new CompoundTag();
        keyTag.putString("name", RegistryHelper.getKey(stack.getItem()).toString());
        return keyTag;
    }

    //If the item in question does no longer exist in the registry, return null.
    @Nullable
    public static StorageKey deserialize(CompoundTag nbt) {
        ResourceLocation rl = ResourceLocation.parse(nbt.getString("name"));
        Item i = BuiltInRegistries.ITEM.get(rl);
        if (i == null || i == Items.AIR) {
            return null;
        }
        return new StorageKey(new ItemStack(i));
    }
}
