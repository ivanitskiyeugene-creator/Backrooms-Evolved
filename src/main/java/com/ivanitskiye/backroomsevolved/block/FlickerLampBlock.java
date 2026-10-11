package com.ivanitskiye.backroomsevolved.block;

import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Жёлтая/белая лампа: иногда мигает, изредка перегорает навсегда.
 * Перегоревшую можно восстановить пыльцой светокамня.
 */
public class FlickerLampBlock extends BackroomsLampBlock {
    public FlickerLampBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, true).setValue(BURNED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, BURNED);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(BURNED);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (state.getValue(BURNED)) return;
        int roll = rand.nextInt(220);
        if (roll == 0) {
            // Перегорела. Навсегда.
            level.setBlock(pos, state.setValue(BURNED, true).setValue(LIT, false), 3);
        } else if (roll < 8) {
            // Короткое мигание
            level.setBlock(pos, state.setValue(LIT, false), 3);
            level.scheduleTick(pos, this, 4 + rand.nextInt(10));
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!state.getValue(BURNED) && !state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(BURNED) && player.getItemInHand(hand).is(Items.GLOWSTONE_DUST)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(BURNED, false).setValue(LIT, true), 3);
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
                level.playSound(null, pos, ModSounds.CAMERA_CLICK.get(), SoundSource.BLOCKS, 0.6f, 0.8f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
