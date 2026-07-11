/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.reflection;

import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ReflectionHelper
 * Created by HellFirePvP
 * Date: 21.04.2019 / 19:46
 */
public class ReflectionHelper {

    private static BiConsumer<ItemEntity, Boolean> itemEntitySkipPhysicRenderer;
    private static BiConsumer<ItemEntity, Integer> itemEntityAge;
    private static BiConsumer<ItemEntity, Float> itemEntityBobOffset;

    public static void setSkipItemPhysicsRender(ItemEntity entity) {
        if (itemEntitySkipPhysicRenderer == null) {
            itemEntitySkipPhysicRenderer = getFieldSetter(ItemEntity.class, "skipPhysicRenderer", Field::setBoolean);
        }

        itemEntitySkipPhysicRenderer.accept(entity, true);
    }

    public static void setItemEntityAge(ItemEntity entity, int age) {
        if (itemEntityAge == null) {
            itemEntityAge = getFieldSetter(ItemEntity.class, "age", Field::setInt);
        }

        itemEntityAge.accept(entity, age);
    }

    public static void setItemEntityBobOffset(ItemEntity entity, float bobOffset) {
        if (itemEntityBobOffset == null) {
            itemEntityBobOffset = getFieldSetter(ItemEntity.class, "bobOffs", Field::setFloat);
        }

        itemEntityBobOffset.accept(entity, bobOffset);
    }

    private static <T, V> BiConsumer<T, V> getFieldSetter(Class<T> owningClass, String fieldName, FieldSetter<T, V> fieldSetter) {
        final Field field = findField(owningClass, fieldName);
        if (field == null) {
            return (object, value) -> {};
        } else {
            return (object, value) -> {
                try {
                    fieldSetter.setFieldValue(field, object, value);
                } catch (Exception ignored) {}
            };
        }
    }

    @Nullable
    private static <T> Field findField(Class<T> owningClass, String fieldName) {
        try {
            return ObfuscationReflectionHelper.findField(owningClass, fieldName);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Function<Object[], Object> resolveConstructor(Class<?> owningClass, Class<?>... params) {
        return (invokeParams) -> {
            try {
                Constructor<?> ctor = owningClass.getDeclaredConstructor(params);
                ctor.setAccessible(true);
                return ctor.newInstance(invokeParams);
            } catch (Exception e) {
                throw new ReflectionException("Failed to resolve/call Constructor!", e);
            }
        };
    }

    private static BiFunction<Object, Object[], Object> resolveMethod(Class<?> owningClass, String methodName, Class<?>... params) {
        return (owningObject, invokeParams) -> {
            try {
                Method m = owningClass.getDeclaredMethod(methodName, params);
                m.setAccessible(true);
                return m.invoke(owningObject, invokeParams);
            } catch (Exception e) {
                throw new ReflectionException("Failed to resolve/call Method!", e);
            }
        };
    }

    private static interface FieldSetter<T, V> {

        void setFieldValue(Field f, T object, V value) throws IllegalAccessException;

    }
}
