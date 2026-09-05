package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.task.TaskManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SpendBonusPointPacket(Identifier abilityId) implements CustomPacketPayload {

    public static final Type<SpendBonusPointPacket> TYPE = new Type<>(AbilityLock.identifier("spend_bonus_point"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpendBonusPointPacket> STREAM_CODEC =
        StreamCodec.composite(
            Identifier.STREAM_CODEC, SpendBonusPointPacket::abilityId,
            SpendBonusPointPacket::new
        );

    public static final IPayloadHandler<SpendBonusPointPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TaskManager.spendBonusPoint(player, packet.abilityId());
            }
        });
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}