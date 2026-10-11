package com.ivanitskiye.backroomsevolved.network;

import com.ivanitskiye.backroomsevolved.item.CameraItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент просит сервер переключить запись камеры (клавиша R). */
public class CameraActionPacket {
    public static final byte ACTION_TOGGLE_RECORD = 0;

    private final byte action;

    public CameraActionPacket(byte action) {
        this.action = action;
    }

    public static void encode(CameraActionPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.action);
    }

    public static CameraActionPacket decode(FriendlyByteBuf buf) {
        return new CameraActionPacket(buf.readByte());
    }

    public static void handle(CameraActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (msg.action != ACTION_TOGGLE_RECORD) return;
            var player = context.getSender();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof CameraItem)) return;
            if (CameraItem.getBattery(stack) <= 0) return;
            var tag = stack.getOrCreateTag();
            boolean now = !tag.getBoolean(CameraItem.TAG_REC);
            tag.putBoolean(CameraItem.TAG_REC, now);
            player.getInventory().setChanged();
            player.level().playSound(null, player.blockPosition(),
                    com.ivanitskiye.backroomsevolved.registry.ModSounds.CAMERA_REC_BEEP.get(),
                    SoundSource.PLAYERS, 0.6f, now ? 1.2f : 0.8f);
        });
        context.setPacketHandled(true);
    }
}
