package com.mythicvisuals.module.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public void set(Boolean value) {
        this.value = value != null && value;
    }

    @Override
    public Object toJsonValue() {
        return value;
    }

    @Override
    public void fromJsonValue(Object o) {
        if (o instanceof Boolean b) {
            set(b);
        } else {
            reset();
        }
    }
}
