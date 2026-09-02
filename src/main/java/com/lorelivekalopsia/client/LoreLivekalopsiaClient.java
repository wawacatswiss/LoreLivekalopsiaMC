package com.lorelivekalopsia.client;

import com.lorelivekalopsia.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class LoreLivekalopsiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        HudRenderCallback.EVENT.register(new LoreHudOverlay());

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SYNC_LORE_S2C, (client, handler, buf, responseSender) -> {
            boolean mode = buf.readBoolean();
            int lives = buf.readInt();
            client.execute(() -> {
                ClientLoreState.setLoreMode(mode);
                ClientLoreState.setCanonLives(lives);
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.TOGGLE_LORE_KEY.wasPressed()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(ModNetworking.TOGGLE_LORE_C2S, PacketByteBufs.empty());
                }
            }
        });
    }
}
