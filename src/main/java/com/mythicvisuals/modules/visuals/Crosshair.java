package com.mythicvisuals.modules.visuals;

import com.mythicvisuals.core.hud.HudModule;
import com.mythicvisuals.module.setting.EnumSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Custom crosshair overlay (dot/plus/gap). Drawn over vanilla. */
public class Crosshair extends HudModule {
    public enum Style {
        DOT, PLUS, GAP
    }

    private final EnumSetting<Style> style;

    public Crosshair() {
        super("crosshair", "Crosshair", "Custom center crosshair", 0, 0);
        this.style = setting(new EnumSetting<>("style", "Style", Style.PLUS, Style.class));
    }

    @Override
    protected List<String> lines(Minecraft client) {
        return List.of();
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Minecraft client) {
        float s = scale.get().floatValue();
        int cx = client.getWindow().getGuiScaledWidth() / 2 + x.get();
        int cy = client.getWindow().getGuiScaledHeight() / 2 + y.get();
        int color = textColor.get() | 0xFF000000;
        ctx.pose().pushMatrix();
        try {
            ctx.pose().scale(s, s);
            int x = Math.round(cx / s);
            int y = Math.round(cy / s);
            switch (style.get()) {
                case DOT -> ctx.fill(x - 1, y - 1, x + 2, y + 2, color);
                case GAP -> {
                    int g = 4;
                    ctx.fill(x - g - 4, y, x - g, y + 1, color);
                    ctx.fill(x + g, y, x + g + 4, y + 1, color);
                    ctx.fill(x, y - g - 4, x + 1, y - g, color);
                    ctx.fill(x, y + g, x + 1, y + g + 4, color);
                }
                default -> { // PLUS
                    ctx.fill(x - 4, y, x + 5, y + 1, color);
                    ctx.fill(x, y - 4, x + 1, y + 5, color);
                }
            }
        } finally {
            ctx.pose().popMatrix();
        }
    }

    @Override
    public int[] bounds(Minecraft client) {
        int cx = client.getWindow().getGuiScaledWidth() / 2 + x.get();
        int cy = client.getWindow().getGuiScaledHeight() / 2 + y.get();
        return new int[]{cx - 8, cy - 8, 16, 16};
    }
}
