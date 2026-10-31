package com.mythicvisuals.modules.visuals;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.IntegerSetting;
import net.minecraft.client.Minecraft;

/** Custom FOV while enabled, previous value restored on disable. */
public class FovChanger extends Module {
    private final IntegerSetting fov;
    private int previous = 90;
    private boolean saved;

    public FovChanger() {
        super("fov_changer", "FOV Changer", "Custom field of view", Category.VISUALS);
        this.fov = setting(new IntegerSetting("fov", "Field of view", 110, 30, 179, 1));
    }

    @Override
    public void onEnable() {
        previous = Minecraft.getInstance().options.fov().get();
        saved = true;
        Minecraft.getInstance().options.fov().set(fov.get());
    }

    @Override
    public void onDisable() {
        if (saved) {
            Minecraft.getInstance().options.fov().set(previous);
            saved = false;
        }
    }
}
