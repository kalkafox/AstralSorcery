/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.entity;

import com.google.common.base.Predicate;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.util.random.WeightedRandomList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:26
 */
public class EntityUtils {

    private static final Random random = new Random();

    @Nullable
    public static Player getPlayer(UUID playerUUID, LogicalSide direction) {
        return direction.isClient() ? getPlayerClient(playerUUID) : getPlayerServer(playerUUID);
    }

    @Nullable
    public static Player getPlayerServer(UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(playerUUID);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Player getPlayerClient(UUID playerUUID) {
        ClientLevel clWorld = Minecraft.getInstance().level;
        if (clWorld == null) {
            return null;
        }
        return clWorld.getPlayerByUUID(playerUUID);
    }

    public static void applyPotionEffectAtHalf(LivingEntity entity, MobEffectInstance effect) {
        MobEffectInstance activeEffect = entity.getEffect(effect.getEffect());
        if (activeEffect != null) {
            if (activeEffect.getDuration() <= effect.getDuration() / 2) {
                entity.addEffect(effect);
            }
        } else {
            entity.addEffect(effect);
        }
    }

    public static void applyVortexMotion(Supplier<Vector3> positionSupplier, Consumer<Vector3> addMotion, Vector3 to, double vortexRange, double multiplier) {
        Vector3 pos = positionSupplier.get();
        double diffX = (to.getX() - pos.getX()) / vortexRange;
        double diffY = (to.getY() - pos.getY()) / vortexRange;
        double diffZ = (to.getZ() - pos.getZ()) / vortexRange;
        double dist = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        if (1.0D - dist > 0.0D) {
            double dstFactorSq = (1.0D - dist) * (1.0D - dist);
            Vector3 toAdd = new Vector3();
            toAdd.setX(diffX / dist * dstFactorSq * 0.15D * multiplier);
            toAdd.setY(diffY / dist * dstFactorSq * 0.15D * multiplier);
            toAdd.setZ(diffZ / dist * dstFactorSq * 0.15D * multiplier);
            addMotion.accept(toAdd);
        }
    }

    @Nullable
    public static LivingEntity performWorldSpawningAt(ServerLevel level, BlockPos pos, MobCategory category, MobSpawnType reason, boolean ignoreWeighting, int ignoreSpawnCheckFlags) {
        Holder<Biome> b = level.getBiome(pos);
        StructureManager mgr = level.structureManager();
        List<MobSpawnSettings.SpawnerData> spawnList = new ArrayList<>(
                level.getChunkSource().getGenerator().getMobsAt(b, mgr, MobCategory.MONSTER, pos).unwrap());
        spawnList = new ArrayList<>(EventHooks.getPotentialSpawns(level, category, pos, WeightedRandomList.create(spawnList)).unwrap());
        spawnList.removeIf(s -> !s.type.canSummon());
        MobSpawnSettings.SpawnerData entry;
        if (ignoreWeighting) {
            entry = MiscUtils.getRandomEntry(spawnList, random);
        } else {
            entry = MiscUtils.getWeightedRandomEntry(spawnList, random, ee -> ee.getWeight().asInt());
        }

        if (entry != null) {
            float x = pos.getX() + 0.5F;
            float y = pos.getY();
            float z = pos.getZ() + 0.5F;

            BlockState state = level.getBlockState(pos);
            if (!state.isCollisionShapeFullBlock(level, pos) && canEntitySpawnHere(level, pos, entry.type, reason, ignoreSpawnCheckFlags, null)) {
                Mob entity;
                try {
                    entity = (Mob) entry.type.create(level);
                } catch (Exception exception) {
                    return null;
                }
                if (entity == null) {
                    return null;
                }

                entity.moveTo(x, y, z, random.nextFloat() * 360F, 0F);
                if (!EventHooks.checkSpawnPosition(entity, level, reason)) { //We already did the default test before.
                    return null;
                }

                EventHooks.finalizeMobSpawn(entity, level, level.getCurrentDifficultyAt(pos), reason, null);

                level.addFreshEntityWithPassengers(entity);
                return entity;
            }
        }
        return null;
    }

    public static boolean canEntitySpawnHere(ServerLevel level, BlockPos at, EntityType<? extends Entity> type, MobSpawnType spawnReason, int ignoreCheckFlags, @Nullable Consumer<Entity> preCheckEntity) {
        if (type.getCategory() == MobCategory.MISC || !type.canSummon() || !level.getWorldBorder().isWithinBounds(at)) {
            return false;
        }
        if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_PLACEMENT_RULES)) {
            if (!SpawnPlacements.isSpawnPositionOk(type, level, at)) {
                return false;
            }
            if (!SpawnPlacements.checkSpawnRules(type, level, spawnReason, at, RandomSource.create(random.nextLong()))) {
                return false;
            }
        }
        if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_BLOCK_COLLISION)) {
            if (!level.noCollision(type.getSpawnAABB(at.getX() + 0.5, at.getY(), at.getZ() + 0.5))) {
                return false;
            }
        }

        Entity entity = type.create(level);
        if (entity == null) {
            return false;
        }
        entity.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        if (preCheckEntity != null) {
            preCheckEntity.accept(entity);
        }

        if (entity instanceof Mob mobEntity) {
            if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_ENTITY_SPAWN_CONDITIONS)) {
                if (!mobEntity.checkSpawnRules(level, spawnReason)) {
                    return false;
                }
            }
            if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_ENTITY_COLLISION)) {
                if (!mobEntity.checkSpawnObstruction(level)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nonnull
    public static List<ItemStack> generateLoot(LivingEntity entity, Random random, DamageSource srcDeath, @Nullable LivingEntity lastAttacker) {
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        ServerLevel sw = (ServerLevel) entity.getCommandSenderWorld();

        if (!sw.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return Collections.emptyList();
        }

        ResourceKey<LootTable> lootTableKey = entity.getLootTable();
        LootTable table = srv.reloadableRegistries().getLootTable(lootTableKey);
        LootParams.Builder builder = new LootParams.Builder(sw)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, srcDeath)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, srcDeath.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, srcDeath.getDirectEntity());
        if (lastAttacker != null) {
            if (lastAttacker instanceof Player) {
                builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player) lastAttacker)
                        .withLuck(((Player) lastAttacker).getLuck());
            }
        }

        return table.getRandomItems(builder.create(LootContextParamSets.ENTITY), RandomSource.create(random.nextLong()));
    }

    @Nullable
    public static <T extends Entity> T getNearestEntity(LevelAccessor level, Class<T> type, AABB box, Vector3 closestTo) {
        List<T> entities = level.getEntities(EntityTypeTest.forClass(type), box, Entity::isAlive);
        return selectClosest(entities, closestTo::distanceSquared);
    }

    public static Predicate<? super Entity> selectEntities(Class<? extends Entity>... entities) {
        return (Predicate<Entity>) entity -> {
            if (entity == null || !entity.isAlive()) return false;
            Class<? extends Entity> clazz = entity.getClass();
            for (Class<? extends Entity> test : entities) {
                if (test.isAssignableFrom(clazz)) return true;
            }
            return false;
        };
    }

    public static Predicate<? super Entity> selectItemClassInstanceof(Class<?> itemClass) {
        return (Predicate<Entity>) entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity)) return false;
            ItemStack i = ((ItemEntity) entity).getItem();
            if (i.isEmpty()) return false;
            return itemClass.isAssignableFrom(i.getItem().getClass());
        };
    }

    public static Predicate<? super Entity> selectItem(Item item) {
        return (Predicate<Entity>) entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity)) return false;
            ItemStack i = ((ItemEntity) entity).getItem();
            if (i.isEmpty()) return false;
            return i.getItem().equals(item);
        };
    }

    public static Predicate<? super Entity> selectItemStack(Function<ItemStack, Boolean> acceptor) {
        return entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity)) return false;
            ItemStack i = ((ItemEntity) entity).getItem();
            if (i.isEmpty()) return false;
            return acceptor.apply(i);
        };
    }

    @Nullable
    public static <T> T selectClosest(Collection<T> elements, Function<T, Double> dstFunc) {
        if (elements.isEmpty()) return null;

        double dstClosest = Double.MAX_VALUE;
        T closestElement = null;
        for (T value : elements) {
            double dst = dstFunc.apply(value);
            if (dst < dstClosest) {
                closestElement = value;
                dstClosest = dst;
            }
        }
        return closestElement;
    }

    public static class SpawnConditionFlags {

        public static final int IGNORE_PLACEMENT_RULES         = 0b0001;
        public static final int IGNORE_ENTITY_COLLISION        = 0b0010;
        public static final int IGNORE_BLOCK_COLLISION         = 0b0100;
        public static final int IGNORE_ENTITY_SPAWN_CONDITIONS = 0b1000;

        public static final int IGNORE_COLLISIONS = IGNORE_BLOCK_COLLISION | IGNORE_ENTITY_COLLISION;
        public static final int IGNORE_SPAWN_CONDITIONS = IGNORE_PLACEMENT_RULES | IGNORE_ENTITY_SPAWN_CONDITIONS;
        public static final int IGNORE_ALL = IGNORE_COLLISIONS | IGNORE_SPAWN_CONDITIONS; //Why would you actually use this? Consider not calling the method..

        public static boolean isSet(int flags, int flag) {
            return (flags & flag) != 0;
        }

    }
}
