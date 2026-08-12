package com.benbenlaw.abilitylock;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityRegistry;
import com.benbenlaw.abilitylock.ability.RestrictionsInit;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.config.ClientConfig;
import com.benbenlaw.abilitylock.config.ServerConfig;
import com.benbenlaw.abilitylock.network.AbilityLockNetworking;
import com.benbenlaw.abilitylock.task.TaskRegistry;
import com.benbenlaw.abilitylock.task.Tasks;
import com.mojang.logging.LogUtils;
import mezz.jei.api.JeiPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AbilityLock.MOD_ID)
public class AbilityLock {
    public static final String MOD_ID = "abilitylock";
    public static final Logger LOGGER = LogManager.getLogger();

    public AbilityLock(final IEventBus eventBus, final ModContainer modContainer) {
        AbilityLockAttachments.ATTACHMENT_TYPES.register(eventBus);
        Abilities.init();
        RestrictionsInit.init();
        AbilityRegistry.validate();
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

