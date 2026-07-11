/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.lib.GameRulesAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.*;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.ForgeHooks;
import net.neoforged.neoforge.common.ForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.ForgeEventFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.level.ClipContext;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MiscUtils
 * Created by HellFirePvP
 * Date: 01.08.2016 / 13:38
 */
public class MiscUtils {

    @Nullable
    public static <T> T getTileAt(BlockGetter level, BlockPos pos, Class<T> tileClass, boolean forceChunkLoad) {
        if (level == null || pos == null) return null; //Duh.
        if (level instanceof LevelAccessor) {
            if (!((LevelAccessor) level).getChunkSource().isChunkLoaded(new ChunkPos(pos)) && !forceChunkLoad) {
                return null;
            }
        }
        BlockEntity te = level.getTileEntity(pos);
        if (te == null) return null;
        if (tileClass.isInstance(te)) return (T) te;
        return null;
    }

    public static boolean canEntityTickAt(LevelAccessor level, BlockPos pos) {
        ChunkPos chPos = new ChunkPos(pos);
        if (!level.getChunkSource().isChunkLoaded(chPos)) {
            return false;
        }
        if (level.isClientSide() || !(level instanceof ServerLevel)) {
            //Assume if a chunk is present and loaded on the client that it is valid for the client.
            return true;
        }
        ServerChunkCache chunkSource = ((ServerLevel) level).getChunkSource();
        return !chunkSource.chunkMap.isOutsideSpawningRadius(chPos);
    }

    public static List<BlockSnapshot> captureBlockChanges(Level level, Runnable r) {
        level.captureBlockSnapshots = true;
        r.run();
        level.captureBlockSnapshots = false;
        List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();
        return blockSnapshots;
    }

    @Nullable
    public static <T> T getRandomEntry(Collection<T> HELPER, Random random) {
        if (HELPER == null || HELPER.isEmpty()) {
            return null;
        }
        int index = random.nextInt(HELPER.size());
        return Iterables.get(HELPER, index);
    }

    @Nullable
    public static <T> T getRandomEntry(T[] array, Random random) {
        if (array == null || array.length <= 0) {
            return null;
        }
        return array[random.nextInt(array.length)];
    }

    @Nullable
    public static ModContainer getCurrentlyActiveMod() {
        return ModLoadingContext.get().getActiveContainer();
    }

