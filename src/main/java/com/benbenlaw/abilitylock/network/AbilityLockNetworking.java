package com.benbenlaw.abilitylock.network;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.network.packet.AbilityUnlockToastPacket;
import com.benbenlaw.abilitylock.network.packet.SpeedrunFinishedPacket;
import com.benbenlaw.abilitylock.network.packet.StopSpeedrunTimerPacket;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AbilityLockNetworking {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AbilityLock.MOD_ID);

        //To Client From Server
        registrar.playToClient(SyncAbilityLockPacket.TYPE, SyncAbilityLockPacket.STREAM_CODEC, SyncAbilityLockPacket.HANDLER);
        registrar.playToClient(SyncTaskProgressPacket.TYPE, SyncTaskProgressPacket.STREAM_CODEC, SyncTaskProgressPacket.HANDLER);
        registrar.playToClient(StopSpeedrunTimerPacket.TYPE, StopSpeedrunTimerPacket.STREAM_CODEC, StopSpeedrunTimerPacket.HANDLER);
        registrar.playToClient(AbilityUnlockToastPacket.TYPE, AbilityUnlockToastPacket.STREAM_CODEC, AbilityUnlockToastPacket.HANDLER);

        //To Server From Client
        registrar.playToServer(SpeedrunFinishedPacket.TYPE, SpeedrunFinishedPacket.STREAM_CODEC, SpeedrunFinishedPacket.HANDLER);
    }
}