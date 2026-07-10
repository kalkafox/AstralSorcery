/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.entity.InteractableEntity;
import hellfirepvp.astralsorcery.common.item.ItemChisel;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
 * Class: EntityCrystal
 * Created by HellFirePvP
 * Date: 21.08.2019 / 21:57
 */
public class EntityCrystal extends EntityItemExplosionResistant implements InteractableEntity {

    public EntityCrystal(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityCrystal(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z) {
        super(type, level, x, y, z);
    }

    public EntityCrystal(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z, ItemStack stack) {
        super(type, level, x, y, z, stack);
    }

    public static EntityType.IFactory<EntityCrystal> factoryCrystal() {
        return (spawnEntity, level) -> new EntityCrystal(EntityTypesAS.ITEM_CRYSTAL, level);
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
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemCrystalBase) {

                    CrystalAttributes thisAttributes = ((ItemCrystalBase) thisStack.getItem()).getAttributes(thisStack);
                    if (thisAttributes != null) {

                        //TODO chipping sound ?
                        boolean doDamage = false;
                        if (random.nextFloat() < 0.35F) {
                            int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, held);
                            doDamage = this.splitCrystal(thisAttributes, fortuneLevel);
                        }
                        if (doDamage || random.nextFloat() < 0.35F) {
                            held.damageItem(1, (Player) entity, (player) -> player.sendBreakAnimation(InteractionHand.MAIN_HAND));
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean splitCrystal(CrystalAttributes thisAttributes, int fortuneLevel) {
        ItemCrystalBase newBase = ((ItemCrystalBase) this.getItem().getItem()).getInertDuplicateItem();
        if (newBase == null) {
            return false;
        }
        ItemStack created = new ItemStack(newBase);
        if (created.isEmpty()) {
            return false;
        }
        int maxSplit = Mth.ceil(thisAttributes.getTotalTierLevel() / 2F);
        if (maxSplit >= thisAttributes.getTotalTierLevel()) {
            return false;
        }

        int lostModifiers = 0;
        if (maxSplit > 1 && random.nextFloat() < (0.6F / (fortuneLevel + 1))) {
            lostModifiers++;
            if (maxSplit > 2 && random.nextFloat() < (0.2F / (fortuneLevel + 1))) {
                lostModifiers++;
            }
        }

        CrystalAttributes resultThisAttributes = thisAttributes;
        CrystalAttributes.Builder resultSplitAttributes = CrystalAttributes.Builder.properties(false);
        for (int i = 0; i < maxSplit; i++) {
            CrystalProperty prop = MiscUtils.getRandomEntry(resultThisAttributes.getProperties(), random);
            if (prop == null) {
                break;
            }
            resultThisAttributes = resultThisAttributes.modifyLevel(prop, -1);
            if (lostModifiers > 0) {
                lostModifiers--;
            } else {
                resultSplitAttributes.addProperty(prop, 1);
            }
        }

        ((ItemCrystalBase) this.getItem().getItem()).setAttributes(this.getItem(), resultThisAttributes);
        newBase.setAttributes(created, resultSplitAttributes.build());
        ItemUtils.dropItemNaturally(getCommandSenderWorld(), this.getX(), this.getY() + 0.25F, this.getZ(), created);
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide() && this.age + 10 >= this.timeout) {
            this.age = 0;
        }
    }
}
