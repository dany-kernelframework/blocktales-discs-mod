package com.kf.client;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NullMarked
public class DiscTabStyleReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor,
                                          PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        return CompletableFuture
                .supplyAsync(() -> DiscTabStyle.load(currentReload.resourceManager()), taskExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(DiscTabStyle::install, reloadExecutor);
    }
}