package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncAbilityLockPacket(AbilityLockData data) implements CustomPacketPayload {

    public static final Type<SyncAbilityLockPacket> TYPE = new Type<>(AbilityLock.identifier("sync_ability_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAbilityLockPacket> STREAM_CODEC =
        StreamCodec.composite(
            AbilityLockData.STREAM_CODEC,
            SyncAbilityLockPacket::data,
            SyncAbilityLockPacket::new
        );

    public static final IPayloadHandler<SyncAbilityLockPacket> HANDLER = (packet, context) -> {

        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.setData(AbilityLockAttachments.ABILITY_LOCK, packet.data());
            }
        });
    };


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
    }
}