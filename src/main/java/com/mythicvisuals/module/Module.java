package com.mythicvisuals.module;

import com.mythicvisuals.module.setting.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One toggleable feature. Subclasses declare settings via {@link #setting}
 * and implement {@link #onEnable}/{@link #onDisable}. Lifecycle errors are
 * isolated by {@link ModuleManager} and never propagate.
 */
public abstract class Module {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String id, String name, String description, Category category) {
        if (id == null || !id.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("bad module id: " + id);
        }
        this.id = id;
        this.name = name == null ? id : name;
        this.description = description == null ? "" : description;
        this.category = category == null ? Category.UTILITY : category;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    /**
     * Localized display name (module.mythicvisuals.&lt;id&gt;), falls back
     * to {@link #name()} outside the game (unit tests) or if missing.
     */
    public String displayName() {
        try {
            String key = "module.mythicvisuals." + id;
            String localized = net.minecraft.client.resources.language.I18n.get(key);
            if (localized != null && !localized.equals(key)) {
                return localized;
            }
        } catch (Exception ignored) {
        }
        return name;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    public Setting<?> setting(String name) {
        for (Setting<?> s : settings) {
            if (s.name().equals(name)) {
                return s;
            }
        }
        return null;
    }

    protected <S extends Setting<?>> S setting(S s) {
        if (s == null) {
            throw new IllegalArgumentException("setting is null");
        }
        settings.add(s);
        return s;
    }

    /** Internal lifecycle hook; public so ModuleManager can call it. */
    public void onEnable() {
    }

    /** Internal lifecycle hook; public so ModuleManager can call it. */
    public void onDisable() {
    }

    /** Internal state flip; public so ModuleManager/ConfigManager can call it. */
    public void setEnabledInternal(boolean enabled) {
        this.enabled = enabled;
    }

    /** Internal serialization; public for ConfigManager. */
    public Map<String, Object> serialize() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", enabled);
        Map<String, Object> s = new LinkedHashMap<>();
        for (Setting<?> setting : settings) {
            s.put(setting.name(), setting.toJsonValue());
        }
        out.put("settings", s);
        return out;
    }

    /** Internal deserialization; public for ConfigManager. */
    @SuppressWarnings("unchecked") // Setting<?> -> Setting<Object> is safe: fromJsonValue takes Object
    public void deserialize(Object o) {
        if (!(o instanceof Map<?, ?> map)) {
            return;
        }
        Object enabledRaw = map.get("enabled");
        if (enabledRaw instanceof Boolean b) {
            setEnabledInternal(b);
        }
        Object settingsRaw = map.get("settings");
        if (settingsRaw instanceof Map<?, ?> smap) {
            for (Setting<?> setting : settings) {
                if (smap.containsKey(setting.name())) {
                    try {
                        ((Setting<Object>) setting).fromJsonValue(smap.get(setting.name()));
                    } catch (ClassCastException e) {
                        setting.reset();
                    }
                }
            }
        }
    }
}
