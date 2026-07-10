/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.time;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.data.config.registry.TileAccelerationBlacklistRegistry;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TimeStopEffectHelper
 * Created by HellFirePvP
 * Date: 31.08.2019 / 13:30
 */
public class TimeStopEffectHelper {

    private static final Random random = new Random();

    @Nonnull
    private final BlockPos position;
    private final float range;
    private final TimeStopZone.EntityTargetController targetController;

    private TimeStopEffectHelper(@Nonnull BlockPos position, float range, TimeStopZone.EntityTargetController targetController) {
        this.position = position;
        this.range = range;
        this.targetController = targetController;
    }

    static TimeStopEffectHelper fromZone(TimeStopZone zone) {
        return new TimeStopEffectHelper(zone.offset, zone.range, zone.targetController);
    }

    @Nonnull
    public BlockPos getPosition() {
        return position;
    }

    public float getRange() {
        return range;
    }

    public TimeStopZone.EntityTargetController getTargetController() {
        return targetController;
    }

    @OnlyIn(Dist.CLIENT)
    static void playEntityParticles(LivingEntity e) {
        EntityDimensions size = e.getSize(e.getPose());
        double x = e.getX() - size.width / 2F + random.nextFloat() * size.width;
        double y = e.getY() + random.nextFloat() * size.height;
        double z = e.getZ() - size.width / 2F + random.nextFloat() * size.width;
        showBreakingParticles(x, y, z);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playEntityParticles(PktPlayEffect ev) {
        Vector3 at = ByteBufUtils.readVector(ev.getExtraData());
        showBreakingParticles(at.getX(), at.getY(), at.getZ());
    }

    @OnlyIn(Dist.CLIENT)
    static void showBreakingParticles(double x, double y, double z) {
        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(x, y, z))
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.WHITE)
                .setScaleMultiplier(0.3F + random.nextFloat() * 0.5F)
                .setMaxAge(40 + random.nextInt(20));
    }

    @OnlyIn(Dist.CLIENT)
    public void playClientTickEffect() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        List<LivingEntity> entities = level.getEntitiesWithinAABB(LivingEntity.class,
                new AABB(-range, -range, -range, range, range, range).offset(position.getX(), position.getY(), position.getZ()),
                EntitySelector.withinRange(position.getX(), position.getY(), position.getZ(), range));

        for (LivingEntity e : entities) {
            if (e != null && e.isAlive() && targetController.shouldFreezeEntity(e) && random.nextInt(3) == 0) {
                playEntityParticles(e);
            }
        }

        int minX = Mth.floor((position.getX() - range) / 16.0D);
        int maxX = Mth.floor((position.getX() + range) / 16.0D);
        int minZ = Mth.floor((position.getZ() - range) / 16.0D);
        int maxZ = Mth.floor((position.getZ() + range) / 16.0D);

        for (int xx = minX; xx <= maxX; ++xx) {
            for (int zz = minZ; zz <= maxZ; ++zz) {
                LevelChunk ch = level.getChunk(xx, zz);
                if (!ch.isEmpty()) {
                    Map<BlockPos, BlockEntity> map = ch.getBlockEntities();
                    for (Map.Entry<BlockPos, BlockEntity> teEntry : map.entrySet()) {

                        BlockEntity te = teEntry.getValue();
                        if (TileAccelerationBlacklistRegistry.INSTANCE.canBeInfluenced(te) && te.getBlockPos().distSqr(position, range)) {

                            double x = te.getBlockPos().getX() + random.nextFloat();
                            double y = te.getBlockPos().getY() + random.nextFloat();
                            double z = te.getBlockPos().getZ() + random.nextFloat();

                            showBreakingParticles(x, y, z);
                        }
                    }
                }
            }
        }

        Vector3 pos;
        for (int i = 0; i < 10; i++) {
            pos = Vector3.random().normalize().mul(random.nextFloat() * range).add(position);
            showBreakingParticles(pos.getX(), pos.getY(), pos.getZ());
        }

        if (random.nextInt(4) == 0) {
            Vector3 rand1 = Vector3.random().normalize().mul(random.nextFloat() * range).add(position);
            Vector3 rand2 = Vector3.random().normalize().mul(random.nextFloat() * range).add(position);
            if (rand1.distance(rand2) > 10) {
                Vector3 dir = rand1.vectorFromHereTo(rand2);
                rand2 = rand1.clone().add(dir.normalize().mul(10));
            }
            EffectHelper.of(EffectTemplatesAS.LIGHTNING)
                    .spawn(rand1)
                    .makeDefault(rand2)
                    .color(VFXColorFunction.WHITE);
        }
    }

    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag out = new CompoundTag();
        NBTHelper.writeBlockPosToNBT(this.position, out);
        out.putFloat("range", this.range);
        out.put("targetController", this.targetController.serializeNBT());
        return out;
    }

    @Nonnull
    public static TimeStopEffectHelper deserializeNBT(CompoundTag cmp) {
        BlockPos at = NBTHelper.readBlockPosFromNBT(cmp);
        float range = cmp.getFloat("range");
        return new TimeStopEffectHelper(at, range, TimeStopZone.EntityTargetController.deserializeNBT(cmp.getCompound("targetController")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeStopEffectHelper that = (TimeStopEffectHelper) o;

        return Float.compare(that.range, range) == 0 &&
                position.equals(that.position);
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + (range != +0.0f ? Float.floatToIntBits(range) : 0);
        return result;
    }

}
