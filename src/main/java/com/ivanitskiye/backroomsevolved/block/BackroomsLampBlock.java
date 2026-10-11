package com.ivanitskiye.backroomsevolved.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * База гудящих ламп Бекрумса. Свет даёт, только если горит (и не сгорела).
 * Конкретное поведение (мигание/перегорание или пульс) — в подклассах.
 */
public abstract class BackroomsLampBlock extends Block {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final BooleanProperty BURNED = BooleanProperty.create("burned");

    protected BackroomsLampBlock(Properties props) {
        super(props);
    }

    public static boolean lightOf(BlockState state) {
        return state.getValue(LIT) && (!state.hasProperty(BURNED) || !state.getValue(BURNED));
    }
}
