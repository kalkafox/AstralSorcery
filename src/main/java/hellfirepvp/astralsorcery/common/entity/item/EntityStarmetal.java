/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.entity.InteractableEntity;
import hellfirepvp.astralsorcery.common.item.ItemChisel;
import hellfirepvp.astralsorcery.common.item.ItemStarmetalIngot;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityStarmetal
 * Created by HellFirePvP
 * Date: 17.05.2020 / 10:08
 */
public class EntityStarmetal extends EntityCustomItemReplacement implements InteractableEntity {

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        ReflectionHelper.setSkipItemPhysicsRender(this);
        refreshDimensions();
    }

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z) {
        this(type, level);
        this.setPosition(x, y, z);
        this.setYRot(this.random.nextFloat() * 360.0F);
        this.setDeltaMovement(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
    }

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z, ItemStack stack) {
        this(type, level, x, y, z);
        this.setItem(stack);
        this.timeout = stack.isEmpty() ? 6000 : stack.getEntityLifespan(level);
    }

    public static EntityType.IFactory<EntityStarmetal> factoryStarmetalIngot() {
        return (spawnEntity, level) -> new EntityStarmetal(EntityTypesAS.ITEM_STARMETAL_INGOT, level);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (!this.getCommandSenderWorld().isClientSide() && entity instanceof ServerPlayer) {
            ItemStack held = ((ServerPlayer) entity).getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty() && held.getItem() instanceof ItemChisel) {

                ItemStack thisStack = this.getItem();
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemStarmetalIngot) {

                    boolean doDamage = false;
                    if (random.nextFloat() < 0.4F) {
                        int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, held);
                        doDamage = this.createStardust(fortuneLevel);
                    }
                    if (doDamage || random.nextFloat() < 0.35F) {
                        held.damageItem(1, (Player) entity, (player) -> player.sendBreakAnimation(InteractionHand.MAIN_HAND));
                    }
                }
            }
        }
        return true;
    }

    private boolean createStardust(int fortuneLevel) {
        ItemStack created = new ItemStack(ItemsAS.STARDUST);
        ItemUtils.dropItemNaturally(getCommandSenderWorld(), this.getX(), this.getY() + 0.25F, this.getZ(), created);

        float breakIngot = 0.90F;
        breakIngot -= Mth.clamp(fortuneLevel, 0, 10) * 0.06F;
        if (random.nextFloat() < breakIngot) {
            ItemStack thisStack = this.getItem();
            thisStack.shrink(1);
            this.setItem(thisStack);
        }
        return true;
    }

    @Override
    public void tick() {
        boolean onGround = this.onGround();
        super.tick();
        if (this.onGround() != onGround) {
            refreshDimensions();
        }
    }

    @Override
    public void setOnGround(boolean grounded) {
        boolean updateSize = isOnGround() != grounded;
        super.setOnGround(grounded);
        if (updateSize) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getSize(Pose poseIn) {
        if (!this.onGround()) {
            return EntityType.ITEM.getSize();
        }
        return this.getType().getSize();
    }
}
