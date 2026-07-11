/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyDigTypes
 * Created by HellFirePvP
 * Date: 31.08.2019 / 17:30
 */
public class KeyDigTypes extends KeyPerk {

    public KeyDigTypes(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(this::onHarvest);
        bus.addListener(this::onHarvestSpeed);
    }

    private void onHarvest(PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) {
            return;
        }

        Player player = event.getEntity();
        LogicalSide direction = this.getSide(player);
        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        if (prog.getPerkData().hasPerkEffect(this)) {
            ItemStack heldMainHand = player.getMainHandItem();
            if (!heldMainHand.isEmpty() && heldMainHand.is(ItemTags.PICKAXES)) {
                BlockState target = event.getTargetBlock();
                if (target.is(BlockTags.MINEABLE_WITH_SHOVEL) || target.is(BlockTags.MINEABLE_WITH_AXE)) {
                    event.setCanHarvest(true);
                }
            }
        }
    }

    private void onHarvestSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        LogicalSide direction = this.getSide(player);
        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        if (prog.getPerkData().hasPerkEffect(this)) {
            BlockState broken = event.getState();
            ItemStack playerMainHand = player.getMainHandItem();
            if (!playerMainHand.isEmpty()) {
                if (playerMainHand.is(ItemTags.PICKAXES)) {
                    if (!broken.is(BlockTags.MINEABLE_WITH_PICKAXE) &&
                            (broken.is(BlockTags.MINEABLE_WITH_AXE) || broken.is(BlockTags.MINEABLE_WITH_SHOVEL))) {
                        EventFlags.CHECK_BREAK_SPEED.executeWithFlag(() -> {
                            MiscUtils.tryMultiple(
                                    () -> player.getDigSpeed(Blocks.STONE.defaultBlockState(), event.getPosition().orElse(player.blockPosition())),
                                    () -> player.getDigSpeed(Blocks.STONE.defaultBlockState(), null),
                                    () -> BlockUtils.getSimpleBreakSpeed(player, playerMainHand, Blocks.STONE.defaultBlockState())
                            ).ifPresent(speedModifier -> event.setNewSpeed(Math.max(event.getNewSpeed(), speedModifier)));
                        });
                    }
                }
            }
        }
    }
}
