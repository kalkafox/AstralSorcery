/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBaseItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.entity.item.EntityItemExplosionResistant;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemConstellationPaper
 * Created by HellFirePvP
 * Date: 21.07.2019 / 14:47
 */
public class ItemConstellationPaper extends Item implements ItemDynamicColor, ConstellationBaseItem {

    public ItemConstellationPaper() {
        super(new Properties()
                .maxStackSize(1)
                .group(CommonProxy.ITEM_GROUP_AS_PAPERS));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this, 1));

            for (IConstellation c : ConstellationRegistry.getAllConstellations()) {
                ItemStack cPaper = new ItemStack(this, 1);
                setConstellation(cPaper, c);
                items.add(cPaper);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> toolTip, TooltipFlag flag) {
        IConstellation c = getConstellation(stack);
        if (c != null && c.canDiscover(Minecraft.getInstance().player, ResearchHelper.getClientProgress())) {
            toolTip.add(c.getConstellationName().withStyle(ChatFormatting.BLUE));
        } else {
            toolTip.add(Component.translatable("astralsorcery.misc.noinformation").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return InteractionResultHolder.success(held);
        }
        if (level.isClientSide() && getConstellation(held) != null) {
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            AstralSorcery.getProxy().openGui(player, GuiType.CONSTELLATION_PAPER, getConstellation(held));
        }
        return InteractionResultHolder.success(held);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack itemstack) {
        EntityItemExplosionResistant res = new EntityItemExplosionResistant(EntityTypesAS.ITEM_EXPLOSION_RESISTANT, level, location.getX(), location.getY(), location.getZ(), itemstack);
        res.read(location.writeWithoutTypeId(new CompoundTag()));
        if (itemstack.getItem() instanceof ItemConstellationPaper) {
            IConstellation cst = getConstellation(itemstack);
            if (cst != null) {
                res.applyColor(cst.getConstellationColor());
            }
        }
        if (location instanceof ItemEntity) {
            res.setReplacedEntity((ItemEntity) location);
        }
        return res;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player)) {
            return;
        }

        IConstellation cst = getConstellation(stack);
        if (cst == null) {
            PlayerProgress progress = ResearchHelper.getProgress((Player) entity, LogicalSide.SERVER);

            List<IConstellation> constellations = new ArrayList<>();
            for (IConstellation c : ConstellationRegistry.getAllConstellations()) {
                if (c.canDiscover((Player) entity, progress)) {
                    constellations.add(c);
                }
            }

            for (ResourceLocation strConstellation : progress.getKnownConstellations()) {
                IConstellation c = ConstellationRegistry.getConstellation(strConstellation);
                if (c != null) {
                    constellations.remove(c);
                }
            }
            for (ResourceLocation strConstellation : progress.getSeenConstellations()) {
                IConstellation c = ConstellationRegistry.getConstellation(strConstellation);
                if (c != null) {
                    constellations.remove(c);
                }
            }

            IConstellation constellation = MiscUtils.getRandomEntry(constellations, level.random);
            if (constellation != null) {
                setConstellation(stack, constellation);
            }
        }


        cst = getConstellation(stack);
        if (cst != null) {
            PlayerProgress progress = ResearchHelper.getProgress((Player) entity, LogicalSide.SERVER);

            boolean has = false;
            for (ResourceLocation strConstellation : progress.getSeenConstellations()) {
                IConstellation c = ConstellationRegistry.getConstellation(strConstellation);
                if (c != null && c.equals(cst)) {
                    has = true;
                    break;
                }
            }
            if (!has) {
                if (cst.canDiscover((Player) entity, progress) && ResearchManager.memorizeConstellation(cst, (Player) entity)) {
                    ResearchHelper.sendConstellationMemorizationMessage(entity, progress, cst);
                }
            }
        }

        super.inventoryTick(stack, level, entity, slot, isSelected);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        IConstellation c = getConstellation(stack);
        if (c != null) {
            if (ResearchHelper.getClientProgress().hasConstellationDiscovered(c)) {
                return 0xFF000000 | c.getConstellationColor().getRGB();
            }
        }
        return 0xFF595959;
    }

    @Nullable
    public IConstellation getConstellation(ItemStack stack) {
        return IConstellation.readFromNBT(NBTHelper.getPersistentData(stack));
    }

    public boolean setConstellation(ItemStack stack, @Nullable IConstellation constellation) {
        constellation.save(NBTHelper.getPersistentData(stack));
        return true;
    }
}
