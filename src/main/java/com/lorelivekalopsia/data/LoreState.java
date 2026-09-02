package com.lorelivekalopsia.data;

import com.lorelivekalopsia.LoreLivekalopsia;
import com.lorelivekalopsia.config.ModConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoreState extends PersistentState {
    private static final String STATE_KEY = LoreLivekalopsia.MOD_ID;
    private final Map<UUID, PlayerLoreData> players = new HashMap<>();

    public LoreState() {
    }

    public static LoreState fromNbt(NbtCompound tag) {
        LoreState state = new LoreState();
        NbtCompound playersTag = tag.getCompound("Players");
        for (String key : playersTag.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                NbtCompound playerTag = playersTag.getCompound(key);
                boolean loreMode = playerTag.getBoolean("LoreMode");
                int lives = playerTag.getInt("CanonLives");
                state.players.put(uuid, new PlayerLoreData(uuid, loreMode, lives));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtCompound playersTag = new NbtCompound();
        for (Map.Entry<UUID, PlayerLoreData> entry : players.entrySet()) {
            NbtCompound playerTag = new NbtCompound();
            playerTag.putBoolean("LoreMode", entry.getValue().isLoreMode());
            playerTag.putInt("CanonLives", entry.getValue().getCanonLives());
            playersTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("Players", playersTag);
        return tag;
    }

    public static LoreState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(LoreState::fromNbt, LoreState::new, STATE_KEY);
    }

    public PlayerLoreData getPlayerData(UUID uuid) {
        return players.computeIfAbsent(uuid, id -> {
            markDirty();
            return new PlayerLoreData(id, false, ModConfig.get().defaultLives);
        });
    }
}
