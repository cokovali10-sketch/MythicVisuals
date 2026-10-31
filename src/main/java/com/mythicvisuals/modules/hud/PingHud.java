package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.List;

public class PingHud extends HudModule {
    public PingHud() {
        super("ping", "Ping", "Server latency in milliseconds", 4, 28);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        int ping = -1;
        if (client.getConnection() != null && client.player != null) {
            PlayerInfo entry = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }
        return List.of(ping >= 0 ? "Ping: " + ping + " ms" : "Ping: --");
    }
}
