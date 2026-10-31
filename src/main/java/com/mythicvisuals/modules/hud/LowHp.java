package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Fullscreen pulsing red vignette below a health threshold. */
public class LowHp extends HudModule {
    private final com.mythicvisuals.module.setting.NumberSetting threshold;

    public LowHp() {
        super("low_hp", "Low HP Warning", "Red pulse when hurt", 4, 216);
        this.threshold = setting(new com.mythicvisuals.module.setting.NumberSetting(
                "threshold", "Health below", 6.0, 1.0, 20.0, 0.5));
    }

    @Override
    protected List<String> lines(Minecraft client) {
        return List.of();
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Minecraft client) {
        float hp = client.player.getHealth();
        float max = threshold.get().floatValue();
        if (hp <= 0.0F || hp >= max) {
            return;
        }
        // pulse 1.5 Hz, stronger the lower the health
        double phase = (System.currentTimeMillis() / 1000.0) * Math.PI * 2.0 * 1.5;
        double pulse = 0.5 + 0.5 * Math.sin(phase);
        double urgency = 1.0 - (hp / max);
        int alpha = (int) (40 + 90 * urgency * (0.4 + 0.6 * pulse));
        int color = (Math.max(0, Math.min(255, alpha)) << 24) | 0xAA0000;
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        ctx.fill(0, 0, w, h, color);
        if (urgency > 0.6) {
            String s = "!! " + Math.round(hp / 2.0F) + " HP !!";
            ctx.text(client.font, Component.literal(s),
                    (w - client.font.width(s)) / 2, h / 2 - 20, 0xFFFF5555, true);
        }
    }

    @Override
    public int[] bounds(Minecraft client) {
        return new int[]{x.get(), y.get(), 0, 0}; // fullscreen, not draggable
    }
}
