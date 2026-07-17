package com.kf.client.mixin;

import com.kf.client.LyricsTracker;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.JukeboxSong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {

    @Inject(method = "playJukeboxSong", at = @At("HEAD"))
    private void discs$onPlayJukeboxSong(Holder<JukeboxSong> songHolder, BlockPos pos, CallbackInfo ci) {
        LyricsTracker.onJukeboxSongStarted(pos, songHolder.value());
    }

    @Inject(method = "stopJukeboxSongAndNotifyNearby", at = @At("HEAD"))
    private void discs$onStopJukeboxSong(BlockPos pos, CallbackInfo ci) {
        LyricsTracker.onJukeboxSongStopped(pos);
    }
}