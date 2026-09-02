package com.lorelivekalopsia.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class LoreDisplay {
    private LoreDisplay() {}

    public static Formatting getLifeColor(int lives, int maxLives) {
        if (lives >= maxLives) {
            return Formatting.GREEN;
        } else if (lives > 1) {
            return Formatting.YELLOW;
        } else if (lives == 1) {
            return Formatting.RED;
        }
        return Formatting.DARK_GRAY;
    }

    public static MutableText getHeartsText(int lives, int maxLives) {
        int total = Math.max(maxLives, lives);
        Formatting activeColor = getLifeColor(lives, maxLives);

        MutableText text = Text.empty();
        for (int i = 0; i < total; i++) {
            if (i < lives) {
                text.append(Text.literal("❤").formatted(activeColor));
            } else {
                text.append(Text.literal("❤").formatted(Formatting.DARK_GRAY));
            }
        }
        return text;
    }
}
