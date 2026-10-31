package com.mythicvisuals;

import com.mythicvisuals.core.ConfigManager;
import com.mythicvisuals.core.KeybindManager;
import com.mythicvisuals.core.ModuleManager;
import com.mythicvisuals.core.hud.HudModule;
import com.mythicvisuals.gui.ClickGui;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.modules.combat.EnemyEsp;
import com.mythicvisuals.modules.combat.HitParticles;
import com.mythicvisuals.modules.combat.HitSounds;
import com.mythicvisuals.modules.combat.KillEffects;
import com.mythicvisuals.modules.combat.ToggleSprint;
import com.mythicvisuals.modules.combat.Trails;
import com.mythicvisuals.modules.debug.DebugModule;
import com.mythicvisuals.modules.hud.ArmorHud;
import com.mythicvisuals.modules.hud.ComboHud;
import com.mythicvisuals.modules.hud.HitMarkers;
import com.mythicvisuals.modules.hud.LowHp;
import com.mythicvisuals.modules.hud.CoordinatesHud;
import com.mythicvisuals.modules.hud.CpsHud;
import com.mythicvisuals.modules.hud.DamageNumbers;
import com.mythicvisuals.modules.hud.DirectionHud;
import com.mythicvisuals.modules.hud.FpsHud;
import com.mythicvisuals.modules.hud.KeystrokesHud;
import com.mythicvisuals.modules.hud.PingHud;
import com.mythicvisuals.modules.hud.PotionsHud;
import com.mythicvisuals.modules.hud.ReachHud;
import com.mythicvisuals.modules.hud.TargetHud;
import com.mythicvisuals.modules.visuals.BlockHighlight;
import com.mythicvisuals.modules.visuals.Crosshair;
import com.mythicvisuals.modules.visuals.DamageFlash;
import com.mythicvisuals.modules.visuals.FovChanger;
import com.mythicvisuals.modules.visuals.FullBright;
import com.mythicvisuals.render.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phase 3 entry point: managers + debug module + HUD elements + persisted state.
 */
public class MythicVisuals implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Branding.MOD_ID);

    private static MythicVisuals instance;

    private ModuleManager modules;
    private ConfigManager config;
    private KeybindManager keybinds;

    public static MythicVisuals get() {
        return instance;
    }

    public ModuleManager modules() {
        return modules;
    }

    public ConfigManager config() {
        return config;
    }

    public KeybindManager keybinds() {
        return keybinds;
    }

    @Override
    public void onInitializeClient() {
        try {
            init();
        } catch (Exception e) {
            LOGGER.error("[{}] initialization failed, mod disabled (game continues)",
                    Branding.MOD_NAME, e);
        }
    }

    private void init() {
        instance = this;
        modules = new ModuleManager();
        config = new ConfigManager(modules);
        modules.register(new DebugModule());
        java.util.List<HudModule> hud = java.util.List.of(
                new FpsHud(), new CpsHud(), new PingHud(), new CoordinatesHud(),
                new DirectionHud(), new KeystrokesHud(), new ArmorHud(), new PotionsHud(),
                new ComboHud(), new ReachHud(), new TargetHud(), new HitMarkers(), new LowHp(),
                new DamageNumbers());
        for (HudModule element : hud) {
            modules.register(element);
        }
        modules.register(new FullBright());
        modules.register(new FovChanger());
        modules.register(new BlockHighlight());
        modules.register(new DamageFlash());
        modules.register(new Crosshair());
        modules.register(new ToggleSprint());
        modules.register(new EnemyEsp());
        modules.register(new HitParticles());
        modules.register(new HitSounds());
        modules.register(new KillEffects());
        modules.register(new Trails());
        HudRenderer.init(hud);
        config.load();
        keybinds = new KeybindManager(modules);
        keybinds.register();
        KeyMapping guiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mythicvisuals.gui", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, KeybindManager.CATEGORY));
        // NOTE: never resolve key display names here (getTranslatedKeyMessage
        // calls native glfwGetKeyName, illegal before the window exists and
        // crashes startup in _initGlfw). Log the raw key code only.
        LOGGER.info("[{}] GUI key bound to code {} ({} modules have toggle binds).",
                Branding.MOD_NAME, GLFW.GLFW_KEY_RIGHT_SHIFT, modules.all().size());
        LOGGER.info("[{}] v{} initialized: {} module(s) registered.",
                Branding.MOD_NAME, Branding.MOD_VERSION, modules.all().size());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> config.save());
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                tick(client, guiKey);
            } catch (Exception e) {
                LOGGER.warn("[{}] client tick failed (game continues)", Branding.MOD_NAME, e);
            }
        });
    }

    private void tick(net.minecraft.client.Minecraft client, KeyMapping guiKey) {
        while (guiKey.consumeClick()) {
            LOGGER.info("[{}] GUI key pressed, screen={}.", Branding.MOD_NAME, client.gui.screen());
            if (client.gui.screen() instanceof ClickGui) {
                client.gui.setScreen(null);
            } else {
                client.gui.setScreen(new ClickGui(modules, keybinds, config, () -> config.save()));
            }
        }
        for (Module m : modules.all()) {
            if (m.isEnabled() && m instanceof ClientTickable tickable) {
                try {
                    tickable.clientTick(client);
                } catch (Exception e) {
                    LOGGER.warn("[{}] tick failed for '{}'", Branding.MOD_NAME, m.id(), e);
                }
            }
        }
    }
}
