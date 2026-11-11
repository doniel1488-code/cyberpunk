package ru.neon.cyberpunk;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    private static final int SANDEVISTAN = 3;

    private static final double SANDEVISTAN_RADIUS = 64.0;
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
            case SANDEVISTAN -> "cyberpunk.implant.sandevistan";
            default -> throw new IllegalArgumentException("Unknown implant type: " + implantType);
        };
    }

    public static void activate(ServerPlayerEntity player, int implantType) {
        if (implantType < ACCELERATOR || implantType > SANDEVISTAN) {
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
            case SANDEVISTAN -> {
                duration = 180;
                cooldown = 300;
            }
            default -> {
                return;
            }
        }

        ACTIVE.put(uuid, new ActiveAbility(implantType, now + duration));
        playerCooldowns.put(implantType, now + cooldown);
        float activationStress = implantType == SANDEVISTAN ? 30.0f : implantType == BERSERK ? 24.0f : 18.0f;
        STRESS.merge(uuid, activationStress, Float::sum);

        float pitch = implantType == SANDEVISTAN ? 1.85f : implantType == ACCELERATOR ? 1.7f : 1.2f;
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.PLAYERS, 0.8f, pitch);
        player.sendMessage(Text.translatable(
                implantType == SANDEVISTAN ? "message.cyberpunk.sandevistan_activated" : "message.cyberpunk.activated"
        ), true);
    }

    /**
     * Called by the MobEntity mixin on the logical server.
     * A mob in an active Sandevistan field receives one full tick out of every seven.
     */
    public static boolean isTimeDilated(MobEntity mob) {
        if (mob.getWorld().isClient || !mob.isAlive()) {
            return false;
        }

        for (PlayerEntity player : mob.getWorld().getPlayers()) {
            if (!(player instanceof ServerPlayerEntity owner) || !owner.isAlive()) {
                continue;
            }

            ActiveAbility ability = ACTIVE.get(owner.getUuid());
            if (ability != null
                    && ability.implantType() == SANDEVISTAN
                    && owner.squaredDistanceTo(mob) <= SANDEVISTAN_RADIUS * SANDEVISTAN_RADIUS) {
                return true;
            }
        }
        return false;
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
                case SANDEVISTAN -> tickSandevistan(player);
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
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    3, 0.28, 0.35, 0.28, 0.015);
        }
    }

    private static void tickOpticalCamo(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 6, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 0, true, false, false));

        if (player.age % 8 == 0) {
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    2, 0.3, 0.45, 0.3, 0.01);
        }
    }

    private static void tickBerserk(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 6, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6, 1, true, false, false));

        if (player.age % 4 == 0) {
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.25, 0.4, 0.25, 0.0);
        }
    }

    private static void tickSandevistan(ServerPlayerEntity player) {
        // The owner keeps normal world ticking and receives a strong burst of movement speed.
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 2, true, false, false));

        if (player.age % 2 == 0) {
            double trailX = player.getX() - player.getVelocity().x * 2.0;
            double trailZ = player.getZ() - player.getVelocity().z * 2.0;
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    trailX, player.getY() + 0.85, trailZ,
                    4, 0.12, 0.38, 0.12, 0.025);
            world.spawnParticles(ParticleTypes.END_ROD,
                    trailX, player.getY() + 0.85, trailZ,
                    1, 0.08, 0.25, 0.08, 0.005);
        }
    }

    private record ActiveAbility(int implantType, long expiresAt) {
    }
}
