package ru.neon.cyberpunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class CyberpunkClient implements ClientModInitializer {
    private static KeyBinding acceleratorKey;
    private static KeyBinding opticalCamoKey;
    private static KeyBinding berserkKey;
    private static KeyBinding sandevistanKey;

    @Override
    public void onInitializeClient() {
        acceleratorKey = register("key.cyberpunk.neural_accelerator", GLFW.GLFW_KEY_V);
        opticalCamoKey = register("key.cyberpunk.optical_camo", GLFW.GLFW_KEY_C);
        berserkKey = register("key.cyberpunk.berserk", GLFW.GLFW_KEY_X);
        sandevistanKey = register("key.cyberpunk.sandevistan", GLFW.GLFW_KEY_B);

        CyberpunkVisuals.register();
        SandevistanTrail.register();
        ClientPlayNetworking.registerGlobalReceiver(CyberpunkVisualPayload.ID, (payload, context) ->
                context.client().execute(() -> CyberpunkVisuals.trigger(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SandevistanTrailPayload.ID, (payload, context) ->
                context.client().execute(() -> SandevistanTrail.accept(payload)));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            SandevistanTrail.tick(client);
            while (acceleratorKey.wasPressed()) ClientPlayNetworking.send(new ActivateImplantPayload(0));
            while (opticalCamoKey.wasPressed()) ClientPlayNetworking.send(new ActivateImplantPayload(1));
            while (berserkKey.wasPressed()) ClientPlayNetworking.send(new ActivateImplantPayload(2));
            while (sandevistanKey.wasPressed()) ClientPlayNetworking.send(new ActivateImplantPayload(3));
        });
    }

    private static KeyBinding register(String translationKey, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey, InputUtil.Type.KEYSYM, keyCode, "category.cyberpunk.implants"));
    }
}
