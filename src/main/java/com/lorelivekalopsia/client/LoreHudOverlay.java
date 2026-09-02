package com.lorelivekalopsia.client;

import com.lorelivekalopsia.config.ModConfig;
import com.lorelivekalopsia.util.LoreDisplay;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class LoreHudOverlay implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        if (ClientLoreState.isLoreMode()) {
            int lives = ClientLoreState.getCanonLives();
            int maxLives = ModConfig.get().maxLives;
            Text hearts = LoreDisplay.getHeartsText(lives, maxLives);
            drawContext.drawTextWithShadow(client.textRenderer, hearts, 6, 6, 0xFFFFFF);
        }
    }
}
