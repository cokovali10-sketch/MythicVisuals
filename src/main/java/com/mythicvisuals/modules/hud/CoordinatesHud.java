package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;

import java.util.List;

public class CoordinatesHud extends HudModule {
    public CoordinatesHud() {
        super("coordinates", "Coordinates", "Block XYZ position", 4, 40);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        return List.of("XYZ: " + client.player.getBlockX()
                + " / " + client.player.getBlockY()
                + " / " + client.player.getBlockZ());
    }
}
