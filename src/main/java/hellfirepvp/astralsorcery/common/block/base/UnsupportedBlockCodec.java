/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 1.21's {@link net.minecraft.world.level.block.BaseEntityBlock} requires a
 * {@code codec()} override. Astral Sorcery blocks are never constructed through
 * the block codec system (they are registered eagerly with their own property
 * setup), so this shared placeholder satisfies the contract without supporting
 * codec-based construction.
 */
public final class UnsupportedBlockCodec {

    private UnsupportedBlockCodec() {}

    public static <B extends Block> MapCodec<B> unsupported() {
        return BlockBehaviour.simpleCodec(properties -> {
            throw new UnsupportedOperationException("Astral Sorcery blocks are not codec-constructable");
        });
    }
}
