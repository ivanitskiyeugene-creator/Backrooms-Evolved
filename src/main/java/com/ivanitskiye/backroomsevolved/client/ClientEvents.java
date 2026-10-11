package com.ivanitskiye.backroomsevolved.client;

import com.ivanitskiye.backroomsevolved.BackroomsEvolved;
import com.ivanitskiye.backroomsevolved.item.CameraItem;
import com.ivanitskiye.backroomsevolved.network.CameraActionPacket;
import com.ivanitskiye.backroomsevolved.network.ModNetwork;
import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Клиентская атмосфера: санити, туман сырости, запотевание, глитчи, ИК-тон,
 * завал камеры, фигуры и эхо шагов.
 */
public class ClientEvents {
    public static final ResourceLocation FIGURE_TEXTURE =
            new ResourceLocation(BackroomsEvolved.MODID, "textures/misc/figure.png");

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack camera = mc.player.getMainHandItem();
        boolean hasCamera = camera.getItem() instanceof CameraItem;
        ItemStack cam = hasCamera ? camera : ItemStack.EMPTY;

        // клавиша записи
        if (hasCamera && ClientSetup.KEY_RECORD != null && ClientSetup.KEY_RECORD.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new CameraActionPacket(CameraActionPacket.ACTION_TOGGLE_RECORD));
        }

        SanityManager.tick(mc.player, mc.level, cam);
        RoomSampler.tick(mc.level, mc.player.blockPosition());
        RecordingManager.tick(mc, cam);

        // глитчи рядом с сущностями, когда камера в руке
        if (!cam.isEmpty() && CameraItem.getBattery(cam) > 0
                && !mc.level.getEntitiesOfClass(Monster.class, mc.player.getBoundingBox().inflate(10)).isEmpty()) {
            SanityManager.glitchTimer = Math.max(SanityManager.glitchTimer, 3);
            if (RANDOM.nextInt(50) == 0) {
                mc.player.playSound(ModSounds.CAMERA_STATIC.get(), 0.3f, 1f);
            }
        }
    }

    // ---------------------------------------------------------------- GUI
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        GuiGraphics g = event.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();

        ItemStack camera = mc.player.getMainHandItem();
        boolean hasCamera = camera.getItem() instanceof CameraItem;
        boolean ir = hasCamera && CameraItem.isIR(camera) && CameraItem.getBattery(camera) > 0;

        // ИК-тонировка
        if (ir) {
            g.fill(0, 0, w, h, 0x14003300);
        }

        // запотевание/виньетка от сырости
        float humidity = SanityManager.humidity;
        if (humidity > 0.03f) {
            for (int i = 0; i < 4; i++) {
                int alpha = (int) (humidity * 26 * (4 - i));
                int t = 6 + i * 7;
                g.fill(0, 0, w, t, (alpha << 24) | 0x0A0A12);
                g.fill(0, h - t, w, h, (alpha << 24) | 0x0A0A12);
                g.fill(0, 0, t, h, (alpha << 24) | 0x0A0A12);
                g.fill(w - t, 0, w, h, (alpha << 24) | 0x0A0A12);
            }
        }

        // индикатор записи
        if (hasCamera && RecordingManager.hasActiveRecording()) {
            if ((mc.level.getGameTime() / 10) % 2 == 0) {
                g.fill(8, 8, 14, 14, 0xFFFF3333);
            }
            g.drawString(mc.font, Component.translatable("hud.backroomsevolved.rec"), 18, 8, 0xFFFFFF);
        }

        // шкала рассудка
        float sanity = SanityManager.sanity;
        if (sanity > 1) {
            int bw = 80;
            int x = w - bw - 8;
            int y = 8;
            g.fill(x - 1, y - 1, x + bw + 1, y + 7, 0xAA000000);
            int filled = (int) (bw * sanity / 100f);
            int color = sanity > 70 ? 0xFFCC2222 : sanity > 40 ? 0xFFCCAA22 : 0xFF77AA33;
            g.fill(x, y, x + filled, y + 5, color);
            Component label = Component.translatable("hud.backroomsevolved.sanity");
            g.drawString(mc.font, label, x - mc.font.width(label) - 4, y - 1, 0xAAAAAA);
        }

        // глитч-полосы
        if (SanityManager.glitchTimer > 0) {
            int bands = 2 + RANDOM.nextInt(3);
            for (int i = 0; i < bands; i++) {
                int y = RANDOM.nextInt(h);
                int bh = 1 + RANDOM.nextInt(3);
                int col = RANDOM.nextBoolean() ? 0x60FFFFFF : 0x60000000;
                g.fill(0, y, w, y + bh, col);
            }
            if (RANDOM.nextInt(3) == 0) {
                g.fill(RANDOM.nextInt(w / 2), RANDOM.nextInt(h),
                        RANDOM.nextInt(w / 2) + w / 2, RANDOM.nextInt(h / 8) + h / 8 + 2, 0x30FFFFFF);
            }
        }

        // фигура
        if (SanityManager.figureTimer > 0) {
            int fw = (int) (h * 0.55f * (32f / 64f));
            int fh = (int) (h * 0.55f);
            g.setColor(1f, 1f, 1f, SanityManager.figureAlpha);
            g.blit(FIGURE_TEXTURE, (int) SanityManager.figureX - fw / 2, (int) SanityManager.figureY,
                    0, 0, fw, fh, 32, 64);
            g.setColor(1f, 1f, 1f, 1f);
        }
    }

    // ---------------------------------------------------------------- туман
    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        float humidity = SanityManager.humidity;
        float sanity = SanityManager.sanity;
        if (humidity < 0.02f && sanity < 70f) return;
        float far = event.getFarPlane();
        float k = Math.max(humidity * 0.65f, sanity > 70 ? (sanity - 70f) / 30f * 0.35f : 0f);
        event.setFarPlane(far * (1f - k));
        event.setNearPlane(Math.min(event.getNearPlane() + 2f * humidity, far * (1f - k) * 0.5f));
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.RenderFogColor event) {
        float humidity = SanityManager.humidity;
        if (humidity < 0.02f) return;
        float k = humidity * 0.3f;
        event.setRed(event.getRed() * (1f - k) + 0.35f * k);
        event.setGreen(event.getGreen() * (1f - k) + 0.34f * k);
        event.setBlue(event.getBlue() * (1f - k) + 0.24f * k);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float sanity = SanityManager.sanity;
        if (sanity > 70) {
            float amp = (sanity - 70f) / 30f * 1.4f;
            long t = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
            event.setRoll(event.getRoll() + amp * (float) Math.sin(t * 0.05));
        }
    }

    // ---------------------------------------------------------------- эхо
    @SubscribeEvent
    public static void onPlaySound(PlaySoundSourceEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        SoundInstance sound = event.getSound();
        if (EchoSoundInstance.isEcho(sound)) return;
        ResourceLocation loc = sound.getLocation();
        if (!"minecraft".equals(loc.getNamespace())) return;
        String path = loc.getPath();
        if (!path.contains("step") && !path.contains("break")) return;
        if (SanityManager.density < 0.08f) return;

        int delay = Math.max(4, Math.min(36, 3 + RoomSampler.openness / 24));
        var pos = mc.player.position();
        SoundEvent wrapped = SoundEvent.createVariableRangeEvent(loc);
        EchoSoundInstance echo = new EchoSoundInstance(
                wrapped, sound.getSource() != null ? sound.getSource() : SoundSource.NEUTRAL,
                sound.getVolume() * 0.3f, sound.getPitch() * 0.85f,
                pos.x, pos.y, pos.z);
        mc.getSoundManager().playDelayed(echo, delay);
    }
}
