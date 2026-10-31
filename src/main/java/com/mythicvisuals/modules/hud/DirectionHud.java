package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.DirectionUtil;
import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;

import java.util.List;

public class DirectionHud extends HudModule {
    public DirectionHud() {
        super("direction", "Direction", "Compass direction and axis", 4, 52);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        float yaw = client.player.getYRot();
        return List.of(DirectionUtil.compass(yaw) + " (" + DirectionUtil.axis(yaw) + ")");
    }
}
