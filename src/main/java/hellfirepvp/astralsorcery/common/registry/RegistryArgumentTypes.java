/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.cmd.argument.ArgumentTypeConstellation;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryArgumentTypes
 * Created by HellFirePvP
 * Date: 04.12.2020 / 16:21
 */
public class RegistryArgumentTypes {

    private RegistryArgumentTypes() {}

    public static void init() {
        register(AstralSorcery.key("constellation"), ArgumentTypeConstellation.class,
                SingletonArgumentInfo.contextFree(ArgumentTypeConstellation::any));
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> void register(ResourceLocation key, Class<A> argumentClazz, I info) {
        ArgumentTypeInfos.registerByClass(argumentClazz, info);
        AstralRegistries.register(AstralRegistries.COMMAND_ARGUMENT_TYPES, key, info);
    }
}
