package com.mythicvisuals.module.setting;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, String description, double defaultValue,
                         double min, double max, double step) {
        super(name, description, defaultValue);
        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 1.0 : step;
        set(defaultValue);
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    @Override
    public void set(Double value) {
        double v = value == null ? defaultValue : value;
        if (Double.isNaN(v)) {
            v = defaultValue;
        }
        this.value = Math.min(max, Math.max(min, v));
    }

    @Override
    public Object toJsonValue() {
        return value;
    }

    @Override
    public void fromJsonValue(Object o) {
        if (o instanceof Number n) {
            set(n.doubleValue());
        } else {
            reset();
        }
    }
}
