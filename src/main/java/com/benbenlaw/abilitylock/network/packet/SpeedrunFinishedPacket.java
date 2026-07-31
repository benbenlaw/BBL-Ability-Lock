package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.util.SpeedrunManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SpeedrunFinishedPacket(long elapsedMillis) implements CustomPacketPayload {

    public static final Type<SpeedrunFinishedPacket> TYPE = new Type<>(AbilityLock.identifier("speedrun_finished"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeedrunFinishedPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            SpeedrunFinishedPacket::elapsedMillis,
            SpeedrunFinishedPacket::new
        );

    public static final IPayloadHandler<SpeedrunFinishedPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SpeedrunManager.onTimeReceived(serverPlayer, packet.elapsedMillis());
            }
        });
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}