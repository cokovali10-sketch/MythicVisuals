package com.mythicvisuals.module;

public enum Category {
    HUD,
    COMBAT,
    VISUALS,
    WORLD,
    UTILITY,
    INTERFACE;

    /** Localized name (category.mythicvisuals.&lt;NAME&gt;), falls back to name(). */
    public String displayName() {
        try {
            String key = "category.mythicvisuals." + name();
            String localized = net.minecraft.client.resources.language.I18n.get(key);
            if (localized != null && !localized.equals(key)) {
                return localized;
            }
        } catch (Exception ignored) {
        }
        return name();
    }
}
