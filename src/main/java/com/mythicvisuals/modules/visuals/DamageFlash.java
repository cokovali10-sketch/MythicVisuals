package com.mythicvisuals.modules.visuals;

import com.mythicvisuals.core.hud.HudModule;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.setting.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Red screen flash whenever you take damage. Fades fast. */
public class DamageFlash extends HudModule implements ClientTickable {
    private final BooleanSetting flash;
    private float lastHp = -1;
    private long lastHurtMs = -1;

    public DamageFlash() {
        super("damage_flash", "Damage Flash", "Red flash on hurt", 4, 228);
        this.flash = setting(new BooleanSetting("flash", "Fullscreen flash", true));
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null) {
            lastHp = -1;
            return;
        }
        float hp = client.player.getHealth();
        if (lastHp >= 0 && hp < lastHp - 0.01F) {
            lastHurtMs = System.currentTimeMillis();
        }
        lastHp = hp;
    }

    @Override
    protected List<String> lines(Minecraft client) {
        return List.of();
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Minecraft client) {
        long age = System.currentTimeMillis() - lastHurtMs;
        if (lastHurtMs < 0 || age < 0 || age > 500 || !flash.get()) {
            return;
        }
        int alpha = (int) (110 * (1.0 - (double) age / 500));
        int color = (Math.max(0, Math.min(255, alpha)) << 24) | 0xCC0000;
        ctx.fill(0, 0,
                client.getWindow().getGuiScaledWidth(),
                client.getWindow().getGuiScaledHeight(), color);
    }

    @Override
    public int[] bounds(Minecraft client) {
        return new int[]{x.get(), y.get(), 0, 0}; // fullscreen, not draggable
    }
}
