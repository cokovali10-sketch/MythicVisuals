package com.mythicvisuals.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Versioned JSON config with corruption backups.
 * File: config/mythicvisuals/config.json
 * Shape: {"schema":1,"modules":{"id":{"enabled":bool,"settings":{...}}}}.
 * Unknown modules are ignored; a broken file is backed up and reset.
 */
public class ConfigManager {
    public static final int SCHEMA = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final ModuleManager modules;
    private final Path file;

    public ConfigManager(ModuleManager modules) {
        this(modules, FabricLoader.getInstance().getConfigDir()
                .resolve("mythicvisuals").resolve("config.json"));
    }

    /** Test/dev seam: explicit path. Production uses the Fabric config dir. */
    public ConfigManager(ModuleManager modules, Path file) {
        this.modules = modules;
        this.file = file;
    }

    public Path file() {
        return file;
    }

    public void load() {
        loadFrom(file, true);
    }

    public boolean save() {
        return saveTo(file);
    }

    // ---------- profiles ----------

    /** Profiles live in config/mythicvisuals/profiles/<name>.json. */
    public Path profilesDir() {
        return file.getParent().resolve("profiles");
    }

    public static boolean validProfileName(String name) {
        return name != null && name.matches("[a-z0-9_]{1,32}");
    }

    public List<String> listProfiles() {
        Path dir = profilesDir();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replaceAll("\\.json$", ""))
                    .filter(ConfigManager::validProfileName)
                    .forEach(out::add);
        } catch (IOException e) {
            LOGGER.error("[mythicvisuals] Profile list failed: {}", e.toString());
        }
        Collections.sort(out);
        return out;
    }

    public boolean saveProfile(String name) {
        if (!validProfileName(name)) {
            return false;
        }
        return saveTo(profilesDir().resolve(name + ".json"));
    }

    /** Applies the profile and makes it the current config. */
    public boolean loadProfile(String name) {
        if (!validProfileName(name)) {
            return false;
        }
        Path p = profilesDir().resolve(name + ".json");
        if (!Files.isRegularFile(p)) {
            return false;
        }
        loadFrom(p, false);
        return save();
    }

    public boolean deleteProfile(String name) {
        if (!validProfileName(name)) {
            return false;
        }
        try {
            return Files.deleteIfExists(profilesDir().resolve(name + ".json"));
        } catch (IOException e) {
            LOGGER.error("[mythicvisuals] Profile delete failed: {}", e.toString());
            return false;
        }
    }

    // ---------- file io ----------

    private void loadFrom(Path path, boolean backupOnCorrupt) {
        if (!Files.isRegularFile(path)) {
            return; // first run: defaults stay, saved later
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            if (backupOnCorrupt) {
                backupCorrupt(path, e);
            } else {
                LOGGER.warn("[mythicvisuals] Profile {} unreadable ({}), keeping current.",
                        path.getFileName(), e.toString());
            }
            return;
        }
        int schema = root.has("schema") ? safeInt(root.get("schema"), 0) : 0;
        if (schema != SCHEMA) {
            LOGGER.warn("[mythicvisuals] Config schema {} != {}, keeping defaults.",
                    schema, SCHEMA);
            return;
        }
        if (!root.has("modules") || !root.get("modules").isJsonObject()) {
            return;
        }
        JsonObject mods = root.getAsJsonObject("modules");
        for (Module m : modules.all()) {
            if (!mods.has(m.id()) || !mods.get(m.id()).isJsonObject()) {
                continue;
            }
            applyTo(m, mods.getAsJsonObject(m.id()));
        }
    }

    private void applyTo(Module module, JsonObject o) {
        if (o.has("enabled") && o.get("enabled").isJsonPrimitive()
                && o.get("enabled").getAsJsonPrimitive().isBoolean()) {
            module.setEnabledInternal(o.get("enabled").getAsBoolean());
        }
        if (o.has("settings") && o.get("settings").isJsonObject()) {
            JsonObject s = o.getAsJsonObject("settings");
            for (Setting<?> setting : module.settings()) {
                if (!s.has(setting.name())) {
                    continue;
                }
                try {
                    setting.fromJsonValue(gsonToPlain(s.get(setting.name())));
                } catch (Exception e) {
                    setting.reset();
                }
            }
        }
    }

    /** Converts Gson tree to plain JDK values understood by Setting.fromJsonValue. */
    static Object gsonToPlain(JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (el.isJsonPrimitive()) {
            var p = el.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return p.getAsBoolean();
            }
            if (p.isNumber()) {
                double d = p.getAsDouble();
                if (d == Math.rint(d) && !Double.isInfinite(d)
                        && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                    return (int) d;
                }
                return d;
            }
            return p.getAsString();
        }
        if (el.isJsonArray()) {
            var out = new java.util.ArrayList<>();
            for (JsonElement e : el.getAsJsonArray()) {
                out.add(gsonToPlain(e));
            }
            return out;
        }
        return null; // objects unsupported by settings: caller resets
    }

    private boolean saveTo(Path path) {
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("schema", SCHEMA);
            JsonObject mods = new JsonObject();
            for (Module m : modules.all()) {
                JsonObject mo = new JsonObject();
                mo.addProperty("enabled", m.isEnabled());
                JsonObject s = new JsonObject();
                for (Setting<?> setting : m.settings()) {
                    putPlain(s, setting.name(), setting.toJsonValue());
                }
                mo.add("settings", s);
                mods.add(m.id(), mo);
            }
            root.add("modules", mods);
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOGGER.error("[mythicvisuals] Config save failed: {}", e.toString());
            return false;
        }
    }

    private static void putPlain(JsonObject o, String key, Object v) {
        if (v == null) {
            o.add(key, null);
        } else if (v instanceof Boolean b) {
            o.addProperty(key, b);
        } else if (v instanceof Number n) {
            o.addProperty(key, n);
        } else if (v instanceof String s) {
            o.addProperty(key, s);
        } else if (v instanceof java.util.List<?> list) {
            var arr = new com.google.gson.JsonArray();
            for (Object e : list) {
                if (e instanceof String s) {
                    arr.add(s);
                } else if (e instanceof Number n) {
                    arr.add(n);
                } else if (e instanceof Boolean b) {
                    arr.add(b);
                }
            }
            o.add(key, arr);
        }
    }

    public boolean resetModule(String id) {
        Module m = modules.get(id);
        if (m == null) {
            return false;
        }
        for (Setting<?> s : m.settings()) {
            s.reset();
        }
        modules.setEnabled(m, false);
        return save();
    }

    private void backupCorrupt(Path path, Exception e) {
        try {
            String stamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path bak = path.resolveSibling("config.json.bak." + stamp);
            Files.createDirectories(path.getParent());
            Files.move(path, bak, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("[mythicvisuals] Corrupt config backed up to {} ({}), defaults kept.",
                    bak.getFileName(), e.toString());
        } catch (IOException io) {
            LOGGER.error("[mythicvisuals] Corrupt config backup failed: {}", io.toString());
        }
    }

    private static int safeInt(JsonElement el, int fallback) {
        try {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsInt();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    /** Visible for tests. */
    Map<String, Object> snapshotForTest() {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        for (Module m : modules.all()) {
            out.put(m.id(), m.serialize());
        }
        return out;
    }
}
