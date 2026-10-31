# Mythic Visuals

Free minimalist visual Minecraft client mod (Fabric). HUD, combat helpers, ClickGUI, profiles.
No accounts, no subscriptions, no telemetry.

## Status: v1.0.0 — release

- [x] Core: modules, 9 setting types, JSON config + profiles, per-module keybinds
- [x] HUD: FPS, CPS, Ping, XYZ, Direction, Keystrokes, Armor, Potions, Combo, Reach
- [x] HUD styling: color, shadow, background + opacity, scale, drag-move editor
- [x] ClickGUI (RSHIFT): categories, search, sliders, keybind capture, profiles
- [x] Visuals/combat: FullBright, FOV Changer, ToggleSprint
- [ ] In-game verification (see checklist below)

## Requirements (branch mc-26.2)

- Minecraft Java Edition **26.2**
- Fabric Loader **0.19.5**
- Fabric API **0.161.0+26.2**
- Java **25**

## Install

1. Install Fabric Loader 0.19.5 for MC 1.21.11, put Fabric API in `mods/`.
2. Drop `mythicvisuals-1.0.0.jar` into `mods/`.
3. Launch, press **Right Shift** in game.

## Controls

- **Right Shift** — open/close ClickGUI (LMB toggles module, RMB selects)
- **ClickGUI → HUD Layout** — drag HUD elements, ESC saves
- **ClickGUI → Profiles** — save/load/delete config presets
- Module keybinds — set per module in ClickGUI or vanilla Controls screen

## Build

```bat
gradlew.bat build
```

Jar: `build/libs/mythicvisuals-1.0.0.jar` (26.2 build).

## In-game checklist (not yet verified)

1. Mod loads, `[Mythic Visuals] v1.0.0 initialized: 14 module(s)` in log
2. HUD elements render and toggle via ClickGUI
3. RSHIFT opens GUI, sliders drag, keybind capture works, search filters
4. HUD Layout drag moves elements, positions persist after restart
5. Profiles save/load/delete roundtrip
6. FullBright/FOV/ToggleSprint behave, restore on disable

## Docs

- `docs/ARCHITECTURE.md` — module map
- `DEVELOPMENT_STATUS.md` — phase log

## License

MIT — see `LICENSE`.
