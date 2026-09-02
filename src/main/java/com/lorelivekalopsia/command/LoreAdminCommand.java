package com.lorelivekalopsia.command;

import com.lorelivekalopsia.config.ModConfig;
import com.lorelivekalopsia.data.PlayerLoreData;
import com.lorelivekalopsia.data.PlayerLoreManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.Collection;

public class LoreAdminCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("loreadmin")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("lives")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 100))
                                                .executes(ctx -> executeSetLives(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount")))))
                                .then(CommandManager.literal("add")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> executeAddLife(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount")))))
                                .then(CommandManager.literal("take")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> executeTakeLife(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount")))))))
                .then(CommandManager.literal("revive")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(ctx -> executeRevive(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets")))))
                .then(CommandManager.literal("limbo")
                        .then(CommandManager.literal("set")
                                .executes(ctx -> executeSetLimboHere(ctx.getSource()))
                                .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                        .executes(ctx -> executeSetLimboPos(ctx.getSource(), Vec3ArgumentType.getVec3(ctx, "pos")))))
                        .then(CommandManager.literal("send")
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(ctx -> executeSendToLimbo(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"))))))
                .then(CommandManager.literal("mode")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> executeSetMode(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), BoolArgumentType.getBool(ctx, "enabled"))))))
                .then(CommandManager.literal("reset")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(ctx -> executeReset(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets")))))
                .then(CommandManager.literal("reload")
                        .executes(ctx -> executeReload(ctx.getSource())))
        );
    }

    private static int executeSetLives(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int lives) {
        for (ServerPlayerEntity player : targets) {
            PlayerLoreManager.setCanonLives(player, lives);
            player.sendMessage(
                    Text.literal("An admin set your Canon Lives to ").formatted(Formatting.YELLOW)
                            .append(Text.literal(String.valueOf(lives)).formatted(Formatting.RED))
                            .append(Text.literal(".").formatted(Formatting.YELLOW)),
                    false
            );
        }
        source.sendMessage(Text.literal("Updated Canon Lives for " + targets.size() + " player(s).").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeAddLife(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        for (ServerPlayerEntity player : targets) {
            PlayerLoreManager.addCanonLives(player, amount);
            player.sendMessage(
                    Text.literal("An admin granted you ").formatted(Formatting.GREEN)
                            .append(Text.literal("+" + amount + " Canon Life(s)").formatted(Formatting.RED))
                            .append(Text.literal("!").formatted(Formatting.GREEN)),
                    false
            );
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        source.sendMessage(Text.literal("Added " + amount + " life(s) to " + targets.size() + " player(s).").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeTakeLife(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        for (ServerPlayerEntity player : targets) {
            PlayerLoreData data = PlayerLoreManager.getPlayerData(player);
            int newLives = Math.max(0, data.getCanonLives() - amount);
            PlayerLoreManager.setCanonLives(player, newLives);
            player.sendMessage(
                    Text.literal("An admin removed ").formatted(Formatting.RED)
                            .append(Text.literal(amount + " Canon Life(s)").formatted(Formatting.YELLOW))
                            .append(Text.literal(" from you.").formatted(Formatting.RED)),
                    false
            );
        }
        source.sendMessage(Text.literal("Removed " + amount + " life(s) from " + targets.size() + " player(s).").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeRevive(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity player : targets) {
            PlayerLoreData data = PlayerLoreManager.getPlayerData(player);
            if (data.getCanonLives() <= 0) {
                PlayerLoreManager.setCanonLives(player, 1);
            }
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR || player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                player.changeGameMode(GameMode.SURVIVAL);
            }

            BlockPos spawnPos = player.getServer().getOverworld().getSpawnPos();
            player.teleport(player.getServer().getOverworld(), spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

            PlayerLoreManager.setLoreMode(player, false, true);
            player.sendMessage(
                    Text.literal("You have been revived from Limbo with ").formatted(Formatting.GREEN)
                            .append(Text.literal(data.getCanonLives() + " Canon Life").formatted(Formatting.YELLOW))
                            .append(Text.literal(".").formatted(Formatting.GREEN)),
                    false
            );
            player.playSound(SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        source.sendMessage(Text.literal("Revived " + targets.size() + " player(s) and returned them to Survival.").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeSetLimboHere(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Must be run by a player to use current location."));
            return 0;
        }
        ModConfig config = ModConfig.get();
        config.hasLimboSet = true;
        config.limboDimension = player.getWorld().getRegistryKey().getValue().toString();
        config.limboX = player.getX();
        config.limboY = player.getY();
        config.limboZ = player.getZ();
        config.limboYaw = player.getYaw();
        config.limboPitch = player.getPitch();
        ModConfig.save();

        source.sendMessage(Text.literal(String.format("Limbo location set to current pos: (%.1f, %.1f, %.1f) in %s",
                config.limboX, config.limboY, config.limboZ, config.limboDimension)).formatted(Formatting.GREEN));
        return 1;
    }

    private static int executeSetLimboPos(ServerCommandSource source, Vec3d pos) {
        ModConfig config = ModConfig.get();
        config.hasLimboSet = true;
        config.limboDimension = source.getWorld().getRegistryKey().getValue().toString();
        config.limboX = pos.x;
        config.limboY = pos.y;
        config.limboZ = pos.z;
        config.limboYaw = 0.0f;
        config.limboPitch = 0.0f;
        ModConfig.save();

        source.sendMessage(Text.literal(String.format("Limbo location set to: (%.1f, %.1f, %.1f) in %s",
                config.limboX, config.limboY, config.limboZ, config.limboDimension)).formatted(Formatting.GREEN));
        return 1;
    }

    private static int executeSendToLimbo(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity player : targets) {
            PlayerLoreManager.teleportToLimbo(player);
            player.sendMessage(Text.literal("You have entered Limbo in Adventure Mode.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
        }
        source.sendMessage(Text.literal("Sent " + targets.size() + " player(s) to Limbo.").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeSetMode(ServerCommandSource source, Collection<ServerPlayerEntity> targets, boolean enabled) {
        for (ServerPlayerEntity player : targets) {
            PlayerLoreManager.setLoreMode(player, enabled, true);
        }
        source.sendMessage(Text.literal("Updated Lore Mode for " + targets.size() + " player(s).").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeReset(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        ModConfig config = ModConfig.get();
        for (ServerPlayerEntity player : targets) {
            PlayerLoreManager.setCanonLives(player, config.defaultLives);
            PlayerLoreManager.setLoreMode(player, false, true);
            if (player.interactionManager.getGameMode() == GameMode.SPECTATOR || player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                player.changeGameMode(GameMode.SURVIVAL);
            }
            player.sendMessage(Text.literal("Your lore state and lives have been reset to defaults.").formatted(Formatting.YELLOW), false);
        }
        source.sendMessage(Text.literal("Reset lore data for " + targets.size() + " player(s).").formatted(Formatting.GREEN));
        return targets.size();
    }

    private static int executeReload(ServerCommandSource source) {
        ModConfig.load();
        if (source.getServer() != null) {
            for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
                PlayerLoreManager.updatePlayerListName(player);
                com.lorelivekalopsia.network.ModNetworking.syncToClient(player);
            }
        }
        source.sendMessage(Text.literal("Config reloaded successfully.").formatted(Formatting.GREEN));
        return 1;
    }
}
