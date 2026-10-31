package com.mythicvisuals.core.hud;

/** Pure yaw-to-compass helper, unit tested. */
public final class DirectionUtil {
    private static final String[] WINDS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    private DirectionUtil() {
    }

    /** 8-wind compass direction for a Minecraft yaw in degrees. */
    public static String compass(double yaw) {
        double normalized = ((yaw % 360) + 360) % 360;
        return WINDS[(int) Math.floor((normalized + 22.5) / 45.0) % 8];
    }

    /** Facing axis with sign, e.g. "+Z" or "-X". */
    public static String axis(double yaw) {
        double normalized = ((yaw % 360) + 360) % 360;
        if (normalized >= 315 || normalized < 45) {
            return "+Z";
        }
        if (normalized >= 45 && normalized < 135) {
            return "-X";
        }
        if (normalized >= 135 && normalized < 225) {
            return "-Z";
        }
        return "+X";
    }
}
