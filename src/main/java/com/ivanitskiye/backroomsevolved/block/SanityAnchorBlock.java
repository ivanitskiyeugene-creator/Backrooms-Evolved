package com.ivanitskiye.backroomsevolved.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Якорь рассудка: тёплый свет, рядом с которым рассудок восстанавливается.
 */
public class SanityAnchorBlock extends Block {
    public SanityAnchorBlock(Properties props) {
        super(props);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.3 + rand.nextDouble() * 0.4,
                    pos.getY() + 0.9,
                    pos.getZ() + 0.3 + rand.nextDouble() * 0.4,
                    0, 0.02 + rand.nextDouble() * 0.02, 0);
        }
        if (rand.nextInt(20) == 0) {
            level.addParticle(ParticleTypes.COMPOSTER,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    0, 0.03, 0);
        }
    }
}
