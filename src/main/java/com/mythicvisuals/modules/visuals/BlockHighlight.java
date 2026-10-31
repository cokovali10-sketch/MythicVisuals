package com.mythicvisuals.modules.visuals;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.EnumSetting;
import com.mythicvisuals.module.setting.IntegerSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Dotted particle outline on the targeted block.
 * (True line outlines need the render-submit pipeline + in-game testing;
 * particles are 100% vanilla API and always visible.)
 */
public class BlockHighlight extends Module implements ClientTickable {
    public enum OutlineKind {
        MAGIC, FLAME, SOUL, NOTE
    }

    private final EnumSetting<OutlineKind> kind;
    private final IntegerSetting density;
    private int tick;

    public BlockHighlight() {
        super("block_highlight", "Block Highlight", "Dotted outline on aimed block", Category.VISUALS);
        this.kind = setting(new EnumSetting<>("style", "Particle", OutlineKind.MAGIC, OutlineKind.class));
        this.density = setting(new IntegerSetting("density", "Corners per tick", 2, 1, 8, 1));
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        if (!(client.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (++tick % 2 != 0) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        ParticleOptions p = switch (kind.get()) {
            case FLAME -> ParticleTypes.FLAME;
            case SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
            case NOTE -> ParticleTypes.NOTE;
            default -> ParticleTypes.ENCHANTED_HIT;
        };
        // 8 corners, a few per tick
        int[][] corners = {{0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0},
                {0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1}};
        for (int i = 0; i < Math.min(density.get(), 8); i++) {
            int[] c = corners[(tick + i) % 8];
            client.level.addParticle(p,
                    pos.getX() + c[0], pos.getY() + c[1], pos.getZ() + c[2],
                    0.0, 0.0, 0.0);
        }
    }
}
