/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
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

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z) {
        super(type, level, x, y, z);
    }

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z, ItemStack stack) {
        super(type, level, x, y, z, stack);
    }

    public static EntityType.EntityFactory<EntityDazzlingGem> factoryGem() {
        return (spawnEntity, level) -> new EntityDazzlingGem(EntityTypesAS.ITEM_CRYSTAL, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && this.getAge() + 10 >= this.lifespan) {
            ReflectionHelper.setItemEntityAge(this, 0);
        }
    }
}
