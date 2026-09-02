package com.lorelivekalopsia.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int maxLives = 3;
    public int defaultLives = 3;
    public boolean sendToLimboOnZeroLives = true;
    public boolean loseLifeOnAnyDeathInLore = true;
    public boolean allowSuicideWithdrawal = false;

    public boolean hasLimboSet = false;
    public String limboDimension = "minecraft:overworld";
    public double limboX = 0.0;
    public double limboY = 100.0;
    public double limboZ = 0.0;
    public float limboYaw = 0.0f;
    public float limboPitch = 0.0f;

    private static ModConfig INSTANCE;

    public static File getConfigFile() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            if (configDir != null) {
                return configDir.resolve("lorelivekalopsia.json").toFile();
            }
        } catch (Throwable ignored) {
        }
        return new File("config/lorelivekalopsia.json");
    }

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void setInstance(ModConfig config) {
        INSTANCE = config;
    }

    public static void load() {
        File file = getConfigFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
        }
        File file = getConfigFile();
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
