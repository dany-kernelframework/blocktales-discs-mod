package com.kf.client;

import com.kf.Discs;
import com.kf.DiscLyrics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.JukeboxSong;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LyricsTracker {

    private static @Nullable BlockPos trackedPos;
    private static @Nullable String trackedKey;
    private static long ticksSinceStart;
    private static @Nullable String currentLine;

    // Fading logic tracking
    private static long ticksSinceLineChanged;
    private static boolean currentLineIsLast;

    public static void onJukeboxSongStarted(BlockPos pos, JukeboxSong song) {
        String combinedKey = resolveLyricsKey(song);

        if (combinedKey == null) {
            if (pos.equals(trackedPos)) {
                clear();
            }
            return;
        }

        trackedPos = pos;
        trackedKey = combinedKey;
        ticksSinceStart = 0;
        ticksSinceLineChanged = 0;
        currentLine = lineAt(combinedKey, 0);
    }

    public static void onJukeboxSongStopped(BlockPos pos) {
        if (pos.equals(trackedPos)) { //its MIGHT be null so no FUCK you
            clear();
        }
    }

    private static void clear() {
        trackedPos = null;
        trackedKey = null;
        currentLine = null;
        ticksSinceLineChanged = 0;
        currentLineIsLast = false;
    }

    public static void tick() {
        if (trackedKey == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.isPaused()) {
            return;
        }

        ticksSinceStart++;
        String newLine = lineAt(trackedKey, ticksSinceStart);

        if (!java.util.Objects.equals(newLine, currentLine)) {
            currentLine = newLine;
            ticksSinceLineChanged = 0;
        } else {
            ticksSinceLineChanged++;
        }
    }

    private static @Nullable String resolveLyricsKey(JukeboxSong song) {
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

        String combinedKey = id.getPath();
        boolean hasLyrics = DiscLyrics.hasLyrics(combinedKey);

        return hasLyrics ? combinedKey : null;
    }

    private static @Nullable String lineAt(String combinedKey, long ticksSinceSongStarted) {
        List<DiscLyrics.LyricLine> lines = DiscLyrics.get(combinedKey);
        if (lines.isEmpty()) {
            currentLineIsLast = false;
            return null;
        }

        float elapsedSeconds = ticksSinceSongStarted / 20.0f;

        String result = null;
        boolean isLast = false;
        for (int i = 0; i < lines.size(); i++) {
            DiscLyrics.LyricLine line = lines.get(i);
            if (line.time() > elapsedSeconds) {
                break;
            }
            result = line.text();
            isLast = (i == lines.size() - 1);
        }

        currentLineIsLast = isLast;
        return result;
    }

    public static @Nullable String getCurrentLine() {
        return currentLine;
    }

    public static long getTicksSinceLineChanged() {
        return ticksSinceLineChanged;
    }

    public static boolean isCurrentLineLast() {
        return currentLineIsLast;
    }

    public static @Nullable BlockPos getTrackedPos() {
        return trackedPos;
    }
}