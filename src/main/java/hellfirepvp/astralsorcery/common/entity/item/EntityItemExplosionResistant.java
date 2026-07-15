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
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityItemExplosionResistant
 * Created by HellFirePvP
 * Date: 18.08.2019 / 11:22
 */
public class EntityItemExplosionResistant extends EntityItemHighlighted {

    public EntityItemExplosionResistant(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityItemExplosionResistant(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z) {
        super(type, level, x, y, z);
    }

    public EntityItemExplosionResistant(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z, ItemStack stack) {
        super(type, level, x, y, z, stack);
    }

    public static EntityType.EntityFactory<EntityItemExplosionResistant> factoryExplosionResistant() {
        return (spawnEntity, level) -> new EntityItemExplosionResistant(EntityTypesAS.ITEM_EXPLOSION_RESISTANT, level);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.hurt(source, amount);
    }
}
