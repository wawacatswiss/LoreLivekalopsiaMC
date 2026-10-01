package com.lorelivekalopsia.command;

import com.lorelivekalopsia.config.ModConfig;
import com.lorelivekalopsia.data.PlayerLoreData;
import com.lorelivekalopsia.data.PlayerLoreManager;
import com.lorelivekalopsia.item.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class LoreCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lore")
                .then(CommandManager.literal("toggle")
                        .executes(ctx -> executeToggle(ctx.getSource())))
                .then(CommandManager.literal("on")
                        .executes(ctx -> executeSetMode(ctx.getSource(), true)))
                .then(CommandManager.literal("off")
                        .executes(ctx -> executeSetMode(ctx.getSource(), false)))
                .then(CommandManager.literal("status")
                        .executes(ctx -> executeStatus(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ctx -> executeStatus(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "target")))))
                .then(CommandManager.literal("withdraw")
                        .executes(ctx -> executeWithdraw(ctx.getSource(), 1))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 10))
                                .executes(ctx -> executeWithdraw(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount")))))
        );

        dispatcher.register(CommandManager.literal("withdraw")
                .executes(ctx -> executeWithdraw(ctx.getSource(), 1))
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 10))
                        .executes(ctx -> executeWithdraw(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount"))))
        );
    }

    private static int executeToggle(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can toggle Lore mode."));
            return 0;
        }
        return PlayerLoreManager.toggleLoreMode(player) ? 1 : 0;
    }

    private static int executeSetMode(ServerCommandSource source, boolean enable) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can set Lore mode."));
            return 0;
        }
        return PlayerLoreManager.setLoreMode(player, enable, false) ? 1 : 0;
    }

    private static int executeStatus(ServerCommandSource source, ServerPlayerEntity target) {
        PlayerLoreData data = PlayerLoreManager.getPlayerData(target);
        ModConfig config = ModConfig.get();

        source.sendMessage(Text.literal("--- Lore Status: ").formatted(Formatting.GOLD)
                .append(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW))
                .append(Text.literal(" ---").formatted(Formatting.GOLD)));
        source.sendMessage(Text.literal("Lore Mode: ").formatted(Formatting.GRAY)
                .append(data.isLoreMode() ? Text.literal("Enabled").formatted(Formatting.GREEN) : Text.literal("Disabled").formatted(Formatting.RED)));
        source.sendMessage(Text.literal("Canon Lives: ").formatted(Formatting.GRAY)
                .append(Text.literal(data.getCanonLives() + "/" + config.maxLives).formatted(Formatting.RED)));
        return 1;
    }

    private static int executeWithdraw(ServerCommandSource source, int amount) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can withdraw Canon Lives."));
            return 0;
        }

        PlayerLoreData data = PlayerLoreManager.getPlayerData(player);
        ModConfig config = ModConfig.get();

        if (amount <= 0) {
            player.sendMessage(Text.literal("Amount must be at least 1.").formatted(Formatting.RED), false);
            return 0;
        }

        if (data.getCanonLives() < amount) {
            player.sendMessage(
                    Text.literal("You only have ").formatted(Formatting.RED)
                            .append(Text.literal(String.valueOf(data.getCanonLives())).formatted(Formatting.YELLOW))
                            .append(Text.literal(" Canon Lives.").formatted(Formatting.RED)),
                    false
            );
            player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return 0;
        }

        if ((data.getCanonLives() - amount) < 1) {
            player.sendMessage(Text.literal("You cannot withdraw your last remaining Canon Life.").formatted(Formatting.RED), false);
            player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return 0;
        }

        int newLives = data.getCanonLives() - amount;
        PlayerLoreManager.setCanonLives(player, newLives);

        ItemStack hearts = new ItemStack(ModItems.CANON_HEART, amount);
        player.getInventory().offerOrDrop(hearts);

        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 0.8f);
        player.sendMessage(
                Text.literal("Withdrew ").formatted(Formatting.YELLOW)
                        .append(Text.literal(amount + " Canon Heart(s)").formatted(Formatting.RED))
                        .append(Text.literal(". Remaining Lives: ").formatted(Formatting.YELLOW))
                        .append(Text.literal(newLives + "/" + config.maxLives).formatted(Formatting.RED)),
                false
        );
        return amount;
    }
}
