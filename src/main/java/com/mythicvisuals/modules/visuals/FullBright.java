package com.mythicvisuals.modules.visuals;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;

/** Max gamma while enabled, previous value restored on disable. */
public class FullBright extends Module implements ClientTickable {
    private final NumberSetting brightness;
    private double previous = 1.0;
    private boolean saved;

    public FullBright() {
        super("fullbright", "FullBright", "Maximum brightness, no potions needed", Category.VISUALS);
        this.brightness = setting(new NumberSetting("brightness", "Gamma value", 12.0, 1.0, 16.0, 0.5));
    }

    @Override
    public void onEnable() {
        previous = Minecraft.getInstance().options.gamma().get();
        saved = true;
    }

    @Override
    public void onDisable() {
        if (saved) {
            Minecraft.getInstance().options.gamma().set(previous);
            saved = false;
        }
    }

    @Override
    public void clientTick(Minecraft client) {
        double target = brightness.get();
        if (!Double.valueOf(target).equals(client.options.gamma().get())) {
            client.options.gamma().set(target);
        }
    }
}
