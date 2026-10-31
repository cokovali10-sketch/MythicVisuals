package com.mythicvisuals.gui;

import com.mythicvisuals.core.KeybindManager;
import com.mythicvisuals.core.ModuleManager;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.ColorSetting;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import com.mythicvisuals.module.setting.KeybindSetting;
import com.mythicvisuals.module.setting.MultiSelectSetting;
import com.mythicvisuals.module.setting.NumberSetting;
import com.mythicvisuals.module.setting.Setting;
import com.mythicvisuals.module.setting.StringSetting;
import net.minecraft.client.KeyMapping;

/** One rendered row in the settings panel. Pure view-model, unit testable. */
public final class SettingRow {
    public enum Kind {
        MODULE_BIND, MODULE_TOGGLE, BOOLEAN, SLIDER, ENUM, COLOR, KEYBIND, STRING, MULTI, MULTI_OPTION
    }

    private final Kind kind;
    private final Module module;
    private final Setting<?> setting;
    private final String option;

    private SettingRow(Kind kind, Module module, Setting<?> setting, String option) {
        this.kind = kind;
        this.module = module;
        this.setting = setting;
        this.option = option;
    }

    public static SettingRow moduleBind(Module module) {
        return new SettingRow(Kind.MODULE_BIND, module, null, null);
    }

    public static SettingRow moduleToggle(Module module) {
        return new SettingRow(Kind.MODULE_TOGGLE, module, null, null);
    }

    public static SettingRow setting(Setting<?> setting) {
        return new SettingRow(kindFor(setting), null, setting, null);
    }

    public static SettingRow option(MultiSelectSetting setting, String option) {
        return new SettingRow(Kind.MULTI_OPTION, null, setting, option);
    }

    private static Kind kindFor(Setting<?> s) {
        if (s instanceof BooleanSetting) {
            return Kind.BOOLEAN;
        }
        if (s instanceof IntegerSetting || s instanceof NumberSetting) {
            return Kind.SLIDER;
        }
        if (s instanceof EnumSetting) {
            return Kind.ENUM;
        }
        if (s instanceof ColorSetting) {
            return Kind.COLOR;
        }
        if (s instanceof KeybindSetting) {
            return Kind.KEYBIND;
        }
        if (s instanceof StringSetting) {
            return Kind.STRING;
        }
        return Kind.MULTI;
    }

    public Kind kind() {
        return kind;
    }

    public Setting<?> setting() {
        return setting;
    }

    public String option() {
        return option;
    }

    public String label() {
        return switch (kind) {
            case MODULE_BIND -> "Keybind";
            case MODULE_TOGGLE -> "Enabled";
            case MULTI_OPTION -> "  " + option;
            default -> setting.name();
        };
    }

    public String valueText(KeybindManager keybinds) {
        return switch (kind) {
            case MODULE_BIND -> {
                KeyMapping b = keybinds.bindingFor(module.id());
                yield b == null ? "--" : b.getTranslatedKeyMessage().getString();
            }
            case MODULE_TOGGLE -> module.isEnabled() ? "ON" : "OFF";
            case BOOLEAN -> ((BooleanSetting) setting).get() ? "ON" : "OFF";
            case SLIDER -> sliderText();
            case ENUM -> ((EnumSetting<?>) setting).get().toString();
            case COLOR -> String.format("#%06X", ((ColorSetting) setting).get() & 0xFFFFFF);
            case KEYBIND -> keyName(((KeybindSetting) setting).get());
            case STRING -> ((StringSetting) setting).get();
            case MULTI -> ((MultiSelectSetting) setting).get().size() + " selected";
            case MULTI_OPTION -> ((MultiSelectSetting) setting).get().contains(option) ? "ON" : "OFF";
        };
    }

    public boolean active(ModuleManager modules) {
        return switch (kind) {
            case MODULE_TOGGLE -> module.isEnabled();
            case BOOLEAN -> ((BooleanSetting) setting).get();
            case MULTI_OPTION -> ((MultiSelectSetting) setting).get().contains(option);
            default -> true;
        };
    }

    public double sliderRatio() {
        if (setting instanceof IntegerSetting s) {
            return (s.get() - s.min()) / (double) Math.max(1, s.max() - s.min());
        }
        if (setting instanceof NumberSetting s) {
            return (s.get() - s.min()) / Math.max(1e-9, s.max() - s.min());
        }
        return 0;
    }

    public String sliderText() {
        if (setting instanceof IntegerSetting s) {
            return String.valueOf(s.get());
        }
        if (setting instanceof NumberSetting s) {
            return String.valueOf(Math.round(s.get() * 100.0) / 100.0);
        }
        return "";
    }

    private static String keyName(int code) {
        return code == KeybindSetting.UNBOUND ? "NONE" : "KEY " + code;
    }
}
