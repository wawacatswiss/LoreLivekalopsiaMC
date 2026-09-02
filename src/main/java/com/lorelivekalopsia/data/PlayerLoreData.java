package com.lorelivekalopsia.data;

import java.util.UUID;

public class PlayerLoreData {
    private final UUID uuid;
    private boolean loreMode;
    private int canonLives;

    public PlayerLoreData(UUID uuid) {
        this.uuid = uuid;
        this.loreMode = false;
        this.canonLives = 3;
    }

    public PlayerLoreData(UUID uuid, boolean loreMode, int canonLives) {
        this.uuid = uuid;
        this.loreMode = loreMode;
        this.canonLives = canonLives;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isLoreMode() {
        return loreMode;
    }

    public void setLoreMode(boolean loreMode) {
        this.loreMode = loreMode;
    }

    public int getCanonLives() {
        return canonLives;
    }

    public void setCanonLives(int canonLives) {
        this.canonLives = Math.max(0, canonLives);
    }
}
