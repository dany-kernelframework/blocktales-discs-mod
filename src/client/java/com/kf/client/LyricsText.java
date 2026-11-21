package com.kf.client;

import com.kf.DiscLyrics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

final class LyricsText {

    private static final double scramble_sec = 0.6;
    private static final long glitch_tick = 100;
    private static final float glitch_burst = 0.24f;
    private static final float glitch_letter = 0.2f;

    static Component compose(DiscLyrics.LyricLine line, double elapsed, DiscLyrics.Effect effect,
                             boolean effectsOn, int rgb, long nowMs) {
        long seed = seedOf(line);
        double age = elapsed - line.time();
        boolean scramble = effectsOn && (effect == DiscLyrics.Effect.scramble || effect == DiscLyrics.Effect.glitch);
        long glitchTick = nowMs / glitch_tick;
        boolean burst = effectsOn && effect == DiscLyrics.Effect.glitch && noise(seed * 7 + glitchTick) < glitch_burst;

        Runs runs = new Runs();
        int index = 0;

        if (line.words().isEmpty()) {
            for (char c : line.text().toCharArray()) {
                runs.add(c, rgb, isObfuscated(c, index++, seed, age, scramble, burst, glitchTick));
            }
        } else {
            int dimmed = dim(rgb);
            for (DiscLyrics.Word word : line.words()) {
                int color = word.time() <= elapsed ? rgb : dimmed;
                for (char c : word.text().toCharArray()) {
                    runs.add(c, color, isObfuscated(c, index++, seed, age, scramble, burst, glitchTick));
                }
            }
        }

        runs.flush();
        return runs.result;
    }

    static Component plain(DiscLyrics.LyricLine line, int rgb) {
        StringBuilder text = new StringBuilder();
        if (line.words().isEmpty()) {
            text.append(line.text());
        } else {
            line.words().forEach(w -> text.append(w.text()));
        }
        return Component.literal(text.toString()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    static int dim(int rgb) {
        return (rgb >> 1) & 0x7F7F7F;
    }

    // this is bullshittery
    static int[] shake(DiscLyrics.LyricLine line, long nowMs) {
        long seed = seedOf(line);
        long tick = nowMs / 60;
        int dx = (int) (noise(seed * 3 + tick) * 3) - 1;
        int dy = (int) (noise(seed * 5 + tick) * 3) - 1;
        return new int[]{dx, dy};
    }

    private static boolean isObfuscated(char c, int index, long seed, double age, boolean scramble,
                                        boolean burst, long glitchTick) {
        if (c == ' ') return false;
        if (scramble && age < scramble_sec && noise(seed * 31 + index) > age / scramble_sec) {
            return true;
        }
        return burst && noise(seed * 13 + glitchTick * 57 + index) < glitch_letter;
    }

    private static long seedOf(DiscLyrics.LyricLine line) {
        return Float.floatToIntBits(line.time()) * 1000003L + line.text().hashCode();
    }

    private static final class Runs {
        final MutableComponent result = Component.empty();
        private final StringBuilder run = new StringBuilder();
        private int color;
        private boolean obfuscated;

        void add(char c, int newColor, boolean newObfuscated) {
            if (!run.isEmpty() && (newColor != color || newObfuscated != obfuscated)) {
                flush();
            }
            run.append(c);
            color = newColor;
            obfuscated = newObfuscated;
        }

        void flush() {
            if (run.isEmpty()) return;
            MutableComponent piece = Component.literal(run.toString())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
            if (obfuscated) {
                piece.withStyle(ChatFormatting.OBFUSCATED);
            }
            result.append(piece);
            run.setLength(0);
        }
    }

    private static float noise(long seed) {
        return (mix(seed) >>> 40) / (float) (1 << 24);
    }

    // splitmix64 (again)
    private static long mix(long value) {
        value += 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
