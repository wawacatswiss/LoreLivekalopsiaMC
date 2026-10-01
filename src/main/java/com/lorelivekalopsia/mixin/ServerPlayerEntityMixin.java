package com.lorelivekalopsia.mixin;

import com.lorelivekalopsia.data.PlayerLoreData;
import com.lorelivekalopsia.data.PlayerLoreManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerLoreManager.handlePlayerDeath(player, damageSource);
    }

    @Redirect(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"
            )
    )
    private void suppressVanillaDeathMessage(PlayerManager instance, Text message, boolean overlay) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerLoreData data = PlayerLoreManager.getPlayerData(player);
        if (data.isLoreMode() || data.getCanonLives() <= 0) {
            return;
        }
        instance.broadcast(message, overlay);
    }

    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerListName(CallbackInfoReturnable<Text> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getServer() == null) {
            return;
        }
        cir.setReturnValue(PlayerLoreManager.getFormattedTabName(player));
    }
}
