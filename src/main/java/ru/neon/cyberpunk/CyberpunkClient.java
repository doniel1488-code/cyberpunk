package ru.neon.cyberpunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class CyberpunkClient implements ClientModInitializer {
    private static KeyBinding acceleratorKey;
    private static KeyBinding opticalCamoKey;
    private static KeyBinding berserkKey;
    private static KeyBinding sandevistanKey;
    private static KeyBinding mantisKey;
    private static KeyBinding gorillaKey;
    private static KeyBinding kerenzikovKey;
    private static KeyBinding bloodPumpKey;
    private static KeyBinding projectileLauncherKey;
    private static KeyBinding reinforcedTendonsKey;
    private static KeyBinding monowireKey;

    @Override
    public void onInitializeClient() {
        acceleratorKey = register("key.cyberpunk.neural_accelerator", GLFW.GLFW_KEY_V);
        opticalCamoKey = register("key.cyberpunk.optical_camo", GLFW.GLFW_KEY_C);
        berserkKey = register("key.cyberpunk.berserk", GLFW.GLFW_KEY_X);
        sandevistanKey = register("key.cyberpunk.sandevistan", GLFW.GLFW_KEY_B);
        mantisKey = register("key.cyberpunk.mantis_arms", GLFW.GLFW_KEY_M);
        gorillaKey = register("key.cyberpunk.gorilla_arms", GLFW.GLFW_KEY_G);
        kerenzikovKey = register("key.cyberpunk.kerenzikov", GLFW.GLFW_KEY_K);
        bloodPumpKey = register("key.cyberpunk.blood_pump", GLFW.GLFW_KEY_P);
        projectileLauncherKey = register("key.cyberpunk.projectile_launcher", GLFW.GLFW_KEY_R);
        reinforcedTendonsKey = register("key.cyberpunk.reinforced_tendons", GLFW.GLFW_KEY_J);
        monowireKey = register("key.cyberpunk.monowire", GLFW.GLFW_KEY_H);

        CyberpunkVisuals.register();
        SandevistanTrail.register();
        ClientPlayNetworking.registerGlobalReceiver(CyberpunkVisualPayload.ID, (payload, context) ->
                context.client().execute(() -> CyberpunkVisuals.trigger(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SandevistanTrailPayload.ID, (payload, context) ->
                context.client().execute(() -> SandevistanTrail.accept(payload)));
        ClientPlayNetworking.registerGlobalReceiver(CyberwareStatePayload.ID, (payload, context) ->
                context.client().execute(() -> CyberwareScreen.acceptState(payload)));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            SandevistanTrail.tick(client);
            while (acceleratorKey.wasPressed()) activate(0);
            while (opticalCamoKey.wasPressed()) activate(1);
            while (berserkKey.wasPressed()) activate(2);
            while (sandevistanKey.wasPressed()) activate(3);
            while (mantisKey.wasPressed()) activate(4);
            while (gorillaKey.wasPressed()) activate(5);
            while (kerenzikovKey.wasPressed()) activate(6);
            while (bloodPumpKey.wasPressed()) activate(8);
            while (projectileLauncherKey.wasPressed()) activate(9);
            while (reinforcedTendonsKey.wasPressed()) activate(10);
            while (monowireKey.wasPressed()) activate(13);
        });
    }

    private static void activate(int type) {
        ClientPlayNetworking.send(new ActivateImplantPayload(type));
    }

    public static String getActivationKeyLabel(int implantType) {
        KeyBinding binding = switch (implantType) {
            case 0 -> acceleratorKey;
            case 1 -> opticalCamoKey;
            case 2 -> berserkKey;
            case 3 -> sandevistanKey;
            case 4 -> mantisKey;
            case 5 -> gorillaKey;
            case 6 -> kerenzikovKey;
            case 8 -> bloodPumpKey;
            case 9 -> projectileLauncherKey;
            case 10 -> reinforcedTendonsKey;
            case 13 -> monowireKey;
            default -> null;
        };
        return binding == null
                ? Text.translatable("message.cyberpunk.passive").getString()
                : binding.getBoundKeyLocalizedText().getString();
    }

    private static KeyBinding register(String translationKey, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey, InputUtil.Type.KEYSYM, keyCode, "category.cyberpunk.implants"));
    }
}
