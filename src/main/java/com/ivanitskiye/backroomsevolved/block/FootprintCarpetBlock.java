package com.ivanitskiye.backroomsevolved.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Ковёр, на котором остаются вмятины от шагов. Следы медленно расправляются.
 * Если кто-то прошёл до тебя — следы уже есть.
 */
public class FootprintCarpetBlock extends Block {
    public static final IntegerProperty FOOTPRINTS = IntegerProperty.create("footprints", 0, 3);

    public FootprintCarpetBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FOOTPRINTS, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FOOTPRINTS);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity && entity.isAlive()) {
            int f = state.getValue(FOOTPRINTS);
            if (f < 3) {
                level.setBlock(pos, state.setValue(FOOTPRINTS, f + 1), 3);
            }
            scheduleDecay(level, pos);
        }
        super.stepOn(level, pos, state, entity);
    }

    private void scheduleDecay(Level level, BlockPos pos) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 200 + level.random.nextInt(80));
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        int f = state.getValue(FOOTPRINTS);
        if (f > 0) {
            level.setBlock(pos, state.setValue(FOOTPRINTS, f - 1), 3);
            level.scheduleTick(pos, this, 200 + rand.nextInt(80));
        }
    }
}
