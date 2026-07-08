/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.entity.item.EntityStarmetal;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemStarmetalIngot
 * Created by HellFirePvP
 * Date: 21.07.2019 / 12:24
 */
public class ItemStarmetalIngot extends Item {

    public ItemStarmetalIngot() {
        super(new Properties()
                .group(CommonProxy.ITEM_GROUP_AS));
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        EntityStarmetal res = new EntityStarmetal(EntityTypesAS.ITEM_STARMETAL_INGOT, world, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
        res.read(location.writeWithoutTypeId(new CompoundTag()));
        if (location instanceof ItemEntity) {
            res.setReplacedEntity((ItemEntity) location);
        }
        return res;
    }
}
