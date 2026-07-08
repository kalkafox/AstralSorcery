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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ForgeHooks;
import net.neoforged.neoforge.event.ForgeEventFactory;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:26
 */
public class EntityUtils {

    private static final Random rand = new Random();

    @Nullable
    public static Player getPlayer(UUID playerUUID, LogicalSide side) {
        return side.isClient() ? getPlayerClient(playerUUID) : getPlayerServer(playerUUID);
    }

    @Nullable
    public static Player getPlayerServer(UUID playerUUID) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        if (server == null) {
            return null;
        }
        return server.getPlayerList().getPlayerByUUID(playerUUID);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Player getPlayerClient(UUID playerUUID) {
        ClientLevel clWorld = Minecraft.getInstance().world;
        if (clWorld == null) {
            return null;
        }
        return clWorld.getPlayerByUuid(playerUUID);
    }

    public static void applyPotionEffectAtHalf(LivingEntity entity, MobEffectInstance effect) {
        MobEffectInstance activeEffect = entity.getActivePotionEffect(effect.getPotion());
        if (activeEffect != null) {
            if (activeEffect.duration <= effect.duration / 2) {
                entity.addPotionEffect(effect);
            }
        } else {
            entity.addPotionEffect(effect);
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
    public static LivingEntity performWorldSpawningAt(ServerLevel world, BlockPos pos, MobCategory category, MobSpawnType reason, boolean ignoreWeighting, int ignoreSpawnCheckFlags) {
        Biome b = world.getBiome(pos);
        StructureManager mgr = world.func_241112_a_();
        List<MobSpawnInfo.Spawners> spawnList = world.getChunkProvider().getChunkGenerator().func_230353_a_(b, mgr, EntityClassification.MONSTER, pos);
        spawnList = ForgeEventFactory.getPotentialSpawns(world, category, pos, spawnList);
        spawnList.removeIf(s -> !s.type.isSummonable());
        MobSpawnInfo.Spawners entry;
        if (ignoreWeighting) {
            entry = MiscUtils.getRandomEntry(spawnList, rand);
        } else {
            entry = MiscUtils.getWeightedRandomEntry(spawnList, rand, ee -> ee.itemWeight);
        }

        if (entry != null) {
            float x = pos.getX() + 0.5F;
            float y = pos.getY();
            float z = pos.getZ() + 0.5F;

            BlockState state = world.getBlockState(pos);
            if (!state.isNormalCube(world, pos) && canEntitySpawnHere(world, pos, entry.type, reason, ignoreSpawnCheckFlags, null)) {
                Mob entity;
                try {
                    entity = (Mob) entry.type.create(world);
                } catch (Exception exception) {
                    return null;
                }
                if (entity == null) {
                    return null;
                }

                entity.setLocationAndAngles(x, y, z, rand.nextFloat() * 360F, 0F);
                int result = ForgeHooks.canEntitySpawn(entity, world, x, y, z, null, reason); //We already did the default test before.
                if (result == -1) {
                    return null;
                }

                if (!ForgeEventFactory.doSpecialSpawn(entity, world, x, y, z, null, reason)) {
                    entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), reason, null, null);
                }

                world.func_242417_l(entity);
                return entity;
            }
        }
        return null;
    }

    public static boolean canEntitySpawnHere(ServerLevel world, BlockPos at, EntityType<? extends Entity> type, MobSpawnType spawnReason, int ignoreCheckFlags, @Nullable Consumer<Entity> preCheckEntity) {
        if (type.getClassification() == EntityClassification.MISC || !type.isSummonable() || !world.getWorldBorder().contains(at)) {
            return false;
        }
        if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_PLACEMENT_RULES)) {
            EntitySpawnPlacementRegistry.PlacementType placementType = EntitySpawnPlacementRegistry.getPlacementType(type);
            if (!WorldEntitySpawner.canSpawnAtBody(placementType, world, at, type)) {
                return false;
            }
            if (!EntitySpawnPlacementRegistry.canSpawnEntity(type, world, spawnReason, at, rand)) {
                return false;
            }
        }
        if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_BLOCK_COLLISION)) {
            if (!world.hasNoCollisions(type.getBoundingBoxWithSizeApplied(at.getX() + 0.5, at.getY(), at.getZ() + 0.5))) {
                return false;
            }
        }

        Entity entity = type.create(world);
        if (entity == null) {
            return false;
        }
        entity.setLocationAndAngles(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, world.rand.nextFloat() * 360.0F, 0.0F);
        if (preCheckEntity != null) {
            preCheckEntity.accept(entity);
        }

        if (entity instanceof LivingEntity) {
            if (entity instanceof Mob) {
                Mob mobEntity = (Mob) entity;
                Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(mobEntity, world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), null, spawnReason);
                if (canSpawn == Event.Result.DENY) {
                    return false;
                } else if (canSpawn == Event.Result.DEFAULT) {
                    if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_ENTITY_SPAWN_CONDITIONS)) {
                        if (!mobEntity.canSpawn(world, spawnReason)) {
                            return false;
                        }
                    }
                    if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_ENTITY_COLLISION)) {
                        if (!mobEntity.isNotColliding(world)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Nonnull
    public static List<ItemStack> generateLoot(LivingEntity entity, Random rand, DamageSource srcDeath, @Nullable LivingEntity lastAttacker) {
        MinecraftServer srv = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        ServerLevel sw = (ServerLevel) entity.getEntityWorld();

        if (!sw.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            return Collections.emptyList();
        }

        ResourceLocation lootTableKey = entity.getLootTableResourceLocation();
        LootTable table = srv.getLootTableManager().getLootTableFromLocation(lootTableKey);
        LootContext.Builder builder = new LootContext.Builder(sw)
                .withRandom(rand)
                .withParameter(LootParameters.THIS_ENTITY, entity)
                .withParameter(LootParameters.field_237457_g_, entity.getPositionVec())
                .withParameter(LootParameters.DAMAGE_SOURCE, srcDeath)
                .withNullableParameter(LootParameters.KILLER_ENTITY, srcDeath.getTrueSource())
                .withNullableParameter(LootParameters.DIRECT_KILLER_ENTITY, srcDeath.getImmediateSource());
        if (lastAttacker != null) {
            if (lastAttacker instanceof Player) {
                builder.withParameter(LootParameters.LAST_DAMAGE_PLAYER, (Player) lastAttacker)
                        .withLuck(((Player) lastAttacker).getLuck());
            }
        }

        return table.generate(builder.build(LootParameterSets.ENTITY));
    }

    @Nullable
    public static <T extends Entity> T getClosestEntity(LevelAccessor world, Class<T> type, AABB box, Vector3 closestTo) {
        List<T> entities = world.getEntitiesWithinAABB(type, box, Entity::isAlive);
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
        for (T element : elements) {
            double dst = dstFunc.apply(element);
            if (dst < dstClosest) {
                closestElement = element;
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
