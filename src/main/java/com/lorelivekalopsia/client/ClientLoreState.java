package com.lorelivekalopsia.client;

public class ClientLoreState {
    private static boolean loreMode = false;
    private static int canonLives = 3;

    public static boolean isLoreMode() {
        return loreMode;
    }

    public static void setLoreMode(boolean mode) {
        loreMode = mode;
    }

    public static int getCanonLives() {
        return canonLives;
    }

    public static void setCanonLives(int lives) {
        canonLives = lives;
    }
}
