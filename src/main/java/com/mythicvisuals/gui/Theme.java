package com.mythicvisuals.gui;

/**
 * Mythic dark-red glass theme. Single source of truth for every pixel
 * the mod draws: GUI panels, HUD accents, outlines.
 */
public final class Theme {
    /** Primary dark-red accent. */
    public static final int ACCENT = 0xFFB3121F;
    /** Brighter red for hover/active highlights. */
    public static final int ACCENT_BRIGHT = 0xFFE03040;
    /** Deep red for selected/active text on accent. */
    public static final int ACCENT_TEXT = 0xFFFFFFFF;
    /** Glass panel background (translucent near-black with red tint). */
    public static final int PANEL = 0xB8100B0B;
    /** Header strip. */
    public static final int HEADER = 0xC01E0E10;
    /** Header gradient end (fades into panel). */
    public static final int HEADER_FADE = 0x801E0E10;
    /** Top glass highlight line. */
    public static final int GLASS_TOP = 0x66FF8080;
    /** Module row background. */
    public static final int ROW = 0x991A0E0E;
    /** Row hover. */
    public static final int ROW_HOVER = 0xD02E1214;
    /** 1px glass border. */
    public static final int BORDER = 0xFF5A1218;
    /** Bright and dim text. */
    public static final int TEXT_BRIGHT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFFB8A8A8;
    /** Dark overlay behind modal screens. */
    public static final int DIM = 0xA0080505;

    private Theme() {
    }

    /** Accent with custom alpha (0-255). */
    public static int accent(int alpha) {
        return (alpha << 24) | (ACCENT & 0xFFFFFF);
    }

    /** Darkens any ARGB color by factor 0..1. */
    public static int darken(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (int) (((argb >> 16) & 0xFF) * factor);
        int g = (int) (((argb >> 8) & 0xFF) * factor);
        int b = (int) ((argb & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
