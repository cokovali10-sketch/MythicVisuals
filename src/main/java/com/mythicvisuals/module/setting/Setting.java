package com.mythicvisuals.module.setting;

import java.util.Objects;

/**
 * Typed module setting with a default, reset and JSON-safe
 * serialization (String/Number/Boolean/List only, no Gson here).
 */
public abstract class Setting<T> {
    protected final String name;
    protected final String description;
    protected T value;
    protected T defaultValue;

    protected Setting(String name, String description, T defaultValue) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public T get() {
        return value;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public void reset() {
        set(defaultValue);
    }

    public abstract void set(T value);

    /** JSON-safe value (String, Number, Boolean or List of those). */
    public abstract Object toJsonValue();

    /** Unknown or wrong-typed input resets to default, never throws. */
    public abstract void fromJsonValue(Object o);
}
