package com.mythicvisuals.module.setting;

public class IntegerSetting extends Setting<Integer> {
    private final int min;
    private final int max;
    private final int step;

    public IntegerSetting(String name, String description, int defaultValue,
                          int min, int max, int step) {
        super(name, description, defaultValue);
        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 1 : step;
        set(defaultValue);
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public int step() {
        return step;
    }

    @Override
    public void set(Integer value) {
        int v = value == null ? defaultValue : value;
        this.value = Math.min(max, Math.max(min, v));
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
