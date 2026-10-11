package com.ivanitskiye.backroomsevolved.client;

import com.ivanitskiye.backroomsevolved.item.CameraItem;
import com.ivanitskiye.backroomsevolved.registry.ModBlocks;
import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Рассудок (санити). Растёт среди блоков Бекрумса и в темноте,
 * сбрасывается рядом с якорем рассудка или вне Бекрумса.
 * На высоких значениях — шёпот, сердцебиение, фигуры, глитчи, завал камеры.
 */
public class SanityManager {
    public static float sanity = 0f;
    /** 0..1 — насколько "сыро" вокруг (для тумана и запотевания). */
    public static float humidity = 0f;
    /** Доля блоков Бекрумса вокруг игрока. */
    public static float density = 0f;

    public static int glitchTimer = 0;
    public static int figureTimer = 0;
    public static float figureX = 0f;
    public static float figureY = 0f;
    public static float figureAlpha = 0f;

    private static boolean anchorNear = false;
    private static int dampNear = 0;
    private static int tickCount = 0;
    private static final Random RANDOM = new Random();

    public static void tick(LocalPlayer player, Level level, ItemStack camera) {
        tickCount++;
        if (player.tickCount <= 1) reset();

        if (tickCount % 20 == 0) sample(player, level);

        // рост/сброс рассудка
        if (anchorNear) {
            sanity -= 0.15f;
        } else if (density > 0.1f) {
            int light = level.getMaxLocalRawBrightness(player.blockPosition());
            sanity += 0.03f * density + (light < 5 ? 0.012f : 0f);
        } else {
            sanity -= 0.03f;
        }
        sanity = clamp(sanity, 0f, 100f);

        // влажность стремится к цели
        float target = Math.min(1f, dampNear / 6f);
        humidity += (target - humidity) * 0.01f;

        if (glitchTimer > 0) glitchTimer--;
        if (figureTimer > 0) figureTimer--;

        // --- галлюцинации
        if (sanity > 50 && chance(0.002f)) {
            playAt(player, ModSounds.WHISPER.get().getLocation(), 0.7f, 0.9f + RANDOM.nextFloat() * 0.2f);
        }
        if (sanity > 70) {
            if (chance(0.0015f)) {
                playAt(player, ModSounds.HEARTBEAT.get().getLocation(), 0.9f, 1f);
            }
            if (chance(0.0012f)) {
                spawnFigure(player);
            }
        }
        if (sanity > 85 && chance(0.002f)) {
            glitchTimer = Math.max(glitchTimer, 8);
            playAt(player, ModSounds.CAMERA_STATIC.get().getLocation(), 0.25f, 1f);
        }
        // В ИК-режиме иногда видно то, чего не видно глазом
        if (!camera.isEmpty() && CameraItem.isIR(camera) && CameraItem.getBattery(camera) > 0
                && sanity > 40 && chance(0.0008f)) {
            spawnFigure(player);
        }
    }

    public static void spawnFigure(LocalPlayer player) {
        figureTimer = 12 + RANDOM.nextInt(8);
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() : 400;
        int h = mc.getWindow() != null ? mc.getWindow().getGuiScaledHeight() : 300;
        figureX = w * 0.15f + RANDOM.nextFloat() * w * 0.7f;
        figureY = h * 0.2f + RANDOM.nextFloat() * h * 0.4f;
        figureAlpha = 0.2f + RANDOM.nextFloat() * 0.3f;
    }

    private static void sample(LocalPlayer player, Level level) {
        int backrooms = 0;
        int total = 0;
        dampNear = 0;
        anchorNear = false;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -3, -4), center.offset(4, 4, 4))) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            total++;
            if (ModBlocks.isBackroomsBlock(block)) backrooms++;
            if (block == ModBlocks.DAMP_CARPET.get()) dampNear++;
            if (block == ModBlocks.SANITY_ANCHOR.get()) anchorNear = true;
        }
        density = (float) backrooms / Math.max(1, total);
    }

    private static void playAt(LocalPlayer player, net.minecraft.resources.ResourceLocation sound,
                               float volume, float pitch) {
        double dx = RANDOM.nextGaussian() * 4;
        double dy = RANDOM.nextGaussian() * 1.5;
        double dz = RANDOM.nextGaussian() * 4;
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                sound, SoundSource.AMBIENT, volume, pitch, false, 0,
                SoundInstance.Attenuation.LINEAR,
                player.getX() + dx, player.getY() + dy, player.getZ() + dz, false));
    }

    public static void reset() {
        sanity = 0;
        humidity = 0;
        glitchTimer = 0;
        figureTimer = 0;
    }

    private static boolean chance(float p) {
        return RANDOM.nextFloat() < p;
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }
}
