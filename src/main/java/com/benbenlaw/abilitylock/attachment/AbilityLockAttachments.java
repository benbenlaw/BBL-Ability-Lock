package com.benbenlaw.abilitylock.attachment;

import com.benbenlaw.abilitylock.AbilityLock;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class AbilityLockAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AbilityLock.MOD_ID);

    public static final Supplier<AttachmentType<AbilityLockData>> ABILITY_LOCK =
            ATTACHMENT_TYPES.register("ability_lock", () ->
                    AttachmentType.builder(() -> new AbilityLockData(new HashSet<>()))
                            .serialize(AbilityLockData.CODEC.fieldOf("ability_lock"))
                            .sync(AbilityLockData.STREAM_CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<TaskProgressData>> TASK_PROGRESS =
            ATTACHMENT_TYPES.register("task_progress", () ->
                    AttachmentType.builder(() -> new TaskProgressData(new HashMap<>(), new HashSet<>(), new HashMap<>()))
                            .serialize(TaskProgressData.CODEC.fieldOf("task_progress"))
                            .sync(TaskProgressData.STREAM_CODEC)
                            .copyOnDeath()
                            .build()
            );
}
