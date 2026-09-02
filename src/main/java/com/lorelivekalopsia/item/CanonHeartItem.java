package com.lorelivekalopsia.item;

import com.lorelivekalopsia.config.ModConfig;
import com.lorelivekalopsia.data.PlayerLoreData;
import com.lorelivekalopsia.data.PlayerLoreManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CanonHeartItem extends Item {
    public CanonHeartItem() {
        super(new Settings().maxCount(16).rarity(Rarity.EPIC));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            return TypedActionResult.pass(stack);
        }

        if (user instanceof ServerPlayerEntity serverPlayer) {
            PlayerLoreData data = PlayerLoreManager.getPlayerData(serverPlayer);
            int maxLives = ModConfig.get().maxLives;

            if (data.getCanonLives() >= maxLives) {
                serverPlayer.sendMessage(
                        Text.literal("You already have maximum Canon Lives (" + maxLives + "/" + maxLives + ").").formatted(Formatting.RED),
                        true
                );
                serverPlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1.0f, 1.0f);
                return TypedActionResult.fail(stack);
            }

            if (PlayerLoreManager.addCanonLives(serverPlayer, 1)) {
                if (!serverPlayer.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                serverPlayer.playSound(SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8f, 1.2f);
                serverPlayer.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HEART,
                            serverPlayer.getX(), serverPlayer.getY() + 1.2, serverPlayer.getZ(),
                            15, 0.5, 0.5, 0.5, 0.1);
                    serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
                            25, 0.4, 0.6, 0.4, 0.2);
                }

                serverPlayer.sendMessage(
                        Text.literal("+1 Canon Life! ").formatted(Formatting.GREEN)
                                .append(Text.literal("You now have ").formatted(Formatting.GRAY))
                                .append(Text.literal(data.getCanonLives() + "/" + maxLives).formatted(Formatting.YELLOW))
                                .append(Text.literal(" Canon Lives.").formatted(Formatting.GRAY)),
                        false
                );
                return TypedActionResult.consume(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int maxLives = ModConfig.get().maxLives;
        tooltip.add(Text.literal("An essence of canon existence.").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("Right-click to restore ").formatted(Formatting.YELLOW)
                .append(Text.literal("+1 Canon Life").formatted(Formatting.GREEN))
                .append(Text.literal(".").formatted(Formatting.YELLOW)));
        tooltip.add(Text.literal("Maximum " + maxLives + " Canon Lives").formatted(Formatting.DARK_GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
