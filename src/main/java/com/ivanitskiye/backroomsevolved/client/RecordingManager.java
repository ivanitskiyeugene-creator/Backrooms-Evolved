package com.ivanitskiye.backroomsevolved.client;

import com.ivanitskiye.backroomsevolved.item.CameraItem;
import com.ivanitskiye.backroomsevolved.network.ModNetwork;
import com.ivanitskiye.backroomsevolved.network.RecordingDonePacket;
import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Framebuffer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Запись с камеры: раз в 10 тиков снимает кадр с экрана (уменьшенный),
 * держит в памяти до 240 кадров, по остановке пишет PNG на диск
 * в {@code <gameDir>/backrooms_recordings/<id>/}.
 */
public class RecordingManager {
    public static final int CAPTURE_EVERY_TICKS = 10;
    public static final int MAX_FRAMES = 240;
    public static final int FRAME_W = 320;
    public static final int FRAME_H = 180;

    public static boolean recording = false;
    private static String recId = "";
    private static final List<NativeImage> frames = new ArrayList<>();
    private static int tickCounter = 0;

    public static void tick(Minecraft mc, ItemStack camera) {
        boolean want = !camera.isEmpty()
                && camera.getItem() instanceof CameraItem
                && CameraItem.isRecording(camera)
                && CameraItem.getBattery(camera) > 0;

        if (want && !recording) start(mc);
        if (!want && recording) stop(mc, camera);

        if (recording) {
            if (++tickCounter % CAPTURE_EVERY_TICKS == 0) capture(mc);
        }
    }

    private static void start(Minecraft mc) {
        recording = true;
        tickCounter = 0;
        recId = Long.toHexString(System.currentTimeMillis());
        frames.clear();
        if (mc.player != null) {
            mc.player.playSound(ModSounds.CAMERA_REC_BEEP.get(), 0.6f, 1.2f);
        }
    }

    private static void capture(Minecraft mc) {
        if (frames.size() >= MAX_FRAMES) {
            stop(mc, mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY);
            return;
        }
        Framebuffer fb = mc.getMainRenderTarget();
        try {
            NativeImage full = new NativeImage(fb.viewWidth, fb.viewHeight, false);
            fb.bindRead();
            full.downloadTexture(0, false);
            NativeImage scaled = downscale(full, FRAME_W, FRAME_H);
            full.close();
            frames.add(scaled);
        } catch (Exception ignored) {
            // кадр пропущен — не страшно
        }
    }

    private static NativeImage downscale(NativeImage src, int w, int h) {
        NativeImage out = new NativeImage(w, h, false);
        float sx = src.getWidth() / (float) w;
        float sy = src.getHeight() / (float) h;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setPixelRGBA(x, y, src.getPixelRGBA((int) (x * sx), (int) (y * sy)));
            }
        }
        return out;
    }

    private static void stop(Minecraft mc, ItemStack camera) {
        recording = false;
        int count = frames.size();
        if (count > 0 && !recId.isEmpty()) {
            File dir = new File(mc.gameDirectory, "backrooms_recordings" + File.separator + recId);
            if (dir.mkdirs() || dir.isDirectory()) {
                for (int i = 0; i < frames.size(); i++) {
                    NativeImage img = frames.get(i);
                    try {
                        img.save(new File(dir, String.format("frame_%03d.png", i)));
                    } catch (IOException ignored) {
                    }
                    img.close();
                }
            }
            frames.clear();
            if (mc.player != null) {
                mc.player.playSound(ModSounds.CAMERA_REC_BEEP.get(), 0.6f, 0.8f);
                ModNetwork.CHANNEL.sendToServer(new RecordingDonePacket(recId, count));
            }
        } else {
            for (NativeImage img : frames) img.close();
            frames.clear();
        }
    }

    public static String recordingsRoot() {
        return "backrooms_recordings";
    }

    public static boolean hasActiveRecording() {
        return recording;
    }

    public static void playBeepHint(Minecraft mc) {
        if (mc.player != null && mc.level != null) {
            mc.level.playSound(mc.player, mc.player.blockPosition(),
                    ModSounds.CAMERA_REC_BEEP.get(), SoundSource.PLAYERS, 0.5f, 1f);
        }
    }
}
