/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.util.item.ItemComparator;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.Util;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.fml.common.thread.EffectiveSide;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AmuletEnchantmentHelper
 * Created by HellFirePvP
 * Date: 11.08.2019 / 20:25
 */
public class AmuletEnchantmentHelper {

    public static final String KEY_AS_OWNER = "AS_Amulet_Holder";

    public static void removeAmuletTagsAndCleanup(Player player, boolean keepEquipped) {
        Inventory inv = player.getItems();
        for (int i = 0; i < inv.items.size(); i++) {
            if (i == inv.selected && keepEquipped) {
                continue;
            }
            removeAmuletOwner(inv.items.get(i));
        }
        removeAmuletOwner(player.containerMenu.getCarried());
        if (!keepEquipped) {
            for (int i = 0; i < inv.itemStack.size(); i++) {
                removeAmuletOwner(inv.itemStack.get(i));
            }
            for (int i = 0; i < inv.offhand.size(); i++) {
                removeAmuletOwner(inv.offhand.get(i));
            }
        }
    }

    @Nonnull
    private static UUID getWornPlayerUUID(ItemStack anyTool) {
        if (DynamicEnchantmentHelper.canHaveDynamicEnchantment(anyTool)) {
            CompoundTag data = NBTHelper.getExistingData(anyTool);
            if (data != null) {
                return NBTHelper.getUUID(data, KEY_AS_OWNER, Util.NIL_UUID);
            }
        }
        return Util.NIL_UUID;
    }

    public static void applyAmuletOwner(ItemStack tool, Player wearer) {
        if (DynamicEnchantmentHelper.canHaveDynamicEnchantment(tool)) {
            NBTHelper.getData(tool).putUUID(KEY_AS_OWNER, wearer.getUUID());
        }
    }

    private static void removeAmuletOwner(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag data = NBTHelper.getExistingData(stack);
        if (data != null) {
            NBTHelper.removeUUID(data, KEY_AS_OWNER);
        }
    }

    @Nullable
    public static Player getPlayerHavingTool(ItemStack anyTool) {
        UUID plUUID = getWornPlayerUUID(anyTool);
        if (plUUID.getLeastSignificantBits() == 0 && plUUID.getMostSignificantBits() == 0) {
            return null;
        }
        Player player;
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            player = resolvePlayerClient(plUUID);
        } else {
            MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            if (server == null) {
                return null;
            }
            player = server.getPlayerList().getPlayer(plUUID);
        }
        if (player == null) {
            return null;
        }

        int originalDamage = anyTool.getDamageValue(); //Save the original damage on the tool

        //Check if the player actually wears/carries the tool
        boolean foundTool = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            anyTool.setDamageValue(stack.getDamageValue()); //Make sure the damages are equal before comparing
            if (ItemComparator.compare(stack, anyTool, ItemComparator.Clause.Sets.ITEMSTACK_STRICT)) {
                foundTool = true;
                break;
            }
        }
        anyTool.setDamageValue(originalDamage); //Reset original damage on the tool
        if (!foundTool) return null;

        return player;
    }

    @Nullable
    static Tuple<ItemStack, Player> getWornAmulet(ItemStack anyTool) {
        Player player = getPlayerHavingTool(anyTool);
        if (player == null) return null;

        // 1.21 port: Curios integration is excluded from the build for now.
        //Optional<ImmutableTriple<String, Integer, ItemStack>> curios =
        //        IntegrationCurios.getCurio(player, (stack) -> stack.getItem() instanceof ItemEnchantmentAmulet);
        //return curios.map(trpl -> new Tuple<>(trpl.right, player)).orElse(null);
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static Player resolvePlayerClient(UUID plUUID) {
        Optional<Level> w = LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT);
        if (w.isEmpty()) {
            return null;
        }
        for (Player player : w.get().players()) {
            if (player.getUUID().equals(plUUID)) {
                return player;
            }
        }
        return null;
    }

}
