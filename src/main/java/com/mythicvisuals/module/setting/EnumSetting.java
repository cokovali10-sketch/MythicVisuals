package com.mythicvisuals.module.setting;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final Class<T> type;

    public EnumSetting(String name, String description, T defaultValue, Class<T> type) {
        super(name, description, defaultValue);
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        this.type = type;
        set(defaultValue);
    }

    public Class<T> type() {
        return type;
    }

    public T[] options() {
        return type.getEnumConstants();
    }

    @Override
    public void set(T value) {
        this.value = value == null ? defaultValue : value;
    }

    /** Accepts enum instances or case-insensitive names. */
    @SuppressWarnings("unchecked")
    public void setByName(String name) {
        if (name == null) {
            reset();
            return;
        }
        for (T c : options()) {
            if (c.name().equalsIgnoreCase(name)) {
                set(c);
                return;
            }
        }
        reset();
    }

    @Override
    public Object toJsonValue() {
        return value == null ? null : value.name();
    }

    @Override
    public void fromJsonValue(Object o) {
        if (o instanceof String s) {
            setByName(s);
        } else {
            reset();
        }
    }
}
