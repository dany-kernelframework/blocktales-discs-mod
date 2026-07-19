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
    private static @Nullable Integer currentLineColor; // is 0xRRGGBB, null is default color

    private static long ticksSinceLineChanged;
    private static boolean currentLineIsLast;
    private static long ticksUntilNextLine;

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
        ticksUntilNextLine = -1;
        currentLineIsLast = false;

        updateLineState();
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
        currentLineColor = null;
        ticksSinceLineChanged = 0;
        currentLineIsLast = false;
        ticksUntilNextLine = -1;
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
        updateLineState();
    }

    private static void updateLineState() {
        if (trackedKey == null) return;

        List<DiscLyrics.LyricLine> lines = DiscLyrics.get(trackedKey);
        if (lines.isEmpty()) {
            currentLineIsLast = false;
            currentLine = null;
            currentLineColor = null;
            ticksUntilNextLine = -1;
            return;
        }

        float elapsedSeconds = ticksSinceStart / 20.0f;

        String newResult = null;
        Integer newColor = null;
        boolean isLast = false;
        long nextLineTicks = -1;

        for (int i = 0; i < lines.size(); i++) {
            DiscLyrics.LyricLine line = lines.get(i);
            if (line.time() > elapsedSeconds) {
                nextLineTicks = (long) (line.time() * 20.0f);
                break;
            }
            newResult = line.text();
            newColor = line.color();
            isLast = (i == lines.size() - 1);
        }

        if (!java.util.Objects.equals(newResult, currentLine)) {
            currentLine = newResult;
            currentLineColor = newColor;
            ticksSinceLineChanged = 0;
        } else {
            ticksSinceLineChanged++;
        }

        currentLineIsLast = isLast;
        ticksUntilNextLine = nextLineTicks != -1 ? (nextLineTicks - ticksSinceStart) : -1;
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

    public static @Nullable String getCurrentLine() { return currentLine; }
    public static @Nullable Integer getCurrentLineColor() { return currentLineColor; }
    public static long getTicksSinceLineChanged() { return ticksSinceLineChanged; }
    public static boolean isCurrentLineLast() { return currentLineIsLast; }
    public static long getTicksUntilNextLine() { return ticksUntilNextLine; }
    public static @Nullable BlockPos getTrackedPos() { return trackedPos; }
} //quite unorthodox to put a bunch of statics here but it's the best way i figured