package com.ivanitskiye.backroomsevolved.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Оценивает "открытость" комнаты вокруг игрока (объём воздуха),
 * чтобы эхо в маленькой комнате было коротким, а в огромном зале — длинным и гулким.
 */
public class RoomSampler {
    public static final int MAX_VOLUME = 800;
    /** Количество доступных воздушных блоков вокруг игрока. */
    public static int openness = 64;

    private static int sampleCounter = 0;

    public static void tick(Level level, BlockPos origin) {
        if (++sampleCounter % 40 != 0) return;
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(origin.immutable());
        seen.add(origin.immutable());
        int air = 0;
        while (!queue.isEmpty() && seen.size() < MAX_VOLUME) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.canBeReplaced()) continue;
            air++;
            for (var dir : new BlockPos[]{pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west()}) {
                if (seen.add(dir.immutable())) queue.add(dir);
            }
        }
        openness = Math.max(16, Math.min(MAX_VOLUME, air));
    }
}
