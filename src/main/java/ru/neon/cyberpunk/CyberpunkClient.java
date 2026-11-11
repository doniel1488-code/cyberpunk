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

    @Override
    public void onInitializeClient() {
        acceleratorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cyberpunk.neural_accelerator", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                "category.cyberpunk.implants"));
        opticalCamoKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cyberpunk.optical_camo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C,
                "category.cyberpunk.implants"));
        berserkKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cyberpunk.berserk", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X,
                "category.cyberpunk.implants"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (acceleratorKey.wasPressed()) {
                ClientPlayNetworking.send(new ActivateImplantPayload(0));
            }
            while (opticalCamoKey.wasPressed()) {
                ClientPlayNetworking.send(new ActivateImplantPayload(1));
            }
            while (berserkKey.wasPressed()) {
                ClientPlayNetworking.send(new ActivateImplantPayload(2));
            }
        });
    }
}
