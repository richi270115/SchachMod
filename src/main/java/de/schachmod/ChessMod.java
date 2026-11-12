package de.schachmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class ChessMod implements ModInitializer {
    public static final String MOD_ID = "schachmod";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> ChessCommands.register(dispatcher));
        UseBlockCallback.EVENT.register(ChessInteraction::onUseBlock);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ChessManager.clear());
    }
}
