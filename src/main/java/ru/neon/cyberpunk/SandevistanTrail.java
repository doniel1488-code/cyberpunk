package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SandevistanTrail {
    private static final int MAX_SAMPLES = 10;
    private static final int MAX_GHOSTS = 7;
    private static final Map<Integer, Integer> ACTIVE_TICKS = new HashMap<>();
    private static final Map<Integer, Integer> FADE_TICKS = new HashMap<>();
    private static final Map<Integer, Deque<Pose>> HISTORY = new HashMap<>();
    private static ClientWorld trackedWorld;

    private SandevistanTrail() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(SandevistanTrail::render);
    }

    public static void accept(SandevistanTrailPayload payload) {
        int entityId = payload.entityId();
        if (payload.durationTicks() <= 0) {
            ACTIVE_TICKS.remove(entityId);
            if (HISTORY.containsKey(entityId)) FADE_TICKS.put(entityId, 7);
        } else {
            ACTIVE_TICKS.put(entityId, payload.durationTicks());
            FADE_TICKS.remove(entityId);
        }
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null) {
            clear();
            return;
        }
        if (trackedWorld != client.world) {
            clear();
            trackedWorld = client.world;
        }

        Iterator<Map.Entry<Integer, Integer>> activeIterator = ACTIVE_TICKS.entrySet().iterator();
        while (activeIterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = activeIterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                int id = entry.getKey();
                activeIterator.remove();
                if (HISTORY.containsKey(id)) FADE_TICKS.put(id, 7);
                continue;
            }
            entry.setValue(remaining);

            if (client.world.getEntityById(entry.getKey()) instanceof AbstractClientPlayerEntity player
                    && player.isAlive()) {
                capture(player);
            }
        }

        Iterator<Map.Entry<Integer, Integer>> fadeIterator = FADE_TICKS.entrySet().iterator();
        while (fadeIterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = fadeIterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0 || client.world.getEntityById(entry.getKey()) == null) {
                HISTORY.remove(entry.getKey());
                fadeIterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    private static void capture(AbstractClientPlayerEntity player) {
        Deque<Pose> history = HISTORY.computeIfAbsent(player.getId(), ignored -> new ArrayDeque<>());
        Pose pose = new Pose(player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getBodyYaw(), player.getHeadYaw(), player.getPitch());

        Pose last = history.peekFirst();
        if (last != null) {
            double dx = pose.x() - last.x();
            double dy = pose.y() - last.y();
            double dz = pose.z() - last.z();
            float yawDelta = Math.abs(wrapDegrees(pose.yaw() - last.yaw()));
            if (dx * dx + dy * dy + dz * dz < 0.0025 && yawDelta < 2.0f) return;
        }

        history.addFirst(pose);
        while (history.size() > MAX_SAMPLES) history.removeLast();
    }

    private static float wrapDegrees(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || context.matrixStack() == null || HISTORY.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();
        float tickProgress = context.tickCounter().getTickProgress(true);
        OutlineVertexConsumerProvider outlines = client.getBufferBuilders().getOutlineVertexConsumers();
        int drawn = 0;

        for (Map.Entry<Integer, Deque<Pose>> entry : HISTORY.entrySet()) {
            if (!ACTIVE_TICKS.containsKey(entry.getKey()) && !FADE_TICKS.containsKey(entry.getKey())) continue;
            if (!(client.world.getEntityById(entry.getKey()) instanceof AbstractClientPlayerEntity player)
                    || !player.isAlive()) continue;

            List<Pose> samples = new ArrayList<>(entry.getValue());
            if (samples.size() < 2) continue;
            float fadeFactor = ACTIVE_TICKS.containsKey(entry.getKey())
                    ? 1.0f : FADE_TICKS.getOrDefault(entry.getKey(), 0) / 7.0f;

            int sampleIndex = 0;
            for (Pose pose : samples) {
                if (sampleIndex++ == 0) continue; // Vanilla already rendered the current player.
                if (drawn >= 28) break;

                float ageFade = 1.0f - (sampleIndex - 1) / (float) MAX_GHOSTS;
                int alpha = Math.max(18, Math.min(150, Math.round(135.0f * ageFade * fadeFactor)));
                outlines.setColor(22, 225, 255, alpha);

                float oldYaw = player.getYaw();
                float oldPitch = player.getPitch();
                float oldBodyYaw = player.getBodyYaw();
                float oldHeadYaw = player.getHeadYaw();

                matrices.push();
                try {
                    player.setYaw(pose.yaw());
                    player.setBodyYaw(pose.bodyYaw());
                    player.setHeadYaw(pose.headYaw());
                    player.setPitch(pose.pitch());

                    client.getEntityRenderDispatcher().render(
                            player,
                            pose.x() - camera.x,
                            pose.y() - camera.y,
                            pose.z() - camera.z,
                            tickProgress,
                            matrices,
                            outlines,
                            client.getEntityRenderDispatcher().getLight(player, tickProgress)
                    );
                } finally {
                    player.setYaw(oldYaw);
                    player.setBodyYaw(oldBodyYaw);
                    player.setHeadYaw(oldHeadYaw);
                    player.setPitch(oldPitch);
                    matrices.pop();
                }
                drawn++;
                if (sampleIndex > MAX_GHOSTS) break;
            }
        }

        if (drawn > 0) outlines.draw();
    }

    private static void clear() {
        ACTIVE_TICKS.clear();
        FADE_TICKS.clear();
        HISTORY.clear();
        trackedWorld = null;
    }

    private record Pose(double x, double y, double z, float yaw, float bodyYaw, float headYaw, float pitch) {
    }
}
