package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncTaskProgressPacket(TaskProgressData data) implements CustomPacketPayload {

    public static final Type<SyncTaskProgressPacket> TYPE = new Type<>(AbilityLock.identifier("sync_task_progress"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTaskProgressPacket> STREAM_CODEC =
        StreamCodec.composite(
            TaskProgressData.STREAM_CODEC,
            SyncTaskProgressPacket::data,
            SyncTaskProgressPacket::new
        );

    public static final IPayloadHandler<SyncTaskProgressPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.setData(AbilityLockAttachments.TASK_PROGRESS, packet.data());
            }
        });
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}