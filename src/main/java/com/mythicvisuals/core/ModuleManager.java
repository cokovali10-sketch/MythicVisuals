package com.mythicvisuals.core;

import com.mythicvisuals.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owns all modules. Toggle errors are isolated and logged, never thrown. */
public class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

    private final Map<String, Module> modules = new LinkedHashMap<>();

    public void register(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("module is null");
        }
        if (modules.containsKey(module.id())) {
            throw new IllegalArgumentException("duplicate module id: " + module.id());
        }
        modules.put(module.id(), module);
    }

    public Module get(String id) {
        return modules.get(id);
    }

    public List<Module> all() {
        return new ArrayList<>(modules.values());
    }

    public List<Module> byCategory(com.mythicvisuals.module.Category category) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules.values()) {
            if (m.category() == category) {
                out.add(m);
            }
        }
        return out;
    }

    /** Toggles and runs lifecycle; returns false if the lifecycle threw. */
    public boolean setEnabled(Module module, boolean enabled) {
        if (module == null) {
            return false;
        }
        if (module.isEnabled() == enabled) {
            return true;
        }
        try {
            if (enabled) {
                module.onEnable();
            } else {
                module.onDisable();
            }
        } catch (Exception e) {
            LOGGER.error("[mythicvisuals] Module '{}' failed to {}: {}",
                    module.id(), enabled ? "enable" : "disable", e.toString());
            return false;
        }
        module.setEnabledInternal(enabled);
        return true;
    }

    public boolean toggle(Module module) {
        return setEnabled(module, !module.isEnabled());
    }

    public void enableAll() {
        for (Module m : modules.values()) {
            setEnabled(m, true);
        }
    }

    public void disableAll() {
        for (Module m : modules.values()) {
            setEnabled(m, false);
        }
    }
}
