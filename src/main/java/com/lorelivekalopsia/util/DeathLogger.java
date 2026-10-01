package com.lorelivekalopsia.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lorelivekalopsia.LoreLivekalopsia;
import com.lorelivekalopsia.config.ModConfig;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class DeathLogger {
    private static final Gson GSON = new Gson();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private DeathLogger() {}

    public static void logCanonDeath(ServerPlayerEntity player, DamageSource source, int remaining, int maxLives) {
        String name = player.getName().getString();
        String cause = source.getDeathMessage(player).getString();
        BlockPos pos = player.getBlockPos();
        String where = player.getWorld().getRegistryKey().getValue() + " (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";

        String plain = "[Canon Death] " + cause + " | lives " + remaining + "/" + maxLives + " | " + where;
        String discord = "💀 **" + escape(name) + "** lost a Canon Life\n"
                + escape(cause) + "\n"
                + "Lives: " + remaining + "/" + maxLives + " • " + where
                + (remaining <= 0 ? "\n⚰️ Entered Limbo" : "");
        emit(plain, discord);
    }

    public static void logRevive(String name, String by) {
        emit("[Revive] " + name + " revived by " + by,
                "✨ **" + escape(name) + "** was revived from Limbo by " + escape(by));
    }

    private static void emit(String plain, String discord) {
        ModConfig config = ModConfig.get();
        if (!config.logDeaths) {
            return;
        }
        LoreLivekalopsia.LOGGER.info(plain);

        String url = config.deathWebhookUrl;
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", "Canon Log");
            body.addProperty("content", discord.length() > 2000 ? discord.substring(0, 2000) : discord);
            JsonObject mentions = new JsonObject();
            mentions.add("parse", new com.google.gson.JsonArray());
            body.add("allowed_mentions", mentions);

            HttpRequest request = HttpRequest.newBuilder(URI.create(url.trim()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                    .build();
            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding()).whenComplete((response, error) -> {
                if (error != null) {
                    LoreLivekalopsia.LOGGER.warn("Death webhook failed: {}", error.toString());
                } else if (response.statusCode() >= 300) {
                    LoreLivekalopsia.LOGGER.warn("Death webhook returned HTTP {}", response.statusCode());
                }
            });
        } catch (IllegalArgumentException e) {
            LoreLivekalopsia.LOGGER.warn("deathWebhookUrl in the config is not a valid URL; skipping webhook.");
        }
    }

    private static String escape(String s) {
        return s.replaceAll("([\\\\*_~`|>])", "\\\\$1");
    }
}
