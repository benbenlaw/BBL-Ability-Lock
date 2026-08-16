package com.benbenlaw.abilitylock.network.packet;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.client.AbilityUnlockToastManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record AbilityUnlockToastPacket(Identifier abilityId, boolean bonus) implements CustomPacketPayload {

    public static final Type<AbilityUnlockToastPacket> TYPE = new Type<>(AbilityLock.identifier("ability_unlock_toast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityUnlockToastPacket> STREAM_CODEC =
        StreamCodec.composite(
            Identifier.STREAM_CODEC, AbilityUnlockToastPacket::abilityId,
            ByteBufCodecs.BOOL, AbilityUnlockToastPacket::bonus,
            AbilityUnlockToastPacket::new
        );

    public static final IPayloadHandler<AbilityUnlockToastPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> AbilityUnlockToastManager.show(packet.abilityId(), packet.bonus()));
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}