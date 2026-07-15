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
import net.minecraft.core.registries.BuiltInRegistries;

import hellfirepvp.astralsorcery.common.util.RegistryHelper;

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

            this.type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(nbt.getString("entity")));
        }

        @Override
        public void save(CompoundTag nbt) {
            super.save(nbt);

            nbt.putString("entity", RegistryHelper.getKey(this.type).toString());
        }

        public static EntitySpawnEntry createEntry(ServerLevel level, BlockPos pos, MobSpawnType reason) {
            Biome biome = level.getBiome(pos).value();
            MobCategory category = DayTimeHelper.isNight(level) ? MobCategory.MONSTER : MobCategory.CREATURE;
            MobSpawnSettings.SpawnerData entry = biome.getMobSettings().getMobs(category)
                    .getRandom(level.random)
                    .orElse(null);
            if (entry == null) {
                return null; //Duh.
            }
            EntityType<?> type = entry.type;
            if (type != null && EntityUtils.canEntitySpawnHere(level, pos, type, reason, EntityUtils.SpawnConditionFlags.IGNORE_SPAWN_CONDITIONS,
                    (e) -> e.addTag(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY))) {
                return new EntitySpawnEntry(pos, type);
            }
            return null;
        }

        public void spawn(ServerLevel level, MobSpawnType reason) {
            if (this.type == null) {
                return;
            }

            Entity e = this.type.create(level);
            if (e != null) {
                e.addTag(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY);

                BlockPos at = getBlockPos();
                e.moveTo(
                        at.getX() + 0.5,
                        at.getY() + 0.5,
                        at.getZ() + 0.5,
                        level.random.nextFloat() * 360.0F, 0.0F);
                if (e instanceof Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), reason, null);
                    if (!mob.checkSpawnObstruction(level)) {
                        e.discard();
                        return;
                    }
                }
                level.addFreshEntity(e);
                level.levelEvent(2004, e.blockPosition(), 0);
                level.levelEvent(2004, e.blockPosition(), 0);
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
        public void save(CompoundTag nbt) {
            super.save(nbt);

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
        public void save(CompoundTag nbt) {
            super.save(nbt);

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
        public BlockPos getBlockPos() {
            return this.pos;
        }

        @Override
        public void save(CompoundTag nbt) {}

        @Override
        public void readFromNBT(CompoundTag nbt) {}

    }
}
