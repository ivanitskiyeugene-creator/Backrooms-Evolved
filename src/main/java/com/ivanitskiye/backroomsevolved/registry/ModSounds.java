package com.ivanitskiye.backroomsevolved.registry;

import com.ivanitskiye.backroomsevolved.BackroomsEvolved;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BackroomsEvolved.MODID);

    public static final RegistryObject<SoundEvent> LAMP_HUM =
            SOUNDS.register("lamp_hum", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("lamp_hum")));
    public static final RegistryObject<SoundEvent> CAMERA_CLICK =
            SOUNDS.register("camera_click", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("camera_click")));
    public static final RegistryObject<SoundEvent> CAMERA_POWER_DOWN =
            SOUNDS.register("camera_power_down", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("camera_power_down")));
    public static final RegistryObject<SoundEvent> CAMERA_STATIC =
            SOUNDS.register("camera_static", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("camera_static")));
    public static final RegistryObject<SoundEvent> WHISPER =
            SOUNDS.register("whisper", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("whisper")));
    public static final RegistryObject<SoundEvent> HEARTBEAT =
            SOUNDS.register("heartbeat", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("heartbeat")));
    public static final RegistryObject<SoundEvent> BASEMENT_AMBIENCE =
            SOUNDS.register("basement_ambience", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("basement_ambience")));
    public static final RegistryObject<SoundEvent> CAMERA_REC_BEEP =
            SOUNDS.register("camera_rec_beep", () -> SoundEvent.createVariableRangeEvent(BackroomsEvolved.loc("camera_rec_beep")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
