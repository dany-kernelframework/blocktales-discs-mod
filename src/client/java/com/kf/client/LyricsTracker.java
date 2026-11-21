package com.kf.client;

import com.kf.DiscLyrics;
import com.kf.Discs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.JukeboxSong;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LyricsTracker {

    private static final double dist = 64.0;
    private static final double fade_dist = 48.0;

    private static final Map<BlockPos, Track> TRACKS = new HashMap<>();
    private static long lastNanos = System.nanoTime();

    private static final class Track {
        final String key;
        final BlockPos pos;
        final long startNanos;
        long pausedNanos;

        Track(String key, BlockPos pos, long startNanos) {
            this.key = key;
            this.pos = pos;
            this.startNanos = startNanos;
        }
    }

    public record View(String key, double elapsed, float distanceAlpha,
                       DiscLyrics.@Nullable Song song,
                       DiscLyrics.@Nullable LyricLine line,
                       DiscLyrics.@Nullable LyricLine next) {}

    public static void onJukeboxSongStarted(BlockPos pos, JukeboxSong song) {
        BlockPos key = pos.immutable();
        String songKey = resolveKey(song);

        if (songKey == null) {
            TRACKS.remove(key);
            return;
        }
        TRACKS.put(key, new Track(songKey, key, System.nanoTime()));
    }

    public static void onJukeboxSongStopped(BlockPos pos) {
        TRACKS.remove(pos);
    }

    public static void tick() {
        if (TRACKS.isEmpty()) {
            lastNanos = System.nanoTime();
            return;
        }
        advance(Minecraft.getInstance());
    }

    // za warudo type shit
    private static void advance(Minecraft client) {
        long now = System.nanoTime();
        if (client.isPaused()) {
            long delta = now - lastNanos;
            for (Track track : TRACKS.values()) {
                track.pausedNanos += delta;
            }
        }
        lastNanos = now;
    }

    public static @Nullable View view() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            TRACKS.clear();
            return null;
        }
        if (TRACKS.isEmpty()) {
            return null;
        }

        advance(client);

        Track nearest = null;
        double bestDistSq = dist * dist;
        for (Track track : TRACKS.values()) {
            double distSq = client.player.distanceToSqr(track.pos.getX() + 0.5, track.pos.getY() + 0.5, track.pos.getZ() + 0.5);
            if (distSq <= bestDistSq) {
                bestDistSq = distSq;
                nearest = track;
            }
        }
        if (nearest == null) {
            return null;
        }

        double dist = Math.sqrt(bestDistSq);
        float distanceAlpha = dist > fade_dist
                ? (float) (1.0 - (dist - fade_dist) / (LyricsTracker.dist - fade_dist))
                : 1.0f;

        DiscLyrics.Song song = DiscLyrics.song(nearest.key);
        double elapsed = (System.nanoTime() - nearest.startNanos - nearest.pausedNanos) / 1.0e9;
        if (song != null) {
            elapsed += song.offset();
        }

        DiscLyrics.LyricLine line = null;
        DiscLyrics.LyricLine next = null;
        if (song != null) {
            List<DiscLyrics.LyricLine> lines = song.lines();
            int index = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).time() <= elapsed) {
                    index = i;
                } else {
                    break;
                }
            }
            line = index >= 0 ? lines.get(index) : null;
            next = index + 1 < lines.size() ? lines.get(index + 1) : null;
        }

        return new View(nearest.key, elapsed, distanceAlpha, song, line, next);
    }

    private static @Nullable String resolveKey(JukeboxSong song) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return null;
        }

        Identifier id = client.level.registryAccess()
                .lookupOrThrow(Registries.JUKEBOX_SONG)
                .getKey(song);

        if (id == null || !id.getNamespace().equals(Discs.MOD_ID)) {
            return null;
        }
        return id.getPath();
    }
}
