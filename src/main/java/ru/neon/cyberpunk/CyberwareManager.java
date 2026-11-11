package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
    private static final Map<UUID, Integer> STRESS_TIER = new HashMap<>();
    private static final Map<UUID, Long> OVERLOAD_UNTIL = new HashMap<>();

    private CyberwareManager() {
    }

    public static String getInstallTag(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "cyberpunk.implant.neural_accelerator";
            case OPTICAL_CAMO -> "cyberpunk.implant.optical_camo";
            case BERSERK -> "cyberpunk.implant.berserk";
            case SANDEVISTAN -> "cyberpunk.implant.sandevistan";
            case 4 -> "cyberpunk.implant.mantis_arms";
            default -> throw new IllegalArgumentException("Unknown implant type: " + implantType);
        };
    }

    public static void activate(ServerPlayerEntity player, int implantType) {
        if (implantType < ACCELERATOR || implantType > BERSERK + 2) {
            return;
        }

        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        if (isOverloaded(uuid, now)) {
            player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis_locked"), true);
            return;
        }
        String tag = getInstallTag(implantType);
        if (!player.getCommandTags().contains(tag)) {
            player.sendMessage(Text.translatable("message.cyberpunk.not_installed"), true);
            return;
        }

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
            case ACCELERATOR -> { duration = 180; cooldown = 360; }
            case OPTICAL_CAMO -> { duration = 160; cooldown = 480; }
            case BERSERK -> { duration = 140; cooldown = 500; }
            case SANDEVISTAN -> { duration = 180; cooldown = 300; }
            case 4 -> { duration = 120; cooldown = 420; }
            default -> { return; }
        }

        ACTIVE.put(uuid, new ActiveAbility(implantType, now + duration));
        playerCooldowns.put(implantType, now + cooldown);
        addStress(player, activationStress(implantType));
        activationBurst(player, implantType);
        sendVisual(player, implantType + 1, 12, 112);
        player.sendMessage(Text.translatable(activationMessage(implantType)), true);
    }

    private static float activationStress(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> 7.0f;
            case OPTICAL_CAMO -> 9.0f;
            case BERSERK -> 15.0f;
            case SANDEVISTAN -> 20.0f;
            case 4 -> 13.0f;
            default -> 0.0f;
        };
    }

    private static String activationMessage(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "message.cyberpunk.accelerator_activated";
            case OPTICAL_CAMO -> "message.cyberpunk.camo_activated";
            case BERSERK -> "message.cyberpunk.berserk_activated";
            case SANDEVISTAN -> "message.cyberpunk.sandevistan_activated";
            case 4 -> "message.cyberpunk.mantis_activated";
            default -> "message.cyberpunk.activated";
        };
    }

    private static void activationBurst(ServerPlayerEntity player, int implantType) {
        ServerWorld world = (ServerWorld) player.getWorld();
        double x = player.getX();
        double y = player.getY() + 0.9;
        double z = player.getZ();

        switch (implantType) {
            case ACCELERATOR -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                        SoundCategory.PLAYERS, 0.85f, 1.75f);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 36, 0.75, 0.65, 0.75, 0.06);
                world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 14, 0.55, 0.25, 0.55, 0.025);
            }
            case OPTICAL_CAMO -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.PLAYERS, 0.55f, 1.65f);
                world.spawnParticles(ParticleTypes.PORTAL, x, y, z, 46, 0.65, 0.8, 0.65, 0.2);
                world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 12, 0.4, 0.55, 0.4, 0.025);
            }
            case BERSERK -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
                        SoundCategory.PLAYERS, 1.0f, 0.65f);
                world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 20, 0.65, 0.55, 0.65, 0.1);
                world.spawnParticles(ParticleTypes.CRIT, x, y, z, 32, 0.55, 0.65, 0.55, 0.25);
                world.spawnParticles(ParticleTypes.FLAME, x, y, z, 18, 0.4, 0.4, 0.4, 0.015);
            }
            case SANDEVISTAN -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                        SoundCategory.PLAYERS, 1.0f, 2.0f);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 56, 0.8, 0.8, 0.8, 0.08);
                world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 28, 0.55, 0.65, 0.55, 0.05);
            }
            case 4 -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                        SoundCategory.PLAYERS, 1.0f, 0.7f);
                world.spawnParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 10, 0.7, 0.3, 0.7, 0.0);
                world.spawnParticles(ParticleTypes.CRIT, x, y, z, 24, 0.55, 0.65, 0.55, 0.16);
            }
        }
    }

    private static void sendVisual(ServerPlayerEntity player, int effectId, int duration, int intensity) {
        ServerPlayNetworking.send(player, new CyberpunkVisualPayload(effectId, duration, intensity));
    }

    private static boolean isOverloaded(UUID uuid, long now) {
        Long until = OVERLOAD_UNTIL.get(uuid);
        if (until == null) return false;
        if (now >= until) {
            OVERLOAD_UNTIL.remove(uuid);
            return false;
        }
        return true;
    }

    private static void addStress(ServerPlayerEntity player, float amount) {
        UUID uuid = player.getUuid();
        float next = Math.min(100.0f, STRESS.getOrDefault(uuid, 0.0f) + amount);
        STRESS.put(uuid, next);
        updateStressTier(player, next);
        if (next >= 100.0f) startCyberpsychosis(player);
    }

    private static void updateStressTier(ServerPlayerEntity player, float stress) {
        UUID uuid = player.getUuid();
        int tier = stress >= 85.0f ? 2 : stress >= 60.0f ? 1 : 0;
        int previous = STRESS_TIER.getOrDefault(uuid, 0);
        if (tier == previous) return;
        STRESS_TIER.put(uuid, tier);

        if (tier == 1) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_warning", Math.round(stress)), true);
            sendVisual(player, 8, 10, 48);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS, 0.35f, 0.65f);
        } else if (tier == 2) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_critical"), true);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, false, false, true));
            sendVisual(player, 9, 18, 96);
        } else if (previous > 0) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_stabilized"), true);
        }
    }

    private static void startCyberpsychosis(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        if (isOverloaded(uuid, now)) return;

        ACTIVE.remove(uuid);
        OVERLOAD_UNTIL.put(uuid, now + 240L);
        STRESS.put(uuid, 28.0f);
        STRESS_TIER.put(uuid, 0);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 35, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 240, 1, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 240, 1, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1, false, false, true));
        player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                SoundCategory.PLAYERS, 1.0f, 0.65f);

        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0, player.getZ(),
                35, 0.7, 0.8, 0.7, 0.12);
        world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
                55, 0.75, 0.9, 0.75, 0.06);
        sendVisual(player, 10, 50, 160);
    }

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

        if (isOverloaded(uuid, now)) {
            if (player.age % 20 == 0) {
                ServerWorld world = (ServerWorld) player.getWorld();
                world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
                        5, 0.35, 0.5, 0.35, 0.015);
                sendVisual(player, 10, 6, 72);
            }
            return;
        }

        ActiveAbility ability = ACTIVE.get(uuid);
        if (ability != null && now >= ability.expiresAt()) {
            ACTIVE.remove(uuid);
            ability = null;
            player.sendMessage(Text.translatable("message.cyberpunk.ability_ended"), true);
            sendVisual(player, 11, 8, 45);
        }

        if (ability != null) {
            switch (ability.implantType()) {
                case ACCELERATOR -> tickAccelerator(player);
                case OPTICAL_CAMO -> tickOpticalCamo(player);
                case BERSERK -> tickBerserk(player);
                case SANDEVISTAN -> tickSandevistan(player);
                case 4 -> tickMantisArms(player);
                default -> ACTIVE.remove(uuid);
            }

            float stressPerTick = switch (ability.implantType()) {
                case ACCELERATOR -> 0.018f;
                case OPTICAL_CAMO -> 0.025f;
                case BERSERK -> 0.055f;
                case SANDEVISTAN -> 0.075f;
                case 4 -> 0.045f;
                default -> 0.0f;
            };
            addStress(player, stressPerTick);
            if (isOverloaded(uuid, now)) return;
        } else {
            float stress = Math.max(0.0f, STRESS.getOrDefault(uuid, 0.0f) - 0.035f);
            STRESS.put(uuid, stress);
            updateStressTier(player, stress);
        }
    }

    private static void tickAccelerator(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 6, 1, true, false, false));
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
                    5, 0.28, 0.35, 0.28, 0.025);
        }
    }

    private static void tickOpticalCamo(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 6, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 0, true, false, false));

        if (player.age % 8 == 0) {
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    3, 0.25, 0.45, 0.25, 0.12);
        }
    }

    private static void tickBerserk(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 6, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6, 0, true, false, false));

        if (player.age % 4 == 0) {
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.25, 0.4, 0.25, 0.0);
        }
    }

    private static void tickSandevistan(ServerPlayerEntity player) {
        // The owner keeps normal world ticking and receives a strong burst of movement speed.
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6, 2, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 6, 1, true, false, false));

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

    private static void tickMantisArms(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 8, 2, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 1, true, false, false));
        if (player.age % 3 == 0) {
            Vec3d look = player.getRotationVec(1.0f);
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX() + look.x, player.getY() + 1.0, player.getZ() + look.z,
                    2, 0.25, 0.35, 0.25, 0.0);
            world.spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.8, player.getZ(),
                    4, 0.22, 0.45, 0.22, 0.08);
        }
    }

    private record ActiveAbility(int implantType, long expiresAt) {
    }
}
