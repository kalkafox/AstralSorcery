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
import hellfirepvp.astralsorcery.common.container.factory.ContainerTomeProvider;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.base.PerkExperienceRevealer;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.fml.LogicalSide;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemTome
 * Created by HellFirePvP
 * Date: 09.08.2019 / 21:12
 */
public class ItemTome extends Item implements PerkExperienceRevealer {

    public ItemTome() {
        super(new Properties()
                .maxStackSize(1)
                .group(CommonProxy.ITEM_GROUP_AS));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() && !player.isShiftKeyDown()) {
            AstralSorcery.getProxy().openGui(player, GuiType.TOME);
        } else if (!level.isClientSide() && player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer) {
            new ContainerTomeProvider(player.getItemInHand(hand), player.inventory.selected)
                    .openFor((ServerPlayer) player);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState blockstate = level.getBlockState(context.getBlockPos());
        if (blockstate.getBlock() instanceof LecternBlock) {
            return LecternBlock.tryPlaceBook(level, context.getBlockPos(), blockstate, context.getItem()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public static Container getTomeStorage(ItemStack stack, Player player) {
        SimpleContainer inventory = new SimpleContainer(27);
        getStoredConstellations(stack, player).stream().map(cst -> {
            ItemStack cstPaper = new ItemStack(ItemsAS.CONSTELLATION_PAPER);
            if (cstPaper.getItem() instanceof ConstellationBaseItem) {
                ((ConstellationBaseItem) cstPaper.getItem()).setConstellation(cstPaper, cst);
            }
            return cstPaper;
        }).forEach(inventory::addItem);
        return inventory;
    }

    public static List<IConstellation> getStoredConstellations(ItemStack stack, Player player) {
        LinkedList<IConstellation> out = new LinkedList<>();

        PlayerProgress prog = ResearchHelper.getProgress(player, player.getCommandSenderWorld().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        if (prog.isValid()) {
            prog.getStoredConstellationPapers().stream()
                    .map(ConstellationRegistry::getConstellation)
                    .filter(Objects::nonNull)
                    .forEach(out::add);
        }
        return out;
    }
}
