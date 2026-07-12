/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.nbt;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTHelper
 * Created by HellFirePvP
 * Date: 07.05.2016 / 02:15
 */
public class NBTHelper {

    @Nonnull
    public static CompoundTag getPersistentData(Entity entity) {
        return getPersistentData(entity.getPersistentData());
    }

    @Nonnull
    public static CompoundTag getPersistentData(ItemStack item) {
        return getPersistentData(getData(item));
    }

    @Nonnull
    public static CompoundTag getPersistentData(CompoundTag base) {
        CompoundTag pattern;
        if (hasPersistentData(base)) {
            pattern = base.getCompound(AstralSorcery.MODID);
        } else {
            pattern = new CompoundTag();
            base.put(AstralSorcery.MODID, pattern);
        }
        return pattern;
    }

    public static boolean hasPersistentData(Entity entity) {
        return hasPersistentData(entity.getPersistentData());
    }

    public static boolean hasPersistentData(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        return data != null && hasPersistentData(data.getUnsafe());
    }

    public static boolean hasPersistentData(CompoundTag base) {
        return base.contains(AstralSorcery.MODID) && base.get(AstralSorcery.MODID) instanceof CompoundTag;
    }


    public static void removePersistentData(Entity entity) {
        removePersistentData(entity.getPersistentData());
    }

    public static void removePersistentData(ItemStack item) {
        CustomData.update(DataComponents.CUSTOM_DATA, item, NBTHelper::removePersistentData);
    }

    public static void removePersistentData(CompoundTag base) {
        base.remove(AstralSorcery.MODID);
    }

