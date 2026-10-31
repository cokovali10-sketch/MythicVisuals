package com.mythicvisuals.core;

import com.mythicvisuals.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * One toggle keybind per module (KeyCategory "Mythic Visuals").
 * Keybinds live in vanilla controls; toggle state in ModuleManager.
 */
public class KeybindManager {
    /** Translation key resolved by the game: key.category.mythicvisuals.main. */
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mythicvisuals", "main"));

    private final ModuleManager modules;
    private final Map<String, KeyMapping> bindings = new HashMap<>();
    private boolean registered;

    public KeybindManager(ModuleManager modules) {
        this.modules = modules;
    }

    public void register() {
        // Idempotent: entrypoints must survive repeated init in one JVM.
        if (registered) {
            return;
        }
        registered = true;
        for (Module m : modules.all()) {
            KeyMapping binding = new KeyMapping(
                    "key.mythicvisuals.module." + m.id(),
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    CATEGORY);
            KeyMappingHelper.registerKeyMapping(binding);
            bindings.put(m.id(), binding);
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                tick(client);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(KeybindManager.class)
                        .warn("[mythicvisuals] keybind tick failed (game continues)", e);
            }
        });
    }

    private void tick(net.minecraft.client.Minecraft client) {
        if (client.player == null) {
            return;
        }
        for (Module m : modules.all()) {
            KeyMapping b = bindings.get(m.id());
            if (b != null) {
                while (b.consumeClick()) {
                    modules.toggle(m);
                }
            }
        }
    }

    public KeyMapping bindingFor(String moduleId) {
        return bindings.get(moduleId);
    }
}
