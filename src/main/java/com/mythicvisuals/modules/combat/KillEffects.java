package com.mythicvisuals.modules.combat;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

/** Kill celebration: particle burst + level-up sound on confirmed kill. */
public class KillEffects extends Module implements ClientTickable {
    private final BooleanSetting sound;
    private LivingEntity tracked;
    private boolean listenersRegistered;

    public KillEffects() {
        super("kill_effects", "Kill Effects", "Burst and sound on kill", Category.COMBAT);
        this.sound = setting(new BooleanSetting("sound", "Play sound", true));
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                try {
                    if (player == Minecraft.getInstance().player && entity instanceof LivingEntity living) {
                        tracked = living;
                    }
                } catch (Exception e) {
                    com.mythicvisuals.core.CrashGuard.warnOnce(
                            org.slf4j.LoggerFactory.getLogger(KillEffects.class),
                            "kill-track", "Kill tracking failed", e);
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
    }

    @Override
    public void onDisable() {
        tracked = null;
    }

    @Override
    public void clientTick(Minecraft client) {
        LivingEntity t = tracked;
        if (t == null || client.player == null || client.level == null) {
            return;
        }
        if (client.player.distanceTo(t) > 64.0F) {
            tracked = null; // unloaded or walked away, not a kill
            return;
        }
        if (!t.isAlive() || t.isRemoved()) {
            tracked = null;
            celebrate(client, t);
        }
    }

    private void celebrate(Minecraft client, LivingEntity t) {
        double px = t.getX();
        double py = t.getY() + 1.0;
        double pz = t.getZ();
        for (int i = 0; i < 16; i++) {
            double vx = (Math.random() - 0.5) * 1.6;
            double vy = Math.random() * 1.2;
            double vz = (Math.random() - 0.5) * 1.6;
            client.level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
        }
        if (sound.get()) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.PLAYER_LEVELUP, 0.6F, 1.4F));
        }
    }
}
