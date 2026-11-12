package ru.neon.cyberpunk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CyberwareActionPayload(int action, int implantType) implements CustomPayload {
    public static final Id<CyberwareActionPayload> ID =
            new Id<>(Identifier.of(CyberpunkMod.MOD_ID, "cyberware_action"));

    public static final PacketCodec<RegistryByteBuf, CyberwareActionPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, CyberwareActionPayload::action,
                    PacketCodecs.VAR_INT, CyberwareActionPayload::implantType,
                    CyberwareActionPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
