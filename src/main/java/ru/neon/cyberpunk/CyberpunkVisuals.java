package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CyberpunkVisuals {
    private static int effectId;
    private static int ticksLeft;
    private static int intensity;

    private CyberpunkVisuals() {
    }

    public static void trigger(CyberpunkVisualPayload payload) {
        effectId = payload.effectId();
        ticksLeft = Math.max(1, payload.duration());
        intensity = Math.max(0, Math.min(180, payload.intensity()));
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
        float fade = Math.min(1.0f, ticksLeft / 5.0f);
        int alpha = Math.max(0, Math.min(180, Math.round(intensity * fade)));
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
        int color = (alpha << 24) | rgb;
        context.fill(0, 0, width, height, color);

        int edge = Math.max(2, Math.round(8.0f * fade));
        int edgeColor = (Math.min(150, alpha) << 24) | rgb;
        context.fill(0, 0, width, edge, edgeColor);
        context.fill(0, height - edge, width, height, edgeColor);
        context.fill(0, 0, edge, height, edgeColor);
        context.fill(width - edge, 0, width, height, edgeColor);

        if (ticksLeft >= 8) {
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
            context.drawCenteredTextWithShadow(client.textRenderer,
                    Text.translatable(key), width / 2, height / 2 - 26, 0xFFFFFFFF);
        }
        ticksLeft--;
    }
}
