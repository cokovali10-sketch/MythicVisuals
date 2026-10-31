package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.CpsCounter;
import com.mythicvisuals.core.hud.HudModule;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

import java.util.List;

public class CpsHud extends HudModule {
    private final CpsCounter left = new CpsCounter();
    private final CpsCounter right = new CpsCounter();
    private boolean listenersRegistered;

    public CpsHud() {
        super("cps", "CPS", "Left | right clicks per second", 4, 16);
    }

    @Override
    public void onEnable() {
        // Fabric events cannot be unregistered: register exactly once,
        // counters simply stop being read while disabled.
        if (listenersRegistered) {
            return;
        }
        listenersRegistered = true;
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            try {
                left.record(System.currentTimeMillis());
            } catch (Exception e) {
                com.mythicvisuals.core.CrashGuard.warnOnce(
                        org.slf4j.LoggerFactory.getLogger(CpsHud.class),
                        "cps-left", "CPS counter failed", e);
            }
            return InteractionResult.PASS;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            try {
                left.record(System.currentTimeMillis());
            } catch (Exception e) {
                com.mythicvisuals.core.CrashGuard.warnOnce(
                        org.slf4j.LoggerFactory.getLogger(CpsHud.class),
                        "cps-left", "CPS counter failed", e);
            }
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            try {
                right.record(System.currentTimeMillis());
            } catch (Exception e) {
                com.mythicvisuals.core.CrashGuard.warnOnce(
                        org.slf4j.LoggerFactory.getLogger(CpsHud.class),
                        "cps-right", "CPS counter failed", e);
            }
            return InteractionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            try {
                right.record(System.currentTimeMillis());
            } catch (Exception e) {
                com.mythicvisuals.core.CrashGuard.warnOnce(
                        org.slf4j.LoggerFactory.getLogger(CpsHud.class),
                        "cps-right", "CPS counter failed", e);
            }
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            try {
                right.record(System.currentTimeMillis());
            } catch (Exception e) {
                com.mythicvisuals.core.CrashGuard.warnOnce(
                        org.slf4j.LoggerFactory.getLogger(CpsHud.class),
                        "cps-right", "CPS counter failed", e);
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    protected List<String> lines(Minecraft client) {
        long now = System.currentTimeMillis();
        return List.of(left.count(now) + " | " + right.count(now) + " CPS");
    }
}
