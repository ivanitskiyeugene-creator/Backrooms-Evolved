package com.ivanitskiye.backroomsevolved.item;

import com.ivanitskiye.backroomsevolved.client.ClientSetup;
import com.ivanitskiye.backroomsevolved.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;

/**
 * Камера Бекрумса: батарея, ИК-режим (ночное видение), запись кадров.
 * <ul>
 *   <li>ПКМ — переключить режим (обычный / ИК);</li>
 *   <li>ПКМ + Shift — открыть просмотр записи;</li>
 *   <li>клавиша записи (R) — начать/остановить запись;</li>
 *   <li>батарея тратится, зарядка — в верстаке батареей.</li>
 * </ul>
 */
public class CameraItem extends Item {
    public static final String TAG_BATTERY = "battery";
    public static final String TAG_IR = "ir";
    public static final String TAG_REC = "recording";
    public static final String TAG_REC_ID = "rec_id";
    public static final String TAG_REC_FRAMES = "rec_frames";

    public static final int MAX_BATTERY = 100;
    public static final int DEFAULT_BATTERY = 60;

    public CameraItem(Properties props) {
        super(props);
    }

    public static int getBattery(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_BATTERY) ? tag.getInt(TAG_BATTERY) : DEFAULT_BATTERY;
    }

    public static void setBattery(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(TAG_BATTERY, clamp(value, 0, MAX_BATTERY));
    }

    public static boolean isIR(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(TAG_IR);
    }

    public static boolean isRecording(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(TAG_REC);
    }

    public static int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (player.isShiftKeyDown() && stack.getOrCreateTag().getInt(TAG_REC_FRAMES) > 0
                    && FMLEnvironment.dist == Dist.CLIENT) {
                ClientSetup.openPlayback(stack);
            }
        } else {
            if (!player.isShiftKeyDown() && getBattery(stack) > 0) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.putBoolean(TAG_IR, !tag.getBoolean(TAG_IR));
                level.playSound(null, player.blockPosition(), ModSounds.CAMERA_CLICK.get(),
                        SoundSource.PLAYERS, 0.5f, tag.getBoolean(TAG_IR) ? 1.4f : 1f);
                player.getInventory().setChanged();
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!selected || !(entity instanceof Player player)) return;
        if (level.getGameTime() % 20 != 0) return;

        int battery = getBattery(stack);
        if (battery <= 0) {
            if (!level.isClientSide) player.removeEffect(MobEffects.NIGHT_VISION);
            return;
        }

        boolean ir = isIR(stack);
        boolean rec = isRecording(stack);
        setBattery(stack, battery - (1 + (ir ? 1 : 0) + (rec ? 1 : 0)));

        if (!level.isClientSide) {
            if (ir) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, false, false, false));
            }
            if (getBattery(stack) <= 0) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            player.getInventory().setChanged();
        } else if (getBattery(stack) <= 0) {
            player.playSound(ModSounds.CAMERA_POWER_DOWN.get(), 0.8f, 1f);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int battery = getBattery(stack);
        ChatFormatting fmt = battery > 50 ? ChatFormatting.GREEN : battery > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED;
        tooltip.add(Component.translatable("tooltip.backroomsevolved.battery", battery).withStyle(fmt));
        tooltip.add(Component.translatable("tooltip.backroomsevolved.mode",
                Component.translatable(isIR(stack) ? "tooltip.backroomsevolved.mode.ir"
                        : "tooltip.backroomsevolved.mode.normal")).withStyle(ChatFormatting.GRAY));
        int frames = stack.getOrCreateTag().getInt(TAG_REC_FRAMES);
        if (frames > 0) {
            tooltip.add(Component.translatable("tooltip.backroomsevolved.recording", frames).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.backroomsevolved.rec_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            tooltip.add(Component.translatable("tooltip.backroomsevolved.key_hint",
                    com.ivanitskiye.backroomsevolved.client.ClientSetup.recordKeyName())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isRecording(stack);
    }
}
