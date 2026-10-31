package com.mythicvisuals.gui;

import com.mythicvisuals.core.ConfigManager;
import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * HUD layout editor: drag enabled elements with LMB, ESC saves and exits.
 * Rendered over the live game (no dim) so positions match real gameplay.
 */
public class LayoutScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutScreen.class);
    private final List<HudModule> elements;
    private final ConfigManager config;

    private HudModule dragging;
    private int grabDX;
    private int grabDY;

    public LayoutScreen(List<HudModule> elements, ConfigManager config) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("HUD Layout"));
        this.elements = List.copyOf(elements);
        this.config = config;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        config.save();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        try {
        // no renderBackground: live world + real HUD stay visible underneath
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            for (HudModule e : elements) {
                if (!e.isEnabled()) {
                    continue;
                }
                int[] b = e.bounds(client);
                if (b[2] <= 0 || b[3] <= 0) {
                    continue;
                }
                boolean hover = LayoutUtil.hits(b, mouseX, mouseY);
                int color = e == dragging ? Theme.ACCENT_BRIGHT : (hover ? 0xFFFFFFFF : 0x88FFFFFF);
                int x = b[0];
                int y = b[1];
                // 1px outline
                ctx.fill(x - 1, y - 1, x + b[2] + 1, y, color);
                ctx.fill(x - 1, y + b[3], x + b[2] + 1, y + b[3] + 1, color);
                ctx.fill(x - 1, y, x, y + b[3], color);
                ctx.fill(x + b[2], y, x + b[2] + 1, y + b[3], color);
            }
        }
        String hint = dragging != null
                ? dragging.displayName() + " [" + dragging.x.get() + ", " + dragging.y.get() + "]"
                : net.minecraft.client.resources.language.I18n.get("mythicvisuals.hud.layout_hint");
        ctx.text(font, Component.literal(hint),
                (width - font.width(hint)) / 2, 10, 0xFFFFFFFF);
        } catch (Exception e) {
            com.mythicvisuals.core.CrashGuard.warnOnce(LOGGER, "layout-render", "Layout frame failed", e);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        try {
        if (click.button() != 0) {
            return super.mouseClicked(click, doubled);
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return super.mouseClicked(click, doubled);
        }
        // topmost = last registered wins: iterate in reverse
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudModule e = elements.get(i);
            if (!e.isEnabled()) {
                continue;
            }
            int[] b = e.bounds(client);
            if (b[2] > 0 && b[3] > 0 && LayoutUtil.hits(b, click.x(), click.y())) {
                dragging = e;
                grabDX = (int) click.x() - b[0];
                grabDY = (int) click.y() - b[1];
                return true;
            }
        }
        } catch (Exception e) {
            com.mythicvisuals.core.CrashGuard.warnOnce(LOGGER, "layout-click", "Layout click failed", e);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (dragging != null) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                dragging = null;
                return super.mouseDragged(click, dx, dy);
            }
            int[] b = dragging.bounds(client);
            int[] p = LayoutUtil.clamp(
                    (int) click.x() - grabDX, (int) click.y() - grabDY,
                    b[2], b[3], width, height);
            dragging.moveTo(p[0], p[1], b[2]);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        dragging = null;
        return super.mouseReleased(click);
    }
}
