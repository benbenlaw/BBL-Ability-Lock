package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.client.AbilityUnlockToastManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record BonusPointEarnedPacket(int totalPoints) implements CustomPacketPayload {

    public static final Type<BonusPointEarnedPacket> TYPE = new Type<>(AbilityLock.identifier("bonus_point_earned"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BonusPointEarnedPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, BonusPointEarnedPacket::totalPoints,
            BonusPointEarnedPacket::new
        );

    public static final IPayloadHandler<BonusPointEarnedPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> AbilityUnlockToastManager.showBonusPoint(packet.totalPoints()));
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}