package com.mythicvisuals;

import com.mythicvisuals.core.ConfigManager;
import com.mythicvisuals.core.ModuleManager;
import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ModuleManagerTest {

    static class Probe extends Module {
        int enables;
        int disables;
        final BooleanSetting flag;

        Probe() {
            super("probe", "Probe", "Test module.", Category.UTILITY);
            flag = setting(new BooleanSetting("flag", "A flag.", false));
        }

        @Override
        public void onEnable() {
            enables++;
        }

        @Override
        public void onDisable() {
            disables++;
        }
    }

    static class Boom extends Module {
        Boom() {
            super("boom", "Boom", "Always throws.", Category.UTILITY);
        }

        @Override
        public void onEnable() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void registerToggleLifecycle() {
        ModuleManager mm = new ModuleManager();
        Probe p = new Probe();
        mm.register(p);
        assertSame(p, mm.get("probe"));
        assertTrue(mm.setEnabled(p, true));
        assertTrue(p.isEnabled());
        assertEquals(1, p.enables);
        assertTrue(mm.toggle(p));
        assertFalse(p.isEnabled());
        assertEquals(1, p.disables);
    }

    @Test
    void duplicateIdRejected() {
        ModuleManager mm = new ModuleManager();
        mm.register(new Probe());
        assertThrows(IllegalArgumentException.class, () -> mm.register(new Probe()));
    }

    @Test
    void lifecycleErrorIsolated() {
        ModuleManager mm = new ModuleManager();
        Boom b = new Boom();
        mm.register(b);
        assertFalse(mm.setEnabled(b, true));
        assertFalse(b.isEnabled());
    }

    @Test
    void configRoundtrip(@TempDir Path dir) {
        ModuleManager mm = new ModuleManager();
        Probe p = new Probe();
        mm.register(p);
        mm.setEnabled(p, true);
        p.flag.set(true);
        ConfigManager cfg = new ConfigManager(mm, dir.resolve("config.json"));
        assertTrue(cfg.save());
        // fresh manager, same module shape
        ModuleManager mm2 = new ModuleManager();
        Probe p2 = new Probe();
        mm2.register(p2);
        new ConfigManager(mm2, dir.resolve("config.json")).load();
        assertTrue(p2.isEnabled());
        assertTrue(p2.flag.get());
    }

    @Test
    void corruptConfigBackedUp(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("config.json");
        java.nio.file.Files.writeString(f, "{not json");
        ModuleManager mm = new ModuleManager();
        mm.register(new Probe());
        new ConfigManager(mm, f).load();
        assertTrue(java.nio.file.Files.list(dir)
                .anyMatch(p -> p.getFileName().toString().startsWith("config.json.bak.")));
    }
}
