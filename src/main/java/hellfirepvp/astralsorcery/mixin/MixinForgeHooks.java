/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyMagnetDrops;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.loot.LootUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinForgeHooks
 * Created by HellFirePvP
 * Date: 01.01.2022 / 10:06
 */
@Mixin(CommonHooks.class)
public class MixinForgeHooks {

    @Inject(
            method = "modifyLoot(Lnet/minecraft/resources/ResourceLocation;Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void runLootTeleportation(ResourceLocation lootTableId, ObjectArrayList<ItemStack> lootTable, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        List<ItemStack> loot = cir.getReturnValue();

        if (!LootUtil.doesContextFulfillSet(context, LootContextParamSets.BLOCK)) {
            return;
        }
        Entity e = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(e instanceof Player)) {
            return;
        }
        Player player = (Player) e;
        PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!prog.isValid() || !prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyMagnetDrops)) {
            return;
        }

        //Means we're in the 2nd run of loot manipulation, re-run by top.theillusivec4.curios.common.objects.FortuneBonusMultiplier
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        CustomData toolData = tool == null ? null : tool.get(DataComponents.CUSTOM_DATA);
        if (toolData != null && toolData.getUnsafe().contains("HasCuriosFortuneBonus")) {
            loot.removeIf(result -> ItemUtils.dropItemToPlayer(player, result).isEmpty());
        }
        int curiosFortuneBonus = CuriosApi.getCuriosHelper().getCuriosHandler(player)
                .map(ICuriosItemHandler::getFortuneBonus)
                .orElse(0);
        if (curiosFortuneBonus > 0) {
            return; //Do not modify loot, loot modification gets re-run by curios later
        }
        loot.removeIf(result -> ItemUtils.dropItemToPlayer(player, result).isEmpty());
    }
}
