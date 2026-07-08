/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravedStarMap;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemInfusedGlass
 * Created by HellFirePvP
 * Date: 01.05.2020 / 07:29
 */
public class ItemInfusedGlass extends Item {

    public ItemInfusedGlass() {
        super(new Properties()
                .stacksTo(1)
                .durability(5));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        EngravedStarMap map = getEngraving(stack);
        if (map != null) {
            for (ResourceLocation key : map.getConstellationKeys()) {
                IConstellation cst = ConstellationRegistry.getConstellation(key);
                if (cst != null) {
                    String format = "item.astralsorcery.infused_glass.ttip";
                    Component cstName = cst.getConstellationName().withStyle(ChatFormatting.BLUE);

                    if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
                        String percent = String.valueOf(Math.round(map.getDistribution(cst) * 100F));
                        Component creativeHint = Component.translatable("item.astralsorcery.infused_glass.ttip.creative", percent)
                                .withStyle(ChatFormatting.LIGHT_PURPLE);

                        tooltip.add(Component.translatable(format, cstName, creativeHint).withStyle(ChatFormatting.GRAY));
                    } else {
                        tooltip.add(Component.translatable(format, cstName, "").withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.MENDING)) {
            return false;
        }
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || getEngraving(stack) != null;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        EngravedStarMap map = getEngraving(stack);
        if (map != null) {
            return super.getDescriptionId(stack) + ".active";
        }
        return super.getDescriptionId(stack);
    }

    @Nullable
    public static EngravedStarMap getEngraving(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemInfusedGlass)) {
            return null;
        }

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("starmap", Constants.NBT.TAG_COMPOUND)) {
            return EngravedStarMap.deserialize(tag.getCompound("starmap"));
        }
        return null;
    }

    public static void setEngraving(@Nonnull ItemStack stack, @Nullable EngravedStarMap map) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemInfusedGlass)) {
            return;
        }

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (map == null) {
            tag.remove("starmap");
        } else {
            tag.put("starmap", map.serialize());
        }
    }

}
