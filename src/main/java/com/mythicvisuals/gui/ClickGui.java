package com.mythicvisuals.gui;

import com.mythicvisuals.core.ConfigManager;
import com.mythicvisuals.core.KeybindManager;
import com.mythicvisuals.core.ModuleManager;
import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.ColorSetting;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import com.mythicvisuals.module.setting.KeybindSetting;
import com.mythicvisuals.module.setting.MultiSelectSetting;
import com.mythicvisuals.module.setting.NumberSetting;
import com.mythicvisuals.module.setting.Setting;
import com.mythicvisuals.module.setting.StringSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ClickGUI: sidebar categories, searchable module list, settings panel.
 * LMB toggles a module, RMB selects it. Sliders drag, buttons cycle,
 * keybind rows capture the next keypress.
 */
public class ClickGui extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickGui.class);
    private static final int ACCENT = Theme.ACCENT;
    private static final int PANEL = Theme.PANEL;
    private static final int ROW = Theme.ROW;
    private static final int ROW_HOVER = Theme.ROW_HOVER;
    private static final int TEXT_DIM = Theme.TEXT_DIM;
    private static final int TEXT_BRIGHT = Theme.TEXT_BRIGHT;
    private static final int[] COLOR_PRESETS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55,
            0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFF9D4EDD
    };

    private final ModuleManager modules;
    private final KeybindManager keybinds;
    private final ConfigManager config;
    private final Runnable onClose;

    private Category selectedCategory = Category.HUD;
    private boolean profilesMode;
    private java.util.List<String> profileCache = java.util.List.of();
    private Module selectedModule;
    private EditBox search;
    private EditBox stringEditor;
    private StringSetting editingString;

    private double listScroll;
    private double settingsScroll;
    private Setting<?> dragSlider;
    private int dragSliderX;
    private int dragSliderW;
    private KeybindSetting capturingSetting;
    private boolean capturingModuleBind;

    public ClickGui(ModuleManager modules, KeybindManager keybinds, ConfigManager config, Runnable onClose) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("Mythic Visuals"));
        this.modules = modules;
        this.keybinds = keybinds;
        this.config = config;
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        String keptSearch = search == null ? "" : search.getValue();
        String keptEdit = stringEditor == null ? "" : stringEditor.getValue();
        search = new EditBox(font, 0, 0, 150, 16, Component.literal("Search"));
        search.setMaxLength(32);
        search.setHint(Component.literal("Search..."));
        search.setResponder(s -> listScroll = 0);
        search.setValue(keptSearch);
        stringEditor = new EditBox(font, 0, 0, 150, 16, Component.literal("Edit"));
        stringEditor.setMaxLength(64);
        stringEditor.setValue(keptEdit);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        onClose.run();
    }

    // ---------- layout ----------

    private int panelX() {
        return (width - 520) / 2;
    }

    private int panelY() {
        return (height - 340) / 2;
    }

    private int sidebarW() {
        return 90;
    }

    private int listX() {
        return panelX() + sidebarW() + 8;
    }

    private int listW() {
        return 200;
    }

    private int settingsX() {
        return listX() + listW() + 8;
    }

    private int settingsW() {
        return panelX() + 520 - settingsX() - 8;
    }

    // ---------- render ----------

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        try {
        // No extractBackground(): vanilla allows one background blur per frame
        // and it is already consumed elsewhere -> plain dim instead, no crash.
        ctx.fill(0, 0, width, height, Theme.DIM);
        int px = panelX();
        int py = panelY();
        ctx.fill(px, py, px + 520, py + 340, PANEL);
        // glass border
        ctx.fill(px, py, px + 520, py + 1, Theme.BORDER);
        ctx.fill(px, py + 339, px + 520, py + 340, Theme.BORDER);
        ctx.fill(px, py, px + 1, py + 340, Theme.BORDER);
        ctx.fill(px + 519, py, px + 520, py + 340, Theme.BORDER);
        // glass header: red gradient + top highlight
        ctx.fillGradient(px, py, px + 520, py + 20, Theme.HEADER, Theme.HEADER_FADE);
        ctx.fill(px, py + 20, px + 520, py + 21, Theme.BORDER);
        ctx.fill(px + 1, py + 1, px + 519, py + 2, Theme.GLASS_TOP);
        ctx.text(font, Component.literal("Mythic Visuals"), px + 8, py + 6, ACCENT, true);

        drawSidebar(ctx, mouseX, mouseY);
        drawModuleList(ctx, mouseX, mouseY);
        drawSettings(ctx, mouseX, mouseY);

        search.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        if (editingString != null) {
            stringEditor.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        }
        } catch (Exception e) {
            com.mythicvisuals.core.CrashGuard.warnOnce(LOGGER, "clickgui-render", "ClickGUI frame failed", e);
        }
    }

    private void drawSidebar(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int x = panelX() + 4;
        int y = panelY() + 26;
        for (Category c : Category.values()) {
            boolean hover = inBox(mouseX, mouseY, x, y, sidebarW() - 8, 20);
            boolean active = c == selectedCategory && !profilesMode;
            int bg = active ? ACCENT : (hover ? ROW_HOVER : ROW);
            int fg = active ? Theme.ACCENT_TEXT : TEXT_BRIGHT;
            ctx.fill(x, y, x + sidebarW() - 8, y + 20, bg);
            ctx.text(font, Component.literal(c.displayName()), x + 6, y + 6, fg, true);
            y += 22;
        }
        boolean pHover = inBox(mouseX, mouseY, x, y, sidebarW() - 8, 20);
        ctx.fill(x, y, x + sidebarW() - 8, y + 20,
                profilesMode ? ACCENT : (pHover ? ROW_HOVER : ROW));
        ctx.text(font, Component.literal("Profiles"), x + 6, y + 6,
                profilesMode ? Theme.ACCENT_TEXT : TEXT_BRIGHT, true);
        y += 22;
        boolean lHover = inBox(mouseX, mouseY, x, y, sidebarW() - 8, 20);
        ctx.fill(x, y, x + sidebarW() - 8, y + 20, lHover ? ROW_HOVER : ROW);
        ctx.text(font, Component.literal("HUD Layout"), x + 6, y + 6, TEXT_BRIGHT, true);
    }

    private List<Module> visibleModules() {
        String q = search.getValue().trim().toLowerCase();
        List<Module> out = new ArrayList<>();
        for (Module m : modules.byCategory(selectedCategory)) {
            if (q.isEmpty() || m.name().toLowerCase().contains(q) || m.id().contains(q)
                    || m.displayName().toLowerCase().contains(q)) {
                out.add(m);
            }
        }
        return out;
    }

    private void drawModuleList(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        search.setX(listX());
        search.setY(panelY() + 26);
        search.setWidth(listW());
        if (profilesMode) {
            search.setHint(Component.literal("Profile name..."));
            drawProfiles(ctx, mouseX, mouseY);
            return;
        }
        search.setHint(Component.literal("Search..."));

        int y0 = panelY() + 46;
        int y1 = panelY() + 332;
        List<Module> list = visibleModules();
        int rowH = 20;
        int total = list.size() * (rowH + 2);
        listScroll = clamp(listScroll, 0, Math.max(0, total - (y1 - y0)));

        ctx.enableScissor(listX(), y0, listX() + listW(), y1);
        int y = (int) (y0 - listScroll);
        for (Module m : list) {
            if (y + rowH >= y0 && y <= y1) {
                boolean hover = inBox(mouseX, mouseY, listX(), y, listW(), rowH);
                ctx.fill(listX(), y, listX() + listW(), y + rowH, hover ? ROW_HOVER : ROW);
                if (m == selectedModule) {
                    ctx.fill(listX(), y, listX() + 2, y + rowH, ACCENT);
                }
                int fg = m.isEnabled() ? TEXT_BRIGHT : TEXT_DIM;
                ctx.text(font, Component.literal(m.displayName()), listX() + 6, y + 6, fg, true);
                int right = listX() + listW() - 4;
                KeyMapping b = keybinds.bindingFor(m.id());
                if (b != null) {
                    String label = "[" + b.getTranslatedKeyMessage().getString() + "]";
                    int lw = font.width(label);
                    ctx.text(font, Component.literal(label), right - lw, y + 6, TEXT_DIM, true);
                    right -= lw + 4;
                }
                // glass ON/OFF pill
                String state = m.isEnabled() ? "ON" : "OFF";
                int pillW = font.width(state) + 10;
                int pillX = right - pillW;
                ctx.fill(pillX, y + 4, pillX + pillW, y + 16,
                        m.isEnabled() ? ACCENT : 0xFF2E2E2E);
                ctx.fill(pillX, y + 4, pillX + pillW, y + 5, Theme.GLASS_TOP);
                ctx.text(font, Component.literal(state), pillX + 5, y + 6, 0xFFFFFFFF, false);
            }
            y += rowH + 2;
        }
        ctx.disableScissor();
    }

    /** Profiles mode: save row on top, LMB loads, RMB deletes. */
    private void drawProfiles(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int y0 = panelY() + 46;
        int y1 = panelY() + 332;
        int rowH = 20;
        String name = search.getValue().trim().toLowerCase();
        boolean nameOk = ConfigManager.validProfileName(name);

        ctx.enableScissor(listX(), y0, listX() + listW(), y1);
        int y = y0;
        boolean hoverSave = inBox(mouseX, mouseY, listX(), y, listW(), rowH);
        ctx.fill(listX(), y, listX() + listW(), y + rowH, hoverSave ? ROW_HOVER : ACCENT);
        String saveLabel = nameOk ? "Save as '" + name + "'" : "Type a name [a-z0-9_]";
        ctx.text(font, Component.literal(saveLabel), listX() + 6, y + 6, 0xFF000000, true);
        y += rowH + 2;

        for (String p : profileCache) {
            boolean hover = inBox(mouseX, mouseY, listX(), y, listW(), rowH);
            ctx.fill(listX(), y, listX() + listW(), y + rowH, hover ? ROW_HOVER : ROW);
            ctx.text(font, Component.literal(p), listX() + 6, y + 6, TEXT_BRIGHT, true);
            ctx.text(font, Component.literal("LMB load / RMB del"),
                    listX() + listW() - 4 - font.width("LMB load / RMB del"), y + 6, TEXT_DIM, true);
            y += rowH + 2;
        }
        ctx.disableScissor();
    }

    private boolean clickProfiles(double mx, double my, int button) {
        int y0 = panelY() + 46;
        int rowH = 20;
        if (inBox(mx, my, listX(), y0, listW(), rowH)) {
            String name = search.getValue().trim().toLowerCase();
            if (button == 0 && ConfigManager.validProfileName(name)) {
                config.saveProfile(name);
                profileCache = config.listProfiles();
            }
            return true;
        }
        int y = y0 + rowH + 2;
        for (String p : profileCache) {
            if (inBox(mx, my, listX(), y, listW(), rowH)) {
                if (button == 0) {
                    config.loadProfile(p);
                } else {
                    config.deleteProfile(p);
                    profileCache = config.listProfiles();
                }
                return true;
            }
            y += rowH + 2;
        }
        return false;
    }

    private List<SettingRow> settingRows() {
        List<SettingRow> rows = new ArrayList<>();
        if (selectedModule == null) {
            return rows;
        }
        rows.add(SettingRow.moduleBind(selectedModule));
        rows.add(SettingRow.moduleToggle(selectedModule));
        for (Setting<?> s : selectedModule.settings()) {
            if (s.name().equals("x") || s.name().equals("y")) {
                continue;
            }
            if (s instanceof MultiSelectSetting multi) {
                rows.add(SettingRow.setting(multi));
                for (String o : multi.options()) {
                    rows.add(SettingRow.option(multi, o));
                }
            } else {
                rows.add(SettingRow.setting(s));
            }
        }
        return rows;
    }

    private void drawSettings(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int x = settingsX();
        int y0 = panelY() + 26;
        int y1 = panelY() + 332;
        if (selectedModule == null) {
            ctx.text(font, Component.literal(
                    net.minecraft.client.resources.language.I18n.get("mythicvisuals.hud.select_module")),
                    x + 4, y0 + 4, TEXT_DIM, true);
            return;
        }
        ctx.text(font, Component.literal(selectedModule.displayName()), x + 4, y0, ACCENT, true);

        List<SettingRow> rows = settingRows();
        int rowH = 34;
        int total = rows.size() * (rowH + 2);
        settingsScroll = clamp(settingsScroll, 0, Math.max(0, total - (y1 - y0 - 16)));

        ctx.enableScissor(x, y0 + 14, x + settingsW(), y1);
        int y = (int) (y0 + 14 - settingsScroll);
        for (SettingRow row : rows) {
            if (y + rowH >= y0 && y <= y1) {
                drawRow(ctx, mouseX, mouseY, x, y, settingsW(), rowH, row);
            }
            y += rowH + 2;
        }
        ctx.disableScissor();
    }

    private void drawRow(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int x, int y, int w, int h, SettingRow row) {
        ctx.fill(x, y, x + w, y + h, ROW);
        ctx.text(font, Component.literal(row.label()), x + 4, y + 4, TEXT_BRIGHT, true);
        int wx = x + 4;
        int wy = y + 18;
        int ww = w - 8;
        if (row.kind() == SettingRow.Kind.SLIDER) {
            double ratio = row.sliderRatio();
            ctx.fill(wx, wy + 3, wx + ww, wy + 7, 0xFF000000);
            ctx.fill(wx, wy + 3, (int) (wx + ww * ratio), wy + 7, ACCENT);
            String v = row.sliderText();
            ctx.text(font, Component.literal(v), wx + ww - font.width(v), y + 4, ACCENT, true);
        } else {
            boolean hover = inBox(mouseX, mouseY, wx, wy, ww, 12);
            ctx.fill(wx, wy, wx + ww, wy + 12, hover ? ROW_HOVER : 0xFF000000);
            String v = row.valueText(keybinds);
            if (capturingSetting == row.setting() || (capturingModuleBind && row.kind() == SettingRow.Kind.MODULE_BIND)) {
                v = "> press key <";
            }
            ctx.text(font, Component.literal(v), wx + 4, wy + 2,
                    row.active(modules) ? ACCENT : TEXT_DIM);
        }
    }

    // ---------- input ----------

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        try {
        search.mouseClicked(click, doubled);
        if (editingString != null) {
            stringEditor.mouseClicked(click, doubled);
        }
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        // sidebar
        int sx = panelX() + 4;
        int sy = panelY() + 26;
        for (Category c : Category.values()) {
            if (inBox(mx, my, sx, sy, sidebarW() - 8, 20)) {
                selectedCategory = c;
                selectedModule = null;
                profilesMode = false;
                listScroll = 0;
                return true;
            }
            sy += 22;
        }
        if (inBox(mx, my, sx, sy, sidebarW() - 8, 20)) {
            profilesMode = true;
            selectedModule = null;
            editingString = null;
            profileCache = config.listProfiles();
            search.setValue("");
            return true;
        }
        sy += 22;
        if (inBox(mx, my, sx, sy, sidebarW() - 8, 20)) {
            java.util.List<com.mythicvisuals.core.hud.HudModule> hud = new java.util.ArrayList<>();
            for (Module m : modules.all()) {
                if (m instanceof com.mythicvisuals.core.hud.HudModule h) {
                    hud.add(h);
                }
            }
            minecraft.gui.setScreen(new LayoutScreen(hud, config));
            return true;
        }

        if (profilesMode) {
            return clickProfiles(mx, my, button);
        }

        // module list
        int y0 = panelY() + 46;
        int y1 = panelY() + 332;
        List<Module> list = visibleModules();
        int rowH = 20;
        int y = (int) (y0 - listScroll);
        for (Module m : list) {
            if (y + rowH >= y0 && y <= y1 && inBox(mx, my, listX(), y, listW(), rowH)) {
                if (button == 0) {
                    modules.toggle(m);
                }
                selectedModule = m;
                settingsScroll = 0;
                editingString = null;
                return true;
            }
            y += rowH + 2;
        }

        // settings rows
        if (selectedModule != null) {
            int rx = settingsX();
            int ry0 = panelY() + 26 + 14;
            int ry1 = panelY() + 332;
            int rh = 34;
            int ry = (int) (ry0 - settingsScroll);
            for (SettingRow row : settingRows()) {
                if (ry + rh >= ry0 && ry <= ry1 && inBox(mx, my, rx, ry, settingsW(), rh)) {
                    return clickRow(row, mx, my, rx, ry, settingsW(), rh, button);
                }
                ry += rh + 2;
            }
        }
        } catch (Exception e) {
            com.mythicvisuals.core.CrashGuard.warnOnce(LOGGER, "clickgui-click", "ClickGUI click failed", e);
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean clickRow(SettingRow row, double mx, double my, int x, int y, int w, int h, int button) {
        int wx = x + 4;
        int wy = y + 18;
        int ww = w - 8;
        switch (row.kind()) {
            case MODULE_TOGGLE -> modules.setEnabled(selectedModule, !selectedModule.isEnabled());
            case MODULE_BIND -> capturingModuleBind = true;
            case BOOLEAN -> ((BooleanSetting) row.setting()).set(!((BooleanSetting) row.setting()).get());
            case ENUM -> cycleEnum((EnumSetting<?>) row.setting());
            case COLOR -> cycleColor((ColorSetting) row.setting());
            case KEYBIND -> capturingSetting = (KeybindSetting) row.setting();
            case MULTI -> {
                // header row, not clickable
            }
            case MULTI_OPTION -> {
                if (row.option() == null) {
                    break;
                }
                ((MultiSelectSetting) row.setting()).toggle(row.option());
            }
            case STRING -> {
                editingString = (StringSetting) row.setting();
                stringEditor.setX(wx);
                stringEditor.setY(wy - 2);
                stringEditor.setWidth(ww);
                stringEditor.setValue(editingString.get());
                stringEditor.setFocused(true);
            }
            case SLIDER -> {
                if (button == 0 && my >= wy && my <= wy + 12) {
                    dragSlider = row.setting();
                    dragSliderX = wx;
                    dragSliderW = ww;
                    applySlider(mx);
                }
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (dragSlider != null) {
            applySlider(click.x());
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        dragSlider = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= listX() && x <= listX() + listW()) {
            listScroll = clamp(listScroll - vertical * 20, 0, 100000);
            return true;
        }
        settingsScroll = clamp(settingsScroll - vertical * 20, 0, 100000);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        try {
        if (capturingSetting != null) {
            if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
                capturingSetting.set(KeybindSetting.UNBOUND);
            } else {
                capturingSetting.set(input.key());
            }
            capturingSetting = null;
            return true;
        }
        if (capturingModuleBind && selectedModule != null) {
            KeyMapping b = keybinds.bindingFor(selectedModule.id());
            if (b != null && input.key() != GLFW.GLFW_KEY_ESCAPE) {
                b.setKey(InputConstants.getKey(input));
                KeyMapping.resetMapping();
            }
            capturingModuleBind = false;
            return true;
        }
        if (search.isFocused()) {
            return search.keyPressed(input);
        }
        if (editingString != null && stringEditor.isFocused()) {
            if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_ESCAPE) {
                if (input.key() == GLFW.GLFW_KEY_ENTER) {
                    editingString.set(stringEditor.getValue());
                }
                editingString = null;
                stringEditor.setFocused(false);
                return true;
            }
            return stringEditor.keyPressed(input);
        }
        } catch (Exception e) {
            com.mythicvisuals.core.CrashGuard.warnOnce(LOGGER, "clickgui-key", "ClickGUI key failed", e);
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        try {
        if (search.isFocused()) {
            return search.charTyped(input);
        }
        if (editingString != null && stringEditor.isFocused()) {
            return stringEditor.charTyped(input);
        }
        } catch (Exception e) {
            com.mythicvisuals.core.CrashGuard.warnOnce(LOGGER, "clickgui-char", "ClickGUI char failed", e);
        }
        return super.charTyped(input);
    }

    // ---------- helpers ----------

    private void applySlider(double mouseX) {
        double ratio = clamp((mouseX - dragSliderX) / (double) dragSliderW, 0, 1);
        if (dragSlider instanceof IntegerSetting s) {
            int v = (int) Math.round(s.min() + ratio * (s.max() - s.min()));
            v -= (v - s.min()) % s.step();
            s.set(v);
        } else if (dragSlider instanceof NumberSetting s) {
            double v = s.min() + ratio * (s.max() - s.min());
            v = Math.round(v / s.step()) * s.step();
            s.set(Math.round(v * 100.0) / 100.0);
        }
    }

    private static void cycleEnum(EnumSetting<?> setting) {
        Object[] opts = setting.options();
        String cur = setting.get().toString();
        for (int i = 0; i < opts.length; i++) {
            if (opts[i].toString().equals(cur)) {
                setting.setByName(opts[(i + 1) % opts.length].toString());
                return;
            }
        }
        if (opts.length > 0) {
            setting.setByName(opts[0].toString());
        }
    }

    private static void cycleColor(ColorSetting setting) {
        int cur = setting.get();
        for (int i = 0; i < COLOR_PRESETS.length; i++) {
            if (COLOR_PRESETS[i] == cur) {
                setting.set(COLOR_PRESETS[(i + 1) % COLOR_PRESETS.length]);
                return;
            }
        }
        setting.set(COLOR_PRESETS[0]);
    }

    private static boolean inBox(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
