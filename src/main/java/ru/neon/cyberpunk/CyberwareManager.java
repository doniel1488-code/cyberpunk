package ru.neon.cyberpunk;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CyberwareManager {
    private static final int ACCELERATOR = 0;
    private static final int OPTICAL_CAMO = 1;
    private static final int BERSERK = 2;

    private static final Map<UUID, ActiveAbility> ACTIVE = new HashMap<>();
    private static final Map<UUID, Map<Integer, Long>> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Float> STRESS = new HashMap<>();

    private CyberwareManager() {
    }

    public static String getInstallTag(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "cyberpunk.implant.neural_accelerator";
            case OPTICAL_CAMO -> "cyberpunk.implant.optical_camo";
            case BERSERK -> "cyberpunk.implant.berserk";
            default -> throw new IllegalArgumentException("Unknown implant type: " + implantType);
        };
    }

    public static void activate(ServerPlayerEntity player, int implantType) {
        if (implantType < ACCELERATOR || implantType > BERSERK) {
            return;
        }

        UUID uuid = player.getUuid();
        String tag = getInstallTag(implantType);
        if (!player.getCommandTags().contains(tag)) {
            player.sendMessage(Text.translatable("message.cyberpunk.not_installed"), true);
            return;
        }

        long now = player.getWorld().getTime();
        Map<Integer, Long> playerCooldowns = COOLDOWNS.computeIfAbsent(uuid, ignored -> new HashMap<>());
        long readyAt = playerCooldowns.getOrDefault(implantType, 0L);
        if (now < readyAt) {
            long seconds = Math.max(1L, (readyAt - now + 19L) / 20L);
            player.sendMessage(Text.translatable("message.cyberpunk.cooldown", seconds), true);
            return;
        }

        if (ACTIVE.containsKey(uuid)) {
            player.sendMessage(Text.translatable("message.cyberpunk.already_active"), true);
            return;
        }

        int duration;
        int cooldown;
        switch (implantType) {
            case ACCELERATOR -> {
                duration = 180;
                cooldown = 360;
            }
            case OPTICAL_CAMO -> {
                duration = 160;
                cooldown = 480;
            }
            case BERSERK -> {
                duration = 140;
                cooldown = 500;
            }
            default -> {
                return;
            }
        }

        ACTIVE.put(uuid, new ActiveAbility(implantType, now + duration));
        playerCooldowns.put(implantType, now + cooldown);
        STRESS.merge(uuid, implantType == BERSERK ? 24.0f : 18.0f, Float::sum);

        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.PLAYERS, 0.8f, implantType == ACCELERATOR ? 1.7f : 1.2f);
        player.sendMessage(Text.translatable("message.cyberpunk.activated"), true);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        ActiveAbility ability = ACTIVE.get(uuid);

        if (ability != null && now >= ability.expiresAt()) {
            ACTIVE.remove(uuid);
            ability = null;
            player.sendMessage(Text.translatable("message.cyberpunk.ability_ended"), true);
        }

        if (ability != null) {
            switch (ability.implantType()) {
                case ACCELERATOR -> tickAccelerator(player);
                case OPTICAL_CAMO -> tickOpticalCamo(player);
                case BERSERK -> tickBerserk(player);
                default -> ACTIVE.remove(uuid);
            }
            STRESS.merge(uuid, 0.045f, Float::sum);
        } else {
            STRESS.computeIfPresent(uuid, (ignored, value) -> Math.max(0.0f, value - 0.0125f));
        }

        float stress = STRESS.getOrDefault(uuid, 0.0f);
        if (stress >= 100.0f) {
            STRESS.put(uuid, 45.0f);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 180, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 180, 1, false, false, true));
            player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis"), true);
        }
    }

    private static void tickAccelerator(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 1, true, false, false));
        player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(14.0),
                entity -> entity != player && !(entity instanceof PlayerEntity) && entity.isAlive()
        ).forEach(entity -> entity.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOWNESS, 7, 3, true, false, false)
        ));

        if (player.age % 3 == 0) {
            player.getServerWorld().spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    3, 0.28, 0.35, 0.28, 0.015);
        }
    }

    private static void tickOpticalCamo(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 6, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 0, true, false, false));

        if (player.age % 8 == 0) {
            player.getServerWorld().spawnParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    2, 0.3, 0.45, 0.3, 0.01);
        }
    }

    private static void tickBerserk(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 6, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6, 1, true, false, false));

        if (player.age % 4 == 0) {
            player.getServerWorld().spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.25, 0.4, 0.25, 0.0);
        }
    }

    private record ActiveAbility(int implantType, long expiresAt) {
    }
}
