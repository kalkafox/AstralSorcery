/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.base;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ListEntries
 * Created by HellFirePvP
 * Date: 11.06.2019 / 20:18
 */
public class ListEntries {

    public static class EntitySpawnEntry extends CounterEntry {

        private EntityType<?> type;

        public EntitySpawnEntry(BlockPos pos) {
            super(pos);
        }

        public EntitySpawnEntry(BlockPos pos, EntityType<?> type) {
            super(pos);
            this.type = type;
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);

            this.type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(nbt.getString("entity")));
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            super.writeToNBT(nbt);

            nbt.putString("entity", this.type.getRegistryName().toString());
        }

        public static EntitySpawnEntry createEntry(ServerLevel world, BlockPos pos, MobSpawnType reason) {
            Biome b = world.getBiome(pos);
            List<MobSpawnInfo.Spawners> applicable = new LinkedList<>();
            if (DayTimeHelper.isNight(world)) {
                applicable.addAll(b.getMobSpawnInfo().getSpawners(EntityClassification.MONSTER));
            } else {
                applicable.addAll(b.getMobSpawnInfo().getSpawners(EntityClassification.CREATURE));
            }
            if (applicable.isEmpty()) {
                return null; //Duh.
            }
            Collections.shuffle(applicable);
            MobSpawnInfo.Spawners entry = applicable.get(world.rand.nextInt(applicable.size()));
            EntityType<?> type = entry.type;
            if (type != null && EntityUtils.canEntitySpawnHere(world, pos, type, reason, EntityUtils.SpawnConditionFlags.IGNORE_SPAWN_CONDITIONS,
                    (e) -> e.addTag(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY))) {
                return new EntitySpawnEntry(pos, type);
            }
            return null;
        }

        public void spawn(ServerLevel world, MobSpawnType reason) {
            if (this.type == null) {
                return;
            }

            Entity e = this.type.create(world);
            if (e != null) {
                e.addTag(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY);

                BlockPos at = getPos();
                e.setLocationAndAngles(
                        at.getX() + 0.5,
                        at.getY() + 0.5,
                        at.getZ() + 0.5,
                        world.rand.nextFloat() * 360.0F, 0.0F);
                if (e instanceof Mob) {
                    ((Mob) e).onInitialSpawn(world, world.getDifficultyForLocation(at), reason, null, null);
                    if (!((Mob) e).isNotColliding(world)) {
                        e.remove();
                        return;
                    }
                }
                world.addEntity(e);
                world.playEvent(2004, e.getPosition(), 0);
                world.playEvent(2004, e.getPosition(), 0);
            }
        }
    }

    public static class CounterMaxEntry extends CounterEntry {

        private int maxCount;

        public CounterMaxEntry(BlockPos pos) {
            super(pos);
        }

        public CounterMaxEntry(BlockPos pos, int maxCount) {
            super(pos);
            this.maxCount = maxCount;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            super.writeToNBT(nbt);

            nbt.putInt("maxCount", this.maxCount);
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);

            this.maxCount = nbt.getInt("maxCount");
        }
    }

    public static class CounterEntry extends PosEntry {

        private int counter = 0;

        public CounterEntry(BlockPos pos) {
            super(pos);
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            super.writeToNBT(nbt);

            nbt.putInt("counter", this.counter);
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);

            this.counter = nbt.getInt("counter");
        }
    }

    public static class PosEntry implements CEffectAbstractList.ListEntry {

        private final BlockPos pos;

        public PosEntry(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public BlockPos getPos() {
            return this.pos;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {}

        @Override
        public void readFromNBT(CompoundTag nbt) {}

    }
}
