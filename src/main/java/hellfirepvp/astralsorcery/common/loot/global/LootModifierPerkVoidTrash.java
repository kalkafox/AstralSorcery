/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.loot.global;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.data.config.registry.OreItemRarityRegistry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVoidTrash;
import hellfirepvp.astralsorcery.common.util.loot.LootUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.fml.LogicalSide;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LootModifierPerkVoidTrash
 * Created by HellFirePvP
 * Date: 08.05.2020 / 19:55
 */
public class LootModifierPerkVoidTrash extends LootModifier {

    public static final MapCodec<LootModifierPerkVoidTrash> CODEC = RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).apply(inst, LootModifierPerkVoidTrash::new)
    );

    private LootModifierPerkVoidTrash(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> lootTable, LootContext context) {
        if (!LootUtil.doesContextFulfillSet(context, LootContextParamSets.BLOCK)) {
            return lootTable;
        }
        Entity e = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(e instanceof Player)) {
            return lootTable;
        }
        Player player = (Player) e;
        PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!prog.isValid() || !prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyVoidTrash)) {
            return lootTable;
        }
        if (!PerkTree.PERK_TREE.getPerk(LogicalSide.SERVER, perk -> perk instanceof KeyVoidTrash).isPresent()) {
            return lootTable;
        }

        double chance = KeyVoidTrash.CONFIG.getOreChance() *
                PerkAttributeHelper.getOrCreateMap(player, LogicalSide.SERVER).getAttributeInstance(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);

        Random random = new Random(context.getRandom().nextLong());
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        for (ItemStack stack : lootTable) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack resultStack = stack;
            if (KeyVoidTrash.CONFIG.isTrash(resultStack)) {
                resultStack = ItemStack.EMPTY;

                if (context.getRandom().nextFloat() < chance) {
                    Item drop = OreItemRarityRegistry.VOID_TRASH_REWARD.getRandomItem(random);
                    if (drop != null) {
                        resultStack = new ItemStack(drop);
                    }
                }
            }
            if (!resultStack.isEmpty()) {
                result.add(resultStack);
            }
        }
        return result;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
