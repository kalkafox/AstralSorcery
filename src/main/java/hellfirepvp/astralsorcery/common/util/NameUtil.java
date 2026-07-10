/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import com.google.common.base.CaseFormat;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NameUtil
 * Created by HellFirePvP
 * Date: 23.09.2019 / 18:09
 */
public class NameUtil {

    public static ResourceLocation prefixPath(ResourceLocation key, String prefix) {
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), prefix + key.getPath());
    }

    public static ResourceLocation suffixPath(ResourceLocation key, String playerSuffix) {
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath() + playerSuffix);
    }

    public static ResourceLocation fromClass(Object object) {
        return fromClass(object, null);
    }

    public static ResourceLocation fromClass(Class<?> clazz) {
        return fromClass(clazz, null);
    }

    public static ResourceLocation fromClass(Object object, @Nullable String cutPrefix) {
        return fromClass(object, cutPrefix, null);
    }

    public static ResourceLocation fromClass(Class<?> clazz, @Nullable String cutPrefix) {
        return fromClass(clazz, cutPrefix, null);
    }

    public static ResourceLocation fromClass(Object object, @Nullable String cutPrefix, @Nullable String cutSuffix) {
        return fromClass(object.getClass(), cutPrefix, cutSuffix);
    }

    public static ResourceLocation fromClass(Class<?> clazz, @Nullable String cutPrefix, @Nullable String cutSuffix) {
        String name = clazz.getName();
        if (clazz.getEnclosingClass() != null) {
            name = clazz.getEnclosingClass().getName() + name;
        }
        if (cutPrefix != null && name.startsWith(cutPrefix)) {
            name = name.substring(cutPrefix.length());
        }
        if (cutSuffix != null && name.endsWith(cutSuffix)) {
            name = name.substring(0, name.length() - cutSuffix.length());
        }
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        return AstralSorcery.key(name);
    }

}
