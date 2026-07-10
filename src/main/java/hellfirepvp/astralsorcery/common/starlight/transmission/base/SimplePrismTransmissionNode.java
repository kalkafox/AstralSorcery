/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight.transmission.base;

import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionWorldHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimplePrismTransmissionNode
 * Created by HellFirePvP
 * Date: 03.08.2016 / 16:58
 */
public class SimplePrismTransmissionNode implements IPrismTransmissionNode {

    private boolean ignoreBlockCollision = false;

    private BlockPos thisPos;
    private final Set<BlockPos> sourcesToThis = new HashSet<>();
    private final Map<BlockPos, PrismNext> nextNodes = new HashMap<>();

    public SimplePrismTransmissionNode(BlockPos thisPos) {
        this.thisPos = thisPos;
    }

    @Override
    public BlockPos getLocationPos() {
        return thisPos;
    }

    public void updateIgnoreBlockCollisionState(Level level, boolean ignoreBlockCollision) {
        this.ignoreBlockCollision = ignoreBlockCollision;
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(level);
        if (handle != null) {
            boolean anyChange = false;
            for (PrismNext next : nextNodes.values()) {
                boolean oldState = next.reachable;
                next.reachable = ignoreBlockCollision || next.rayAssist.isClear(level);
                if (next.reachable != oldState) {
                    anyChange = true;
                }
            }
            if (anyChange) {
                handle.notifyTransmissionNodeChange(this);
            }
        }
    }

    public boolean ignoresBlockCollision() {
        return ignoreBlockCollision;
    }

    @Override
    public boolean notifyUnlink(Level level, BlockPos to) {
        return nextNodes.remove(to) != null;
    }

    @Override
    public void notifyLink(Level level, BlockPos pos) {
        addLink(level, pos, true, false);
    }

    private void addLink(Level level, BlockPos pos, boolean doRayCheck, boolean previousRayState) {
        PrismNext nextNode = new PrismNext(this, level, thisPos, pos, doRayCheck, previousRayState);
        this.nextNodes.put(pos, nextNode);
    }

    @Override
    public boolean notifyBlockChange(Level level, BlockPos at) {
        boolean anyChange = false;
        for (PrismNext next : nextNodes.values()) {
            if (next.notifyBlockPlace(level, thisPos, at)) anyChange = true;
        }
        return anyChange;
    }

    @Override
    public void notifySourceLink(Level level, BlockPos source) {
        sourcesToThis.add(source);
    }

    @Override
    public void notifySourceUnlink(Level level, BlockPos source) {
        sourcesToThis.remove(source);
    }

    @Override
    public List<NodeConnection<IPrismTransmissionNode>> queryNext(WorldNetworkHandler handler) {
        List<NodeConnection<IPrismTransmissionNode>> nodes = new LinkedList<>();
        for (BlockPos pos : nextNodes.keySet()) {
            nodes.add(new NodeConnection<>(handler.getTransmissionNode(pos), pos, nextNodes.get(pos).reachable));
        }
        return nodes;
    }

    @Override
    public List<BlockPos> getSources() {
        return new LinkedList<>(sourcesToThis);
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundTag pattern) {
        this.thisPos = NBTHelper.readBlockPosFromNBT(pattern);
        this.sourcesToThis.clear();
        this.ignoreBlockCollision = pattern.getBoolean("ignoreBlockCollision");

        ListTag list = pattern.getList("sources", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            sourcesToThis.add(NBTHelper.readBlockPosFromNBT(list.getCompound(i)));
        }

        ListTag nextList = pattern.getList("nextList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nextList.size(); i++) {
            CompoundTag tag = nextList.getCompound(i);
            BlockPos next = NBTHelper.readBlockPosFromNBT(tag);
            boolean oldState = tag.getBoolean("rayState");
            addLink(null, next, false, oldState); //Rebuild link.
        }
    }

    @Override
    public void save(CompoundTag pattern) {
        NBTHelper.writeBlockPosToNBT(thisPos, pattern);
        pattern.putBoolean("ignoreBlockCollision", this.ignoreBlockCollision);

        ListTag sources = new ListTag();
        for (BlockPos source : sourcesToThis) {
            CompoundTag comp = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(source, comp);
            sources.add(comp);
        }
        pattern.put("sources", sources);

        ListTag nextList = new ListTag();
        for (BlockPos next : nextNodes.keySet()) {
            PrismNext prism = nextNodes.get(next);
            CompoundTag pos = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(next, pos);
            pos.putBoolean("rayState", prism.reachable);
            nextList.add(pos);
        }
        pattern.put("nextList", nextList);
    }

    private static class PrismNext {

        private final SimplePrismTransmissionNode parent;
        private boolean reachable;
        private double distAtCreation;
        private final BlockPos pos;
        private RaytraceAssist rayAssist;

        private PrismNext(SimplePrismTransmissionNode parent, Level level, BlockPos start, BlockPos end, boolean doRayTest, boolean oldRayState) {
            this.parent = parent;
            this.pos = end;
            this.rayAssist = new RaytraceAssist(start, end);
            if (doRayTest) {
                this.reachable = parent.ignoreBlockCollision || rayAssist.isClear(level);
            } else {
                this.reachable = oldRayState;
            }
            this.distAtCreation = end.distSqr(Vec3.copy(start), false);
        }

        private boolean notifyBlockPlace(Level level, BlockPos connect, BlockPos at) {
            Vec3 bPosAt = Vec3.copy(at);
            double dstStart = connect.distSqr(bPosAt, false);
            double dstEnd = pos.distSqr(bPosAt, false);
            if (dstStart > distAtCreation || dstEnd > distAtCreation) return false;
            boolean oldState = this.reachable;
            this.reachable = parent.ignoreBlockCollision || rayAssist.isClear(level);
            return this.reachable != oldState;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePrismTransmissionNode that = (SimplePrismTransmissionNode) o;
        return Objects.equals(thisPos, that.thisPos);

    }

    @Override
    public int hashCode() {
        return thisPos != null ? thisPos.hashCode() : 0;
    }

    public static class Provider extends TransmissionProvider {

        @Override
        public IPrismTransmissionNode get() {
            return new SimplePrismTransmissionNode(null);
        }

    }

}
