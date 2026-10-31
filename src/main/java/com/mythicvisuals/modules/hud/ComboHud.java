package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.modules.combat.ComboTracker;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

import java.util.List;

/** Consecutive hits on entities; resets on damage or 3s pause. */
public class ComboHud extends HudModule implements ClientTickable {
    private final ComboTracker tracker = new ComboTracker();
    private float lastHealth = -1;
    private boolean listenersRegistered;

    public ComboHud() {
        super("combo", "Combo Counter", "Consecutive hits without taking damage", 4, 180);
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                try {
                    if (player == Minecraft.getInstance().player) {
                        tracker.hit(System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    com.mythicvisuals.core.CrashGuard.warnOnce(
                            org.slf4j.LoggerFactory.getLogger(ComboHud.class),
                            "combo-hit", "Combo counter failed", e);
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
        lastHealth = -1;
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null) {
            return;
        }
        // hurt detection: health dropped since last tick
        float hp = client.player.getHealth();
        if (lastHealth >= 0 && hp < lastHealth) {
            tracker.onHurt();
        }
        lastHealth = hp;
    }

    @Override
    protected List<String> lines(Minecraft client) {
        int combo = tracker.combo(System.currentTimeMillis());
        return combo > 0 ? List.of("Combo: " + combo) : List.of();
    }
}
