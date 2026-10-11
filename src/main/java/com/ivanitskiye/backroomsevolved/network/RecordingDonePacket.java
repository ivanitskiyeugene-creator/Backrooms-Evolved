package com.ivanitskiye.backroomsevolved.network;

import com.ivanitskiye.backroomsevolved.item.CameraItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент сохранил запись на диск и сообщает серверу id + количество кадров. */
public class RecordingDonePacket {
    private final String id;
    private final int frames;

    public RecordingDonePacket(String id, int frames) {
        this.id = id;
        this.frames = frames;
    }

    public static void encode(RecordingDonePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.id, 64);
        buf.writeInt(msg.frames);
    }

    public static RecordingDonePacket decode(FriendlyByteBuf buf) {
        return new RecordingDonePacket(buf.readUtf(64), buf.readInt());
    }

    public static void handle(RecordingDonePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof CameraItem)) return;
            var tag = stack.getOrCreateTag();
            tag.putString(CameraItem.TAG_REC_ID, msg.id);
            tag.putInt(CameraItem.TAG_REC_FRAMES, msg.frames);
            player.getInventory().setChanged();
        });
        context.setPacketHandled(true);
    }
}
