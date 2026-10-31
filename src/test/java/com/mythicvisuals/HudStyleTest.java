package com.mythicvisuals;

import com.mythicvisuals.core.hud.Anchor;
import com.mythicvisuals.modules.hud.FpsHud;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudStyleTest {
    @Test
    void defaults() {
        FpsHud hud = new FpsHud();
        assertEquals(0xFFFFFFFF, (int) hud.textColor.get());
        assertTrue(hud.shadow.get());
        assertTrue(hud.background.get());
        assertEquals(110, (int) hud.backgroundOpacity.get());
        assertEquals(1.0, hud.scale.get());
        assertEquals(Anchor.RIGHT, hud.anchor.get());
    }

    @Test
    void styleRoundtrip() {
        FpsHud hud = new FpsHud();
        hud.textColor.set(0xFFFF0000);
        hud.shadow.set(false);
        hud.scale.set(1.5);
        Map<String, Object> saved = hud.serialize();

        FpsHud loaded = new FpsHud();
        loaded.deserialize(saved);
        assertEquals(0xFFFF0000, (int) loaded.textColor.get());
        assertEquals(false, loaded.shadow.get());
        assertEquals(1.5, loaded.scale.get());
    }
}
