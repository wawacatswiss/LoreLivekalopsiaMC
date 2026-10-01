package com.lorelivekalopsia;

import com.lorelivekalopsia.command.LoreAdminCommand;
import com.lorelivekalopsia.command.LoreCommand;
import com.lorelivekalopsia.config.ModConfig;
import com.lorelivekalopsia.data.PlayerLoreManager;
import com.lorelivekalopsia.item.ModItems;
import com.lorelivekalopsia.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoreLivekalopsia implements ModInitializer {
    public static final String MOD_ID = "lorelivekalopsia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        ModItems.registerModItems();
        ModNetworking.registerServerReceivers();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LoreCommand.register(dispatcher);
            LoreAdminCommand.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            PlayerLoreManager.applyPendingRevive(player);
            PlayerLoreManager.handlePlayerRespawn(player);
            PlayerLoreManager.updatePlayerListName(player);
            ModNetworking.syncToClient(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerLoreManager.handlePlayerRespawn(newPlayer);
            PlayerLoreManager.updatePlayerListName(newPlayer);
            ModNetworking.syncToClient(newPlayer);
        });
    }
}
