package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import java.util.List;

/** Center-screen hit marker, fading out. Position follows x/y as offset. */
public class HitMarkers extends HudModule {
    public static final long DISPLAY_MS = 600L;

    private long lastHitMs = -1;
    private boolean listenersRegistered;

    public HitMarkers() {
        super("hitmarkers", "Hitmarkers", "Center X flash on every hit", 0, 0);
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                if (player == Minecraft.getInstance().player) {
                    lastHitMs = System.currentTimeMillis();
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
    }

    @Override
    protected List<String> lines(Minecraft client) {
        return List.of();
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Minecraft client) {
        long age = System.currentTimeMillis() - lastHitMs;
        if (age < 0 || age >= DISPLAY_MS) {
            return;
        }
        float s = scale.get().floatValue();
        int alpha = (int) (255 * (1.0 - (double) age / DISPLAY_MS));
        int color = (Math.max(0, Math.min(255, alpha)) << 24) | (textColor.get() & 0xFFFFFF);
        int cx = client.getWindow().getGuiScaledWidth() / 2 + x.get();
        int cy = client.getWindow().getGuiScaledHeight() / 2 + y.get();
        ctx.pose().pushMatrix();
        try {
            ctx.pose().scale(s, s);
            String mark = "x";
            int w = client.font.width(mark);
            ctx.text(client.font, Component.literal(mark),
                    Math.round(cx / s) - w / 2, Math.round(cy / s),
                    color, shadow.get());
        } finally {
            ctx.pose().popMatrix();
        }
    }

    @Override
    public int[] bounds(Minecraft client) {
        long age = System.currentTimeMillis() - lastHitMs;
        if (age < 0 || age >= DISPLAY_MS) {
            return new int[]{x.get(), y.get(), 0, 0};
        }
        return new int[]{client.getWindow().getGuiScaledWidth() / 2 + x.get() - 8,
                client.getWindow().getGuiScaledHeight() / 2 + y.get(),
                16, 16};
    }
}
