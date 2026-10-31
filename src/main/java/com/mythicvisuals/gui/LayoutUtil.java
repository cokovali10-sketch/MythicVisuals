package com.mythicvisuals.gui;

/** Pure screen-clamp math for the layout editor. Unit tested. */
public final class LayoutUtil {
    private LayoutUtil() {
    }

    /** Keeps an element fully on screen with a small margin. */
    public static int[] clamp(int x, int y, int w, int h, int screenW, int screenH) {
        int margin = 2;
        int cx = Math.max(margin, Math.min(Math.max(margin, screenW - w - margin), x));
        int cy = Math.max(margin, Math.min(Math.max(margin, screenH - h - margin), y));
        return new int[]{cx, cy};
    }

    /** Hit test for screen-space bounds {x, y, w, h}. */
    public static boolean hits(int[] bounds, double mx, double my) {
        return mx >= bounds[0] && mx < bounds[0] + bounds[2]
                && my >= bounds[1] && my < bounds[1] + bounds[3];
    }
}
