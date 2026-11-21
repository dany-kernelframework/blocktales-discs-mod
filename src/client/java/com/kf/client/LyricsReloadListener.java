package com.kf.client;

import com.kf.DiscLyrics;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NullMarked
public class LyricsReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor,
                                          PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        return CompletableFuture
                .supplyAsync(() -> DiscLyrics.parseAll(currentReload.resourceManager()), taskExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(parsed -> {
                    DiscLyrics.install(parsed);
                    LyricsConfig.load();
                }, reloadExecutor);
    }
}
