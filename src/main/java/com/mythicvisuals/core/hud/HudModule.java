package com.mythicvisuals.core.hud;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.ColorSetting;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import com.mythicvisuals.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * HUD element: a {@link Module} with a screen position and text style.
 * Subclasses provide plain text lines; this class applies color, shadow,
 * background and scale uniformly.
 */
public abstract class HudModule extends Module {
    public final IntegerSetting x;
    public final IntegerSetting y;
    public final ColorSetting textColor;
    public final BooleanSetting shadow;
    public final BooleanSetting background;
    public final IntegerSetting backgroundOpacity;
    public final NumberSetting scale;
    public final EnumSetting<Anchor> anchor;

    protected HudModule(String id, String name, String description, int defaultX, int defaultY) {
        super(id, name, description, Category.HUD);
        this.x = setting(new IntegerSetting("x", "Horizontal position", defaultX, 0, 4096, 1));
        this.y = setting(new IntegerSetting("y", "Vertical position", defaultY, 0, 4096, 1));
        this.textColor = setting(new ColorSetting("text_color", "Component color", 0xFFFFFFFF));
        this.shadow = setting(new BooleanSetting("shadow", "Component shadow", true));
        this.background = setting(new BooleanSetting("background", "Dark background", true));
        this.backgroundOpacity = setting(new IntegerSetting("bg_opacity", "Background opacity", 110, 0, 255, 1));
        this.scale = setting(new NumberSetting("scale", "Component scale", 1.0, 0.5, 2.0, 0.1));
        this.anchor = setting(new EnumSetting<>("anchor", "Screen edge", Anchor.RIGHT, Anchor.class));
    }

    /** Plain text lines to draw. Empty list draws nothing (no background either). */
    protected abstract List<String> lines(Minecraft client);

    /**
     * Screen-space bounds {x, y, w, h} for editors/hit-testing.
     * Empty (0x0 at x/y) when there is nothing to draw.
     */
    public int[] bounds(Minecraft client) {
        List<String> lines = lines(client);
        if (lines.isEmpty()) {
            return new int[]{x.get(), y.get(), 0, 0};
        }
        float s = scale.get().floatValue();
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, client.font.width(line));
        }
        int pad = background.get() ? 2 : 0;
        int h = lines.size() * (client.font.lineHeight + 2);
        int wScaled = Math.round((w + pad * 2) * s);
        int x0 = anchor.get() == Anchor.RIGHT ? x.get() - wScaled : x.get();
        return new int[]{x0, y.get(), wScaled, Math.round((h + pad * 2) * s)};
    }

    /**
     * Moves the element so its bounds top-left lands at (screenX, screenY).
     * For RIGHT anchor the stored x is the right edge.
     */
    public void moveTo(int screenX, int screenY, int w) {
        if (anchor.get() == Anchor.RIGHT) {
            x.set(screenX + w);
        } else {
            x.set(screenX);
        }
        y.set(screenY);
    }

    /** Styled frame render. Called only when enabled and a player exists. */
    public void render(GuiGraphicsExtractor ctx, Minecraft client) {
        List<String> lines = lines(client);
        if (lines.isEmpty()) {
            return;
        }
        float s = scale.get().floatValue();
        int lineHeight = client.font.lineHeight + 2;
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, client.font.width(line));
        }
        int pad = 2;
        int bx = Math.round(x.get() / s);
        if (anchor.get() == Anchor.RIGHT) {
            bx -= width;
        }
        int by = Math.round(y.get() / s);

        ctx.pose().pushMatrix();
        try {
            ctx.pose().scale(s, s);
            if (background.get()) {
                int bg = (backgroundOpacity.get() << 24) & 0xFF000000;
                ctx.fill(bx - pad, by - pad, bx + width + pad, by + lines.size() * lineHeight + pad, bg);
            }
            for (int i = 0; i < lines.size(); i++) {
                Component text = Component.literal(lines.get(i));
                ctx.text(client.font, text, bx, by + i * lineHeight, textColor.get(), shadow.get());
            }
        } finally {
            ctx.pose().popMatrix();
        }
    }
}
