/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemInfusedCrystalShovel
 * Created by HellFirePvP
 * Date: 04.04.2020 / 11:05
 */
public class ItemInfusedCrystalShovel extends ItemCrystalShovel {

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level world = player.level();
        if (!world.isClientSide &&
                !player.isShiftKeyDown() &&
                !player.getCooldowns().isOnCooldown(itemstack.getItem()) &&
                player instanceof ServerPlayer serverPlayer) {

            PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
            if (prog.doPerkAbilities()) {
                EventFlags.CHAIN_MINING.executeWithFlag(() -> {
                    if (!world.getBlockState(pos).isAir()) {
                        List<BlockPos> foundBlocks = BlockDiscoverer.discoverBlocksWithSameStateAround(world, pos, true, 8, 200, false);
                        if (!foundBlocks.isEmpty()) {
                            foundBlocks.forEach(at -> {
                                BlockState currentState = world.getBlockState(at);
                                if (!currentState.isAir() && serverPlayer.gameMode.destroyBlock(at)) {
                                    PktPlayEffect ev = new PktPlayEffect(PktPlayEffect.Type.BLOCK_EFFECT)
                                            .addData(buf -> {
                                                ByteBufUtils.writePos(buf, at);
                                                ByteBufUtils.writeBlockState(buf, currentState);
                                            });
                                    PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(world, at, 32));
                                }
                            });

                            serverPlayer.getCooldowns().addCooldown(itemstack.getItem(), 120);
                        }
                    }
                });
            }
        }
        return false;
    }
}
