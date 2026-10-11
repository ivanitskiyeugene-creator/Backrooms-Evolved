package com.ivanitskiye.backroomsevolved.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Экранчик камеры: отмотать и посмотреть, что было.
 * Кадры читаются с диска из {@code backrooms_recordings/<id>/}.
 */
public class CameraPlaybackScreen extends Screen {
    private final String id;
    private final List<ResourceLocation> textures = new ArrayList<>();
    private final List<int[]> sizes = new ArrayList<>();
    private int index = 0;
    private boolean playing = false;
    private int playTick = 0;

    private static final int SLIDER_W = 200;

    protected CameraPlaybackScreen(String id, int frameCount) {
        super(Component.translatable("gui.backroomsevolved.playback"));
        this.id = id;
    }

    @Override
    protected void init() {
        super.init();
        loadFrames();
        int cy = height - 24;
        addRenderableWidget(Button.builder(Component.translatable("gui.backroomsevolved.prev"), b -> step(-1))
                .bounds(width / 2 - 110, cy, 30, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.backroomsevolved.play"), b -> togglePlay())
                .bounds(width / 2 - 70, cy, 40, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.backroomsevolved.next"), b -> step(1))
                .bounds(width / 2 - 20, cy, 30, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(width / 2 + 30, cy, 80, 20).build());
    }

    private void togglePlay() {
        playing = !playing;
    }

    private void step(int d) {
        if (!textures.isEmpty()) {
            index = (index + d + textures.size()) % textures.size();
        }
    }

    private void loadFrames() {
        Minecraft mc = Minecraft.getInstance();
        File dir = new File(mc.gameDirectory, RecordingManager.recordingsRoot() + File.separator + id);
        File[] files = dir.listFiles((d, n) -> n.endsWith(".png"));
        if (files == null) return;
        Arrays.sort(files);
        int i = 0;
        for (File f : files) {
            try (FileInputStream in = new FileInputStream(f)) {
                NativeImage img = NativeImage.read(in);
                DynamicTexture dt = new DynamicTexture(img);
                ResourceLocation rl = new ResourceLocation("backroomsevolved", "playback/frame_" + i);
                mc.getTextureManager().register(rl, dt);
                textures.add(rl);
                sizes.add(new int[]{img.getWidth(), img.getHeight()});
                i++;
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (playing && !textures.isEmpty()) {
            if (++playTick % 8 == 0) {
                index = (index + 1) % textures.size();
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        if (textures.isEmpty()) {
            g.drawCenteredString(font, "—", width / 2, height / 2, 0x888888);
            return;
        }

        int[] size = sizes.get(index);
        int texW = size[0];
        int texH = size[1];

        int availW = width - 60;
        int availH = height - 110;
        float scale = Math.min(availW / (float) texW, availH / (float) texH);
        int dw = (int) (texW * scale);
        int dh = (int) (texH * scale);
        int dx = (width - dw) / 2;
        int dy = 20;

        g.fill(dx - 2, dy - 2, dx + dw + 2, dy + dh + 2, 0xFF111111);
        g.blit(textures.get(index), dx, dy, 0, 0, dw, dh, texW, texH);

        // REC-уголок как у настоящей кассеты
        g.drawString(font, "PLAY ▶", dx + 4, dy + 4, 0xCCFFFFFF);

        // слайдер
        int sx = width / 2 - SLIDER_W / 2;
        int sy = height - 56;
        g.fill(sx, sy, sx + SLIDER_W, sy + 4, 0xFF555555);
        float f = textures.size() > 1 ? index / (float) (textures.size() - 1) : 0f;
        g.fill(sx + (int) (f * SLIDER_W) - 1, sy - 3, sx + (int) (f * SLIDER_W) + 2, sy + 7, 0xFFDDDDDD);

        g.drawCenteredString(font,
                Component.translatable("gui.backroomsevolved.frame", index + 1, textures.size()),
                width / 2, sy + 10, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sx = width / 2 - SLIDER_W / 2;
        int sy = height - 56;
        if (mouseX >= sx - 4 && mouseX <= sx + SLIDER_W + 4 && mouseY >= sy - 6 && mouseY <= sy + 10
                && textures.size() > 1) {
            setFromSlider(mouseX, sx);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        int sx = width / 2 - SLIDER_W / 2;
        int sy = height - 56;
        if (mouseY >= sy - 10 && mouseY <= sy + 14 && textures.size() > 1) {
            setFromSlider(mouseX, sx);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    private void setFromSlider(double mouseX, int sx) {
        float f = (float) ((mouseX - sx) / SLIDER_W);
        f = Math.max(0, Math.min(1, f));
        index = Math.round(f * (textures.size() - 1));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            step(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            step(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            togglePlay();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft mc = Minecraft.getInstance();
        for (ResourceLocation rl : textures) {
            mc.getTextureManager().release(rl);
        }
        textures.clear();
        sizes.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
