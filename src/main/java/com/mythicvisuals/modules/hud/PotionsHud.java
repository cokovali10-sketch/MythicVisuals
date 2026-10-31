package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class PotionsHud extends HudModule {
    public PotionsHud() {
        super("potions", "Potion Effects", "Active effects with time left", 4, 168);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        List<String> out = new ArrayList<>();
        for (MobEffectInstance effect : client.player.getActiveEffects()) {
            int seconds = effect.getDuration() / 20;
            out.add(effect.getEffect().value().getDisplayName().getString()
                    + " " + (seconds / 60) + ":" + String.format("%02d", seconds % 60));
        }
        return out;
    }
}
