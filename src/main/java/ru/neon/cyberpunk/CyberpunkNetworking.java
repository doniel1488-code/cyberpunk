package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class CyberpunkNetworking {
    private CyberpunkNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(ActivateImplantPayload.ID, ActivateImplantPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CyberwareActionPayload.ID, CyberwareActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CyberpunkVisualPayload.ID, CyberpunkVisualPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SandevistanTrailPayload.ID, SandevistanTrailPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CyberwareStatePayload.ID, CyberwareStatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ActivateImplantPayload.ID, (payload, context) ->
                CyberwareManager.activate(context.player(), payload.implantType()));
        ServerPlayNetworking.registerGlobalReceiver(CyberwareActionPayload.ID, (payload, context) ->
                CyberwareManager.handleScreenAction(context.player(), payload));
    }
}
