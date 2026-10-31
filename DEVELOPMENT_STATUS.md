# Development status

## 2026-10-03 — Phase 2: core system DONE (15/15 tests green)
- Module (id/name/desc/category/enabled/8 settings, lifecycle, serialize),
  Category (6), ModuleManager (register/toggle/error isolation).
- 8 setting types, all with defaults/reset/JSON roundtrip + bad-input tolerance.
- ConfigManager: config/mythicvisuals/config.json, schema v1, corrupt-file
  backup, unknown modules ignored, per-module reset.
- KeybindManager: per-module toggle binds, own category
  (key.category.mythicvisuals.main, verified by decompiling 1.21.11 client).
- Entry point wires managers + DebugModule, saves on CLIENT_STOPPING.
- Debt: module display names not yet localized (tracked for GUI phase).
- Build fixes along the way: loom 1.13.6, single-env client layout,
  public lifecycle methods, 1.21 KeyBinding.Category API.
- Next: Phase 3 basic HUD (FPS/CPS/ping/coords/direction/keystrokes/armor/potions).

## 2026-10-03 — Phase 3: basic HUD DONE (22/22 tests green)
- HudModule (x/y settings, drawLine helper) + HudRenderer
  (HudRenderCallback, skips when hudHidden/no player, per-element error isolation).
- 8 elements: FPS, CPS (event-based L/R via attack/use callbacks,
  ActionResult.PASS so gameplay untouched), Ping (null-safe), XYZ,
  Direction (pure DirectionUtil, tested), Keystrokes (WASD/Space/LMB/RMB),
  Armor (durability left), Potions (mm:ss).
- Pure logic tested: DirectionUtil (4 tests), CpsCounter sliding window (3 tests).
- Debt: no styles yet (plain white text), no drag-move (x/y via config only).
- Next: Phase 4 HUD settings (цвет, фон, тень, масштаб? + стили).

## 2026-10-03 — Phase 4: HUD styling DONE (24/24 tests green)
- HudModule refactored: subclasses return plain lines(), base applies
  text_color (ARGB), shadow on/off, dark background + opacity, scale 0.5-2.0.
- Render via Matrix3x2fStack push/scale/pop, top-left anchored; background
  sized from widest line, skipped when no lines.
- Style settings persist through existing config (no format change).
- Debt: no GUI editor yet (values via config file), no per-line colors.
- Next: Phase 5 ClickGUI (категории, бинды, поиск?) или игровая проверка.

## 2026-10-03 — Phase 5: ClickGUI DONE (28/28 tests green)
- ClickGui screen (RSHIFT toggles, ESC closes, game not paused):
  sidebar categories, searchable module list (LMB toggle, RMB select,
  bind label per row), settings panel with scissor scrolling.
- Widgets: boolean toggle, slider with drag (int/double, step-snapped),
  enum cycle, color presets, keybind capture (module binds rewrite vanilla
  KeyBinding + updateKeysByCode), string edit field (ENTER saves),
  multiselect as header + per-option toggles.
- SettingRow view-model (pure, tested); changes persist via save on close.
- Verified against 1.21.11 input API (Click/KeyInput/CharInput records).
- Debt: x/y position editing hidden from panel (drag-move planned),
  no animation, no in-game visual test yet.
- Next: Phase 6 combat/visual modules or in-game verification.

## 2026-10-03 — Phase 6: combat/visual modules DONE (32/32 tests green)
- ClientTickable interface + central END_CLIENT_TICK dispatch (error isolated).
- FullBright (gamma save/restore, brightness 1-16), FOV Changer (save/restore),
  ToggleSprint (forward + food + not sneaking/in water).
- ComboHud (AttackEntityCallback + health-drop reset + 3s expiry via pure
  ComboTracker, tested), ReachHud (block/entity attribute ranges).
- Fixed latent bug: CpsHud/ComboHud register Fabric events exactly once
  (events cannot be unregistered; re-enable no longer duplicates counters).
