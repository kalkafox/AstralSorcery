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

    public void updateIgnoreBlockCollisionState(Level world, boolean ignoreBlockCollision) {
        this.ignoreBlockCollision = ignoreBlockCollision;
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(world);
        if (handle != null) {
            boolean anyChange = false;
            for (PrismNext next : nextNodes.values()) {
                boolean oldState = next.reachable;
                next.reachable = ignoreBlockCollision || next.rayAssist.isClear(world);
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
    public boolean notifyUnlink(Level world, BlockPos to) {
        return nextNodes.remove(to) != null;
    }

    @Override
    public void notifyLink(Level world, BlockPos pos) {
        addLink(world, pos, true, false);
    }

    private void addLink(Level world, BlockPos pos, boolean doRayCheck, boolean previousRayState) {
        PrismNext nextNode = new PrismNext(this, world, thisPos, pos, doRayCheck, previousRayState);
        this.nextNodes.put(pos, nextNode);
    }

    @Override
    public boolean notifyBlockChange(Level world, BlockPos at) {
        boolean anyChange = false;
        for (PrismNext next : nextNodes.values()) {
            if (next.notifyBlockPlace(world, thisPos, at)) anyChange = true;
        }
        return anyChange;
    }

    @Override
    public void notifySourceLink(Level world, BlockPos source) {
        sourcesToThis.add(source);
    }

    @Override
    public void notifySourceUnlink(Level world, BlockPos source) {
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
    public void readFromNBT(CompoundTag compound) {
        this.thisPos = NBTHelper.readBlockPosFromNBT(compound);
        this.sourcesToThis.clear();
        this.ignoreBlockCollision = compound.getBoolean("ignoreBlockCollision");

        ListTag list = compound.getList("sources", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            sourcesToThis.add(NBTHelper.readBlockPosFromNBT(list.getCompound(i)));
        }

        ListTag nextList = compound.getList("nextList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nextList.size(); i++) {
            CompoundTag tag = nextList.getCompound(i);
            BlockPos next = NBTHelper.readBlockPosFromNBT(tag);
            boolean oldState = tag.getBoolean("rayState");
            addLink(null, next, false, oldState); //Rebuild link.
        }
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        NBTHelper.writeBlockPosToNBT(thisPos, compound);
        compound.putBoolean("ignoreBlockCollision", this.ignoreBlockCollision);

        ListTag sources = new ListTag();
        for (BlockPos source : sourcesToThis) {
            CompoundTag comp = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(source, comp);
            sources.add(comp);
        }
        compound.put("sources", sources);

        ListTag nextList = new ListTag();
        for (BlockPos next : nextNodes.keySet()) {
            PrismNext prism = nextNodes.get(next);
            CompoundTag pos = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(next, pos);
            pos.putBoolean("rayState", prism.reachable);
            nextList.add(pos);
        }
        compound.put("nextList", nextList);
    }

    private static class PrismNext {

        private final SimplePrismTransmissionNode parent;
        private boolean reachable;
        private double distanceSq;
        private final BlockPos pos;
        private RaytraceAssist rayAssist;

        private PrismNext(SimplePrismTransmissionNode parent, Level world, BlockPos start, BlockPos end, boolean doRayTest, boolean oldRayState) {
            this.parent = parent;
            this.pos = end;
            this.rayAssist = new RaytraceAssist(start, end);
            if (doRayTest) {
                this.reachable = parent.ignoreBlockCollision || rayAssist.isClear(world);
            } else {
                this.reachable = oldRayState;
            }
            this.distanceSq = end.distanceSq(Vector3d.copy(start), false);
        }

        private boolean notifyBlockPlace(Level world, BlockPos connect, BlockPos at) {
            Vec3 bPosAt = Vector3d.copy(at);
            double dstStart = connect.distanceSq(bPosAt, false);
            double dstEnd = pos.distanceSq(bPosAt, false);
            if (dstStart > distanceSq || dstEnd > distanceSq) return false;
            boolean oldState = this.reachable;
            this.reachable = parent.ignoreBlockCollision || rayAssist.isClear(world);
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
