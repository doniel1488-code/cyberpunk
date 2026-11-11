package ru.neon.cyberpunk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CyberpunkVisualPayload(int effectId, int duration, int intensity) implements CustomPayload {
    public static final Id<CyberpunkVisualPayload> ID =
            new Id<>(CyberpunkMod.id("visual_effect"));

    public static final PacketCodec<RegistryByteBuf, CyberpunkVisualPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, CyberpunkVisualPayload::effectId,
            PacketCodecs.VAR_INT, CyberpunkVisualPayload::duration,
            PacketCodecs.VAR_INT, CyberpunkVisualPayload::intensity,
            CyberpunkVisualPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
