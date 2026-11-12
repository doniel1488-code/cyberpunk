package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CyberpunkVisuals {
    private static int effectId;
    private static int ticksLeft;
    private static int initialTicks;
    private static int intensity;
    private static int pulsePhase;

    private CyberpunkVisuals() {
    }

    public static void trigger(CyberpunkVisualPayload payload) {
        effectId = payload.effectId();
        ticksLeft = Math.max(1, payload.duration());
        initialTicks = ticksLeft;
        intensity = Math.max(0, Math.min(180, payload.intensity()));
        pulsePhase = 0;
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
        // HUD can render multiple times between game ticks; timers must not depend on FPS.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksLeft > 0) {
                ticksLeft--;
                pulsePhase++;
            }
        });
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || ticksLeft <= 0) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        float fadeWindow = Math.max(4.0f, initialTicks * 0.45f);
        float fade = Math.min(1.0f, ticksLeft / fadeWindow);
        int alpha = Math.max(8, Math.min(90, Math.round(intensity * fade * 0.48f)));
        int color = switch (effectId) {
            case 1, 4, 13, 17 -> 0x0029E9F4;
            case 2 -> 0x0068F6FF;
            case 3, 9, 10 -> 0x00FF3155;
            case 5 -> 0x00B8FF6B;
            case 12 -> 0x00FFB34D;
            case 14 -> 0x00FF597D;
            case 15 -> 0x00FF8C42;
            case 16 -> 0x004EDBFF;
            default -> 0x00D6F9FF;
        };
        int edge = (alpha << 24) | color;
        int edgeSize = effectId == 10 ? 5 : 2;
        context.fill(0, 0, width, edgeSize, edge);
        context.fill(0, height - edgeSize, width, height, edge);
        context.fill(0, 0, edgeSize, height, edge);
        context.fill(width - edgeSize, 0, width, height, edge);

        int pulse = pulsePhase % 20;
        int bright = Math.max(18, Math.min(150, Math.round(intensity * fade)));
        int accent = (bright << 24) | color;
        int cx = width / 2, cy = height / 2;

        switch (effectId) {
            case 1 -> { // Neural accelerator: velocity streaks on both sides.
                for (int i = 0; i < 12; i++) {
                    int y = Math.floorMod(pulsePhase * 9 + i * 53, Math.max(1, height));
                    int length = 18 + (i % 4) * 11;
                    int x = i % 2 == 0 ? 5 : Math.max(5, width - length - 5);
                    context.fill(x, y, x + length, y + 1, accent);
                }
            }
            case 2 -> { // Optical camouflage: broken holographic corner brackets.
                int c = (Math.min(110, bright) << 24) | 0x0068F6FF;
                int inset = 12 + pulse % 5;
                int len = 42;
                context.fill(inset, inset, inset + len, inset + 2, c);
                context.fill(inset, inset, inset + 2, inset + len, c);
                context.fill(width - inset - len, inset, width - inset, inset + 2, c);
                context.fill(width - inset - 2, inset, width - inset, inset + len, c);
                context.fill(inset, height - inset - 2, inset + len, height - inset, c);
                context.fill(inset, height - inset - len, inset + 2, height - inset, c);
                context.fill(width - inset - len, height - inset - 2, width - inset, height - inset, c);
                context.fill(width - inset - 2, height - inset - len, width - inset, height - inset, c);
                for (int i = 0; i < 18; i++) {
                    int x = Math.floorMod(pulsePhase * 17 + i * 71, Math.max(1, width));
                    int y = Math.floorMod(pulsePhase * 11 + i * 37, Math.max(1, height));
                    context.fill(x, y, x + 2, y + 2, c);
                }
            }
            case 3 -> { // Berserk: pulsing warning bars.
                int h = pulse < 10 ? 3 : 1;
                context.fill(0, 18, width, 18 + h, accent);
                context.fill(0, height - 18 - h, width, height - 18, accent);
                context.fill(cx - 42, 32, cx + 42, 34, accent);
                context.fill(cx - 42, height - 34, cx + 42, height - 32, accent);
            }
            case 4 -> { // Sandevistan: temporal scan slices.
                for (int i = 0; i < 9; i++) {
                    int y = Math.floorMod(pulsePhase * 7 + i * (height / 9), Math.max(1, height));
                    int x = Math.floorMod(pulsePhase * 13 + i * 47, Math.max(1, width - 70));
                    context.fill(x, y, Math.min(width, x + 54), y + 1, accent);
                }
                context.fill(cx - 1, 0, cx + 1, height, (Math.min(24, bright) << 24) | 0x0029E9F4);
            }
            case 5 -> { // Mantis: twin blade slashes.
                for (int i = 0; i < 6; i++) {
                    int offset = i * 4;
                    context.fill(16 + offset, 26 + offset, 18 + offset, 72 + offset, accent);
                    context.fill(width - 18 - offset, height - 72 - offset,
                            width - 16 - offset, height - 26 - offset, accent);
                }
            }
            case 10 -> { // Cyberpsychosis: broken scanlines and unstable signal blocks.
                int tear = (Math.min(110, bright) << 24) | 0x00FF1838;
                for (int i = 0; i < 12; i++) {
                    int y = Math.floorMod(pulsePhase * 17 + i * 41, Math.max(1, height - 3));
                    int shift = Math.floorMod(pulsePhase * 11 + i * 29, Math.max(1, width / 5));
                    int x = Math.floorMod(width / 4 + shift, Math.max(1, width - 100));
                    int span = 30 + (i % 4) * 24;
                    context.fill(x, y, Math.min(width, x + span), y + (i % 3 == 0 ? 3 : 1), tear);
                }
                if (pulsePhase % 9 < 2) context.fill(0, 0, width, height, 0x12FF102E);
                int jitter = pulsePhase % 4 == 0 ? 3 : pulsePhase % 4 == 1 ? -2 : 0;
                context.drawCenteredTextWithShadow(client.textRenderer,
                        Text.literal("!!! TARGET LOCK // NEURAL FAILURE !!!"),
                        cx + jitter, cy - 34, 0xFFFF5268);
            }
            case 12 -> { // Gorilla arms: heavy impact reticle.
                int r = 22 + pulse % 5;
                context.fill(cx - r, cy - 1, cx - 7, cy + 1, accent);
                context.fill(cx + 7, cy - 1, cx + r, cy + 1, accent);
                context.fill(cx - 1, cy - r, cx + 1, cy - 7, accent);
                context.fill(cx - 1, cy + 7, cx + 1, cy + r, accent);
                context.fill(cx - 2, cy - 2, cx + 2, cy + 2, accent);
            }
            case 13, 17 -> { // Kerenzikov / tendons: directional motion brackets.
                for (int i = 0; i < 7; i++) {
                    int y = cy - 30 + i * 10;
                    int len = 24 + (i % 3) * 12;
                    context.fill(8, y, 8 + len, y + 1, accent);
                    context.fill(width - 8 - len, y, width - 8, y + 1, accent);
                }
                if (effectId == 17) {
                    context.fill(cx - 30, height - 22, cx + 30, height - 20, accent);
                }
            }
            case 14 -> { // Blood pump: heartbeat waveform.
                int baseY = height - 42;
                int[] wave = {0, 0, -5, 7, -18, 24, -7, 0, 0, 0, -4, 6, -12, 16, -4, 0};
                int startX = cx - 80;
                for (int i = 0; i < wave.length - 1; i++) {
                    int x1 = startX + i * 10, x2 = x1 + 10;
                    int y1 = baseY + wave[(i + pulse / 5) % wave.length];
                    int y2 = baseY + wave[(i + 1 + pulse / 5) % wave.length];
                    context.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2) + 2,
                            Math.max(y1, y2) + 2, accent);
                }
            }
            case 15 -> { // Projectile launcher: target acquisition.
                int r = 28 + pulse % 4;
                context.fill(cx - r, cy - r, cx - r + 12, cy - r + 2, accent);
                context.fill(cx - r, cy - r, cx - r + 2, cy - r + 12, accent);
                context.fill(cx + r - 12, cy - r, cx + r, cy - r + 2, accent);
                context.fill(cx + r - 2, cy - r, cx + r, cy - r + 12, accent);
                context.fill(cx - 1, cy - 5, cx + 1, cy + 5, accent);
                context.fill(cx - 5, cy - 1, cx + 5, cy + 1, accent);
            }
            case 16 -> { // Monowire: twin cutting arcs.
                for (int i = 0; i < 8; i++) {
                    int y = cy - 34 + i * 9;
                    int x = 18 + i * 5;
                    context.fill(x, y, x + 2, y + 20, accent);
                    context.fill(width - x - 2, y, width - x, y + 20, accent);
                }
            }
            default -> { }
        }

        String labelKey = switch (effectId) {
            case 1 -> "visual.cyberpunk.accelerator";
            case 2 -> "visual.cyberpunk.camo";
            case 3 -> "visual.cyberpunk.berserk";
            case 4 -> "visual.cyberpunk.sandevistan";
            case 5 -> "visual.cyberpunk.mantis";
            case 8 -> "visual.cyberpunk.warning";
            case 9 -> "visual.cyberpunk.critical";
            case 10 -> "visual.cyberpunk.psychosis";
            case 12 -> "visual.cyberpunk.gorilla";
            case 13 -> "visual.cyberpunk.kerenzikov";
            case 14 -> "visual.cyberpunk.blood_pump";
            case 15 -> "visual.cyberpunk.launcher";
            case 16 -> "visual.cyberpunk.monowire";
            case 17 -> "visual.cyberpunk.tendons";
            default -> "visual.cyberpunk.system";
        };
        if (ticksLeft >= Math.max(4, initialTicks / 3)) {
            int textColor = 0xFFFFFFFF;
            context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable(labelKey),
                    cx, 8, textColor);
        }
    }

}
