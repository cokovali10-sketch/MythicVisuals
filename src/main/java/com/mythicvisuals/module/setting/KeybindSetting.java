package com.mythicvisuals.module.setting;

/**
 * Raw key code storage (GLFW codes, -1 = unbound). Rendering a human-readable
 * name is the GUI layer's job, so this class stays free of client imports.
 */
public class KeybindSetting extends Setting<Integer> {
    public static final int UNBOUND = -1;

    public KeybindSetting(String name, String description, int defaultKeyCode) {
        super(name, description, defaultKeyCode);
    }

    @Override
    public void set(Integer value) {
        this.value = value == null ? defaultValue : value;
    }

    public boolean isBound() {
        return value != UNBOUND;
    }

    @Override
    public Object toJsonValue() {
        return value;
    }

    @Override
    public void fromJsonValue(Object o) {
        if (o instanceof Number n) {
            set(n.intValue());
        } else {
            reset();
        }
    }
}
