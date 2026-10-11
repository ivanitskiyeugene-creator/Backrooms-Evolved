package com.ivanitskiye.backroomsevolved.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Аварийная красная лампа: не перегорает, но тревожно пульсирует.
 * Красный — значит что-то не так.
 */
public class AlarmLampBlock extends BackroomsLampBlock {
    public AlarmLampBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 14);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        level.setBlock(pos, state.setValue(LIT, !state.getValue(LIT)), 3);
        level.scheduleTick(pos, this, 14);
    }
}
