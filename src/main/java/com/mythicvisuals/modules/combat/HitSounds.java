package com.mythicvisuals.modules.combat;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.NumberSetting;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

/** Punchy hit sound with pitch/volume control. Vanilla sounds only. */
public class HitSounds extends Module {
    public enum HitSoundKind {
        STRONG, WEAK, CRIT, SWEEP, ORB
    }

    private final EnumSetting<HitSoundKind> sound;
    private final NumberSetting volume;
    private final NumberSetting pitch;
    private boolean listenersRegistered;

    public HitSounds() {
        super("hit_sounds", "Hit Sounds", "Sound on every hit", Category.COMBAT);
        this.sound = setting(new EnumSetting<>("sound", "Sound", HitSoundKind.STRONG, HitSoundKind.class));
        this.volume = setting(new NumberSetting("volume", "Volume", 1.0, 0.0, 2.0, 0.1));
        this.pitch = setting(new NumberSetting("pitch", "Pitch", 1.2, 0.5, 2.0, 0.1));
    }

    @Override
    public void onEnable() {
        if (!listenersRegistered) {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                try {
                    Minecraft client = Minecraft.getInstance();
                    if (player == client.player && entity instanceof LivingEntity) {
                        client.getSoundManager().play(SimpleSoundInstance.forUI(
                                event(), volume.get().floatValue(), pitch.get().floatValue()));
                    }
                } catch (Exception e) {
                    com.mythicvisuals.core.CrashGuard.warnOnce(
                            org.slf4j.LoggerFactory.getLogger(HitSounds.class),
                            "hit-sounds", "Hit sound failed", e);
                }
                return InteractionResult.PASS;
            });
            listenersRegistered = true;
        }
    }

    private SoundEvent event() {
        return switch (sound.get()) {
            case WEAK -> SoundEvents.PLAYER_ATTACK_WEAK;
            case CRIT -> SoundEvents.PLAYER_ATTACK_CRIT;
            case SWEEP -> SoundEvents.PLAYER_ATTACK_SWEEP;
            case ORB -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            default -> SoundEvents.PLAYER_ATTACK_STRONG;
        };
    }
}
