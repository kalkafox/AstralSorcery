/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockGeometry
 * Created by HellFirePvP
 * Date: 24.11.2019 / 09:54
 */
public class BlockGeometry {

    public static List<BlockPos> getPlane(Direction direction, int radius) {
        return getPlane(direction.getAxis(), radius);
    }

    public static List<BlockPos> getPlane(Direction.Axis axis, int radius) {
        List<BlockPos> out = new ArrayList<>();
        int xRadius = axis == Direction.Axis.X ? 0 : radius;
        int yRadius = axis == Direction.Axis.Y ? 0 : radius;
        int zRadius = axis == Direction.Axis.Z ? 0 : radius;
        for (int xx = -xRadius; xx <= xRadius; xx++) {
            for (int yy = -yRadius; yy <= yRadius; yy++) {
                for (int zz = -zRadius; zz <= zRadius; zz++) {
                    out.add(new BlockPos(xx, yy, zz));
                }
            }
        }
        return out;
    }

    public static List<BlockPos> getSphere(double radius) {
        List<BlockPos> out = new ArrayList<>();
        Vector3 vFrom = new Vector3(0.5, 0.5, 0.5);
        double dst = radius * radius;

        int toX = Mth.ceil(radius);
        int toY = Mth.ceil(radius);
        int toZ = Mth.ceil(radius);
        for (int y = Mth.floor(-radius); y <= toY; y++) {
            for (int x = Mth.floor(-radius); x <= toX; x++) {
                for (int z = Mth.floor(-radius); z <= toZ; z++) {
                    Vector3 result = new Vector3(x, y, z).add(0.5, 0.5, 0.5);
                    if (result.distanceSquared(vFrom) <= dst) {
                        out.add(result.toBlockPos());
                    }
                }
            }
        }
        return out;
    }

    public static List<BlockPos> getHollowSphere(double outerRadius, double innerRadius) {
        List<BlockPos> out = new ArrayList<>();
        Vector3 vFrom = new Vector3(0.5, 0.5, 0.5);
        double dstOuter = outerRadius * outerRadius;
        double dstInner = innerRadius * innerRadius;

        int toX = Mth.ceil(outerRadius);
        int toY = Mth.ceil(outerRadius);
        int toZ = Mth.ceil(outerRadius);
        for (int x = Mth.floor(-outerRadius); x <= toX; x++) {
            for (int y = Mth.floor(-outerRadius); y <= toY; y++) {
                for (int z = Mth.floor(-outerRadius); z <= toZ; z++) {
                    Vector3 result = new Vector3(x, y, z).add(0.5, 0.5, 0.5);
                    double dst = result.distanceSquared(vFrom);
                    if (dst > dstInner && dst <= dstOuter) {
                        out.add(result.toBlockPos());
                    }
                }
            }
        }
        return out;
    }

    public static List<BlockPos> getHollowDome(double outerRadius, double innerRadius) {
        List<BlockPos> out = new ArrayList<>();
        Vector3 vFrom = new Vector3(0.5, 0.5, 0.5);
        double dstOuter = outerRadius * outerRadius;
        double dstInner = innerRadius * innerRadius;

        int toX = Mth.ceil(outerRadius);
        int toY = Mth.ceil(outerRadius);
        int toZ = Mth.ceil(outerRadius);
        for (int x = Mth.floor(-outerRadius); x <= toX; x++) {
            for (int z = Mth.floor(-outerRadius); z <= toZ; z++) {
                for (int y = 0; y <= toY; y++) {
                    Vector3 result = new Vector3(x, y, z).add(0.5, 0.5, 0.5);
                    double dst = result.distanceSquared(vFrom);
                    if (dst > dstInner && dst <= dstOuter) {
                        out.add(result.toBlockPos());
                    }
                }
            }
        }
        return out;
    }

    public static List<BlockPos> getVerticalCone(BlockPos offset, int flatRadius) {
        List<BlockPos> out = new ArrayList<>();

        int lX = offset.getX() - flatRadius;
        int hX = offset.getX() + flatRadius;
        int lZ = offset.getZ() - flatRadius;
        int hZ = offset.getZ() + flatRadius;

        for (int yy = offset.getY(); yy > 0; yy--) {
            for (int xx = lX; xx <= hX; xx++) {
                for (int zz = lZ; zz <= hZ; zz++) {

                    float perc = (float) yy / offset.getY();
                    float dstAllowed = flatRadius * perc;
                    double dX = offset.getX() - xx;
                    double dZ = offset.getZ() - zz;
                    double dstCur = Math.sqrt(dX * dX + dZ * dZ);
                    if (dstCur <= dstAllowed) {
                        out.add(new BlockPos(xx, yy, zz));
                    }
                }
            }
        }

        return out;
    }

}
