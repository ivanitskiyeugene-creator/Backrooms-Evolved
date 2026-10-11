package com.ivanitskiye.backroomsevolved.client;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Звук-эхо: маркер-класс, чтобы эхо не эхило само себя. */
public class EchoSoundInstance extends SimpleSoundInstance {
    public EchoSoundInstance(ResourceLocation sound, SoundSource source, float volume, float pitch,
                             double x, double y, double z) {
        super(sound, source, volume, pitch, RandomSource.create(), false, 0,
                Attenuation.LINEAR, x, y, z, false);
    }

    public static boolean isEcho(SoundInstance instance) {
        return instance instanceof EchoSoundInstance;
    }
}
