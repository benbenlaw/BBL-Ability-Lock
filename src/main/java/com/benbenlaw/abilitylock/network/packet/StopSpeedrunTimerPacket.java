package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.events.client.SpeedrunTimer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record StopSpeedrunTimerPacket() implements CustomPacketPayload {

    public static final Type<StopSpeedrunTimerPacket> TYPE = new Type<>(AbilityLock.identifier("stop_speedrun_timer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StopSpeedrunTimerPacket> STREAM_CODEC =
            StreamCodec.unit(new StopSpeedrunTimerPacket());

    public static final IPayloadHandler<StopSpeedrunTimerPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            if (!SpeedrunTimer.isRunning()) return;

            SpeedrunTimer.stop();
            ClientPacketDistributor.sendToServer(new SpeedrunFinishedPacket(SpeedrunTimer.getElapsedMillis()));
        });
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}