package com.kf.mixin;

import com.kf.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WanderingTraderSpawner.class)
public class WanderingTraderSpawnerMixin {
    @Unique
    private static final float DISC_TRADER_SPAWN_CHANCE = 0.25f;

    @Redirect(
            method = "spawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity spawnDiscTraderInstead(EntityType<?> entityType, ServerLevel level, BlockPos pos, EntitySpawnReason reason) {
        if (level.getRandom().nextFloat() < DISC_TRADER_SPAWN_CHANCE) {
            return ModEntities.DISC_TRADER.spawn(level, pos, reason);
        }
        // fall back to whatever this call was originally spawning (EntityType.WANDERING_TRADER)
        return entityType.spawn(level, pos, reason);
    }
}