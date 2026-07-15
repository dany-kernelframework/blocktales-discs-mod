package com.kf.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WanderingTraderSpawner.class)
public interface WanderingTraderSpawnerAccessor {

    @Invoker("spawn")
    boolean invokeSpawn(ServerLevel level);
}