package com.mythicvisuals;

import com.mythicvisuals.core.ConfigManager;
import com.mythicvisuals.core.ModuleManager;
import com.mythicvisuals.modules.debug.DebugModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfilesTest {
    @TempDir
    Path tmp;

    private ConfigManager config() {
        ModuleManager modules = new ModuleManager();
        modules.register(new DebugModule());
        return new ConfigManager(modules, tmp.resolve("config.json"));
    }

    @Test
    void saveLoadRoundtrip() {
        ModuleManager modules = new ModuleManager();
        modules.register(new DebugModule());
        ConfigManager c = new ConfigManager(modules, tmp.resolve("config.json"));
        assertTrue(c.saveProfile("pvp"));
        assertEquals(List.of("pvp"), c.listProfiles());

        // mutate live state, then restore from profile
        modules.get("debug").setEnabledInternal(true);
        assertTrue(c.loadProfile("pvp"));
        assertFalse(modules.get("debug").isEnabled());
    }

    @Test
    void invalidNamesRejected() {
        ConfigManager c = config();
        assertFalse(c.saveProfile("Bad Name!"));
        assertFalse(c.saveProfile(""));
        assertFalse(c.loadProfile("../escape"));
        assertFalse(c.deleteProfile("nope!"));
        assertTrue(c.listProfiles().isEmpty());
    }

    @Test
    void missingProfileFails() {
        assertFalse(config().loadProfile("ghost"));
    }

    @Test
    void deleteRemoves() {
        ConfigManager c = config();
        assertTrue(c.saveProfile("a"));
        assertTrue(c.saveProfile("b"));
        assertEquals(List.of("a", "b"), c.listProfiles());
        assertTrue(c.deleteProfile("a"));
        assertEquals(List.of("b"), c.listProfiles());
        assertFalse(c.deleteProfile("a"));
    }
}
