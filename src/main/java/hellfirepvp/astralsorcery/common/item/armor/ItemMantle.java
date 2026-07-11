/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.model.armor.ModelArmorMantle;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBaseItem;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectVicio;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.DynamicAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.source.provider.equipment.EquipmentAttributeModifierProvider;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemMantle
 * Created by HellFirePvP
 * Date: 17.02.2020 / 19:09
 */
public class ItemMantle extends ArmorItem implements ItemDynamicColor, ConstellationBaseItem, EquipmentAttributeModifierProvider, AlignmentChargeConsumer {

    private static final UUID MODIFIER_ID = UUID.fromString("aae54b9d-e1c8-4e74-8ac6-efa06093bd1a");
    private static final CacheReference<DynamicAttributeModifier> MINING_SIZE_MODIFIER =
            new CacheReference<>(() -> new DynamicAttributeModifier(MODIFIER_ID, PerkAttributeTypesAS.ATTR_TYPE_MINING_SIZE, ModifierType.ADDITION, 2F));

    private static Object outerModel = null;

    public ItemMantle() {
        // 1.21 port: Holder.direct because items are constructed before registry
        // events fire; the material is also properly registered in CommonProxy.
        super(Holder.direct(CommonProxy.ARMOR_MATERIAL_IMBUED_LEATHER),
                Type.CHESTPLATE,
                new Properties()
                    .stacksTo(1)
                    .durability(486)
        );
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));
            for (IConstellation cst : RegistriesAS.REGISTRY_CONSTELLATIONS.getValues()) {
                if (!(cst instanceof IWeakConstellation)) {
                    continue;
                }

                ItemStack stack = new ItemStack(this);
                this.setConstellation(stack, cst);
                items.add(stack);
            }
        }
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        return MantleEffectVicio.isUsableElytra(stack, (Player) entity);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!(entity instanceof Player)) {
            return false;
        }
        return MantleEffectVicio.isUsableElytra(stack, (Player) entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        IConstellation cst = this.getConstellation(stack);
        if (cst instanceof IWeakConstellation) {
            tooltip.add(cst.getConstellationName().withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public Collection<PerkAttributeModifier> getModifiers(ItemStack stack, Player player, LogicalSide direction, boolean ignoreRequirements) {
        if (ItemMantle.getEffect(stack, ConstellationsAS.evorsio) == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(MINING_SIZE_MODIFIER.get());
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFF;
        }
        IConstellation cst = getConstellation(stack);
        if (cst != null) {
            Color c = cst.getConstellationColor();
            return 0xFF000000 | c.getRGB();
        }
        return 0xFF000000;
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public <A extends HumanoidModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, A _default) {
        if (outerModel == null) {
            outerModel = new ModelArmorMantle();
        }
        return (A) outerModel;
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public String getTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return AstralSorcery.key("textures/model/armor/mantle.png").toString();
    }

    @Nullable
    public static <V extends MantleEffect> V getEffect(@Nullable LivingEntity entity) {
        return getEffect(entity, null);
    }

    @Nullable
    public static <V extends MantleEffect> V getEffect(@Nullable LivingEntity entity, @Nullable IWeakConstellation expected) {
        if (entity == null) {
            return null;
        }
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMantle)) {
            return null;
        }
        return getEffect(stack, expected);
    }

    @Nullable
    public static <V extends MantleEffect> V getEffect(@Nonnull ItemStack stack, @Nullable IWeakConstellation expected) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMantle)) {
            return null;
        }
        IConstellation cst = ((ItemMantle) stack.getItem()).getConstellation(stack);
        if (!(cst instanceof IWeakConstellation)) {
            return null;
        }
        if (expected != null && !expected.equals(cst)) {
            return null;
        }
        MantleEffect effect = ((IWeakConstellation) cst).getMantleEffect();
        if (effect == null || !effect.getConfig().enabled.get()) {
            return null;
        }
        return (V) effect;
    }

    @Nullable
    @Override
    public IConstellation getConstellation(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return IConstellation.readFromNBT(NBTHelper.getPersistentData(stack), IConstellation.getDefaultSaveKey());
    }

    @Override
    public boolean setConstellation(ItemStack stack, @Nullable IConstellation cst) {
        if (stack.isEmpty()) {
            return false;
        }
        if (cst == null) {
            NBTHelper.getPersistentData(stack).remove(IConstellation.getDefaultSaveKey());
        } else {
            cst.save(NBTHelper.getPersistentData(stack), IConstellation.getDefaultSaveKey());
        }
        return true;
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        return 0;
    }
}
