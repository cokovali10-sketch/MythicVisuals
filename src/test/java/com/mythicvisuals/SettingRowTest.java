package com.mythicvisuals;

import com.mythicvisuals.gui.SettingRow;
import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.ColorSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import com.mythicvisuals.module.setting.MultiSelectSetting;
import com.mythicvisuals.module.setting.NumberSetting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingRowTest {
    @Test
    void booleanRow() {
        BooleanSetting s = new BooleanSetting("shadow", "d", true);
        SettingRow row = SettingRow.setting(s);
        assertEquals(SettingRow.Kind.BOOLEAN, row.kind());
        assertEquals("shadow", row.label());
        assertEquals("ON", row.valueText(null));
        assertTrue(row.active(null));
    }

    @Test
    void sliderRow() {
        IntegerSetting s = new IntegerSetting("x", "d", 50, 0, 100, 1);
        SettingRow row = SettingRow.setting(s);
        assertEquals(SettingRow.Kind.SLIDER, row.kind());
        assertEquals(0.5, row.sliderRatio());
        assertEquals("50", row.sliderText());
        NumberSetting n = new NumberSetting("scale", "d", 1.5, 0.5, 2.0, 0.1);
        assertEquals(SettingRow.Kind.SLIDER, SettingRow.setting(n).kind());
    }

    @Test
    void colorRow() {
        ColorSetting s = new ColorSetting("text_color", "d", 0xFFFF0000);
        SettingRow row = SettingRow.setting(s);
        assertEquals(SettingRow.Kind.COLOR, row.kind());
        assertEquals("#FF0000", row.valueText(null));
    }

    @Test
    void multiOptions() {
        MultiSelectSetting s = new MultiSelectSetting("modes", "d",
                List.of("a", "b"), Set.of("a"));
        assertEquals(SettingRow.Kind.MULTI, SettingRow.setting(s).kind());
        assertEquals("1 selected", SettingRow.setting(s).valueText(null));
        SettingRow on = SettingRow.option(s, "a");
        SettingRow off = SettingRow.option(s, "b");
        assertEquals("ON", on.valueText(null));
        assertEquals("OFF", off.valueText(null));
        assertTrue(on.active(null));
        assertFalse(off.active(null));
    }
}
