package ru.neon.cyberpunk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SandevistanTrailPayload(int entityId, int durationTicks) implements CustomPayload {
    public static final Id<SandevistanTrailPayload> ID =
            new Id<>(CyberpunkMod.id("sandevistan_trail"));

    public static final PacketCodec<RegistryByteBuf, SandevistanTrailPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SandevistanTrailPayload::entityId,
            PacketCodecs.VAR_INT, SandevistanTrailPayload::durationTicks,
            SandevistanTrailPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
