package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CyberwareManager {
    private static final int ACCELERATOR = 0;
    private static final int OPTICAL_CAMO = 1;
    private static final int BERSERK = 2;
    private static final int SANDEVISTAN = 3;
    private static final int MANTIS_ARMS = 4;
    private static final int GORILLA_ARMS = 5;
    private static final int KERENZIKOV = 6;
    private static final int SUBDERMAL_ARMOR = 7;
    private static final int BLOOD_PUMP = 8;

    private static final double SANDEVISTAN_RADIUS = 64.0;
    private static final Map<UUID, ActiveAbility> ACTIVE = new HashMap<>();
    private static final Map<UUID, Map<Integer, Long>> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Float> STRESS = new HashMap<>();
    private static final Map<UUID, Integer> STRESS_TIER = new HashMap<>();
    private static final Map<UUID, Long> OVERLOAD_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> RAGE_NEXT_ATTACK = new HashMap<>();
    private static final Map<UUID, Long> GORILLA_HIT_READY = new HashMap<>();

    private CyberwareManager() {
    }

    public static String getInstallTag(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "cyberpunk.implant.neural_accelerator";
            case OPTICAL_CAMO -> "cyberpunk.implant.optical_camo";
            case BERSERK -> "cyberpunk.implant.berserk";
            case SANDEVISTAN -> "cyberpunk.implant.sandevistan";
            case MANTIS_ARMS -> "cyberpunk.implant.mantis_arms";
            case GORILLA_ARMS -> "cyberpunk.implant.gorilla_arms";
            case KERENZIKOV -> "cyberpunk.implant.kerenzikov";
            case SUBDERMAL_ARMOR -> "cyberpunk.implant.subdermal_armor";
            case BLOOD_PUMP -> "cyberpunk.implant.blood_pump";
            default -> throw new IllegalArgumentException("Unknown implant type: " + implantType);
        };
    }

    public static String getActivationKeyTranslation(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "key.cyberpunk.neural_accelerator";
            case OPTICAL_CAMO -> "key.cyberpunk.optical_camo";
            case BERSERK -> "key.cyberpunk.berserk";
            case SANDEVISTAN -> "key.cyberpunk.sandevistan";
            case MANTIS_ARMS -> "key.cyberpunk.mantis_arms";
            case GORILLA_ARMS -> "key.cyberpunk.gorilla_arms";
            case KERENZIKOV -> "key.cyberpunk.kerenzikov";
            case SUBDERMAL_ARMOR -> "message.cyberpunk.passive";
            case BLOOD_PUMP -> "key.cyberpunk.blood_pump";
            default -> "message.cyberpunk.passive";
        };
    }

    public static int getImplantTypeById(String id) {
        return switch (id) {
            case "neural_accelerator" -> ACCELERATOR;
            case "optical_camo" -> OPTICAL_CAMO;
            case "berserk_module" -> BERSERK;
            case "sandevistan" -> SANDEVISTAN;
            case "mantis_arms" -> MANTIS_ARMS;
            case "gorilla_arms" -> GORILLA_ARMS;
            case "kerenzikov" -> KERENZIKOV;
            case "subdermal_armor" -> SUBDERMAL_ARMOR;
            case "blood_pump" -> BLOOD_PUMP;
            default -> -1;
        };
    }

    public static void uninstall(ServerPlayerEntity player, int implantType) {
        UUID uuid = player.getUuid();
        if (!player.getCommandTags().remove(getInstallTag(implantType))) {
            player.sendMessage(Text.translatable("message.cyberpunk.not_installed"), true);
            return;
        }
        ActiveAbility active = ACTIVE.get(uuid);
        if (active != null && active.implantType() == implantType) {
            ACTIVE.remove(uuid);
            if (implantType == SANDEVISTAN) broadcastSandevistanTrail(player, 0);
            sendVisual(player, 11, 10, 34);
        }
        Map<Integer, Long> cooldowns = COOLDOWNS.get(uuid);
        if (cooldowns != null) {
            cooldowns.remove(implantType);
            if (cooldowns.isEmpty()) COOLDOWNS.remove(uuid);
        }
        player.sendMessage(Text.translatable("message.cyberpunk.implant_uninstalled"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.PLAYERS, 0.65f, 1.15f);
    }

    public static void sendStatus(ServerPlayerEntity player) {
        float load = STRESS.getOrDefault(player.getUuid(), 0.0f);
        player.sendMessage(Text.translatable("message.cyberpunk.status_header", Math.round(load)), false);
        for (int type = ACCELERATOR; type <= BLOOD_PUMP; type++) {
            String nameKey = switch (type) {
                case ACCELERATOR -> "item.cyberpunk.neural_accelerator";
                case OPTICAL_CAMO -> "item.cyberpunk.optical_camo";
                case BERSERK -> "item.cyberpunk.berserk_module";
                case SANDEVISTAN -> "item.cyberpunk.sandevistan";
                case MANTIS_ARMS -> "item.cyberpunk.mantis_arms";
                case GORILLA_ARMS -> "item.cyberpunk.gorilla_arms";
                case KERENZIKOV -> "item.cyberpunk.kerenzikov";
                case SUBDERMAL_ARMOR -> "item.cyberpunk.subdermal_armor";
                case BLOOD_PUMP -> "item.cyberpunk.blood_pump";
                default -> "message.cyberpunk.activated";
            };
            boolean installed = player.getCommandTags().contains(getInstallTag(type));
            Text line = Text.literal("• ").append(Text.translatable(nameKey))
                    .append(Text.literal(" — "))
                    .append(Text.translatable(installed ? "message.cyberpunk.status_installed" : "message.cyberpunk.status_missing"))
                    .append(Text.literal(" | "))
                    .append(Text.translatable(getActivationKeyTranslation(type)));
            player.sendMessage(line, false);
            String descriptionKey = switch (type) {
                case ACCELERATOR -> "description.cyberpunk.neural_accelerator";
                case OPTICAL_CAMO -> "description.cyberpunk.optical_camo";
                case BERSERK -> "description.cyberpunk.berserk";
                case SANDEVISTAN -> "description.cyberpunk.sandevistan";
                case MANTIS_ARMS -> "description.cyberpunk.mantis_arms";
                case GORILLA_ARMS -> "description.cyberpunk.gorilla_arms";
                case KERENZIKOV -> "description.cyberpunk.kerenzikov";
                case SUBDERMAL_ARMOR -> "description.cyberpunk.subdermal_armor";
                case BLOOD_PUMP -> "description.cyberpunk.blood_pump";
                default -> "message.cyberpunk.activated";
            };
            player.sendMessage(Text.literal("  ").append(Text.translatable(descriptionKey)), false);
        }
    }

    public static void activate(ServerPlayerEntity player, int implantType) {
        if (implantType < ACCELERATOR || implantType > BLOOD_PUMP) {
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
        if (implantType == SUBDERMAL_ARMOR) {
            player.sendMessage(Text.translatable("message.cyberpunk.subdermal_is_passive"), true);
            return;
        }
        if (implantType == BLOOD_PUMP) {
            activateBloodPump(player, now);
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
            case MANTIS_ARMS -> { duration = 120; cooldown = 420; }
            case GORILLA_ARMS -> { duration = 200; cooldown = 600; }
            case KERENZIKOV -> { duration = 60; cooldown = 240; }
            default -> { return; }
        }

        addStress(player, activationStress(implantType));
        if (isOverloaded(uuid, now)) {
            return;
        }
        ACTIVE.put(uuid, new ActiveAbility(implantType, now + duration));
        playerCooldowns.put(implantType, now + cooldown);
        activationBurst(player, implantType);
        int visualId = switch (implantType) {
            case GORILLA_ARMS -> 12;
            case KERENZIKOV -> 13;
            default -> implantType + 1;
        };
        sendVisual(player, visualId, 12, 112);
        if (implantType == SANDEVISTAN) broadcastSandevistanTrail(player, duration + 20);
        player.sendMessage(Text.translatable(activationMessage(implantType)), true);
    }

    private static float activationStress(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> 12.0f;
            case OPTICAL_CAMO -> 14.0f;
            case BERSERK -> 19.0f;
            case SANDEVISTAN -> 25.0f;
            case MANTIS_ARMS -> 16.0f;
            case GORILLA_ARMS -> 22.0f;
            case KERENZIKOV -> 12.0f;
            default -> 0.0f;
        };
    }

    private static String activationMessage(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "message.cyberpunk.accelerator_activated";
            case OPTICAL_CAMO -> "message.cyberpunk.camo_activated";
            case BERSERK -> "message.cyberpunk.berserk_activated";
            case SANDEVISTAN -> "message.cyberpunk.sandevistan_activated";
            case MANTIS_ARMS -> "message.cyberpunk.mantis_activated";
            case GORILLA_ARMS -> "message.cyberpunk.gorilla_activated";
            case KERENZIKOV -> "message.cyberpunk.kerenzikov_activated";
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
            case MANTIS_ARMS -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                        SoundCategory.PLAYERS, 1.0f, 0.7f);
                world.spawnParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 10, 0.7, 0.3, 0.7, 0.0);
                world.spawnParticles(ParticleTypes.CRIT, x, y, z, 24, 0.55, 0.65, 0.55, 0.16);
            }
            case GORILLA_ARMS -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK,
                        SoundCategory.PLAYERS, 0.9f, 0.65f);
                world.spawnParticles(ParticleTypes.EXPLOSION, x, y, z, 3, 0.45, 0.3, 0.45, 0.0);
                world.spawnParticles(ParticleTypes.CRIT, x, y, z, 32, 0.5, 0.6, 0.5, 0.18);
                world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 15, 0.6, 0.3, 0.6, 0.045);
            }
            case KERENZIKOV -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.PLAYERS, 0.75f, 1.85f);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 42, 0.75, 0.65, 0.75, 0.07);
                world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 14, 0.5, 0.2, 0.5, 0.035);
                Vec3d look = player.getRotationVec(1.0f);
                Vec3d dash = new Vec3d(look.x, 0.0, look.z);
                if (dash.lengthSquared() > 0.001) {
                    dash = dash.normalize();
                    Vec3d oldVelocity = player.getVelocity();
                    player.setVelocity(dash.x * 1.45, Math.max(oldVelocity.y, 0.20), dash.z * 1.45);
                }
            }
        }
    }

    private static void sendVisual(ServerPlayerEntity player, int effectId, int duration, int intensity) {
        ServerPlayNetworking.send(player, new CyberpunkVisualPayload(effectId, duration, intensity));
    }

    private static boolean isOverloaded(UUID uuid, long now) {
        Long until = OVERLOAD_UNTIL.get(uuid);
        return until != null && now < until;
    }

    private static void broadcastSandevistanTrail(ServerPlayerEntity owner, int durationTicks) {
        SandevistanTrailPayload payload = new SandevistanTrailPayload(owner.getId(), durationTicks);
        for (PlayerEntity candidate : owner.getWorld().getPlayers()) {
            if (candidate instanceof ServerPlayerEntity viewer
                    && viewer.squaredDistanceTo(owner) <= 144.0 * 144.0) {
                ServerPlayNetworking.send(viewer, payload);
            }
        }
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
        int tier = stress >= 65.0f ? 2 : stress >= 35.0f ? 1 : 0;
        int previous = STRESS_TIER.getOrDefault(uuid, 0);
        if (tier == previous) return;
        STRESS_TIER.put(uuid, tier);

        if (tier > previous && tier == 1) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_warning", Math.round(stress)), true);
            sendVisual(player, 8, 16, 62);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS, 0.45f, 0.72f);
        } else if (tier > previous && tier == 2) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_critical"), true);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 0, false, false, true));
            sendVisual(player, 9, 24, 118);
        } else if (tier < previous && tier == 0) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_stabilized"), true);
        }
    }

    private static void activateBloodPump(ServerPlayerEntity player, long now) {
        UUID uuid = player.getUuid();
        Map<Integer, Long> playerCooldowns = COOLDOWNS.computeIfAbsent(uuid, ignored -> new HashMap<>());
        long readyAt = playerCooldowns.getOrDefault(BLOOD_PUMP, 0L);
        if (now < readyAt) {
            player.sendMessage(Text.translatable("message.cyberpunk.cooldown",
                    Math.max(1L, (readyAt - now + 19L) / 20L)), true);
            return;
        }

        addStress(player, 18.0f);
        if (isOverloaded(uuid, now)) return;

        playerCooldowns.put(BLOOD_PUMP, now + 900L);
        player.heal(8.0f);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 1, true, true, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 0, true, true, true));
        player.removeStatusEffect(StatusEffects.POISON);
        player.removeStatusEffect(StatusEffects.WITHER);

        ServerWorld world = (ServerWorld) player.getWorld();
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.PLAYERS, 0.8f, 1.35f);
        world.spawnParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(),
                14, 0.5, 0.6, 0.5, 0.04);
        world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(),
                28, 0.55, 0.65, 0.55, 0.12);
        sendVisual(player, 14, 16, 135);
        player.sendMessage(Text.translatable("message.cyberpunk.blood_pump_activated"), true);
    }

    public static void onAfterDamage(LivingEntity target, DamageSource source, float damageTaken) {
        if (target.getWorld().isClient) return;

        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            ActiveAbility ability = ACTIVE.get(attacker.getUuid());
            if (ability != null && ability.implantType() == GORILLA_ARMS && target != attacker) {
                Vec3d direction = target.getPos().subtract(attacker.getPos());
                Vec3d horizontal = new Vec3d(direction.x, 0.0, direction.z);
                if (horizontal.lengthSquared() > 0.001) {
                    horizontal = horizontal.normalize();
                    target.addVelocity(horizontal.x * 0.95, 0.30, horizontal.z * 0.95);
                }
                ServerWorld world = (ServerWorld) target.getWorld();
                world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55), target.getZ(),
                        10, 0.28, 0.35, 0.28, 0.13);
                world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK,
                        SoundCategory.PLAYERS, 0.45f, 1.4f);
            } else if (ability != null && ability.implantType() == MANTIS_ARMS && target != attacker) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 40, 0, false, true, true));
                ServerWorld world = (ServerWorld) target.getWorld();
                world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55), target.getZ(),
                        5, 0.2, 0.3, 0.2, 0.08);
            }
        }

        if (target instanceof ServerPlayerEntity wearer
                && wearer.getCommandTags().contains(getInstallTag(SUBDERMAL_ARMOR))
                && damageTaken > 0.0f) {
            ServerWorld world = (ServerWorld) wearer.getWorld();
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, wearer.getX(), wearer.getY() + 0.9, wearer.getZ(),
                    6, 0.28, 0.42, 0.28, 0.025);
            world.playSound(null, wearer.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS, 0.18f, 1.8f);
        }
    }

    private static void startCyberpsychosis(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        if (isOverloaded(uuid, now)) return;

        ActiveAbility interrupted = ACTIVE.remove(uuid);
        if (interrupted != null && interrupted.implantType() == SANDEVISTAN) {
            broadcastSandevistanTrail(player, 0);
        }

        // A short seizure is followed by 15 seconds of uncontrolled cyberpsycho rage.
        OVERLOAD_UNTIL.put(uuid, now + 300L);
        STRESS.put(uuid, 30.0f);
        STRESS_TIER.put(uuid, 0);
        RAGE_NEXT_ATTACK.put(uuid, now + 8L);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 28, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 55, 1, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 35, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 24, 0, false, false, true));
        player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                SoundCategory.PLAYERS, 1.0f, 0.65f);

        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0, player.getZ(),
                35, 0.7, 0.8, 0.7, 0.12);
        world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
                55, 0.75, 0.9, 0.75, 0.06);
        world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.9, player.getZ(),
                22, 0.5, 0.55, 0.5, 0.035);
        sendVisual(player, 10, 70, 175);
    }

    private static void tickCyberpsychosisRage(ServerPlayerEntity player, long now) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 8, 3, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 2, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 8, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 8, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 8, 0, true, false, false));
        if (player.age % 4 == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.9, player.getZ(),
                    4, 0.35, 0.5, 0.35, 0.025);
            world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0, player.getZ(),
                    2, 0.3, 0.35, 0.3, 0.08);
            world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.8, player.getZ(),
                    2, 0.25, 0.35, 0.25, 0.02);
        }

        // Preserve camera and movement input: rage strikes visible targets around the player.
        UUID uuid = player.getUuid();
        if (now < RAGE_NEXT_ATTACK.getOrDefault(uuid, 0L)) return;
        ServerWorld world = (ServerWorld) player.getWorld();
        var targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(3.5, 1.5, 3.5),
                entity -> entity != player && entity.isAlive() && entity.isAttackable()
                        && (!(entity instanceof PlayerEntity other)
                        || (!other.isCreative() && !other.isSpectator()))
        );
        int hits = 0;
        for (LivingEntity target : targets) {
            if (!player.canSee(target)) continue;
            if (target.damage(player.getDamageSources().playerAttack(player), 5.0f)) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 12, 0, true, true, true));
                world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55), target.getZ(),
                        7, 0.22, 0.32, 0.22, 0.12);
                world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR,
                        target.getX(), target.getBodyY(0.55), target.getZ(),
                        4, 0.18, 0.25, 0.18, 0.08);
                hits++;
            }
        }
        if (hits > 0) {
            player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                    SoundCategory.PLAYERS, 0.8f, 0.68f + player.getRandom().nextFloat() * 0.25f);
            sendVisual(player, 10, 5, 72);
        }
        RAGE_NEXT_ATTACK.put(uuid, now + 10L);
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
            if (ability != null) {
                if (ability.implantType() == SANDEVISTAN
                        && owner.squaredDistanceTo(mob) <= SANDEVISTAN_RADIUS * SANDEVISTAN_RADIUS) {
                    return true;
                }
                if (ability.implantType() == KERENZIKOV
                        && owner.squaredDistanceTo(mob) <= 12.0 * 12.0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickPlayer(player);
        }
    }

    public static void removePlayer(UUID uuid) {
        ACTIVE.remove(uuid);
        COOLDOWNS.remove(uuid);
        STRESS.remove(uuid);
        STRESS_TIER.remove(uuid);
        OVERLOAD_UNTIL.remove(uuid);
        RAGE_NEXT_ATTACK.remove(uuid);
        GORILLA_HIT_READY.remove(uuid);
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();

        // Subdermal Armor is passive and remains online even during other implant states.
        if (player.getCommandTags().contains(getInstallTag(SUBDERMAL_ARMOR))) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 25, 0, true, false, false));
        }

        boolean fullCyberwareArmor =
                player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.CYBERWARE_HELMET)
                && player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.CYBERWARE_CHESTPLATE)
                && player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.CYBERWARE_LEGGINGS)
                && player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.CYBERWARE_BOOTS);
        if (fullCyberwareArmor) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 25, 1, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 25, 0, true, false, false));
        }

        Long rageEndsAt = OVERLOAD_UNTIL.get(uuid);
        if (rageEndsAt != null && now < rageEndsAt) {
            tickCyberpsychosisRage(player, now);
            if (player.age % 60 == 0) {
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                        SoundCategory.PLAYERS, 0.35f, 0.72f);
            }
            return;
        }
        if (rageEndsAt != null) {
            OVERLOAD_UNTIL.remove(uuid);
            RAGE_NEXT_ATTACK.remove(uuid);
            player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis_recovered"), true);
            sendVisual(player, 11, 18, 82);
        }

        ActiveAbility ability = ACTIVE.get(uuid);
        if (ability != null && now >= ability.expiresAt()) {
            ACTIVE.remove(uuid);
            if (ability.implantType() == SANDEVISTAN) broadcastSandevistanTrail(player, 0);
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
                case MANTIS_ARMS -> tickMantisArms(player);
                case GORILLA_ARMS -> tickGorillaArms(player);
                case KERENZIKOV -> tickKerenzikov(player);
                default -> ACTIVE.remove(uuid);
            }

            float stressPerTick = switch (ability.implantType()) {
                case ACCELERATOR -> 0.035f;
                case OPTICAL_CAMO -> 0.050f;
                case BERSERK -> 0.080f;
                case SANDEVISTAN -> 0.110f;
                case MANTIS_ARMS -> 0.065f;
                case GORILLA_ARMS -> 0.095f;
                case KERENZIKOV -> 0.060f;
                default -> 0.0f;
            };
            addStress(player, stressPerTick);
            if (isOverloaded(uuid, now)) return;
        } else {
            float stress = Math.max(0.0f, STRESS.getOrDefault(uuid, 0.0f) - 0.045f);
            STRESS.put(uuid, stress);
            updateStressTier(player, stress);
        }
    }

    private static void tickAccelerator(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 2, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 8, 1, true, false, false));
        player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(14.0),
                entity -> entity != player && !(entity instanceof PlayerEntity) && entity.isAlive()
        ).forEach(entity -> entity.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOWNESS, 7, 3, true, false, false)
        ));

        if (player.age % 3 == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();
            double trailX = player.getX() - player.getVelocity().x * 1.5;
            double trailZ = player.getZ() - player.getVelocity().z * 1.5;
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    trailX, player.getY() + 0.85, trailZ,
                    7, 0.24, 0.38, 0.24, 0.035);
            world.spawnParticles(ParticleTypes.CLOUD,
                    trailX, player.getY() + 0.15, trailZ,
                    2, 0.18, 0.08, 0.18, 0.01);
        }
        if (player.age % 20 == 0) {
            sendVisual(player, 1, 6, 22);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS, 0.18f, 1.9f);
        }
    }

    private static void tickOpticalCamo(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 8, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 1, true, false, false));

        if (player.age % 6 == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    2, 0.22, 0.42, 0.22, 0.10);
            world.spawnParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.8, player.getZ(),
                    1, 0.25, 0.35, 0.25, 0.006);
        }
        if (player.age % 20 == 0) {
            sendVisual(player, 2, 6, 18);
        }
    }

    private static void tickBerserk(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 8, 2, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 8, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 8, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 0, true, false, false));

        if (player.age % 3 == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 0.9, player.getZ(),
                    3, 0.32, 0.48, 0.32, 0.015);
            world.spawnParticles(ParticleTypes.CRIT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    4, 0.28, 0.42, 0.28, 0.12);
        }
        if (player.age % 20 == 0) {
            sendVisual(player, 3, 6, 20);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
                    SoundCategory.PLAYERS, 0.18f, 0.72f);
        }
    }

    private static void tickSandevistan(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 2, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 8, 1, true, false, false));
        if (player.age % 6 == 0) broadcastSandevistanTrail(player, 14);

        if (player.age % 2 == 0) {
            double trailX = player.getX() - player.getVelocity().x * 2.4;
            double trailZ = player.getZ() - player.getVelocity().z * 2.4;
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    trailX, player.getY() + 0.85, trailZ,
                    6, 0.12, 0.38, 0.12, 0.035);
            world.spawnParticles(ParticleTypes.END_ROD,
                    trailX, player.getY() + 0.85, trailZ,
                    2, 0.08, 0.25, 0.08, 0.008);
            world.spawnParticles(ParticleTypes.CLOUD,
                    trailX, player.getY() + 0.12, trailZ,
                    1, 0.1, 0.05, 0.1, 0.005);
        }
        if (player.age % 20 == 0) {
            sendVisual(player, 4, 7, 26);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                    SoundCategory.PLAYERS, 0.12f, 2.0f);
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
                    5, 0.22, 0.45, 0.22, 0.10);
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.8, player.getZ(),
                    3, 0.22, 0.35, 0.22, 0.02);
        }
        if (player.age % 20 == 0) {
            sendVisual(player, 5, 6, 20);
        }
    }

    private static void tickGorillaArms(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 10, 3, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 10, 1, true, false, false));

        if (player.age % 3 == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();
            Vec3d look = player.getRotationVec(1.0f);
            world.spawnParticles(ParticleTypes.CRIT,
                    player.getX() + look.x * 0.7, player.getY() + 0.95, player.getZ() + look.z * 0.7,
                    5, 0.22, 0.32, 0.22, 0.12);
            world.spawnParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.15, player.getZ(),
                    2, 0.28, 0.06, 0.28, 0.01);
        }
        if (player.age % 20 == 0) sendVisual(player, 12, 6, 22);
    }

    private static void tickKerenzikov(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 8, 3, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 8, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 8, 0, true, false, false));

        if (player.age % 2 == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();
            double x = player.getX() - player.getVelocity().x * 1.7;
            double z = player.getZ() - player.getVelocity().z * 1.7;
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, player.getY() + 0.8, z,
                    5, 0.12, 0.32, 0.12, 0.025);
            world.spawnParticles(ParticleTypes.CLOUD, x, player.getY() + 0.12, z,
                    2, 0.16, 0.06, 0.16, 0.015);
        }
        if (player.age % 15 == 0) sendVisual(player, 13, 5, 24);
    }

    private record ActiveAbility(int implantType, long expiresAt) {
    }
}
