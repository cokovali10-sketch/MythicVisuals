package com.mythicvisuals.render;

import com.mythicvisuals.core.CrashGuard;
import com.mythicvisuals.core.hud.HudModule;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Draws every enabled {@link HudModule} as one vanilla HUD layer.
 * An element failing 5 frames in a row is auto-disabled for the session
 * (logged once) so one broken element can never kill the HUD.
 */
public final class HudRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudRenderer.class);
    private static final int MAX_FAILURES = 5;
    private static boolean registered;

    private HudRenderer() {
    }

    public static void init(List<HudModule> elements) {
        // Idempotent: a second layer would double-draw everything.
        if (registered) {
            return;
        }
        registered = true;
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("mythicvisuals", "modules"),
                (graphics, delta) -> {
                    try {
                        renderAll(elements, graphics);
                    } catch (Exception e) {
                        CrashGuard.warnOnce(LOGGER, "hud-layer", "HUD layer failed", e);
                    }
                });
    }

    private static void renderAll(List<HudModule> elements, GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) {
            return;
        }
        for (HudModule element : elements) {
            if (!element.isEnabled()) {
                continue;
            }
            String key = "hud:" + element.id();
            if (CrashGuard.failures(key) >= MAX_FAILURES) {
                continue; // auto-disabled for this session
            }
            try {
                element.render(graphics, client);
            } catch (Exception e) {
                boolean alive = CrashGuard.noteFailure(key, MAX_FAILURES);
                CrashGuard.warnOnce(LOGGER, key,
                        alive ? "HUD element failed, will retry"
                                : "HUD element failed 5x, disabled for this session", e);
            }
        }
    }
}
