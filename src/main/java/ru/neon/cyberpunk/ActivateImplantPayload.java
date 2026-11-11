package ru.neon.cyberpunk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ActivateImplantPayload(int implantType) implements CustomPayload {
    public static final Id<ActivateImplantPayload> ID =
            new Id<>(Identifier.of(CyberpunkMod.MOD_ID, "activate_implant"));

    public static final PacketCodec<RegistryByteBuf, ActivateImplantPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, ActivateImplantPayload::implantType,
                    ActivateImplantPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
