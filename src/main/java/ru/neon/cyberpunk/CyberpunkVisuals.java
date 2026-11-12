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
        int rgb = switch (effectId) {
            case 1 -> 0x00BFFAFF;
            case 2 -> 0x007A73FF;
            case 3 -> 0x00FF385A;
            case 4 -> 0x0000E8FF;
            case 5 -> 0x00B7FF6B;
            case 6 -> 0x007BFFF2;
            case 7 -> 0x00946BFF;
            case 8 -> 0x00E5D9FF;
            case 9 -> 0x00FF7A70;
            case 10 -> 0x00FF173D;
            case 12 -> 0x00FF914D;
            case 13 -> 0x004EDBFF;
            case 14 -> 0x00FF597D;
            case 15 -> 0x00FFB34D;
            case 16 -> 0x0047E6D0;
            default -> 0x00D6F9FF;
        };

        // The view is not tinted. Only a thin peripheral frame indicates the active implant.
        int edgeAlpha = Math.max(8, Math.min(42, Math.round(intensity * fade * 0.20f)));
        int edgeColor = (edgeAlpha << 24) | rgb;
        context.fill(0, 0, width, 2, edgeColor);
        context.fill(0, height - 2, width, height, edgeColor);
        context.fill(0, 0, 2, height, edgeColor);
        context.fill(width - 2, 0, width, height, edgeColor);

        if (effectId == 4) {
            int streakColor = (Math.min(62, edgeAlpha + 12) << 24) | 0x00D9FFFF;
            int band = Math.max(10, width / 12);
            for (int i = 0; i < 5; i++) {
                int y = Math.floorMod(pulsePhase * 7 + i * (height / 5), Math.max(1, height - 3));
                int length = Math.max(12, band - i);
                context.fill(2, y, Math.min(band, length), y + 1, streakColor);
                context.fill(Math.max(2, width - length), y, width - 2, y + 1, streakColor);
            }
        }

        if (effectId == 10 || effectId == 9) {
            int glitchColor = (Math.min(70, edgeAlpha + 18) << 24) | 0x00FF263E;
            int band = Math.max(12, width / 10);
            for (int i = 0; i < 4; i++) {
                int y = Math.floorMod(ticksLeft * 13 + i * (height / 4), Math.max(1, height - 2));
                int length = Math.max(12, band - i * 3);
                context.fill(2, y, Math.min(width / 2, 2 + length), y + 1, glitchColor);
                context.fill(Math.max(width / 2, width - 2 - length), y, width - 2, y + 1, glitchColor);
            }

            if (effectId == 10) {
                // Short, uneven scanline tears and signal flashes: the center remains readable.
                int tearAlpha = Math.min(38, edgeAlpha + 12);
                int tearColor = (tearAlpha << 24) | 0x00FF1838;
                for (int i = 0; i < 7; i++) {
                    if (((pulsePhase + i * 3) % 5) < 2) {
                        int y = Math.floorMod(pulsePhase * 17 + i * 47, Math.max(1, height - 2));
                        int shift = Math.floorMod(pulsePhase * 11 + i * 29, Math.max(1, width / 7));
                        int tearWidth = Math.max(24, width / 4);
                        int start = Math.floorMod(width / 3 + shift, Math.max(1, width - tearWidth));
                        context.fill(start, y, start + tearWidth, y + 2, tearColor);
                    }
                }
                if (pulsePhase % 13 < 2) {
                    context.fill(0, 0, width, height, 0x0DFF102E);
                }
                int jitter = pulsePhase % 4 == 0 ? 3 : pulsePhase % 4 == 1 ? -2 : 0;
                context.drawCenteredTextWithShadow(client.textRenderer,
                        Text.literal("!!! TARGET LOCK // NEURAL FAILURE !!!"),
                        width / 2 + jitter, height / 2 - 34, 0xFFFF5268);
            }
        }

        if (ticksLeft >= Math.max(4, initialTicks / 3)) {
            String key = switch (effectId) {
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
                default -> "visual.cyberpunk.system";
            };
            context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable(key), width / 2, 8, 0xFFFFFFFF);
        }
    }
}
