package com.mythicvisuals.modules.combat;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Glowing particle trail at your feet while moving. */
public class Trails extends Module implements ClientTickable {
    public enum TrailKind {
        FLAME, SOUL, CRIT, HEART, NOTE
    }

    private final EnumSetting<TrailKind> kind;
    private final IntegerSetting interval;
    private final BooleanSetting onlyMoving;

    private int tick;
    private double lastX;
    private double lastZ;
    private boolean hasLast;

    public Trails() {
        super("trails", "Trails", "Particle trail while moving", Category.VISUALS);
        this.kind = setting(new EnumSetting<>("mode", "Particle", TrailKind.SOUL, TrailKind.class));
        this.interval = setting(new IntegerSetting("interval", "Every N ticks", 2, 1, 10, 1));
        this.onlyMoving = setting(new BooleanSetting("only_moving", "Only while moving", true));
    }

    @Override
    public void onDisable() {
        hasLast = false;
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        tick++;
        if (tick % Math.max(1, interval.get()) != 0) {
            return;
        }
        double x = client.player.getX();
        double z = client.player.getZ();
        if (onlyMoving.get()) {
            if (hasLast) {
                double dx = x - lastX;
                double dz = z - lastZ;
                if (dx * dx + dz * dz < 0.0025) {
                    return;
                }
            }
            hasLast = true;
            lastX = x;
            lastZ = z;
        }
        ParticleOptions p = switch (kind.get()) {
            case FLAME -> ParticleTypes.FLAME;
            case CRIT -> ParticleTypes.CRIT;
            case HEART -> ParticleTypes.HEART;
            case NOTE -> ParticleTypes.NOTE;
            default -> ParticleTypes.SOUL_FIRE_FLAME;
        };
        client.level.addParticle(p, x, client.player.getY() + 0.1, z, 0.0, 0.05, 0.0);
    }
}
