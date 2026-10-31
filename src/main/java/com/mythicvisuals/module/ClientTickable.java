package com.mythicvisuals.module;

import net.minecraft.client.Minecraft;

/** Modules needing per-tick updates implement this; dispatched centrally. */
public interface ClientTickable {
    void clientTick(Minecraft client);
}
