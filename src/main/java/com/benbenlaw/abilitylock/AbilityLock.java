package com.benbenlaw.abilitylock;

import com.benbenlaw.abilitylock.ability.AbilityTypes;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.config.ClientConfig;
import com.benbenlaw.abilitylock.config.ServerConfig;
import com.benbenlaw.abilitylock.network.AbilityLockNetworking;
import com.benbenlaw.abilitylock.task.TaskTypes;
import com.benbenlaw.abilitylock.task.Tasks;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AbilityLock.MOD_ID)
public class AbilityLock {
    public static final String MOD_ID = "abilitylock";
    public static final Logger LOGGER = LogManager.getLogger();

    public AbilityLock(final IEventBus eventBus, final ModContainer modContainer) {

        AbilityTypes.init();
        TaskTypes.init();


        AbilityLockAttachments.ATTACHMENT_TYPES.register(eventBus);

        Tasks.init();
        eventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "bbl/abilitylock/client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "bbl/abilitylock/server.toml");
    }


    public void commonSetup(RegisterPayloadHandlersEvent event) {
        AbilityLockNetworking.registerNetworking(event);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }


}

