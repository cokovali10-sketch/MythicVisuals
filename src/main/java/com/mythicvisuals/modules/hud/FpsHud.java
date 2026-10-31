package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;

import java.util.List;

public class FpsHud extends HudModule {
    public FpsHud() {
        super("fps", "FPS", "Frames per second", 4, 4);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        return List.of("FPS: " + client.getFps());
    }
}
