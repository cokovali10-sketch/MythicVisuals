package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Locale;

/** Last attacked target: name, health and distance. Fades after 5s. */
public class TargetHud extends HudModule {
    public static final long EXPIRE_MS = 5000L;

    private LivingEntity target;
    private long lastHitMs = -1;
    private boolean listenersRegistered;

    public TargetHud() {
        super("target", "Target HUD", "Last attacked enemy info", 4, 204);
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                try {
                    if (player == Minecraft.getInstance().player && entity instanceof LivingEntity living) {
                        target = living;
                        lastHitMs = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    com.mythicvisuals.core.CrashGuard.warnOnce(
                            org.slf4j.LoggerFactory.getLogger(TargetHud.class),
                            "target-track", "Target tracking failed", e);
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
    }

    @Override
    protected List<String> lines(Minecraft client) {
        LivingEntity t = target;
        if (t == null || !t.isAlive() || System.currentTimeMillis() - lastHitMs > EXPIRE_MS) {
            return List.of();
        }
        String hp = String.format(Locale.ROOT, "HP: %d/%d",
                Math.max(0, Math.round(t.getHealth())), Math.round(t.getMaxHealth()));
        String dist = String.format(Locale.ROOT, "Dist: %.1f", client.player.distanceTo(t));
        return List.of(t.getName().getString(), hp, dist);
    }
}
