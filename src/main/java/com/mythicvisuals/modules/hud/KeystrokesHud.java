package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.List;

public class KeystrokesHud extends HudModule {
    public KeystrokesHud() {
        super("keystrokes", "Keystrokes", "Pressed movement keys and mouse buttons", 4, 64);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        Options o = client.options;
        return List.of(
                key("W", o.keyUp.isDown()),
                key("A", o.keyLeft.isDown())
                        + " " + key("S", o.keyDown.isDown())
                        + " " + key("D", o.keyRight.isDown()),
                key("Space", o.keyJump.isDown()),
                key("LMB", o.keyAttack.isDown())
                        + " " + key("RMB", o.keyUse.isDown()));
    }

    private static String key(String label, boolean pressed) {
        return (pressed ? "[x] " : "[ ] ") + label;
    }
}
