package com.ivanitskiye.backroomsevolved.client;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Звук-эхо: маркер-класс, чтобы эхо не эхило само себя. */
public class EchoSoundInstance extends SimpleSoundInstance {
    public EchoSoundInstance(SoundEvent sound, SoundSource source, float volume, float pitch,
                             double x, double y, double z) {
        super(sound, source, volume, pitch, false, 0, Attenuation.LINEAR, x, y, z, false);
    }

    public static boolean isEcho(SoundInstance instance) {
        return instance instanceof EchoSoundInstance;
    }
}
