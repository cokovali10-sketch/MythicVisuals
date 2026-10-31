package com.mythicvisuals.module.setting;

public class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description, defaultValue == null ? "" : defaultValue);
        this.maxLength = maxLength <= 0 ? 256 : maxLength;
        set(this.value);
    }

    public int maxLength() {
        return maxLength;
    }

    @Override
    public void set(String value) {
        String v = value == null ? defaultValue : value;
        this.value = v.length() > maxLength ? v.substring(0, maxLength) : v;
    }

    @Override
    public Object toJsonValue() {
        return value;
    }

    @Override
    public void fromJsonValue(Object o) {
        if (o instanceof String s) {
            set(s);
        } else {
            reset();
        }
    }
}
