package com.mythicvisuals;

import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.ColorSetting;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import com.mythicvisuals.module.setting.KeybindSetting;
import com.mythicvisuals.module.setting.MultiSelectSetting;
import com.mythicvisuals.module.setting.NumberSetting;
import com.mythicvisuals.module.setting.StringSetting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {

    enum Mode { A, B }

    @Test
    void booleanRoundtrip() {
        BooleanSetting s = new BooleanSetting("x", "", true);
        assertEquals(true, s.toJsonValue());
        s.fromJsonValue(false);
        assertFalse(s.get());
        s.fromJsonValue("nope");
        assertTrue(s.get()); // bad input resets to default=true
    }

    @Test
    void booleanBadInputResets() {
        BooleanSetting s = new BooleanSetting("x", "", true);
        s.fromJsonValue(42);
        assertTrue(s.get());
    }

    @Test
    void numberClamps() {
        NumberSetting s = new NumberSetting("x", "", 5.0, 0.0, 10.0, 0.5);
        s.set(99.0);
        assertEquals(10.0, s.get());
        s.set(-3.0);
        assertEquals(0.0, s.get());
        s.fromJsonValue(7);
        assertEquals(7.0, s.get());
        s.fromJsonValue("bad");
        assertEquals(5.0, s.get());
    }

    @Test
    void integerClamps() {
        IntegerSetting s = new IntegerSetting("x", "", 3, 0, 5, 1);
        s.set(99);
        assertEquals(5, s.get());
        s.fromJsonValue(2.9);
        assertEquals(2, s.get());
    }

    @Test
    void enumByName() {
        EnumSetting<Mode> s = new EnumSetting<>("x", "", Mode.A, Mode.class);
        assertEquals("A", s.toJsonValue());
        s.setByName("b");
        assertEquals(Mode.B, s.get());
        s.setByName("zzz");
        assertEquals(Mode.A, s.get());
        s.fromJsonValue("B");
        assertEquals(Mode.B, s.get());
    }

    @Test
    void colorHex() {
        ColorSetting s = new ColorSetting("x", "", 0xFF112233);
        assertEquals(0x11, s.red());
        s.setHex("#00FF00");
        assertEquals(0xFF00FF00, s.get());
        s.setHex("garbage");
        assertEquals(0xFF112233, s.get());
        s.fromJsonValue(0xFF000000);
        assertEquals(0xFF000000, s.get());
    }

    @Test
    void keybind() {
        KeybindSetting s = new KeybindSetting("x", "", KeybindSetting.UNBOUND);
        assertFalse(s.isBound());
        s.set(33);
        assertTrue(s.isBound());
        assertEquals(33, s.toJsonValue());
        s.fromJsonValue(null);
        assertFalse(s.isBound());
    }

    @Test
    void stringCapsLength() {
        StringSetting s = new StringSetting("x", "", "abc", 2);
        assertEquals("ab", s.get());
        s.fromJsonValue("hello");
        assertEquals("he", s.get());
        s.fromJsonValue(5);
        assertEquals("ab", s.get());
    }

    @Test
    void multiSelectFiltersUnknown() {
        MultiSelectSetting s = new MultiSelectSetting("x", "",
                List.of("a", "b"), Set.of("a", "zzz"));
        assertEquals(Set.of("a"), s.get());
        s.toggle("b");
        assertEquals(Set.of("a", "b"), s.get());
        s.toggle("a");
        assertEquals(Set.of("b"), s.get());
        s.toggle("nope");
        assertEquals(Set.of("b"), s.get());
        s.fromJsonValue(List.of("a", "nope"));
        assertEquals(Set.of("a"), s.get());
        assertEquals(List.of("a"), s.toJsonValue());
    }
}
