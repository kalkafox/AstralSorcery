/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.object.TransformReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldBlockPos
 * Created by HellFirePvP
 * Date: 07.11.2016 / 11:47
 */
public class WorldBlockPos extends BlockPos {

    private final TransformReference<ResourceKey<Level>, Level> worldReference;

    private WorldBlockPos(TransformReference<ResourceKey<Level>, Level> worldReference, BlockPos pos) {
        super(pos);
        this.worldReference = worldReference;
    }

    private WorldBlockPos(ResourceKey<Level> type, BlockPos pos, Function<ResourceKey<Level>, Level> worldProvider) {
        super(pos);
        this.worldReference = new TransformReference<>(type, worldProvider);
    }

    public static WorldBlockPos wrapServer(Level level, BlockPos pos) {
        return new WorldBlockPos(level.dimension(), pos, type -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server.getLevel(type);
        });
    }

    public static WorldBlockPos wrapTileEntity(BlockEntity tile) {
        return new WorldBlockPos(tile.getLevel().dimension(), tile.getBlockPos(), type -> tile.getLevel());
    }

    public ResourceKey<Level> getWorldKey() {
        return this.worldReference.getReference();
    }

    private WorldBlockPos wrapInternal(BlockPos pos) {
        return new WorldBlockPos(this.worldReference, pos);
    }

    @Override
    public WorldBlockPos add(int x, int y, int z) {
        return wrapInternal(super.offset(x, y, z));
    }

    @Override
    public WorldBlockPos add(double x, double y, double z) {
        return wrapInternal(super.add(x, y, z));
    }

    @Override
    public WorldBlockPos add(Vec3i vec) {
        return wrapInternal(super.add(vec));
    }

    @Nullable
    public <T extends BlockEntity> T getTileAt(Class<T> tileClass, boolean forceChunkLoad) {
        Level level = this.worldReference.getValue();
        if (level != null) {
            return MiscUtils.getTileAt(level, this, tileClass, forceChunkLoad);
        }
        return null;
    }

    @Nullable
    public Level getLevel() {
        return this.worldReference.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorldBlockPos that = (WorldBlockPos) o;
        return Objects.equals(getWorldKey(), that.getWorldKey());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getWorldKey().hashCode();
        return result;
    }
}
