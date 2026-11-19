package com.kf.client;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NullMarked
public class DiscLabelsReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor,
                                          PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        return CompletableFuture
                .supplyAsync(() -> DiscLabels.load(currentReload.resourceManager()), taskExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(DiscLabels::install, reloadExecutor);
    }
}