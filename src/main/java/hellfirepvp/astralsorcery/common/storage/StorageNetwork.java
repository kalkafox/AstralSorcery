/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.storage;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StorageNetwork
 * Created by HellFirePvP
 * Date: 30.05.2019 / 14:57
 */
public class StorageNetwork {

    private CoreArea master = null;
    private final Map<BlockPos, AABB> cores = Maps.newHashMap();

    //True if set.
    public boolean setMaster(@Nullable BlockPos pos) {
        if (pos == null) {
            this.master = null;
            return true;
        }
        if (cores.containsKey(pos)) {
            this.master = new CoreArea(pos, this.cores.get(pos));
            return true;
        }
        return false;
    }

    @Nullable
    public CoreArea getMaster() {
        return master;
    }

    //True if it didn't overwrite a previous one
    public boolean addCore(BlockPos pos, AABB box) {
        return this.cores.put(pos, box) == null;
    }

    //True if it had a position like that
    public boolean removeCore(BlockPos pos) {
        return this.cores.remove(pos) != null;
    }

    public List<CoreArea> getCores() {
        return MapStream.of(this.cores).toList(CoreArea::new);
    }

    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (CoreArea coreData : this.getCores()) {
            CompoundTag coreTag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(coreData.getBlockPos(), coreTag);
            NBTHelper.writeBoundingBox(coreData.getOffsetBox(), coreTag);
            list.add(coreTag);
        }
        tag.put("cores", list);

        CoreArea master;
        if ((master = getMaster()) != null) {
            NBTHelper.setAsSubTag(tag, "master", nbt -> NBTHelper.writeBlockPosToNBT(master.getBlockPos(), nbt));
        }
    }

    public void readFromNBT(CompoundTag tag) {
        this.cores.clear();

        ListTag list = tag.getList("cores", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag coreTag = list.getCompound(i);
            BlockPos pos = NBTHelper.readBlockPosFromNBT(coreTag);
            AABB box = NBTHelper.readBoundingBox(coreTag);
            this.addCore(pos, box);
        }

        this.setMaster(NBTHelper.readFromSubTag(tag, "master", NBTHelper::readBlockPosFromNBT));
    }

    public static class CoreArea {

        private final BlockPos pos;
        private final AABB offsetBox;

        private CoreArea(BlockPos pos, AABB offsetBox) {
            this.pos = pos;
            this.offsetBox = offsetBox;
        }

        public BlockPos getBlockPos() {
            return pos;
        }

        public AABB getOffsetBox() {
            return offsetBox;
        }

        public AABB getRealBox() {
            return offsetBox.move(getBlockPos());
        }
    }

}