package com.ivanitskiye.backroomsevolved.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

public class ClientSetup {
    public static KeyMapping KEY_RECORD;

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientSetup::onKeys);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        MinecraftForge.EVENT_BUS.register(AmbienceManager.class);
    }

    private static void onKeys(RegisterKeyMappingsEvent event) {
        KEY_RECORD = new KeyMapping("key.backroomsevolved.record", GLFW.GLFW_KEY_R,
                "key.categories.backroomsevolved");
        event.register(KEY_RECORD);
    }

    /** Открыть экран просмотра записи камеры (только клиент). */
    public static void openPlayback(ItemStack camera) {
        var tag = camera.getOrCreateTag();
        String id = tag.getString(com.ivanitskiye.backroomsevolved.item.CameraItem.TAG_REC_ID);
        int frames = tag.getInt(com.ivanitskiye.backroomsevolved.item.CameraItem.TAG_REC_FRAMES);
        if (!id.isEmpty() && frames > 0) {
            Minecraft.getInstance().setScreen(new CameraPlaybackScreen(id, frames));
        }
    }

    public static String recordKeyName() {
        return KEY_RECORD != null ? KEY_RECORD.getTranslatedKeyMessage().getString() : "R";
    }
}
