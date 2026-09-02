package com.lorelivekalopsia.network;

import com.lorelivekalopsia.LoreLivekalopsia;
import com.lorelivekalopsia.data.PlayerLoreData;
import com.lorelivekalopsia.data.PlayerLoreManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ModNetworking {
    public static final Identifier TOGGLE_LORE_C2S = new Identifier(LoreLivekalopsia.MOD_ID, "toggle_lore_c2s");
    public static final Identifier SYNC_LORE_S2C = new Identifier(LoreLivekalopsia.MOD_ID, "sync_lore_s2c");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_LORE_C2S, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                PlayerLoreManager.toggleLoreMode(player);
            });
        });
    }

    public static void syncToClient(ServerPlayerEntity player) {
        PlayerLoreData data = PlayerLoreManager.getPlayerData(player);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(data.isLoreMode());
        buf.writeInt(data.getCanonLives());
        ServerPlayNetworking.send(player, SYNC_LORE_S2C, buf);
    }
}
