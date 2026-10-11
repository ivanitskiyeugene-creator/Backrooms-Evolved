package com.ivanitskiye.backroomsevolved.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Влажный ковёр: шаги вязнут, а рядом с ним воздух сыреет (клиентский туман/запотевание).
 */
public class DampCarpetBlock extends FootprintCarpetBlock {
    public DampCarpetBlock(Properties props) {
        super(props);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        // Лёгкое "чавканье" — идёшь медленнее
        entity.makeStuckInBlock(state, new Vec3(0.85D, 1.0D, 0.85D));
    }
}