- Dropped TimeChanger: no client-side time setter exists (server-driven).
- Debt: no in-game visual test yet; gamma >1 may clamp on some options screens.
- Next: Phase 7 profiles/presets, Capes/Alt Manager, or in-game verification.

## 2026-10-03 — Phase 7: profiles DONE (36/36 tests green)
- ConfigManager: loadFrom/saveTo refactor, profiles/ dir, list/save/load/
  delete with [a-z0-9_]{1,32} validation; loading a profile applies it and
  makes it current; corrupt profiles keep current state (no backup spam).
- ClickGui Profiles mode: name input (search field reused), save row,
  LMB load / RMB delete, back via any category.
- Debt: no in-game visual test yet (build + units only).
- Next: Capes/Cosmetics, Alt Manager, HUD drag-move, or in-game verification.

## 2026-10-03 — Phase 8: HUD drag-move DONE (38/38 tests green)
- HudModule.bounds() (screen-space rect incl. scale + background padding).
- LayoutScreen over live game (no dim): hover/drag outlines, LMB drag with
  grab offset, screen-edge clamp, ESC saves via config.
- ClickGui sidebar "HUD Layout" button opens the editor.
- LayoutUtil pure math tested (clamp/hit-test).
- Debt: no in-game visual test yet (build + units only).
- Next: Capes/Cosmetics, Alt Manager, or in-game verification + release.

## 2026-10-03 — Phase 9: v1.0.0 release prep DONE (38/38 tests green)
- Version bumped 0.1.0 -> 1.0.0 (gradle.properties + Branding).
- README rewritten: features, install, controls, in-game checklist.
- clean build: mythicvisuals-1.0.0.jar (75 KB) + sources jar.
- Debt: in-game verification pending (needs a real launcher run by user).
- Parked (need runtime testing or auth infra): Capes/Cosmetics (render
  layers), Alt Manager (Microsoft OAuth), mixins in general.

## 2026-10-04 — Branch mc-26.2: full port to MC 26.2 DONE (38/38 green)
- 26.x is unobfuscated: loom 1.16.2 (Gradle 9.4, JDK 25) + Mojang names,
  no Yarn (mirrors FAPI 26.2 branch + example-mod 26.2 template).
- Mechanical yarn->mojmap rename (21 files) + manual GUI/HUD rewrite:
  GuiGraphicsExtractor (pose/fill/text/scissor), HudElementRegistry layer,
  Screen(Minecraft, Font, Component) + extractRenderState, EditBox,
  KeyEvent/MouseButtonEvent/CharacterEvent, KeyMapping API, Gui.screen(),
  Gui.hud, Options.fov()/gamma(), OptionInstance.get/set, Font.width.
- BUILD SUCCESSFUL, 38/38 tests green, jar installed to 26.2 instance.
- Needs in-game verification by user.

## 2026-10-03 — Phase 0/1: environment + scaffold + green build
- Env: JDK 21.0.11 (Temurin), Gradle 8.14.3 (D:\tools), no system Gradle used.
- Target fixed: MC 1.21.11 stable, Yarn 1.21.11+build.6, Loader 0.19.5,
  Fabric API 0.141.6+1.21.11, Java 21, loom 1.13.6.
- 26.x MC line checked: no Yarn mappings + intermediary 0.0.0 placeholder
  => not buildable today. Retarget later via gradle.properties.
- Loom version walk: 1.10.7 (does not exist) -> 1.11.8 (needs Gradle 8.14;
  javadoc-namespace clash with FAPI 0.141.x) -> 1.12.7 (FAPI built with
  loom 1.13.3, rejected) -> 1.13.6 OK.
- Phase 1 done: entrypoint logs brand, en_us+ru_ru, BrandingTest, MIT, README,
  ARCHITECTURE.md. BUILD SUCCESSFUL, tests green, jar 3915 bytes.
- Next: Phase 2 core (Module/ModuleManager, 8 setting types, JSON config
  with versions/backups, keybinds, Fabric events, lifecycle).
