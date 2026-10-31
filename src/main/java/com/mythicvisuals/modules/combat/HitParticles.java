package com.mythicvisuals.modules.combat;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

/** Vanilla particle burst on every hit (crit-aware). Client-side only. */
public class HitParticles extends Module {
    private final IntegerSetting count;
    private final BooleanSetting critOnly;
    private boolean listenersRegistered;

    public HitParticles() {
        super("hit_particles", "Hit Particles", "Crit burst on every hit", Category.COMBAT);
        this.count = setting(new IntegerSetting("count", "Particles per hit", 8, 1, 20, 1));
        this.critOnly = setting(new BooleanSetting("crit_only", "Only on crits", false));
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                try {
                    Minecraft client = Minecraft.getInstance();
                    if (player == client.player && entity instanceof LivingEntity living && client.level != null) {
                        boolean crit = player.fallDistance > 0.0F;
                        if (!critOnly.get() || crit) {
                            burst(client, living, crit);
                        }
                    }
                } catch (Exception e) {
                    com.mythicvisuals.core.CrashGuard.warnOnce(
                            org.slf4j.LoggerFactory.getLogger(HitParticles.class),
                            "hit-particles", "Hit particles failed", e);
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
    }

    private void burst(Minecraft client, LivingEntity target, boolean crit) {
        double px = target.getX();
        double py = target.getEyeY() - 0.4;
        double pz = target.getZ();
        for (int i = 0; i < count.get(); i++) {
            double vx = (Math.random() - 0.5) * 0.9;
            double vy = Math.random() * 0.7;
            double vz = (Math.random() - 0.5) * 0.9;
            client.level.addParticle(crit ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT,
                    px, py, pz, vx, vy, vz);
        }
    }
}