    @Nonnull
    public static <T> T getEnumEntry(Class<T> enumClazz, int index) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Called getEnumEntry on class " + enumClazz.getName() + " which isn't an enum.");
        }
        T[] values = enumClazz.getEnumConstants();
        if (values.length == 0) {
            throw new IllegalArgumentException(enumClazz.getName() + " has no enum constants.");
        }
        return values[Mth.clamp(index, 0, values.length - 1)];
    }

    @Nullable
    public static <T> T getWeightedRandomEntry(Collection<T> list, Random random, Function<T, Integer> getWeightFunction) {
        if (list.isEmpty()) {
            return null;
        }
        List<WRItemObject<T>> weightedItems = new ArrayList<>(list.size());
        for (T e : list) {
            weightedItems.add(new WRItemObject<>(getWeightFunction.apply(e), e));
        }
        WRItemObject<T> item = WeightedRandom.getRandomItem(random, weightedItems);
        return item != null ? item.getValue() : null;
    }

    public static <T, V extends Comparable<V>> V getMaxEntry(Collection<T> elements, Function<T, V> valueFunction) {
        return getMaxEntry(MiscUtils.transformCollection(elements, valueFunction));
    }

    public static <T extends Comparable<T>> T getMaxEntry(Collection<T> elements) {
        T maxElement = null;
        for (T value : elements) {
            if (maxElement == null || maxElement.compareTo(value) < 0) {
                maxElement = value;
            }
        }
        return maxElement;
    }

    public static <T, V extends Comparable<V>> V getMinEntry(Collection<T> elements, Function<T, V> valueFunction) {
        return getMinEntry(MiscUtils.transformCollection(elements, valueFunction));
    }

    public static <T extends Comparable<T>> T getMinEntry(Collection<T> elements) {
        T minElement = null;
        for (T value : elements) {
            if (minElement == null || minElement.compareTo(value) > 0) {
                minElement = value;
            }
        }
        return minElement;
    }

    public static boolean canSeeSky(Level level, BlockPos at, boolean loadChunk, boolean defaultValue) {
        return canSeeSky(level, at, loadChunk, false, defaultValue);
    }

    public static boolean canSeeSky(Level level, BlockPos at, boolean loadChunk, boolean allowInNoSkyWorlds, boolean defaultValue) {
        if (level.getGameRules().getBoolean(GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE)) {
            return true;
        }
        if (allowInNoSkyWorlds && !level.dimensionType().hasSkyLight()) {
            return true;
        }
        if (!loadChunk) {
            return MiscUtils.executeWithChunk(level, at, () -> {
                return level.canSeeSkyFromBelowWater(at);
            }, defaultValue);
        }
        return level.canSeeSkyFromBelowWater(at);
    }

    public static <T> Runnable apply(Consumer<T> func, Supplier<T> supply) {
        return () -> func.accept(supply.get());
    }

    public static <T, U> Consumer<T> apply(BiConsumer<T, U> func, Supplier<U> supply) {
        return (t) -> func.accept(t, supply.get());
    }

    public static <T, U, V> BiConsumer<T, U> apply(TriConsumer<T, U, V> func, Supplier<V> supply) {
        return (t, u) -> func.accept(t, u, supply.get());
    }

    public static <T, R> Supplier<R> apply(Function<T, R> func, Supplier<T> supply) {
        return () -> func.apply(supply.get());
    }

    public static <T, P, R> Function<P, R> apply(BiFunction<T, P, R> func, Supplier<T> supply) {
        return p -> func.apply(supply.get(), p);
    }

    public static <T, V> Function<T, V> nullFunction(Runnable run) {
        return nullFunction((v) -> run.run());
    }

    public static <T, V> Function<T, V> nullFunction(Consumer<T> run) {
        return (t) -> {
            run.accept(t);
            return null;
        };
    }

    public static <T> Supplier<T> nullSupplier(Runnable run) {
        return () -> {
            run.run();
            return null;
        };
    }

    public static <T, V> List<V> transformList(List<T> list, Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    public static <T, V> Set<V> transformSet(Set<T> list, Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toSet());
    }

    public static <T, V> Collection<V> transformCollection(Collection<T> list, Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    public static <K, V, N> Map<K, N> remap(Map<K, V> map, Function<V, N> remapFct) {
        return MapStream.of(map).mapValue(remapFct).toMap();
    }

    public static <T> void mergeList(Collection<T> src, List<T> dst) {
        for (T value : src) {
            if (!dst.contains(value)) {
                dst.add(value);
            }
        }
    }

    public static <T> void cutList(Collection<? extends T> toRemove, List<T> from) {
        for (T value : toRemove) {
            from.remove(value);
        }
    }

    public static <T> List<T> copyList(List<T> list) {
        List<T> l = new ArrayList<>(list.size());
        Collections.copy(l, list);
        return l;
    }

    public static <T> Set<T> copySet(Set<T> set) {
        Set<T> s = new HashSet<>(set.size());
        s.addAll(set);
        return s;
    }

    @Nullable
    public static <T> T iterativeSearch(Collection<T> HELPER, Predicate<T> matchingFct) {
        for (T value : HELPER) {
            if (matchingFct.test(value)) {
                return value;
            }
        }
        return null;
    }

    public static <T> boolean contains(Collection<T> HELPER, Predicate<T>  matchingFct) {
        return iterativeSearch(HELPER, matchingFct) != null;
    }

    public static <T> boolean matchesAny(T value, Collection<Predicate<T>> tests) {
        for (Predicate<T> test : tests) {
            if (test.test(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFluidBlock(BlockState state) {
        return state.getBlock() instanceof LiquidBlock;
    }

    @Nullable
    public static Fluid tryGetFuild(BlockState state) {
        if (!isFluidBlock(state)) {
            return null;
        }
        if (state.getBlock() instanceof LiquidBlock) {
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                return fluidState.getType();
            }
        }
        return null;
    }

    public static boolean canPlayerAttackServer(@Nullable LivingEntity source, @Nonnull LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof Player) {
            Player plTarget = (Player) target;
            if (target.getCommandSenderWorld() instanceof ServerLevel &&
                    target.getCommandSenderWorld().getServer() != null &&
                    target.getCommandSenderWorld().getServer().isPvpAllowed()) {
                return false;
            }
            if (plTarget.isSpectator() || plTarget.isCreative()) {
                return false;
            }
            if (source instanceof Player &&
                    !((Player) source).canAttackPlayer(plTarget)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlayerBreakBlockPos(Player player, BlockPos tryBreak) {
        BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent(player.getCommandSenderWorld(), tryBreak, player.getCommandSenderWorld().getBlockState(tryBreak), player);
        NeoForge.EVENT_BUS.post(ev);
        return !ev.isCanceled();
    }

    public static boolean canPlayerPlaceBlockPos(Player player, BlockState tryPlace, BlockPos pos, Direction againstSide) {
        Level level = player.getCommandSenderWorld();
        level.captureBlockSnapshots = true;
        level.setBlock(pos, tryPlace);
        level.captureBlockSnapshots = false;

        List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();

        boolean cancelPlacement = false;
        if (blockSnapshots.size() > 1) {
            cancelPlacement = ForgeEventFactory.onMultiBlockPlace(player, blockSnapshots, againstSide);
        } else if (blockSnapshots.size() == 1) {
            cancelPlacement = ForgeEventFactory.onBlockPlace(player, blockSnapshots.get(0), againstSide);
        }
        for (BlockSnapshot blocksnapshot : Lists.reverse(blockSnapshots)) {
            level.restoringBlockSnapshots = true;
            blocksnapshot.restore(true, false);
            level.restoringBlockSnapshots = false;
        }
        return !cancelPlacement;
    }

    public static boolean isConnectionEstablished(ServerPlayer player) {
        return player.connection != null && player.connection.connection != null && player.connection.connection.isConnected();
    }

    public static long getRandomWorldSeed(WorldGenLevel level) {
        return new Random(level.getSeed()).nextLong();
    }

    @Nullable
    public static Tuple<InteractionHand, ItemStack> getMainOrOffHand(LivingEntity entity, Item search) {
        return getMainOrOffHand(entity, stack -> !stack.isEmpty() && stack.getItem().equals(search));
    }

    @Nullable
    public static Tuple<InteractionHand, ItemStack> getMainOrOffHand(LivingEntity entity, Predicate<ItemStack> acceptorFnc) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack held = entity.getItemInHand(hand);
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            hand = InteractionHand.OFF_HAND;
            held = entity.getItemInHand(hand);
        }
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            return null;
        }
        return new Tuple<>(hand, held);
    }

    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toTitleCase(str.charAt(0)) + str.substring(1);
    }

    @Nullable
    public static <T extends Entity> T transferEntityTo(T entity, ResourceKey<Level> target, BlockPos targetPos) {
        if (entity.getCommandSenderWorld().isClientSide) {
            return null; //No transfers on clientside.
        }
        entity.getZ(false);
        ResourceKey<Level> src = entity.getCommandSenderWorld().dimension();
        if (!src.equals(target)) {
            if (!ForgeHooks.onTravelToDimension(entity, target)) {
                return null;
            }

            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            ServerLevel targetWorld = srv.getLevel(target);
            if (targetWorld == null) {
                return null;
            }
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer) entity).teleport(targetWorld,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.1,
                        targetPos.getZ() + 0.5,
                        entity.getYRot(),
                        entity.getXRot());
            } else {
                entity = (T) entity.changeDimension(targetWorld, new NoOpTeleporter(targetWorld, targetPos));
                if (entity == null) {
                    return null;
                }
            }
        }
        entity.setPositionAndUpdate(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        return entity;
    }

    @Nullable
    public static BlockPos itDownTopBlock(Level level, BlockPos at) {
        ChunkAccess chunk = level.getChunk(at);
        BlockPos downPos = null;

        for (BlockPos blockpos = new BlockPos(at.getX(), chunk.getHighestSectionPosition() + 16, at.getZ()); blockpos.getY() >= 0; blockpos = downPos) {
            downPos = blockpos.below();
            BlockState test = level.getBlockState(downPos);
            if (!level.isEmptyBlock(downPos) && !test.isIn(BlockTags.LEAVES) && test.isFaceSturdy(level, downPos, Direction.UP)) {
                break;
            }
        }

        return downPos;
    }

    public static List<Vector3> getCirclePositions(Vector3 rotationPivot, Vector3 axis, double radius, int amountOfPointsOnCircle) {
        List<Vector3> out = new LinkedList<>();
        Vector3 circleVec = axis.clone().perpendicular().normalize().mul(radius);
        double degPerPoint = 360D / ((double) amountOfPointsOnCircle);
        for (int i = 0; i < amountOfPointsOnCircle; i++) {
            double deg = i * degPerPoint;
            out.add(circleVec.clone().mirror(Math.toRadians(deg), axis.clone()).add(rotationPivot));
        }
        return out;
    }

    public static Vector3 getRandomCirclePosition(Vector3 rotationPivot, Vector3 axis, double radius) {
        return getCirclePosition(rotationPivot, axis, radius, Math.random() * 360);
    }

    public static Vector3 getCirclePosition(Vector3 rotationPivot, Vector3 axis, double radius, double degree) {
        Vector3 circleVec = axis.clone().perpendicular().normalize().mul(radius);
        return circleVec.mirror(Math.toRadians(degree), axis.clone()).add(rotationPivot);
    }

    public static Vector3 limitVelocityToMinecraftLimit(Vector3 ap) {
        double maxDir = Math.max(Math.abs(ap.getX()), Math.max(Math.abs(ap.getY()), Math.abs(ap.getZ())));
        if (maxDir <= 3.9) { //SEntityVelocityPacket 3.9 * 8000 short value limit
            return ap;
        }
        return ap.mul(3.9 / maxDir);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Player player) {
        return rayTraceLookBlock(player, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
    }

    @Nonnull
    public static HitResult rayTraceLook(Player player) {
        return rayTraceLook(player, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Player player, ClipContext.BlockMode block, ClipContext.FluidMode fluid) {
        return rayTraceLookBlock(player, block, fluid, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
    }

    @Nonnull
    public static HitResult rayTraceLook(Player player, ClipContext.BlockMode block, ClipContext.FluidMode fluid) {
        return rayTraceLook(player, block, fluid, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Player player, double reachDst) {
        return rayTraceLookBlock(player, ClipContext.BlockMode.COLLIDER, ClipContext.FluidMode.ANY, reachDst);
    }

    @Nonnull
    public static HitResult rayTraceLook(Player player, double reachDst) {
        return rayTraceLook(player, ClipContext.BlockMode.COLLIDER, ClipContext.FluidMode.ANY, reachDst);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Entity entity, ClipContext.BlockMode block, ClipContext.FluidMode fluid, double reachDst) {
        HitResult rtr = rayTraceLook(entity, block, fluid, reachDst);
        if (rtr.getType() == HitResult.Type.BLOCK && rtr instanceof BlockHitResult) {
            return (BlockHitResult) rtr;
        }
        return null;
    }

    @Nonnull
    public static HitResult rayTraceLook(Entity entity, ClipContext.BlockMode block, ClipContext.FluidMode fluid, double reachDst) {
        Vec3 pos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 lookVec = entity.getLookAngle();
        Vec3 end = pos.add(lookVec.x * reachDst, lookVec.y * reachDst, lookVec.z * reachDst);
        ClipContext ctx = new ClipContext(pos, end, block, fluid, entity);
        return entity.level().clipWithInteractionOverride(ctx);
    }

    public static Color calcRandomConstellationColor(float perc) {
        return new Color(Color.HSBtoRGB((230F + (50F * perc)) / 360F, 0.8F, 0.8F - (0.3F * perc)));
    }

    public static void applyRandomOffset(Vector3 target, Random random) {
        applyRandomOffset(target, random, 1F);
    }

    public static void applyRandomOffset(Vector3 target, Random random, float multiplier) {
        target.addX(random.nextFloat() * multiplier * (random.nextBoolean() ? 1 : -1));
        target.addY(random.nextFloat() * multiplier * (random.nextBoolean() ? 1 : -1));
        target.addZ(random.nextFloat() * multiplier * (random.nextBoolean() ? 1 : -1));
    }

    public static void applyRandomCircularOffset(Vector3 target, Random random) {
        applyRandomOffset(target, random, 1F);
    }

    public static void applyRandomCircularOffset(Vector3 target, Random random, float multiplier) {
        Vector3 v = Vector3.random().normalize().mul(random.nextFloat() * multiplier);
        target.addX(v.getX() * (random.nextBoolean() ? 1 : -1));
        target.addY(v.getY() * (random.nextBoolean() ? 1 : -1));
        target.addZ(v.getZ() * (random.nextBoolean() ? 1 : -1));
    }

    public static void executeWithChunk(LevelReader level, ChunkPos pos, Runnable run) {
        executeWithChunk(level, pos.asBlockPos(), nullSupplier(run));
    }

    public static void executeWithChunk(LevelReader level, BlockPos pos, Runnable run) {
        executeWithChunk(level, pos, nullSupplier(run));
    }

    public static <T> T executeWithChunk(LevelReader level, BlockPos pos, Supplier<T> run) {
        return executeWithChunk(level, pos, run, (T) null);
    }

    public static <T> T executeWithChunk(LevelReader level, BlockPos pos, Supplier<T> run, T defaultValue) {
        if (level instanceof ServerLevel && LogCategory.UNINTENDED_CHUNK_LOADING.isEnabled()) {
            ServerChunkCache provider = ((ServerLevel) level).getChunkSource();
            int prev = provider.getLoadedChunkCount();
            try {
                if (provider.isChunkLoaded(new ChunkPos(pos))) {
                    return run.get();
                }
            } finally {
                int current = ((ServerLevel) level).getChunkSource().getLoadedChunkCount();
                if (current > prev) { //We... don't really care about unloading tbh.
                    AstralSorcery.log.warn("Astral Sorcery loaded a chunk when it intended not to!");
                    AstralSorcery.log.warn("Previous chunk count: " + prev);
                    AstralSorcery.log.warn("Current chunk count: " + current);
                    AstralSorcery.log.warn("Loaded " + (current - prev) + " chunks!");
                    AstralSorcery.log.warn("Stacktrace:", new Exception());
                }
            }
        } else if (level instanceof LevelAccessor) {
            ChunkSource provider = ((LevelAccessor) level).getChunkSource();
            if (provider.canTick(pos)) {
                return run.get();
            }
        } else {
            if (level.hasChunkAt(pos)) {
                return run.get();
            }
        }
        return defaultValue;
    }

    public static <T> void executeWithChunk(LevelReader level, BlockPos pos, T obj, Consumer<T> run) {
        executeWithChunk(level, pos, nullSupplier(apply(run, () -> obj)));
    }

    public static <T, U> void executeWithChunk(LevelReader level, BlockPos pos, T obj, U obj1, BiConsumer<T, U> run) {
        executeWithChunk(level, pos, obj, apply(run, () -> obj1));
    }

    public static <T, R> R executeWithChunk(LevelReader level, BlockPos pos, T obj, Function<T, R> run) {
        return executeWithChunk(level, pos, apply(run, () -> obj));
    }

    public static <T, R> R executeWithChunk(LevelReader level, BlockPos pos, T obj, Function<T, R> run, R _default) {
        return executeWithChunk(level, pos, apply(run, () -> obj), _default);
    }

    public static <T> Function<T, T> mapWithChunk(LevelReader level, Function<T, BlockPos> posFn) {
        return (val) -> executeWithChunk(level, posFn.apply(val), val, Function.identity());
    }

    public static <T> T eitherOf(Random r, T... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)];
    }

    public static <T> T eitherOf(Random r, Supplier<T>... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)].get();
    }

    public static <T> Optional<T> tryMultiple(Supplier<T>... suppliers) {
        for (Supplier<T> factory : suppliers) {
            try {
                return Optional.ofNullable(factory.get());
            } catch (Exception exc) {
                AstralSorcery.log.error(exc);
            }
        }
        return Optional.empty();
    }

    public static boolean isPlayerFakeMP(ServerPlayer player) {
        if (player instanceof FakePlayer) {
            return true;
        }

        boolean isModdedPlayer = false;
        for (Mods mod : Mods.values()) {
            if (!mod.isPresent()) {
                continue;
            }
            Class<?> specificPlayerClass = mod.getExtendedPlayerClass();
            if (specificPlayerClass != null) {
                if (player.getClass() != ServerPlayer.class && player.getClass() == specificPlayerClass) {
                    isModdedPlayer = true;
                    break;
                }
            }
        }
        if (!isModdedPlayer && player.getClass() != ServerPlayer.class) {
            return true;
        }

        if (player.connection == null) {
            return true;
        }
        try {
            player.getIpAddress().length();
            player.connection.connection.getRemoteAddress().toString();
        } catch (Exception exc) {
            return true;
        }
        return false;
    }

}
