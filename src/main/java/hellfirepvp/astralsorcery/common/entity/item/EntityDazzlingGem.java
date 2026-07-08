/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityDazzlingGem
 * Created by HellFirePvP
 * Date: 01.01.2021 / 14:19
 */
public class EntityDazzlingGem extends EntityItemExplosionResistant {

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level world) {
        super(type, world);
    }

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level world, double x, double y, double z) {
        super(type, world, x, y, z);
    }

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level world, double x, double y, double z, ItemStack stack) {
        super(type, world, x, y, z, stack);
    }

    public static EntityType.IFactory<EntityDazzlingGem> factoryGem() {
        return (spawnEntity, world) -> new EntityDazzlingGem(EntityTypesAS.ITEM_CRYSTAL, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote() && this.age + 10 >= this.lifespan) {
            this.age = 0;
        }
    }
}
