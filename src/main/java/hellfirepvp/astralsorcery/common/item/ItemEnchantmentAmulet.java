/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletRandomizeHelper;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.*;
import net.minecraft.ChatFormatting;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemEnchantmentAmulet
 * Created by HellFirePvP
 * Date: 11.08.2019 / 20:07
 */
public class ItemEnchantmentAmulet extends Item implements ItemDynamicColor {

    private static final Random random = new Random();

    public ItemEnchantmentAmulet() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        List<AmuletEnchantment> enchantments = getAmuletEnchantments(stack);
        for (AmuletEnchantment ench : enchantments) {
            tooltip.add(ench.getDisplay().withStyle(ChatFormatting.BLUE));
        }

        if (getAmuletColor(stack).map(color -> color == 0xFFFFFFFF).orElse(false)) {
            tooltip.add(Component.translatable("astralsorcery.amulet.color.colorless")
                    .withStyle(ChatFormatting.ITALIC)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isClientSide() && !getAmuletColor(stack).isPresent()) {
            freezeAmuletColor(stack);
        }
        if (!worldIn.isClientSide() && getAmuletEnchantments(stack).isEmpty()) {
            AmuletRandomizeHelper.rollAmulet(stack);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        Optional<Integer> color = getAmuletColor(stack);
        if (color.isPresent()) {
            return color.get();
        }
        int tick = (int) (ClientScheduler.getClientTick() % 500000L);
        int c = Color.getHSBColor(tick / 500000F, 0.7F, 1F).getRGB();
        return c | 0xFF000000;
    }

    public static Optional<Integer> getAmuletColor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return Optional.empty();
        }
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (!tag.contains("amuletColor")) {
            return Optional.empty();
        }
        return Optional.of(tag.getInt("amuletColor"));
    }

    public static void freezeAmuletColor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return;
        }
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("amuletColor")) {
            return;
        }
        if (random.nextInt(400) == 0) {
            tag.putInt("amuletColor", 0xFFFFFFFF);
        } else {
            tag.putInt("amuletColor", Color.getHSBColor(random.nextFloat(), 0.7F, 1.0F).getRGB() | 0xFF000000);
        }
    }

    public static List<AmuletEnchantment> getAmuletEnchantments(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return Lists.newArrayList();
        }

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (!tag.contains("amuletEnchantments")) {
            return Lists.newArrayList();
        }
        ListTag enchants = tag.getList("amuletEnchantments", Constants.NBT.TAG_COMPOUND);
        List<AmuletEnchantment> enchantments = new ArrayList<>(enchants.size());
        for (int i = 0; i < enchants.size(); i++) {
            AmuletEnchantment ench = AmuletEnchantment.deserialize(enchants.getCompound(i));
            if (ench != null) {
                enchantments.add(ench);
            }
        }
        enchantments.sort(Comparator.comparing(AmuletEnchantment::getType));
        return enchantments;
    }

    public static void setAmuletEnchantments(ItemStack stack, List<AmuletEnchantment> enchantments) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return;
        }
        enchantments.sort(Comparator.comparing(AmuletEnchantment::getType));

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        ListTag enchants = new ListTag();
        for (AmuletEnchantment enchant : enchantments) {
            enchants.add(enchant.serialize());
        }
        tag.put("amuletEnchantments", enchants);
    }
}
