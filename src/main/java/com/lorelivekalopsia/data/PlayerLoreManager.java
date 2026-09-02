package com.lorelivekalopsia.data;

import com.lorelivekalopsia.config.ModConfig;
import com.lorelivekalopsia.network.ModNetworking;
import com.lorelivekalopsia.util.LoreDisplay;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.UUID;

public final class PlayerLoreManager {
    private PlayerLoreManager() {}

    public static PlayerLoreData getPlayerData(ServerPlayerEntity player) {
        return LoreState.getServerState(player.getServer()).getPlayerData(player.getUuid());
    }

    public static PlayerLoreData getPlayerData(MinecraftServer server, UUID uuid) {
        return LoreState.getServerState(server).getPlayerData(uuid);
    }

    public static boolean toggleLoreMode(ServerPlayerEntity player) {
        PlayerLoreData data = getPlayerData(player);
        return setLoreMode(player, !data.isLoreMode(), false);
    }

    public static boolean setLoreMode(ServerPlayerEntity player, boolean targetMode, boolean bypassChecks) {
        PlayerLoreData data = getPlayerData(player);
        ModConfig config = ModConfig.get();

        if (targetMode && data.getCanonLives() <= 0) {
            player.sendMessage(Text.literal("You have 0 Canon Lives and are in Limbo. You cannot enter Lore Mode.").formatted(Formatting.RED), false);
            player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return false;
        }

        data.setLoreMode(targetMode);
        LoreState.getServerState(player.getServer()).markDirty();
        updatePlayerListName(player);
        ModNetworking.syncToClient(player);

        player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.8f, 1.0f);

        if (targetMode) {
            player.sendMessage(
                    Text.literal("Entered Lore Mode. ").formatted(Formatting.GREEN)
                            .append(Text.literal("(Lives: ").formatted(Formatting.GRAY))
                            .append(Text.literal(data.getCanonLives() + "/" + config.maxLives).formatted(Formatting.RED))
                            .append(Text.literal(")").formatted(Formatting.GRAY)),
                    false
            );
        } else {
            player.sendMessage(Text.literal("Left Lore Mode.").formatted(Formatting.GRAY), false);
        }
        return true;
    }

    public static void setCanonLives(ServerPlayerEntity player, int lives) {
        PlayerLoreData data = getPlayerData(player);
        int max = ModConfig.get().maxLives;
        data.setCanonLives(Math.min(max, Math.max(0, lives)));
        LoreState.getServerState(player.getServer()).markDirty();
        updatePlayerListName(player);
        ModNetworking.syncToClient(player);
    }

    public static boolean addCanonLives(ServerPlayerEntity player, int amount) {
        PlayerLoreData data = getPlayerData(player);
        int max = ModConfig.get().maxLives;
        if (data.getCanonLives() >= max) {
            return false;
        }
        data.setCanonLives(Math.min(max, data.getCanonLives() + amount));
        LoreState.getServerState(player.getServer()).markDirty();
        updatePlayerListName(player);
        ModNetworking.syncToClient(player);
        return true;
    }

    public static void handlePlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
        PlayerLoreData data = getPlayerData(player);
        ModConfig config = ModConfig.get();
        MinecraftServer server = player.getServer();

        if (data.getCanonLives() <= 0) {
            if (server != null) {
                server.getPlayerManager().broadcast(
                        Text.literal(player.getName().getString() + " somehow died in Limbo").formatted(Formatting.RED),
                        false
                );
            }
            return;
        }

        if (!data.isLoreMode()) {
            return;
        }

        int remaining = Math.max(0, data.getCanonLives() - 1);
        data.setCanonLives(remaining);

        if (remaining <= 0) {
            data.setLoreMode(false);
        }

        LoreState.getServerState(player.getServer()).markDirty();
        updatePlayerListName(player);
        ModNetworking.syncToClient(player);

        if (server != null) {
            if (remaining > 0) {
                server.getPlayerManager().broadcast(
                        Text.literal(player.getName().getString() + " lost a Canon Life! Remaining: " + remaining + "/" + config.maxLives).formatted(Formatting.RED),
                        false
                );
                player.playSound(SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.0f, 0.8f);
            } else {
                server.getPlayerManager().broadcast(
                        Text.literal(player.getName().getString() + " has entered Limbo").formatted(Formatting.RED),
                        false
                );
            }
        }
    }

    public static void handlePlayerRespawn(ServerPlayerEntity player) {
        PlayerLoreData data = getPlayerData(player);
        ModConfig config = ModConfig.get();

        if (data.getCanonLives() <= 0 && config.sendToLimboOnZeroLives) {
            teleportToLimbo(player);
            player.sendMessage(Text.literal("You are in Limbo in Adventure Mode until revived by an Admin.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("LIMBO").formatted(Formatting.DARK_RED, Formatting.BOLD)));
        }
    }

    public static void teleportToLimbo(ServerPlayerEntity player) {
        ModConfig config = ModConfig.get();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        player.changeGameMode(GameMode.ADVENTURE);

        if (config.hasLimboSet) {
            Identifier dimId = Identifier.tryParse(config.limboDimension);
            RegistryKey<World> dimKey = dimId != null ? RegistryKey.of(RegistryKeys.WORLD, dimId) : World.OVERWORLD;
            ServerWorld targetWorld = server.getWorld(dimKey);
            if (targetWorld == null) {
                targetWorld = server.getOverworld();
            }
            player.teleport(targetWorld, config.limboX, config.limboY, config.limboZ, config.limboYaw, config.limboPitch);
        }
    }

    public static void updatePlayerListName(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
        }
    }

    public static Text getFormattedTabName(ServerPlayerEntity player) {
        PlayerLoreData data = getPlayerData(player);
        if (!data.isLoreMode()) {
            return player.getName().copy();
        }

        return player.getName().copy()
                .append(" ")
                .append(LoreDisplay.getHeartsText(data.getCanonLives(), ModConfig.get().maxLives));
    }
}
