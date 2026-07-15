/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EntityCustomItemReplacement extends ItemEntity {

    @Nullable
    private ItemEntity replacedEntity;

    public EntityCustomItemReplacement(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityCustomItemReplacement(Level worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
    }

    public void setReplacedEntity(@Nullable ItemEntity replacedEntity) {
        this.replacedEntity = replacedEntity;
    }

    @Override
    public void tick() {
        super.tick();

        if (getCommandSenderWorld().isClientSide()) {
            return;
        }

        //If the replaced item seems to be a fake-item, remove this one as well.
        //See ItemEntity#makeFakeItem
        if (this.replacedEntity != null &&
                this.tickCount < 5 &&
                !this.replacedEntity.isAlive() &&
                ReflectionHelper.getItemEntityPickupDelay(this.replacedEntity) == Short.MAX_VALUE &&
                replacedEntity.getAge() == getItem().getEntityLifespan(getCommandSenderWorld()) - 1) {
            this.remove(RemovalReason.DISCARDED);
        }
    }
}
