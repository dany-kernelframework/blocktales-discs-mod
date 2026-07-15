package com.kf;

import com.kf.mixin.WanderingTraderSpawnerAccessor;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("discs")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("testtrader")
                                        .executes(ModCommands::runTestSpawn))
                )
        );
    }


    private static int runTestSpawn(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        WanderingTraderSpawner spawner = new WanderingTraderSpawner(level.getDataStorage());
        boolean spawned = ((WanderingTraderSpawnerAccessor) spawner).invokeSpawn(level);

        context.getSource().sendSuccess(() -> Component.literal(
                spawned
                        ? "spawn event fired (circa 25% chance its the disc trader, rest of the time a regular wandering trader)"
                        : "attempt missed (needs a player nearby + a valid spot + an internal 1-in-10 gate all passing)"
        ), false);

        return spawned ? 1 : 0;
    }
}
