/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.observerlib.common.event.BlockChangeNotifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHandlerAutoLink
 * Created by HellFirePvP
 * Date: 15.05.2020 / 16:59
 */
public class EventHandlerAutoLink implements BlockChangeNotifier.Listener {

    @Override
    public void onChange(Level level, LevelChunk chunk, BlockPos pos, BlockState oldState, BlockState newState) {
        if (level.isClientSide() || !chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
            return;
        }

        Block oldB = oldState.getBlock();
        Block newB = newState.getBlock();

        if (oldB != newB) {
            WorldNetworkHandler handle = WorldNetworkHandler.getNetworkHandler(level);
            handle.informBlockChange(pos);

            if (oldB == Blocks.CRAFTING_TABLE) {
                handle.removeAutoLinkTo(pos);
            }
            if (newB == Blocks.CRAFTING_TABLE) {
                handle.attemptAutoLinkTo(pos);
            }
            if (oldB instanceof BlockAltar) {
                handle.removeAutoLinkTo(pos);
            }
            if (newB instanceof BlockAltar) {
                handle.attemptAutoLinkTo(pos);
            }
        }
    }
}
