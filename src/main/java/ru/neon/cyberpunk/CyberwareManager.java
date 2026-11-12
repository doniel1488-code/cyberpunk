package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CyberwareManager {
    public static final int NEURAL_SLOT = 0;
    public static final int OPTICS_SLOT = 1;
    public static final int OPERATING_SYSTEM_SLOT = 2;
    public static final int ARMS_SLOT = 3;
    public static final int INTEGUMENTARY_SLOT = 4;
    public static final int CIRCULATORY_SLOT = 5;
    public static final int LEGS_SLOT = 6;
    public static final int SLOT_COUNT = 7;

    private static final int ACCELERATOR = 0;
    private static final int OPTICAL_CAMO = 1;
    private static final int BERSERK = 2;
    private static final int SANDEVISTAN = 3;
    private static final int MANTIS_ARMS = 4;
    private static final int GORILLA_ARMS = 5;
    private static final int KERENZIKOV = 6;
    private static final int SUBDERMAL_ARMOR = 7;
    private static final int BLOOD_PUMP = 8;
    private static final int PROJECTILE_LAUNCHER = 9;
    private static final int REINFORCED_TENDONS = 10;
    private static final int BIOMONITOR = 11;
    private static final int SMART_LINK = 12;
    private static final int MONOWIRE = 13;
    private static final int IMPLANT_COUNT = 14;

    private static final double SANDEVISTAN_RADIUS = 64.0;
    private static final Map<UUID, Map<Integer, ActiveAbility>> ACTIVE = new HashMap<>();
    private static final Map<UUID, Map<Integer, Long>> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Float> STRESS = new HashMap<>();
    private static final Map<UUID, Integer> STRESS_TIER = new HashMap<>();
    private static final Map<UUID, Long> OVERLOAD_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> RAGE_NEXT_ATTACK = new HashMap<>();
    private static final Map<UUID, Integer> RAGE_TARGETS = new HashMap<>();
    private static final Map<UUID, Long> BIOMONITOR_READY = new HashMap<>();
    private static final Map<UUID, Long> BLOOD_REGEN_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> BLOOD_REGEN_NEXT = new HashMap<>();
    private static final Map<LivingEntity, Integer> SLOWED_UNTIL = new HashMap<>();
    private static final Map<LivingEntity, BleedState> BLEEDING = new HashMap<>();

    private static final Identifier SPEED_MOD = CyberpunkMod.id("cyberware_speed");
    private static final Identifier ATTACK_MOD = CyberpunkMod.id("cyberware_attack");
    private static final Identifier ATTACK_SPEED_MOD = CyberpunkMod.id("cyberware_attack_speed");
    private static final Identifier ARMOR_MOD = CyberpunkMod.id("cyberware_armor");
    private static final Identifier TOUGHNESS_MOD = CyberpunkMod.id("cyberware_toughness");
    private static final Identifier KNOCKBACK_MOD = CyberpunkMod.id("cyberware_knockback");
    private static final Identifier MINING_MOD = CyberpunkMod.id("cyberware_mining");
    private static final Identifier STEP_MOD = CyberpunkMod.id("cyberware_step");
    private static final Identifier JUMP_MOD = CyberpunkMod.id("cyberware_jump");
    private static final Identifier NEURAL_SLOW_MOD = CyberpunkMod.id("neural_slow");

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
            case PROJECTILE_LAUNCHER -> "cyberpunk.implant.projectile_launcher";
            case REINFORCED_TENDONS -> "cyberpunk.implant.reinforced_tendons";
            case BIOMONITOR -> "cyberpunk.implant.biomonitor";
            case SMART_LINK -> "cyberpunk.implant.smart_link";
            case MONOWIRE -> "cyberpunk.implant.monowire";
            default -> throw new IllegalArgumentException("Unknown implant type: " + implantType);
        };
    }

    public static int getSlotForImplant(int implantType) {
        return switch (implantType) {
            case ACCELERATOR, KERENZIKOV, SMART_LINK -> NEURAL_SLOT;
            case OPTICAL_CAMO -> OPTICS_SLOT;
            case BERSERK, SANDEVISTAN -> OPERATING_SYSTEM_SLOT;
            case MANTIS_ARMS, GORILLA_ARMS, PROJECTILE_LAUNCHER, MONOWIRE -> ARMS_SLOT;
            case SUBDERMAL_ARMOR -> INTEGUMENTARY_SLOT;
            case BLOOD_PUMP, BIOMONITOR -> CIRCULATORY_SLOT;
            case REINFORCED_TENDONS -> LEGS_SLOT;
            default -> -1;
        };
    }

    public static String getSlotTranslationKey(int slot) {
        return switch (slot) {
            case NEURAL_SLOT -> "gui.cyberpunk.slot.neural";
            case OPTICS_SLOT -> "gui.cyberpunk.slot.optics";
            case OPERATING_SYSTEM_SLOT -> "gui.cyberpunk.slot.operating_system";
            case ARMS_SLOT -> "gui.cyberpunk.slot.arms";
            case INTEGUMENTARY_SLOT -> "gui.cyberpunk.slot.integumentary";
            case CIRCULATORY_SLOT -> "gui.cyberpunk.slot.circulatory";
            case LEGS_SLOT -> "gui.cyberpunk.slot.legs";
            default -> "gui.cyberpunk.slot.unknown";
        };
    }

    public static int getInstalledInSlot(ServerPlayerEntity player, int slot) {
        for (int type = 0; type < IMPLANT_COUNT; type++) {
            if (getSlotForImplant(type) == slot
                    && player.getCommandTags().contains(getInstallTag(type))) {
                return type;
            }
        }
        return -1;
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
            case BLOOD_PUMP -> "key.cyberpunk.blood_pump";
            case PROJECTILE_LAUNCHER -> "key.cyberpunk.projectile_launcher";
            case REINFORCED_TENDONS -> "key.cyberpunk.reinforced_tendons";
            case MONOWIRE -> "key.cyberpunk.monowire";
            default -> "message.cyberpunk.passive";
        };
    }

    public static String getImplantNameKey(int implantType) {
        return switch (implantType) {
            case ACCELERATOR -> "item.cyberpunk.neural_accelerator";
            case OPTICAL_CAMO -> "item.cyberpunk.optical_camo";
            case BERSERK -> "item.cyberpunk.berserk_module";
            case SANDEVISTAN -> "item.cyberpunk.sandevistan";
            case MANTIS_ARMS -> "item.cyberpunk.mantis_arms";
            case GORILLA_ARMS -> "item.cyberpunk.gorilla_arms";
            case KERENZIKOV -> "item.cyberpunk.kerenzikov";
            case SUBDERMAL_ARMOR -> "item.cyberpunk.subdermal_armor";
            case BLOOD_PUMP -> "item.cyberpunk.blood_pump";
            case PROJECTILE_LAUNCHER -> "item.cyberpunk.projectile_launcher";
            case REINFORCED_TENDONS -> "item.cyberpunk.reinforced_tendons";
            case BIOMONITOR -> "item.cyberpunk.biomonitor";
            case SMART_LINK -> "item.cyberpunk.smart_link";
            case MONOWIRE -> "item.cyberpunk.monowire";
            default -> "item.cyberpunk.neural_accelerator";
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
            case "projectile_launcher" -> PROJECTILE_LAUNCHER;
            case "reinforced_tendons" -> REINFORCED_TENDONS;
            case "biomonitor" -> BIOMONITOR;
            case "smart_link" -> SMART_LINK;
            case "monowire" -> MONOWIRE;
            default -> -1;
        };
    }

    private static Item getImplantItem(int type) {
        return switch (type) {
            case ACCELERATOR -> ModItems.NEURAL_ACCELERATOR;
            case OPTICAL_CAMO -> ModItems.OPTICAL_CAMO;
            case BERSERK -> ModItems.BERSERK_MODULE;
            case SANDEVISTAN -> ModItems.SANDEVISTAN;
            case MANTIS_ARMS -> ModItems.MANTIS_ARMS;
            case GORILLA_ARMS -> ModItems.GORILLA_ARMS;
            case KERENZIKOV -> ModItems.KERENZIKOV;
            case SUBDERMAL_ARMOR -> ModItems.SUBDERMAL_ARMOR;
            case BLOOD_PUMP -> ModItems.BLOOD_PUMP;
            case PROJECTILE_LAUNCHER -> ModItems.PROJECTILE_LAUNCHER;
            case REINFORCED_TENDONS -> ModItems.REINFORCED_TENDONS;
            case BIOMONITOR -> ModItems.BIOMONITOR;
            case SMART_LINK -> ModItems.SMART_LINK;
            case MONOWIRE -> ModItems.MONOWIRE;
            default -> throw new IllegalArgumentException("Unknown implant type: " + type);
        };
    }

    public static boolean install(ServerPlayerEntity player, int implantType, ItemStack chip) {
        if (implantType < 0 || implantType >= IMPLANT_COUNT || chip == null
                || !chip.isOf(getImplantItem(implantType))) {
            player.sendMessage(Text.translatable("message.cyberpunk.no_implant_chip"), true);
            return false;
        }
        String tag = getInstallTag(implantType);
        if (player.getCommandTags().contains(tag)) {
            player.sendMessage(Text.translatable("message.cyberpunk.implant_already_installed"), true);
            return false;
        }
        int occupied = getInstalledInSlot(player, getSlotForImplant(implantType));
        if (occupied >= 0) {
            player.sendMessage(Text.translatable("message.cyberpunk.slot_occupied",
                    Text.translatable(getImplantNameKey(occupied)),
                    Text.translatable(getSlotTranslationKey(getSlotForImplant(implantType)))), true);
            return false;
        }
        chip.decrement(1);
        player.addCommandTag(tag);
        player.sendMessage(Text.translatable("message.cyberpunk.implant_installed",
                Text.translatable(getImplantNameKey(implantType)),
                Text.translatable(getActivationKeyTranslation(implantType))), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
                SoundCategory.PLAYERS, 0.8f, 1.2f);
        sendCyberwareState(player);
        return true;
    }

    private static boolean installFromInventory(ServerPlayerEntity player, int implantType) {
        if (implantType < 0 || implantType >= IMPLANT_COUNT) return false;
        Item chip = getImplantItem(implantType);
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(chip)) return install(player, implantType, stack);
        }
        player.sendMessage(Text.translatable("message.cyberpunk.no_implant_chip"), true);
        return false;
    }

    public static void uninstall(ServerPlayerEntity player, int implantType) {
        if (implantType < 0 || implantType >= IMPLANT_COUNT) return;
        UUID uuid = player.getUuid();
        if (!player.getCommandTags().remove(getInstallTag(implantType))) {
            player.sendMessage(Text.translatable("message.cyberpunk.not_installed"), true);
            return;
        }
        Map<Integer, ActiveAbility> active = ACTIVE.get(uuid);
        if (active != null && active.remove(implantType) != null) {
            finishAbility(player, implantType);
            if (active.isEmpty()) ACTIVE.remove(uuid);
        }
        Map<Integer, Long> cooldowns = COOLDOWNS.get(uuid);
        if (cooldowns != null) {
            cooldowns.remove(implantType);
            if (cooldowns.isEmpty()) COOLDOWNS.remove(uuid);
        }
        player.sendMessage(Text.translatable("message.cyberpunk.implant_uninstalled"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.PLAYERS, 0.65f, 1.15f);
        sendCyberwareState(player);
    }

    private static void uninstallSlot(ServerPlayerEntity player, int slot) {
        int type = getInstalledInSlot(player, slot);
        if (type < 0) {
            player.sendMessage(Text.translatable("message.cyberpunk.slot_empty"), true);
            return;
        }
        uninstall(player, type);
    }

    public static void handleScreenAction(ServerPlayerEntity player, CyberwareActionPayload payload) {
        switch (payload.action()) {
            case 0 -> sendCyberwareState(player);
            case 1 -> installFromInventory(player, payload.implantType());
            case 2 -> uninstallSlot(player, payload.implantType());
            default -> { }
        }
        sendCyberwareState(player);
    }

    private static void sendCyberwareState(ServerPlayerEntity player) {
        int installedMask = 0;
        for (int type = 0; type < IMPLANT_COUNT; type++) {
            if (player.getCommandTags().contains(getInstallTag(type))) installedMask |= (1 << type);
        }
        long now = player.getWorld().getTime();
        int psychosisTicks = (int) Math.max(0L,
                OVERLOAD_UNTIL.getOrDefault(player.getUuid(), 0L) - now);
        int neuralLoad = Math.round(STRESS.getOrDefault(player.getUuid(), 0.0f));
        ServerPlayNetworking.send(player, new CyberwareStatePayload(installedMask, neuralLoad, psychosisTicks));
    }

    public static void sendStatus(ServerPlayerEntity player) {
        float load = STRESS.getOrDefault(player.getUuid(), 0.0f);
        player.sendMessage(Text.translatable("message.cyberpunk.status_header", Math.round(load)), false);
        for (int type = 0; type < IMPLANT_COUNT; type++) {
            boolean installed = player.getCommandTags().contains(getInstallTag(type));
            Text line = Text.literal("• ").append(Text.translatable(getImplantNameKey(type)))
                    .append(Text.literal(" — "))
                    .append(Text.translatable(installed
                            ? "message.cyberpunk.status_installed" : "message.cyberpunk.status_missing"))
                    .append(Text.literal(" | "))
                    .append(Text.translatable(getActivationKeyTranslation(type)));
            player.sendMessage(line, false);
        }
    }

    public static void activate(ServerPlayerEntity player, int implantType) {
        if (implantType < 0 || implantType >= IMPLANT_COUNT) return;
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        if (isOverloaded(uuid, now)) {
            player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis_locked"), true);
            return;
        }
        if (!player.getCommandTags().contains(getInstallTag(implantType))) {
            player.sendMessage(Text.translatable("message.cyberpunk.not_installed"), true);
            return;
        }
        if (implantType == SUBDERMAL_ARMOR || implantType == BIOMONITOR || implantType == SMART_LINK) {
            player.sendMessage(Text.translatable("message.cyberpunk.subdermal_is_passive"), true);
            return;
        }
        Map<Integer, Long> cooldowns = COOLDOWNS.computeIfAbsent(uuid, ignored -> new HashMap<>());
        long readyAt = cooldowns.getOrDefault(implantType, 0L);
        if (now < readyAt) {
            player.sendMessage(Text.translatable("message.cyberpunk.cooldown",
                    Math.max(1L, (readyAt - now + 19L) / 20L)), true);
            return;
        }

        if (implantType == BLOOD_PUMP) {
            activateBloodPump(player, now, cooldowns);
            return;
        }
        if (implantType == PROJECTILE_LAUNCHER) {
            addStress(player, 20.0f);
            if (isOverloaded(uuid, now)) return;
            cooldowns.put(implantType, now + 420L);
            fireProjectileLauncher(player);
            sendVisual(player, 15, 16, 135);
            player.sendMessage(Text.translatable("message.cyberpunk.launcher_fired"), true);
            return;
        }
        if (implantType == MONOWIRE) {
            addStress(player, 18.0f);
            if (isOverloaded(uuid, now)) return;
            cooldowns.put(implantType, now + 360L);
            fireMonowire(player);
            sendVisual(player, 16, 14, 122);
            player.sendMessage(Text.translatable("message.cyberpunk.monowire_slashed"), true);
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
            case REINFORCED_TENDONS -> { duration = 50; cooldown = 300; }
            default -> { return; }
        }

        addStress(player, activationStress(implantType));
        if (isOverloaded(uuid, now)) return;
        ACTIVE.computeIfAbsent(uuid, ignored -> new HashMap<>())
                .put(implantType, new ActiveAbility(implantType, now + duration));
        cooldowns.put(implantType, now + cooldown);
        activationBurst(player, implantType);
        if (implantType == OPTICAL_CAMO) player.setInvisible(true);
        if (implantType == SANDEVISTAN) broadcastSandevistanTrail(player, duration + 20);
        sendVisual(player, visualId(implantType), 12, 112);
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
            case REINFORCED_TENDONS -> 13.0f;
            default -> 0.0f;
        };
    }

    private static int visualId(int implantType) {
        return switch (implantType) {
            case GORILLA_ARMS -> 12;
            case KERENZIKOV -> 13;
            case BLOOD_PUMP -> 14;
            case PROJECTILE_LAUNCHER -> 15;
            case MONOWIRE -> 16;
            default -> implantType + 1;
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
            case REINFORCED_TENDONS -> "message.cyberpunk.tendons_activated";
            default -> "message.cyberpunk.activated";
        };
    }

    private static void activationBurst(ServerPlayerEntity player, int implantType) {
        ServerWorld world = (ServerWorld) player.getWorld();
        double x = player.getX();
        double y = player.getY() + 0.9;
        double z = player.getZ();
        switch (implantType) {
            case ACCELERATOR, SANDEVISTAN -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                        SoundCategory.PLAYERS, 0.85f, 1.75f);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 42, 0.75, 0.65, 0.75, 0.06);
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
            case KERENZIKOV, REINFORCED_TENDONS -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.PLAYERS, 0.75f, 1.85f);
                Vec3d look = player.getRotationVec(1.0f);
                Vec3d horizontal = new Vec3d(look.x, 0.0, look.z);
                if (horizontal.lengthSquared() > 0.001) horizontal = horizontal.normalize();
                double boost = implantType == REINFORCED_TENDONS ? 0.78 : 1.35;
                double lift = implantType == REINFORCED_TENDONS ? 1.08 : 0.22;
                Vec3d old = player.getVelocity();
                player.setVelocity(horizontal.x * boost, Math.max(old.y, lift), horizontal.z * boost);
                player.velocityModified = true;
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 42, 0.75, 0.65, 0.75, 0.07);
                world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 18, 0.5, 0.2, 0.5, 0.035);
            }
        }
    }

    private static void fireProjectileLauncher(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d origin = player.getEyePos();
        Box search = new Box(origin, origin.add(look.multiply(18.0))).expand(1.8);
        LivingEntity target = world.getEntitiesByClass(LivingEntity.class, search,
                entity -> isValidRageTarget(player, entity)
                        && entity.getPos().subtract(origin).normalize().dotProduct(look) > 0.91)
                .stream().min(Comparator.comparingDouble(player::squaredDistanceTo)).orElse(null);

        for (int i = 1; i <= 12; i++) {
            Vec3d point = origin.add(look.multiply(i * 1.35));
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    3, 0.12, 0.12, 0.12, 0.025);
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 0.75f, 1.4f);
        if (target != null) {
            target.damage(world, player.getDamageSources().playerAttack(player), 11.0f);
            target.takeKnockback(1.15, player.getX() - target.getX(), player.getZ() - target.getZ());
            world.spawnParticles(ParticleTypes.EXPLOSION, target.getX(), target.getBodyY(0.55),
                    target.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
            world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55),
                    target.getZ(), 18, 0.3, 0.35, 0.3, 0.14);
        } else {
            world.spawnParticles(ParticleTypes.SMOKE, origin.x + look.x * 5.0,
                    origin.y + look.y * 5.0, origin.z + look.z * 5.0,
                    18, 0.3, 0.3, 0.3, 0.04);
        }
    }

    private static void fireMonowire(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Box area = player.getBoundingBox().expand(5.0, 2.0, 5.0);
        int hits = 0;
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area,
                entity -> isValidRageTarget(player, entity))) {
            Vec3d toTarget = target.getBoundingBox().getCenter().subtract(player.getEyePos());
            double distance = toTarget.length();
            if (distance > 5.0 || distance < 0.01 || toTarget.normalize().dotProduct(look) < 0.35) continue;
            if (target.damage(world, player.getDamageSources().playerAttack(player), 8.0f)) {
                applyBleed(target, 50);
                target.takeKnockback(0.45, player.getX() - target.getX(), player.getZ() - target.getZ());
                world.spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getBodyY(0.55),
                        target.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
                hits++;
            }
        }
        Vec3d arc = player.getPos().add(look.multiply(2.3)).add(0.0, 1.0, 0.0);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, arc.x, arc.y, arc.z,
                36, 1.25, 0.6, 1.25, 0.06);
        world.spawnParticles(ParticleTypes.CRIT, arc.x, arc.y, arc.z,
                24, 1.0, 0.4, 1.0, 0.12);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 1.0f, 1.55f);
        if (hits == 0) world.spawnParticles(ParticleTypes.END_ROD, arc.x, arc.y, arc.z,
                12, 0.7, 0.25, 0.7, 0.025);
    }

    private static void activateBloodPump(ServerPlayerEntity player, long now, Map<Integer, Long> cooldowns) {
        addStress(player, 18.0f);
        if (isOverloaded(player.getUuid(), now)) return;
        cooldowns.put(BLOOD_PUMP, now + 900L);
        player.heal(7.0f);
        player.setAbsorptionAmount(Math.min(6.0f, player.getAbsorptionAmount() + 4.0f));
        BLOOD_REGEN_UNTIL.put(player.getUuid(), now + 120L);
        BLOOD_REGEN_NEXT.put(player.getUuid(), now + 20L);
        ServerWorld world = (ServerWorld) player.getWorld();
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.PLAYERS, 0.8f, 1.35f);
        world.spawnParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(),
                14, 0.5, 0.6, 0.5, 0.04);
        world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0,
                player.getZ(), 28, 0.55, 0.65, 0.55, 0.12);
        sendVisual(player, 14, 16, 135);
        player.sendMessage(Text.translatable("message.cyberpunk.blood_pump_activated"), true);
    }

    private static void sendVisual(ServerPlayerEntity player, int effectId, int duration, int intensity) {
        ServerPlayNetworking.send(player, new CyberpunkVisualPayload(effectId, duration, intensity));
    }

    private static boolean isOverloaded(UUID uuid, long now) {
        return now < OVERLOAD_UNTIL.getOrDefault(uuid, 0L);
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
        float next = MathHelper.clamp(STRESS.getOrDefault(uuid, 0.0f) + amount, 0.0f, 100.0f);
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
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_warning",
                    Math.round(stress)), true);
            sendVisual(player, 8, 16, 62);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS, 0.45f, 0.72f);
        } else if (tier > previous && tier == 2) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_critical"), true);
            sendVisual(player, 9, 24, 118);
        } else if (tier < previous && tier == 0) {
            player.sendMessage(Text.translatable("message.cyberpunk.neural_load_stabilized"), true);
        }
    }

    public static void onAfterDamage(LivingEntity target, DamageSource source, float damageTaken) {
        if (target.getWorld().isClient || damageTaken <= 0.0f) return;

        if (source.getAttacker() instanceof ServerPlayerEntity attacker && target != attacker) {
            Map<Integer, ActiveAbility> active = ACTIVE.get(attacker.getUuid());
            if (active != null && active.containsKey(GORILLA_ARMS)) {
                Vec3d direction = target.getPos().subtract(attacker.getPos());
                Vec3d horizontal = new Vec3d(direction.x, 0.0, direction.z);
                if (horizontal.lengthSquared() > 0.001) {
                    horizontal = horizontal.normalize();
                    target.addVelocity(horizontal.x * 0.95, 0.30, horizontal.z * 0.95);
                }
                if (target.getWorld() instanceof ServerWorld world) {
                    world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55),
                            target.getZ(), 10, 0.28, 0.35, 0.28, 0.13);
                    world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK,
                            SoundCategory.PLAYERS, 0.45f, 1.4f);
                }
            }
            if (active != null && active.containsKey(MANTIS_ARMS)) applyBleed(target, 60);
            if (playerHas(attacker, SMART_LINK)) applyBleed(target, 35);
        }

        if (target instanceof ServerPlayerEntity wearer && playerHas(wearer, SUBDERMAL_ARMOR)) {
            if (wearer.getWorld() instanceof ServerWorld world) {
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, wearer.getX(), wearer.getY() + 0.9,
                        wearer.getZ(), 6, 0.28, 0.42, 0.28, 0.025);
                world.playSound(null, wearer.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                        SoundCategory.PLAYERS, 0.18f, 1.8f);
            }
        }
    }

    private static boolean playerHas(ServerPlayerEntity player, int implantType) {
        return player.getCommandTags().contains(getInstallTag(implantType));
    }

    private static void applyBleed(LivingEntity target, int duration) {
        BLEEDING.put(target, new BleedState(target.age + duration, target.age + 12));
    }

    private static void applySlow(LivingEntity target, int duration) {
        SLOWED_UNTIL.merge(target, target.age + duration, Math::max);
        EntityAttributeInstance attribute = target.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (attribute != null && !attribute.hasModifier(NEURAL_SLOW_MOD)) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(NEURAL_SLOW_MOD, -0.62,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void tickDebuffs() {
        Iterator<Map.Entry<LivingEntity, Integer>> slowIterator = SLOWED_UNTIL.entrySet().iterator();
        while (slowIterator.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = slowIterator.next();
            LivingEntity entity = entry.getKey();
            if (!entity.isAlive() || entity.age >= entry.getValue()) {
                removeModifier(entity, EntityAttributes.MOVEMENT_SPEED, NEURAL_SLOW_MOD);
                slowIterator.remove();
            } else {
                EntityAttributeInstance attribute = entity.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
                if (attribute != null && !attribute.hasModifier(NEURAL_SLOW_MOD)) {
                    attribute.addTemporaryModifier(new EntityAttributeModifier(NEURAL_SLOW_MOD, -0.62,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }

        Iterator<Map.Entry<LivingEntity, BleedState>> bleedIterator = BLEEDING.entrySet().iterator();
        while (bleedIterator.hasNext()) {
            Map.Entry<LivingEntity, BleedState> entry = bleedIterator.next();
            LivingEntity entity = entry.getKey();
            BleedState state = entry.getValue();
            if (!entity.isAlive() || entity.age >= state.endAge()) {
                bleedIterator.remove();
                continue;
            }
            if (entity.age >= state.nextDamageAge() && entity.getWorld() instanceof ServerWorld world) {
                entity.damage(world, entity.getDamageSources().magic(), 1.0f);
                world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(),
                        entity.getBodyY(0.55), entity.getZ(), 2, 0.18, 0.25, 0.18, 0.04);
                entry.setValue(new BleedState(state.endAge(), entity.age + 12));
            }
        }
    }

    private static void startCyberpsychosis(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        if (isOverloaded(uuid, now)) return;
        Map<Integer, ActiveAbility> interrupted = ACTIVE.remove(uuid);
        if (interrupted != null) {
            interrupted.values().forEach(ability -> finishAbility(player, ability.implantType()));
        }
        OVERLOAD_UNTIL.put(uuid, now + 300L);
        STRESS.put(uuid, 30.0f);
        STRESS_TIER.put(uuid, 0);
        RAGE_NEXT_ATTACK.put(uuid, now + 8L);
        RAGE_TARGETS.remove(uuid);
        player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                SoundCategory.PLAYERS, 1.0f, 0.65f);
        if (player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0,
                    player.getZ(), 35, 0.7, 0.8, 0.7, 0.12);
            world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0,
                    player.getZ(), 55, 0.75, 0.9, 0.75, 0.06);
            world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.9,
                    player.getZ(), 22, 0.5, 0.55, 0.5, 0.035);
        }
        sendVisual(player, 10, 300, 185);
    }

    private static void tickCyberpsychosisRage(ServerPlayerEntity player, long now, Stats stats) {
        stats.speed += 0.40;
        stats.attack += 7.0;
        stats.armor += 8.0;
        stats.toughness += 3.0;
        stats.knockback += 0.55;
        ServerWorld world = (ServerWorld) player.getWorld();
        if (player.age % 3 == 0) {
            world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.9,
                    player.getZ(), 4, 0.35, 0.5, 0.35, 0.025);
            world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0,
                    player.getZ(), 2, 0.3, 0.35, 0.3, 0.08);
            world.spawnParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.8,
                    player.getZ(), 2, 0.25, 0.35, 0.25, 0.02);
        }
        UUID uuid = player.getUuid();
        LivingEntity target = selectRageTarget(player);
        if (target == null) {
            player.setSprinting(false);
            return;
        }
        if (player.age % 4 == 0) {
            world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(),
                    target.getBodyY(1.08), target.getZ(), 2, 0.22, 0.16, 0.22, 0.0);
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(),
                    target.getBodyY(0.55), target.getZ(), 4, 0.3, 0.35, 0.3, 0.04);
        }
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double distanceSquared = dx * dx + dz * dz;
        if (distanceSquared > 2.8 * 2.8) {
            Vec3d desired = new Vec3d(dx, 0.0, dz).normalize();
            Vec3d steering = findPursuitDirection(player, desired);
            if (steering.lengthSquared() > 0.001) {
                float yaw = (float) (MathHelper.atan2(-steering.x, steering.z) * 57.295776f);
                player.setYaw(yaw);
                player.setHeadYaw(yaw);
                player.setSprinting(true);
                Vec3d velocity = player.getVelocity();
                double speed = player.isTouchingWater() ? 0.22 : 0.38;
                player.setVelocity(steering.x * speed, velocity.y, steering.z * speed);
                player.velocityModified = true;
                if (player.isOnGround() && target.getY() > player.getY() + 0.9
                        && world.isSpaceEmpty(player, player.getBoundingBox().offset(0.0, 1.0, 0.0))) {
                    player.jump();
                }
            } else {
                player.setSprinting(false);
                if (player.isOnGround()
                        && world.isSpaceEmpty(player, player.getBoundingBox().offset(0.0, 1.0, 0.0))) {
                    player.jump();
                }
            }
            if (player.age % 3 == 0) {
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX() - player.getVelocity().x * 1.2,
                        player.getY() + 0.75,
                        player.getZ() - player.getVelocity().z * 1.2,
                        5, 0.12, 0.25, 0.12, 0.025);
            }
            return;
        }
        player.setSprinting(false);
        applySlow(target, 18);
        if (now < RAGE_NEXT_ATTACK.getOrDefault(uuid, 0L)) return;
        if (target.damage(world, player.getDamageSources().playerAttack(player), 7.5f)) {
            target.takeKnockback(0.75, player.getX() - target.getX(), player.getZ() - target.getZ());
            world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55),
                    target.getZ(), 9, 0.25, 0.35, 0.25, 0.12);
            world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(),
                    target.getBodyY(0.55), target.getZ(), 5, 0.2, 0.3, 0.2, 0.08);
        }
        player.swingHand(Hand.MAIN_HAND);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                SoundCategory.PLAYERS, 0.9f, 0.62f + player.getRandom().nextFloat() * 0.25f);
        sendVisual(player, 10, 300, 185);
        RAGE_NEXT_ATTACK.put(uuid, now + 12L);
    }

    private static LivingEntity selectRageTarget(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        int currentId = RAGE_TARGETS.getOrDefault(uuid, -1);
        var currentEntity = player.getWorld().getEntityById(currentId);
        if (currentEntity instanceof LivingEntity current && isValidRageTarget(player, current)) {
            return current;
        }
        LivingEntity next = player.getWorld().getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(32.0, 12.0, 32.0),
                entity -> isValidRageTarget(player, entity))
                .stream().min(Comparator.comparingDouble(player::squaredDistanceTo)).orElse(null);
        if (next != null) {
            RAGE_TARGETS.put(uuid, next.getId());
            player.sendMessage(Text.translatable("message.cyberpunk.psychosis_target_locked",
                    next.getDisplayName()), true);
        } else {
            RAGE_TARGETS.remove(uuid);
        }
        return next;
    }

    private static boolean isValidRageTarget(ServerPlayerEntity player, LivingEntity entity) {
        if (entity == player || !entity.isAlive() || !entity.isAttackable()
                || !player.getWorld().getRegistryKey().equals(entity.getWorld().getRegistryKey())
                || player.squaredDistanceTo(entity) > 32.0 * 32.0) return false;
        return !(entity instanceof PlayerEntity other) || (!other.isCreative() && !other.isSpectator());
    }

    private static Vec3d findPursuitDirection(ServerPlayerEntity player, Vec3d desired) {
        float[] offsets = {0.0f, 28.0f, -28.0f, 55.0f, -55.0f, 88.0f, -88.0f, 125.0f, -125.0f, 180.0f};
        Vec3d best = Vec3d.ZERO;
        double bestScore = -Double.MAX_VALUE;
        for (float degrees : offsets) {
            Vec3d candidate = desired.rotateY((float) Math.toRadians(degrees));
            Box nextSpace = player.getBoundingBox().offset(candidate.x * 0.72, 0.0, candidate.z * 0.72);
            if (!player.getWorld().isSpaceEmpty(player, nextSpace)) continue;
            double score = candidate.dotProduct(desired) - Math.abs(degrees) * 0.0008;
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    public static boolean isTimeDilated(MobEntity mob) {
        if (mob.getWorld().isClient || !mob.isAlive()) return false;
        for (PlayerEntity player : mob.getWorld().getPlayers()) {
            if (!(player instanceof ServerPlayerEntity owner) || !owner.isAlive()) continue;
            Map<Integer, ActiveAbility> active = ACTIVE.get(owner.getUuid());
            if (active == null) continue;
            if (active.containsKey(SANDEVISTAN)
                    && owner.squaredDistanceTo(mob) <= SANDEVISTAN_RADIUS * SANDEVISTAN_RADIUS) return true;
            if (active.containsKey(KERENZIKOV)
                    && owner.squaredDistanceTo(mob) <= 12.0 * 12.0) return true;
        }
        return false;
    }

    public static void tick(MinecraftServer server) {
        tickDebuffs();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) tickPlayer(player);
    }

    public static void removePlayer(UUID uuid) {
        ACTIVE.remove(uuid);
        COOLDOWNS.remove(uuid);
        STRESS.remove(uuid);
        STRESS_TIER.remove(uuid);
        OVERLOAD_UNTIL.remove(uuid);
        RAGE_NEXT_ATTACK.remove(uuid);
        RAGE_TARGETS.remove(uuid);
        BIOMONITOR_READY.remove(uuid);
        BLOOD_REGEN_UNTIL.remove(uuid);
        BLOOD_REGEN_NEXT.remove(uuid);
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = player.getWorld().getTime();
        Stats stats = new Stats();

        if (playerHas(player, SUBDERMAL_ARMOR)) {
            stats.armor += 4.0;
            stats.toughness += 1.5;
            stats.knockback += 0.18;
        }
        if (playerHas(player, SMART_LINK)) {
            stats.attack += 1.5;
            stats.attackSpeed += 0.18;
        }

        boolean fullCyberwareArmor =
                player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.CYBERWARE_HELMET)
                && player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.CYBERWARE_CHESTPLATE)
                && player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.CYBERWARE_LEGGINGS)
                && player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.CYBERWARE_BOOTS);
        if (fullCyberwareArmor) {
            stats.armor += 3.0;
            stats.toughness += 1.5;
            stats.speed += 0.08;
        }

        boolean fullAssaultArmor =
                player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.TACTICAL_HELMET)
                && player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.TACTICAL_CHESTPLATE)
                && player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.TACTICAL_LEGGINGS)
                && player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.TACTICAL_BOOTS);
        if (fullAssaultArmor) {
            stats.armor += 3.0;
            stats.toughness += 3.0;
            stats.attack += 2.0;
            stats.knockback += 0.12;
        }

        if (playerHas(player, BIOMONITOR) && player.getHealth() <= player.getMaxHealth() * 0.35
                && now >= BIOMONITOR_READY.getOrDefault(uuid, 0L)) {
            player.heal(4.0f);
            BIOMONITOR_READY.put(uuid, now + 600L);
            if (player.getWorld() instanceof ServerWorld world) {
                world.spawnParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0,
                        player.getZ(), 8, 0.35, 0.4, 0.35, 0.02);
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                        SoundCategory.PLAYERS, 0.55f, 1.8f);
            }
            sendVisual(player, 14, 12, 110);
            player.sendMessage(Text.translatable("message.cyberpunk.biomonitor_triggered"), true);
        }

        Long regenEnds = BLOOD_REGEN_UNTIL.get(uuid);
        if (regenEnds != null) {
            if (now >= regenEnds) {
                BLOOD_REGEN_UNTIL.remove(uuid);
                BLOOD_REGEN_NEXT.remove(uuid);
            } else if (now >= BLOOD_REGEN_NEXT.getOrDefault(uuid, 0L)) {
                player.heal(1.0f);
                BLOOD_REGEN_NEXT.put(uuid, now + 20L);
            }
        }

        Long rageEndsAt = OVERLOAD_UNTIL.get(uuid);
        if (rageEndsAt != null && now < rageEndsAt) {
            tickCyberpsychosisRage(player, now, stats);
            applyStats(player, stats);
            if (player.age % 60 == 0) {
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                        SoundCategory.PLAYERS, 0.35f, 0.72f);
            }
            return;
        }
        if (rageEndsAt != null) {
            OVERLOAD_UNTIL.remove(uuid);
            RAGE_NEXT_ATTACK.remove(uuid);
            RAGE_TARGETS.remove(uuid);
            player.setSprinting(false);
            player.sendMessage(Text.translatable("message.cyberpunk.cyberpsychosis_recovered"), true);
            sendVisual(player, 11, 18, 82);
        }

        Map<Integer, ActiveAbility> active = ACTIVE.get(uuid);
        float stressPerTick = 0.0f;
        if (active != null) {
            for (ActiveAbility ability : new java.util.ArrayList<>(active.values())) {
                int type = ability.implantType();
                if (now >= ability.expiresAt()) {
                    active.remove(type);
                    finishAbility(player, type);
                    player.sendMessage(Text.translatable("message.cyberpunk.ability_ended"), true);
                    sendVisual(player, 11, 8, 45);
                    continue;
                }

                switch (type) {
                    case ACCELERATOR -> {
                        stats.speed += 0.42;
                        stats.mining += 2.5;
                        tickAccelerator(player);
                    }
                    case OPTICAL_CAMO -> {
                        stats.speed += 0.12;
                        player.setInvisible(true);
                        tickOpticalCamo(player);
                    }
                    case BERSERK -> {
                        stats.attack += 6.0;
                        stats.armor += 3.0;
                        stats.knockback += 0.25;
                        stats.speed += 0.10;
                        tickBerserk(player);
                    }
                    case SANDEVISTAN -> {
                        stats.speed += 0.36;
                        stats.mining += 1.5;
                        tickSandevistan(player);
                    }
                    case MANTIS_ARMS -> {
                        stats.attack += 4.5;
                        stats.attackSpeed += 0.38;
                        tickMantisArms(player);
                    }
                    case GORILLA_ARMS -> {
                        stats.attack += 7.0;
                        stats.attackSpeed += 0.15;
                        stats.knockback += 1.2;
                        stats.mining += 2.0;
                        tickGorillaArms(player);
                    }
                    case KERENZIKOV -> {
                        stats.speed += 0.44;
                        stats.jump += 0.28;
                        tickKerenzikov(player);
                    }
                    case REINFORCED_TENDONS -> {
                        stats.speed += 0.15;
                        stats.jump += 0.42;
                        stats.step += 0.35;
                        tickTendons(player);
                    }
                    default -> active.remove(type);
                }

                stressPerTick += switch (type) {
                    case ACCELERATOR -> 0.035f;
                    case OPTICAL_CAMO -> 0.050f;
                    case BERSERK -> 0.080f;
                    case SANDEVISTAN -> 0.110f;
                    case MANTIS_ARMS -> 0.065f;
                    case GORILLA_ARMS -> 0.095f;
                    case KERENZIKOV -> 0.060f;
                    case REINFORCED_TENDONS -> 0.045f;
                    default -> 0.0f;
                };
            }
            if (active.isEmpty()) ACTIVE.remove(uuid);
        }

        if (stressPerTick > 0.0f) {
            addStress(player, stressPerTick);
            if (isOverloaded(uuid, now)) {
                tickCyberpsychosisRage(player, now, stats);
                applyStats(player, stats);
                return;
            }
        } else {
            float stress = Math.max(0.0f, STRESS.getOrDefault(uuid, 0.0f) - 0.045f);
            STRESS.put(uuid, stress);
            updateStressTier(player, stress);
        }
        applyStats(player, stats);
    }

    private static void applyStats(ServerPlayerEntity player, Stats stats) {
        removeModifier(player, EntityAttributes.MOVEMENT_SPEED, SPEED_MOD);
        removeModifier(player, EntityAttributes.ATTACK_DAMAGE, ATTACK_MOD);
        removeModifier(player, EntityAttributes.ATTACK_SPEED, ATTACK_SPEED_MOD);
        removeModifier(player, EntityAttributes.ARMOR, ARMOR_MOD);
        removeModifier(player, EntityAttributes.ARMOR_TOUGHNESS, TOUGHNESS_MOD);
        removeModifier(player, EntityAttributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MOD);
        removeModifier(player, EntityAttributes.MINING_EFFICIENCY, MINING_MOD);
        removeModifier(player, EntityAttributes.STEP_HEIGHT, STEP_MOD);
        removeModifier(player, EntityAttributes.JUMP_STRENGTH, JUMP_MOD);
        setModifier(player, EntityAttributes.MOVEMENT_SPEED, SPEED_MOD, stats.speed,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(player, EntityAttributes.ATTACK_DAMAGE, ATTACK_MOD, stats.attack,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.ATTACK_SPEED, ATTACK_SPEED_MOD, stats.attackSpeed,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.ARMOR, ARMOR_MOD, stats.armor,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.ARMOR_TOUGHNESS, TOUGHNESS_MOD, stats.toughness,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MOD, stats.knockback,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.MINING_EFFICIENCY, MINING_MOD, stats.mining,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.STEP_HEIGHT, STEP_MOD, stats.step,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(player, EntityAttributes.JUMP_STRENGTH, JUMP_MOD, stats.jump,
                EntityAttributeModifier.Operation.ADD_VALUE);
    }

    private static void setModifier(LivingEntity entity, RegistryEntry<EntityAttribute> attribute,
                                    Identifier id, double value, EntityAttributeModifier.Operation operation) {
        if (Math.abs(value) < 0.0001) return;
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
        }
    }

    private static void removeModifier(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    private static void finishAbility(ServerPlayerEntity player, int implantType) {
        if (implantType == OPTICAL_CAMO) player.setInvisible(false);
        if (implantType == SANDEVISTAN) broadcastSandevistanTrail(player, 0);
        if (implantType == OPTICAL_CAMO && player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.9,
                    player.getZ(), 20, 0.35, 0.55, 0.35, 0.10);
        }
    }

    private static void tickAccelerator(ServerPlayerEntity player) {
        player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(14.0),
                entity -> entity != player && !(entity instanceof PlayerEntity) && entity.isAlive())
                .forEach(entity -> applySlow(entity, 8));
        if (player.age % 3 == 0 && player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX() - player.getVelocity().x * 1.5, player.getY() + 0.85,
                    player.getZ() - player.getVelocity().z * 1.5, 7, 0.24, 0.38, 0.24, 0.035);
            world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.15,
                    player.getZ(), 2, 0.18, 0.08, 0.18, 0.01);
        }
        if (player.age % 20 == 0) sendVisual(player, 1, 6, 22);
    }

    private static void tickOpticalCamo(ServerPlayerEntity player) {
        if (player.age % 6 == 0 && player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.9,
                    player.getZ(), 1, 0.22, 0.42, 0.22, 0.10);
        }
        if (player.age % 20 == 0) sendVisual(player, 2, 6, 18);
    }

    private static void tickBerserk(ServerPlayerEntity player) {
        if (player.age % 3 == 0 && player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.9,
                    player.getZ(), 3, 0.32, 0.48, 0.32, 0.015);
            world.spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0,
                    player.getZ(), 4, 0.28, 0.42, 0.28, 0.12);
        }
        if (player.age % 20 == 0) sendVisual(player, 3, 6, 20);
    }

    private static void tickSandevistan(ServerPlayerEntity player) {
        if (player.age % 6 == 0) broadcastSandevistanTrail(player, 14);
        if (player.age % 2 == 0 && player.getWorld() instanceof ServerWorld world) {
            double x = player.getX() - player.getVelocity().x * 2.4;
            double z = player.getZ() - player.getVelocity().z * 2.4;
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, player.getY() + 0.85,
                    z, 6, 0.12, 0.38, 0.12, 0.035);
            world.spawnParticles(ParticleTypes.END_ROD, x, player.getY() + 0.85,
                    z, 2, 0.08, 0.25, 0.08, 0.008);
        }
        if (player.age % 20 == 0) sendVisual(player, 4, 7, 26);
    }

    private static void tickMantisArms(ServerPlayerEntity player) {
        if (player.age % 3 == 0 && player.getWorld() instanceof ServerWorld world) {
            Vec3d look = player.getRotationVec(1.0f);
            world.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX() + look.x, player.getY() + 1.0, player.getZ() + look.z,
                    2, 0.25, 0.35, 0.25, 0.0);
            world.spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.8,
                    player.getZ(), 5, 0.22, 0.45, 0.22, 0.10);
        }
        if (player.age % 20 == 0) sendVisual(player, 5, 6, 20);
    }

    private static void tickGorillaArms(ServerPlayerEntity player) {
        if (player.age % 3 == 0 && player.getWorld() instanceof ServerWorld world) {
            Vec3d look = player.getRotationVec(1.0f);
            world.spawnParticles(ParticleTypes.CRIT,
                    player.getX() + look.x * 0.7, player.getY() + 0.95,
                    player.getZ() + look.z * 0.7, 5, 0.22, 0.32, 0.22, 0.12);
            world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.15,
                    player.getZ(), 2, 0.28, 0.06, 0.28, 0.01);
        }
        if (player.age % 20 == 0) sendVisual(player, 12, 6, 22);
    }

    private static void tickKerenzikov(ServerPlayerEntity player) {
        if (player.age % 2 == 0 && player.getWorld() instanceof ServerWorld world) {
            double x = player.getX() - player.getVelocity().x * 1.7;
            double z = player.getZ() - player.getVelocity().z * 1.7;
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, player.getY() + 0.8,
                    z, 5, 0.12, 0.32, 0.12, 0.025);
        }
        if (player.age % 15 == 0) sendVisual(player, 13, 5, 24);
    }

    private static void tickTendons(ServerPlayerEntity player) {
        if (player.age % 4 == 0 && player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(),
                    player.getY() + 0.15, player.getZ(), 5, 0.28, 0.12, 0.28, 0.025);
        }
        if (player.age % 20 == 0) sendVisual(player, 13, 5, 24);
    }

    private record ActiveAbility(int implantType, long expiresAt) { }
    private record BleedState(int endAge, int nextDamageAge) { }

    private static final class Stats {
        private double speed;
        private double attack;
        private double attackSpeed;
        private double armor;
        private double toughness;
        private double knockback;
        private double mining;
        private double step;
        private double jump;
    }
}
