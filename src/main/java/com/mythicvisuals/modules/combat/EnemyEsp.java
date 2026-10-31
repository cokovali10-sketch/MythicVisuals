package com.mythicvisuals.modules.combat;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.MultiSelectSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Enemy ESP via vanilla glow: no mixins, no packets, client-side only. */
public class EnemyEsp extends Module implements ClientTickable {
    private final MultiSelectSetting targets;
    private final Set<LivingEntity> glowed = new LinkedHashSet<>();

    public EnemyEsp() {
        super("enemy_esp", "Enemy ESP", "White glow outline on selected targets", Category.COMBAT);
        this.targets = setting(new MultiSelectSetting("targets", "Who glows",
                List.of("players", "monsters", "animals"), Set.of("players", "monsters")));
    }

    @Override
    public void onDisable() {
        clear();
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        Set<String> want = targets.get();
        for (LivingEntity e : new ArrayList<>(glowed)) {
            if (!e.isAlive() || e.isRemoved() || !matches(client, e, want)) {
                e.setGlowingTag(false);
                glowed.remove(e);
            }
        }
        for (var entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (!living.isAlive() || glowed.contains(living) || !matches(client, living, want)) {
                continue;
            }
            living.setGlowingTag(true);
            glowed.add(living);
        }
    }

    private static boolean matches(Minecraft client, LivingEntity e, Set<String> want) {
        if (e instanceof Player p) {
            return p != client.player && want.contains("players");
        }
        if (e instanceof Monster) {
            return want.contains("monsters");
        }
        if (e instanceof Animal) {
            return want.contains("animals");
        }
        return false;
    }

    private void clear() {
        for (LivingEntity e : glowed) {
            try {
                e.setGlowingTag(false);
            } catch (Exception ignored) {
            }
        }
        glowed.clear();
    }
}