    public static void deepMerge(CompoundTag dst, CompoundTag src, boolean uniqueArrayEntries) {
        for (String s : src.getAllKeys()) {
            Tag nbtElement = src.get(s);
            if (nbtElement.getId() == Constants.NBT.TAG_COMPOUND) {
                if (dst.contains(s, Constants.NBT.TAG_COMPOUND)) {
                    deepMerge(dst.getCompound(s), (CompoundTag) nbtElement, uniqueArrayEntries);
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Constants.NBT.TAG_LIST) {
                if (dst.contains(s, Constants.NBT.TAG_LIST)) {
                    ListTag dstList = (ListTag) dst.get(s);
                    ListTag srcList = (ListTag) nbtElement;
                    if (dstList.getElementType() == srcList.getElementType()) {
                        deepMergeList(dstList, srcList);
                    } else {
                        dst.put(s, srcList.copy());
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Constants.NBT.TAG_INT_ARRAY) {
                if (dst.contains(s, Constants.NBT.TAG_INT_ARRAY)) {
                    IntArrayTag dstArr = (IntArrayTag) dst.get(s);
                    IntArrayTag srcArr = (IntArrayTag) nbtElement;
                    if (uniqueArrayEntries) {
                        for (IntTag value : srcArr) {
                            if (!dstArr.contains(value)) {
                                dstArr.add(value);
                            }
                        }
                    } else {
                        dstArr.addAll(srcArr);
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Constants.NBT.TAG_LONG_ARRAY) {
                if (dst.contains(s, Constants.NBT.TAG_LONG_ARRAY)) {
                    LongArrayTag dstArr = (LongArrayTag) dst.get(s);
                    LongArrayTag srcArr = (LongArrayTag) nbtElement;
                    if (uniqueArrayEntries) {
                        for (LongTag value : srcArr) {
                            if (!dstArr.contains(value)) {
                                dstArr.add(value);
                            }
                        }
                    } else {
                        dstArr.addAll(srcArr);
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Constants.NBT.TAG_BYTE_ARRAY) {
                if (dst.contains(s, Constants.NBT.TAG_BYTE_ARRAY)) {
                    ByteArrayTag dstArr = (ByteArrayTag) dst.get(s);
                    ByteArrayTag srcArr = (ByteArrayTag) nbtElement;
                    if (uniqueArrayEntries) {
                        for (ByteTag value : srcArr) {
                            if (!dstArr.contains(value)) {
                                dstArr.add(value);
                            }
                        }
                    } else {
                        dstArr.addAll(srcArr);
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else {
                dst.put(s, nbtElement.copy());
            }
        }
    }

    //Stupid NBT stuff ahead. the iterator and the actual .get returns from 2 different lists.
    //Don't use the iterator on ListNBT...
    private static void deepMergeList(ListTag dst, ListTag src) {
        for (int j = 0; j < src.size(); j++) {
            Tag toAdd = src.get(j);

            boolean found = false;
            for (int i = 0; i < dst.size(); i++) {
                Tag existing = dst.get(i);
                if (existing.equals(toAdd)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dst.add(toAdd.copy());
            }
        }
    }

    @Nonnull
    public static <E, N extends Tag> List<E> readList(CompoundTag nbt, String key, int type, Function<N, E> reader) {
        if (!nbt.contains(key, Constants.NBT.TAG_LIST)) {
            return new ArrayList<>();
        }
        return readList(nbt.getList(key, type), reader);
    }

    @Nonnull
    public static <E, N extends Tag> List<E> readList(ListTag nbt, Function<N, E> reader) {
        return nbt.stream()
                .map(n -> reader.apply((N) n))
                .collect(Collectors.toList());
    }

    @Nonnull
    public static <E, N extends Tag> Set<E> readSet(CompoundTag nbt, String key, int type, Function<N, E> reader) {
        if (!nbt.contains(key, Constants.NBT.TAG_LIST)) {
            return new HashSet<>();
        }
        return readSet(nbt.getList(key, type), reader);
    }

    @Nonnull
    public static <E, N extends Tag> Set<E> readSet(ListTag nbt, Function<N, E> reader) {
        return nbt.stream()
                .map(n -> reader.apply((N) n))
                .collect(Collectors.toSet());
    }

    public static <E> void writeList(CompoundTag tag, String key, Collection<E> HELPER, Function<E, Tag> serializer) {
        tag.put(key, writeList(HELPER, serializer));
    }

    public static <E> ListTag writeList(Collection<E> HELPER, Function<E, Tag> serializer) {
        ListTag nbt = new ListTag();
        nbt.addAll(HELPER.stream()
                .map(serializer)
                .collect(Collectors.toList()));
        return nbt;
    }

    public static CompoundTag getData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            CompoundTag pattern = new CompoundTag();
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(pattern));
            data = stack.get(DataComponents.CUSTOM_DATA);
        }
        return data.getUnsafe();
    }

    @Nullable
    public static CompoundTag getExistingData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.getUnsafe();
    }

    public static <T> void writeOptional(CompoundTag nbt, String key, @Nullable T object, BiConsumer<CompoundTag, T> writer) {
        nbt.putBoolean(key + "_present", object != null);
        if (object != null) {
            CompoundTag write = new CompoundTag();
            writer.accept(write, object);
            nbt.put(key, write);
        }
    }

    @Nullable
    public static <T> T readOptional(CompoundTag nbt, String key, Function<CompoundTag, T> reader) {
        return readOptional(nbt, key, reader, null);
    }

    @Nullable
    public static <T> T readOptional(CompoundTag nbt, String key, Function<CompoundTag, T> reader, T _default) {
        if (nbt.getBoolean(key + "_present")) {
            CompoundTag usage = nbt.getCompound(key);
            return reader.apply(usage);
        }
        return _default;
    }

    public static <T extends Enum<T>> void writeEnum(CompoundTag nbt, String key, T enumValue) {
        nbt.putInt(key, enumValue.ordinal());
    }

    public static <T extends Enum<T>> T readEnum(CompoundTag nbt, String key, Class<T> enumClazz) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Passed class is not an enum!");
        }
        return enumClazz.getEnumConstants()[nbt.getInt(key)];
    }

    public static void setBlock(CompoundTag cmp, String key, BlockState state) {
        CompoundTag serialized = getBlockStateNBTTag(state);
        cmp.put(key, serialized);
    }

    @Nullable
    public static BlockState getBlockState(CompoundTag cmp, String key) {
        return getBlockStateFromTag(cmp.getCompound(key));
    }

    @Nonnull
    public static CompoundTag getBlockStateNBTTag(BlockState state) {
        ResourceLocation blockKey = RegistryHelper.getKey(state.getBlock());
        if (blockKey == null) {
            state = Blocks.AIR.defaultBlockState();
            blockKey = RegistryHelper.getKey(state.getBlock());
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("registryName", blockKey.toString());
        ListTag properties = new ListTag();
        for (Property property : state.getProperties()) {
            CompoundTag propTag = new CompoundTag();
            try {
                propTag.putString("value", property.getName(state.getValue(property)));
            } catch (Exception exc) {
                continue;
            }
            propTag.putString("property", property.getName());
            properties.add(propTag);
        }
        tag.put("properties", properties);
        return tag;
    }

    @Nullable
    public static BlockState getBlockStateFromTag(CompoundTag cmp) {
        return getBlockStateFromTag(cmp, null);
    }

    @Nullable
    public static <T extends Comparable<T>> BlockState getBlockStateFromTag(CompoundTag cmp, BlockState _default) {
        ResourceLocation key = ResourceLocation.parse(cmp.getString("registryName"));
        Block block = BuiltInRegistries.BLOCK.get(key);
        if (block == null || block == Blocks.AIR) return _default;
        BlockState state = block.defaultBlockState();
        Collection<Property<?>> properties = state.getProperties();
        ListTag list = cmp.getList("properties", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag propertyTag = list.getCompound(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            Property<T> match = (Property<T>) MiscUtils.iterativeSearch(properties, prop -> prop.getName().equalsIgnoreCase(propertyStr));
            if (match != null) {
                try {
                    Optional<T> opt = match.getValue(valueStr);
                    if (opt.isPresent()) {
                        state = state.setValue(match, opt.get());
                    }
                } catch (Throwable tr) {} // Thanks Exu2
            }
        }
        return state;
    }

    public static void setAsSubTag(CompoundTag pattern, String tag, Consumer<CompoundTag> applyFct) {
        CompoundTag newTag = new CompoundTag();
        applyFct.accept(newTag);
        pattern.put(tag, newTag);
    }

    @Nullable
    public static <T> T readFromSubTag(CompoundTag pattern, String tag, Function<CompoundTag, T> readFct) {
        if (pattern.contains(tag, Constants.NBT.TAG_COMPOUND)) {
            return readFct.apply(pattern.getCompound(tag));
        }
        return null;
    }

    public static void setRegistryEntry(CompoundTag compoundNBT, String tag, Object entry) {
        ResourceLocation registryName = RegistryHelper.getRegistryName(entry);
        ResourceLocation entryName = RegistryHelper.getKey(entry);
        if (registryName == null || entryName == null) {
            throw new IllegalArgumentException("Unregistered value cannot be serialized: " + entry);
        }
        setResourceLocation(compoundNBT, tag + "_registry", registryName);
        setResourceLocation(compoundNBT, tag, entryName);
    }

    @Nullable
    public static <T> T getRegistryEntry(CompoundTag compoundNBT, String tag) {
        ResourceLocation registryName = getId(compoundNBT, tag + "_registry");
        if (registryName != null) {
            ResourceLocation key = getId(compoundNBT, tag);
            if (key != null) {
                return RegistryHelper.getValue(registryName, key);
            }
        }
        return null;
    }

    public static void setResourceLocation(CompoundTag compoundNBT, String tag, ResourceLocation key) {
        compoundNBT.putString(tag, key.toString());
    }

    @Nullable
    public static ResourceLocation getId(CompoundTag compoundNBT, String tag) {
        if (compoundNBT.contains(tag)) {
            return ResourceLocation.parse(compoundNBT.getString(tag));
        }
        return null;
    }

    // 1.21 port: ItemStack/FluidStack (de)serialization now goes through data component codecs that
    // need a HolderLookup.Provider (registry access) rather than plain write(CompoundTag)/read(CompoundTag).
    // None of the current callers of setStack/getStack/setFluid/getType have a Level in scope, so we fall
    // back to RegistryAccess.EMPTY here - same fallback already used by TileInventory#registryAccess() in
    // this codebase. This is lossy for components that reference dynamic registries (e.g. enchantments),
    // consistent with the caveat already called out in DamageSourceUtil for other data-driven registries.
    public static void setStack(CompoundTag pattern, String tag, ItemStack stack) {
        pattern.put(tag, stack.saveOptional(RegistryAccess.EMPTY));
    }

    public static ItemStack getStack(CompoundTag pattern, String tag) {
        return ObjectUtils.firstNonNull(readFromSubTag(pattern, tag, sub -> ItemStack.parseOptional(RegistryAccess.EMPTY, sub)), ItemStack.EMPTY);
    }

    public static void setFluid(CompoundTag pattern, String tag, FluidStack stack) {
        setAsSubTag(pattern, tag, sub -> stack.save(RegistryAccess.EMPTY, sub));
    }

    public static FluidStack getType(CompoundTag pattern, String tag) {
        return ObjectUtils.firstNonNull(readFromSubTag(pattern, tag, sub -> FluidStack.parseOptional(RegistryAccess.EMPTY, sub)), FluidStack.EMPTY);
    }

    public static void removeUUID(CompoundTag pattern, String key) {
        pattern.remove(key);
    }

    public static UUID getUUID(CompoundTag compoundNBT, String key, UUID _default) {
        if (compoundNBT.hasUUID(key)) {
            return compoundNBT.getUUID(key);
        }
        return _default;
    }

    public static CompoundTag writeBlockPosToNBT(BlockPos pos, CompoundTag pattern) {
        pattern.putInt("bposX", pos.getX());
        pattern.putInt("bposY", pos.getY());
        pattern.putInt("bposZ", pos.getZ());
        return pattern;
    }

    public static BlockPos readBlockPosFromNBT(CompoundTag pattern) {
        int x = pattern.getInt("bposX");
        int y = pattern.getInt("bposY");
        int z = pattern.getInt("bposZ");
        return new BlockPos(x, y, z);
    }

    public static CompoundTag writeVector3(Vector3 v) {
        CompoundTag cmp = new CompoundTag();
        writeVector3(v, cmp);
        return cmp;
    }

    public static CompoundTag writeVector3(Vector3 v, CompoundTag pattern) {
        pattern.putDouble("vecPosX", v.getX());
        pattern.putDouble("vecPosY", v.getY());
        pattern.putDouble("vecPosZ", v.getZ());
        return pattern;
    }

    public static Vector3 readVector3(CompoundTag pattern) {
        return new Vector3(
                pattern.getDouble("vecPosX"),
                pattern.getDouble("vecPosY"),
                pattern.getDouble("vecPosZ"));
    }

    public static CompoundTag writeBoundingBox(AABB box, CompoundTag tag) {
        tag.putDouble("boxMinX", box.minX);
        tag.putDouble("boxMinY", box.minY);
        tag.putDouble("boxMinZ", box.minZ);
        tag.putDouble("boxMaxX", box.maxX);
        tag.putDouble("boxMaxY", box.maxY);
        tag.putDouble("boxMaxZ", box.maxZ);
        return tag;
    }

    public static AABB readBoundingBox(CompoundTag tag) {
        return new AABB(
                tag.getDouble("boxMinX"),
                tag.getDouble("boxMinY"),
                tag.getDouble("boxMinZ"),
                tag.getDouble("boxMaxX"),
                tag.getDouble("boxMaxY"),
                tag.getDouble("boxMaxZ"));
    }
}
