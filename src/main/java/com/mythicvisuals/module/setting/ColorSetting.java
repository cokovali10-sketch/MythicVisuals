package com.mythicvisuals.module.setting;

/** ARGB packed int color (0xAARRGGBB). */
public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, String description, int defaultArgb) {
        super(name, description, defaultArgb);
    }

    @Override
    public void set(Integer value) {
        this.value = value == null ? defaultValue : value;
    }

    public void setRgb(int r, int g, int b) {
        set((0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public int alpha() {
        return (value >>> 24) & 0xFF;
    }

    public int red() {
        return (value >>> 16) & 0xFF;
    }

    public int green() {
        return (value >>> 8) & 0xFF;
    }

    public int blue() {
        return value & 0xFF;
    }

    /** Accepts 0xAARRGGBB ints and #AARRGGBB / #RRGGBB strings. */
    public void setHex(String hex) {
        if (hex == null) {
            reset();
            return;
        }
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            long v = Long.parseLong(h, 16);
            if (h.length() == 6) {
                v |= 0xFF000000L;
            }
            set((int) v);
        } catch (NumberFormatException e) {
            reset();
        }
    }

    @Override
    public Object toJsonValue() {
        return value;
    }

    @Override
    public void fromJsonValue(Object o) {
        if (o instanceof Number n) {
            set(n.intValue());
        } else if (o instanceof String s) {
            setHex(s);
        } else {
            reset();
        }
    }
}
