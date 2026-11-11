package ru.neon.cyberpunk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CyberpunkMod implements ModInitializer {
    public static final String MOD_ID = "cyberpunk";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModItems.initialize();
        CyberpunkNetworking.initialize();
        ServerTickEvents.END_SERVER_TICK.register(CyberwareManager::tick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
                CyberwareManager.onAfterDamage(entity, source, damageTaken));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                CyberwareManager.removePlayer(handler.player.getUuid()));
        LOGGER.info("Cyberpunk Fabric initialized: expanded cyberware suite");
    }
}
