package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import com.mythicvisuals.module.ClientTickable;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Recent damage YOU dealt, fading feed. Detected via HP deltas. */
public class DamageNumbers extends HudModule implements ClientTickable {
    public static final long EXPIRE_MS = 1500L;
    private static final int MAX_ENTRIES = 4;

    private final Map<LivingEntity, Float> watched = new HashMap<>();
    private final Deque<Entry> feed = new ArrayDeque<>();
    private boolean listenersRegistered;

    private record Entry(double amount, long time) {
    }

    public DamageNumbers() {
        super("damage_numbers", "Damage Numbers", "Your recent hits feed", 4, 228);
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                try {
                    if (player == Minecraft.getInstance().player && entity instanceof LivingEntity living) {
                        watched.put(living, living.getHealth());
                    }
                } catch (Exception e) {
                    com.mythicvisuals.core.CrashGuard.warnOnce(
                            org.slf4j.LoggerFactory.getLogger(DamageNumbers.class),
                            "dmg-track", "Damage tracking failed", e);
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
    }

    @Override
    public void onDisable() {
        watched.clear();
        feed.clear();
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        watched.entrySet().removeIf(e -> {
            LivingEntity t = e.getKey();
            if (!t.isAlive() || t.isRemoved() || now - lastSeen(t) > 4000L) {
                return true;
            }
            float before = e.getValue();
            float hp = t.getHealth();
            if (hp < before - 0.05F) {
                feed.addLast(new Entry(before - hp, now));
                while (feed.size() > MAX_ENTRIES) {
                    feed.removeFirst();
                }
            }
            e.setValue(hp);
            return false;
        });
        feed.removeIf(en -> now - en.time() > EXPIRE_MS);
    }

    private final Map<LivingEntity, Long> seen = new HashMap<>();

    private long lastSeen(LivingEntity t) {
        return seen.computeIfAbsent(t, k -> System.currentTimeMillis());
    }

    @Override
    protected List<String> lines(Minecraft client) {
        long now = System.currentTimeMillis();
        return feed.stream()
                .filter(en -> now - en.time() <= EXPIRE_MS)
                .map(en -> String.format(Locale.ROOT, "-%.1f", en.amount()))
                .toList();
    }
}
