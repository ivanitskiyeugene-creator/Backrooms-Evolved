package com.ivanitskiye.backroomsevolved.client;

import com.ivanitskiye.backroomsevolved.block.BackroomsLampBlock;
import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Фоновые лупы: гул ламп рядом с горящими лампами и подвальный гул при сырости/безумии.
 * Плюс пыль в свете ламп и "фигура", когда свет мигнул и погас.
 */
public class AmbienceManager {
    private static final Random RANDOM = new Random();
    private static int tickCount = 0;
    private static boolean humActive = false;
    private static boolean basementActive = false;
    private static int lastLitLamps = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            stopAll(mc);
            return;
        }
        if (++tickCount % 10 != 0) return;

        int litLamps = 0;
        BlockPos center = mc.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-7, -5, -7), center.offset(7, 6, 7))) {
            BlockState state = mc.level.getBlockState(pos);
            if (state.getBlock() instanceof BackroomsLampBlock && BackroomsLampBlock.lightOf(state)) {
                litLamps++;
            }
        }
        boolean nearLitLamp = litLamps > 0;

        // Свет мигнул и погас — в темноте что-то могло появиться
        if (lastLitLamps > 0 && litLamps == 0 && SanityManager.density > 0.15f && RANDOM.nextFloat() < 0.5f) {
            SanityManager.spawnFigure(mc.player);
            SanityManager.glitchTimer = Math.max(SanityManager.glitchTimer, 6);
        }
        lastLitLamps = litLamps;

        // Пыль в свете ламп: медленно кружащиеся частицы
        if (nearLitLamp && SanityManager.density > 0.1f) {
            var p = mc.player;
            for (int i = 0; i < 2; i++) {
                mc.level.addParticle(ParticleTypes.WHITE_ASH,
                        p.getX() + (RANDOM.nextDouble() - 0.5) * 8,
                        p.getY() + RANDOM.nextDouble() * 3,
                        p.getZ() + (RANDOM.nextDouble() - 0.5) * 8,
                        (RANDOM.nextDouble() - 0.5) * 0.01, -0.004,
                        (RANDOM.nextDouble() - 0.5) * 0.01);
            }
        }

        boolean wantHum = nearLitLamp;
        boolean wantBasement = SanityManager.humidity > 0.4f || SanityManager.sanity > 85f;

        if (wantHum && !humActive) {
            playLoop(mc, ModSounds.LAMP_HUM.get(), SoundSource.BLOCKS, 0.45f);
            humActive = true;
        } else if (!wantHum && humActive) {
            stopLoop(mc, ModSounds.LAMP_HUM.get(), SoundSource.BLOCKS);
            humActive = false;
        }

        if (wantBasement && !basementActive) {
            playLoop(mc, ModSounds.BASEMENT_AMBIENCE.get(), SoundSource.AMBIENT, 0.5f);
            basementActive = true;
        } else if (!wantBasement && basementActive) {
            stopLoop(mc, ModSounds.BASEMENT_AMBIENCE.get(), SoundSource.AMBIENT);
            basementActive = false;
        }
    }

    private static void playLoop(Minecraft mc, SoundEvent sound, SoundSource source, float volume) {
        mc.getSoundManager().play(new SimpleSoundInstance(
                sound, source, volume, 1f, true, 0,
                SoundInstance.Attenuation.NONE, 0, 0, 0, true));
    }

    private static void stopLoop(Minecraft mc, SoundEvent sound, SoundSource source) {
        mc.getSoundManager().stop(sound.getLocation(), source);
    }

    private static void stopAll(Minecraft mc) {
        if (humActive) stopLoop(mc, ModSounds.LAMP_HUM.get(), SoundSource.BLOCKS);
        if (basementActive) stopLoop(mc, ModSounds.BASEMENT_AMBIENCE.get(), SoundSource.AMBIENT);
        humActive = false;
        basementActive = false;
    }
}
