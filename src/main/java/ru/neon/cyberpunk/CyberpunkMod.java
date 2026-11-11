package ru.neon.cyberpunk;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = net.minecraft.server.command.CommandManager.literal("cyberware")
                    .executes(context -> {
                        CyberwareManager.sendStatus(context.getSource().getPlayerOrThrow());
                        return 1;
                    });
            root.then(net.minecraft.server.command.CommandManager.literal("remove")
                    .then(net.minecraft.server.command.CommandManager.argument("implant", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                for (String id : List.of("neural_accelerator", "optical_camo", "berserk_module",
                                        "sandevistan", "mantis_arms", "gorilla_arms", "kerenzikov",
                                        "subdermal_armor", "blood_pump")) builder.suggest(id);
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                String id = StringArgumentType.getString(context, "implant");
                                int type = CyberwareManager.getImplantTypeById(id);
                                if (type < 0) {
                                    context.getSource().sendError(net.minecraft.text.Text.translatable(
                                            "message.cyberpunk.unknown_implant", id));
                                    return 0;
                                }
                                CyberwareManager.uninstall(context.getSource().getPlayerOrThrow(), type);
                                return 1;
                            })));
            dispatcher.register(root);
        });
        ServerTickEvents.END_SERVER_TICK.register(CyberwareManager::tick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
                CyberwareManager.onAfterDamage(entity, source, damageTaken));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                CyberwareManager.removePlayer(handler.player.getUuid()));
        LOGGER.info("Cyberpunk Fabric initialized: expanded cyberware suite");
    }
}
