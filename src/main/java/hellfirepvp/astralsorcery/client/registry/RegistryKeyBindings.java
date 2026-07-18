/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.input.KeyBindingWrapper;
import hellfirepvp.astralsorcery.client.input.KeyDisablePerkAbilities;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static hellfirepvp.astralsorcery.client.lib.KeyBindingsAS.DISABLE_PERK_ABILITIES;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryKeyBindings
 * Created by HellFirePvP
 * Date: 13.05.2020 / 18:43
 */
public class RegistryKeyBindings {

    private static final Set<KeyBindingWrapper> watchedKeyBindings = new HashSet<>();
    private static final Set<KeyBindingWrapper> bindingsPressed = new HashSet<>();

    public static void init(RegisterKeyMappingsEvent event) {
        DISABLE_PERK_ABILITIES = register(event, "disable_perk_abilities", GLFW.GLFW_KEY_V, KeyDisablePerkAbilities::new);

        NeoForge.EVENT_BUS.addListener(RegistryKeyBindings::onKeyInput);
    }

    private static KeyBindingWrapper register(RegisterKeyMappingsEvent event, String name, int glfwKey) {
        return register(event, name, glfwKey, keyBinding -> new KeyBindingWrapper(keyBinding) {});
    }

    private static KeyBindingWrapper register(RegisterKeyMappingsEvent event, String name, int glfwKey, Function<KeyMapping, KeyBindingWrapper> wrapperCreator) {
        KeyMapping keyBinding = new KeyMapping(String.format("key.%s.%s", AstralSorcery.MODID, name),
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, glfwKey, AstralSorcery.NAME);
        event.register(keyBinding);
        KeyBindingWrapper wrapper = wrapperCreator.apply(keyBinding);
        watchedKeyBindings.add(wrapper);
        return wrapper;
    }

    private static void onKeyInput(InputEvent.Key event) {
        InputConstants.Key from = InputConstants.getKey(event.getKey(), event.getScanCode());
        KeyBindingWrapper eventKey = MiscUtils.iterativeSearch(watchedKeyBindings, keyBinding -> keyBinding.getKeyBinding().getKey().equals(from));
        if (eventKey != null) {
            boolean isPressed = eventKey.getKeyBinding().isDown();
            boolean wasPressed = bindingsPressed.contains(eventKey);
            if (isPressed != wasPressed) {
                if (isPressed) {
                    bindingsPressed.add(eventKey);
                    eventKey.onKeyDown();
                } else {
                    bindingsPressed.remove(eventKey);
                    eventKey.onKeyUp();
                }
            }
        }
    }
}
