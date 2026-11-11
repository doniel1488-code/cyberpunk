package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class CyberpunkNetworking {
    private CyberpunkNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(ActivateImplantPayload.ID, ActivateImplantPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActivateImplantPayload.ID, (payload, context) ->
                CyberwareManager.activate(context.player(), payload.implantType()));
    }
}
