package ru.neon.cyberpunk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CyberwareStatePayload(int installedMask, int neuralLoad, int psychosisTicks)
        implements CustomPayload {
    public static final Id<CyberwareStatePayload> ID =
            new Id<>(Identifier.of(CyberpunkMod.MOD_ID, "cyberware_state"));

    public static final PacketCodec<RegistryByteBuf, CyberwareStatePayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, CyberwareStatePayload::installedMask,
                    PacketCodecs.VAR_INT, CyberwareStatePayload::neuralLoad,
                    PacketCodecs.VAR_INT, CyberwareStatePayload::psychosisTicks,
                    CyberwareStatePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
