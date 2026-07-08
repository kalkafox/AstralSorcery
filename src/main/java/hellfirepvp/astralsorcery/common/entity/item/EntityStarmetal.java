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
import net.neoforged.fml.network.NetworkHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityStarmetal
 * Created by HellFirePvP
 * Date: 17.05.2020 / 10:08
 */
public class EntityStarmetal extends EntityCustomItemReplacement implements InteractableEntity {

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level world) {
        super(type, world);
        ReflectionHelper.setSkipItemPhysicsRender(this);
        recalculateSize();
    }

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level world, double x, double y, double z) {
        this(type, world);
        this.setPosition(x, y, z);
        this.rotationYaw = this.rand.nextFloat() * 360.0F;
        this.setMotion(this.rand.nextDouble() * 0.2D - 0.1D, 0.2D, this.rand.nextDouble() * 0.2D - 0.1D);
    }

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level world, double x, double y, double z, ItemStack stack) {
        this(type, world, x, y, z);
        this.setItem(stack);
        this.lifespan = stack.isEmpty() ? 6000 : stack.getEntityLifespan(world);
    }

    public static EntityType.IFactory<EntityStarmetal> factoryStarmetalIngot() {
        return (spawnEntity, world) -> new EntityStarmetal(EntityTypesAS.ITEM_STARMETAL_INGOT, world);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        if (!this.getEntityWorld().isRemote() && entity instanceof ServerPlayer) {
            ItemStack held = ((ServerPlayer) entity).getHeldItem(Hand.MAIN_HAND);
            if (!held.isEmpty() && held.getItem() instanceof ItemChisel) {

                ItemStack thisStack = this.getItem();
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemStarmetalIngot) {

                    boolean doDamage = false;
                    if (rand.nextFloat() < 0.4F) {
                        int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, held);
                        doDamage = this.createStardust(fortuneLevel);
                    }
                    if (doDamage || rand.nextFloat() < 0.35F) {
                        held.damageItem(1, (Player) entity, (player) -> player.sendBreakAnimation(Hand.MAIN_HAND));
                    }
                }
            }
        }
        return true;
    }

    private boolean createStardust(int fortuneLevel) {
        ItemStack created = new ItemStack(ItemsAS.STARDUST);
        ItemUtils.dropItemNaturally(getEntityWorld(), this.getPosX(), this.getPosY() + 0.25F, this.getPosZ(), created);

        float breakIngot = 0.90F;
        breakIngot -= MathHelper.clamp(fortuneLevel, 0, 10) * 0.06F;
        if (rand.nextFloat() < breakIngot) {
            ItemStack thisStack = this.getItem();
            thisStack.shrink(1);
            this.setItem(thisStack);
        }
        return true;
    }

    @Override
    public void tick() {
        boolean onGround = this.isOnGround();
        super.tick();
        if (this.isOnGround() != onGround) {
            recalculateSize();
        }
    }

    @Override
    public void setOnGround(boolean grounded) {
        boolean updateSize = isOnGround() != grounded;
        super.setOnGround(grounded);
        if (updateSize) {
            recalculateSize();
        }
    }

    @Override
    public EntityDimensions getSize(Pose poseIn) {
        if (!this.isOnGround()) {
            return EntityType.ITEM.getSize();
        }
        return this.getType().getSize();
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
