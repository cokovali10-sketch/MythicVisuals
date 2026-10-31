# Architecture (living doc)

Target: `MYTHIC VISUALS` = CORE SYSTEM + USER INTERFACE + FEATURE MODULES + RENDER SYSTEM.

## Phase 1 (current)

- `Branding` — single source of mod id/name/version strings.
- `MythicVisuals implements ClientModInitializer` — entry point, logs only.
- No mixins, no events, no config yet.

## Planned (Phase 2+)

- `core/`: ModuleManager, ConfigManager (JSON, versioned, backups), EventBus (Fabric events), KeybindManager, lifecycle + error handling.
- `module/`: Module base (id, name, description, category, enabled, settings[]) + 8 setting types.
- `gui/` + `hud/`: Click GUI, HUD editor, themes, profiles.
- `render/`, `animation/`, `particles/`: centralized, pooled, budgeted.
- `localization/`: en_us + ru_ru JSON, no hardcoded strings.
