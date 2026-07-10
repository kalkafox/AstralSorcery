/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.camera.path;

import hellfirepvp.astralsorcery.client.util.camera.*;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.LinkedList;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CameraPath
 * Created by HellFirePvP
 * Date: 02.12.2019 / 19:56
 */
public class CameraPath extends EntityCameraRenderView implements ICameraPersistencyFunction {

    LinkedList<PathPoint> pathPoints = new LinkedList<>();
    private final Vector3 startVector, focus;

    private ICameraTickListener delegate;
    private ICameraStopListener stopDelegate;

    private int totalTickDuration = 0;
    private boolean expired = false;
    private boolean shutdown = false;

    CameraPath(Vector3 startPoint, Vector3 focusPoint, @Nullable ICameraTickListener tick) {
        this.startVector = startPoint;
        this.focus = focusPoint;
        this.setPosRaw(startPoint.getX(), startPoint.getY(), startPoint.getZ());
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        this.delegate = tick;
        setCameraFocus(focusPoint);
        transformToFocusOnPoint(focusPoint, 0, false);
    }

    public void setTickListener(ICameraTickListener delegate) {
        this.delegate = delegate;
    }

    public void setStopListener(ICameraStopListener delegate) {
        this.stopDelegate = delegate;
    }

    void upHeap(Vector3 point, int ticks) {
        this.pathPoints.addLast(new PathPoint(point, ticks));
        this.totalTickDuration += ticks;
    }

    @Override
    public void moveEntityTick(EntityCameraRenderView entity, EntityClientReplacement replacement, int tickCount) {
        if (delegate != null) {
            delegate.onCameraTick(entity, replacement);
        }
        setCameraFocus(Vector3.atEntityCorner(replacement));
        this.expired = this.tickCount > totalTickDuration;
        if (pathPoints.isEmpty()) {
            this.expired = true;
        } else {
            Vector3 position = queryByTicks(tickCount);
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            this.setPosRaw(position.getX(), position.getY(), position.getZ());
        }
    }

    @Override
    public void onStopTransforming() {
        if (stopDelegate != null && Minecraft.getInstance().level != null) {
            stopDelegate.onCameraStop();
        }
    }
    //Prev-Next tuple

    private Vector3 queryByTicks(int ticks) {
        if (ticks <= 0) {
            return startVector;
        }
        int acc = 0;
        PathPoint current = null;
        Vector3 prev;
        for (PathPoint point : pathPoints) {
            int accumulator = acc + point.ticksToGetThere;

            prev = current == null ? startVector : current.dstPoint;
            current = point;

            if (accumulator >= ticks) {
                int interp = current.ticksToGetThere - (accumulator - ticks);
                int dstJump = current.ticksToGetThere;
                return current.dstPoint.clone().subtract(prev).divide(dstJump).mul(Mth.clamp(interp, 1, dstJump)).add(prev);
            } else {
                acc = accumulator;
            }
        }
        return pathPoints.last().dstPoint; //Doesn't happen since the list isn't empty.
    }

    @Override
    public boolean timedOut() {
        return expired;
    }

    @Override
    public void remove() {
        this.expired = true;
    }

    @Override
    public void forceStop() {
        this.shutdown = true;
        remove();
    }

    @Override
    public boolean wasForciblyStopped() {
        return shutdown;
    }

    CameraPath copy() {
        CameraPath c = new CameraPath(this.startVector, this.focus, this.delegate);
        for (PathPoint fp : this.pathPoints) {
            c.pathPoints.addLast(new PathPoint(fp.dstPoint, fp.ticksToGetThere));
        }
        c.totalTickDuration = this.totalTickDuration;
        return c;
    }

    private static class PathPoint {

        private final Vector3 dstPoint;
        private final int ticksToGetThere;

        PathPoint(Vector3 dstPoint, int ticksToGetThere) {
            this.dstPoint = dstPoint;
            this.ticksToGetThere = ticksToGetThere;
        }
    }
}
