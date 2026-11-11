package ru.neon.cyberpunk;

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
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || ticksLeft <= 0) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        float fadeWindow = Math.max(4.0f, initialTicks * 0.45f);
        float fade = Math.min(1.0f, ticksLeft / fadeWindow);
        int alpha = Math.max(0, Math.min(120, Math.round(intensity * fade * 0.55f)));

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
            default -> 0x00D6F9FF;
        };

        context.fill(0, 0, width, height, (alpha << 24) | rgb);

        int edge = Math.max(2, Math.round(7.0f * fade));
        int edgeAlpha = Math.min(110, alpha + 18);
        int edgeColor = (edgeAlpha << 24) | rgb;
        context.fill(0, 0, width, edge, edgeColor);
        context.fill(0, height - edge, width, height, edgeColor);
        context.fill(0, 0, edge, height, edgeColor);
        context.fill(width - edge, 0, width, height, edgeColor);

        // Sandevistan adds fast horizontal scan streaks to sell the time-dilation burst.
        if (effectId == 4 && alpha > 0) {
            int streakAlpha = Math.min(75, alpha);
            int streakColor = (streakAlpha << 24) | 0x00D9FFFF;
            for (int i = 0; i < 6; i++) {
                int y = Math.floorMod(pulsePhase * 9 + i * (height / 7), height);
                int x = Math.floorMod(pulsePhase * 23 + i * 47, Math.max(1, width / 3));
                context.fill(x, y, Math.min(width, x + width / 2), Math.min(height, y + 2), streakColor);
            }
        }

        // Cyberpsychosis deliberately breaks the clean frame with red signal interference.
        if ((effectId == 10 || effectId == 9) && alpha > 0) {
            int glitchAlpha = Math.min(95, alpha + 22);
            int glitchColor = (glitchAlpha << 24) | 0x00FF263E;
            for (int i = 0; i < 5; i++) {
                int y = Math.floorMod(ticksLeft * 17 + i * (height / 5), height);
                int x = Math.floorMod(ticksLeft * 29 + i * 61, Math.max(1, width / 2));
                int barWidth = Math.max(12, width - x);
                context.fill(x, y, Math.min(width, x + barWidth), Math.min(height, y + 2), glitchColor);
            }
            context.fill(0, height / 2 - 1, width, height / 2 + 1, glitchColor);
        }

        if (ticksLeft >= Math.max(6, initialTicks / 3)) {
            String key = switch (effectId) {
                case 1 -> "visual.cyberpunk.accelerator";
                case 2 -> "visual.cyberpunk.camo";
                case 3 -> "visual.cyberpunk.berserk";
                case 4 -> "visual.cyberpunk.sandevistan";
                case 5 -> "visual.cyberpunk.mantis";
                case 6 -> "visual.cyberpunk.tendons";
                case 7 -> "visual.cyberpunk.quickhack";
                case 8 -> "visual.cyberpunk.warning";
                case 9 -> "visual.cyberpunk.critical";
                case 10 -> "visual.cyberpunk.psychosis";
                default -> "visual.cyberpunk.system";
            };
            int textY = effectId == 10 ? height / 2 - 34 : height / 2 - 26;
            context.drawCenteredTextWithShadow(client.textRenderer,
                    Text.translatable(key), width / 2, textY, 0xFFFFFFFF);
        }

        pulsePhase++;
        ticksLeft--;
    }
}
