package com.lorelivekalopsia.data;

import com.lorelivekalopsia.LoreLivekalopsia;
import com.lorelivekalopsia.config.ModConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LoreState extends PersistentState {
    private static final String STATE_KEY = LoreLivekalopsia.MOD_ID;
    private final Map<UUID, PlayerLoreData> players = new HashMap<>();
    private final Set<UUID> pendingRevives = new HashSet<>();

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
        NbtList pending = tag.getList("PendingRevives", NbtElement.STRING_TYPE);
        for (int i = 0; i < pending.size(); i++) {
            try {
                state.pendingRevives.add(UUID.fromString(pending.getString(i)));
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
        NbtList pending = new NbtList();
        for (UUID uuid : pendingRevives) {
            pending.add(NbtString.of(uuid.toString()));
        }
        tag.put("PendingRevives", pending);
        return tag;
    }

    public static LoreState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(LoreState::fromNbt, LoreState::new, STATE_KEY);
    }

    public void addPendingRevive(UUID uuid) {
        if (pendingRevives.add(uuid)) {
            markDirty();
        }
    }

    public boolean consumePendingRevive(UUID uuid) {
        if (pendingRevives.remove(uuid)) {
            markDirty();
            return true;
        }
        return false;
    }

    public PlayerLoreData getPlayerData(UUID uuid) {
        return players.computeIfAbsent(uuid, id -> {
            markDirty();
            return new PlayerLoreData(id, false, ModConfig.get().defaultLives);
        });
    }
}
