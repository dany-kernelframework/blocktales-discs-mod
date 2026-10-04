package com.kf;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// usage (client side):
//  TooltipFrames.add("hatred", 0x7A0000, 0xFF2020);
//  TooltipFrames.add("hatred", 0x7A0000, 0xFF2020, 0.15);
//  TooltipFrames.add("hatred", 0x7A0000, 0xFF2020, 0.15, 0x3A0505, 0x0E0008);

public class TooltipFrames {

    public static final float DEFAULT_SOFTNESS = 0.2f;

    public record Background(int firstColor, int secondColor, float softness) {}

    public record Frame(int topColor, int bottomColor, double speed, @Nullable Background background) {

        public int colorAt(float position, long nowMs) {
            return 0xFF000000 | slidingColor(new int[]{topColor, bottomColor}, position, speed, nowMs);
        }
    }

    private static final Map<Identifier, Frame> frames = new HashMap<>();

    public static void add(String styleName, int topColor, int bottomColor) {
        add(styleName, topColor, bottomColor, 0);
    }

    public static void add(String styleName, int topColor, int bottomColor, double speed) {
        put(styleName, new Frame(ARGB.opaque(topColor), ARGB.opaque(bottomColor), speed, null));
    }

    public static void add(String styleName, int topColor, int bottomColor, double speed, int backgroundFirst, int backgroundSecond) {
        add(styleName, topColor, bottomColor, speed, backgroundFirst, backgroundSecond, DEFAULT_SOFTNESS);
    }

    public static void add(String styleName, int topColor, int bottomColor, double speed, int backgroundFirst, int backgroundSecond, float softness) {
        Background background = new Background(backgroundFirst & 0xFFFFFF, backgroundSecond & 0xFFFFFF, softness);
        put(styleName, new Frame(ARGB.opaque(topColor), ARGB.opaque(bottomColor), speed, background));
    }

    private static void put(String styleName, Frame frame) {
        frames.put(Identifier.fromNamespaceAndPath(Discs.MOD_ID, styleName), frame);
    }

    public static @Nullable Frame get(@Nullable Identifier style) {
        if (style == null) {
            return null;
        }
        return frames.get(style);
    }

    public static int slidingColor(int[] stops, float position, double speed, long nowMs) {
        double phase = position * 0.5 + (nowMs / 1000.0) * speed;
        phase -= Math.floor(phase);

        double wave = phase < 0.5 ? phase * 2 : (1 - phase) * 2;
        return gradientColor(stops, (float) wave);
    }

    private static int gradientColor(int[] stops, float position) {
        if (stops.length == 1) {
            return stops[0] & 0xFFFFFF;
        }

        float scaled = position * (stops.length - 1);
        int index = Math.min((int) scaled, stops.length - 2);
        float blend = scaled - index;

        int from = stops[index];
        int to = stops[index + 1];

        int red = blendChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, blend);
        int green = blendChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, blend);
        int blue = blendChannel(from & 0xFF, to & 0xFF, blend);
        return (red << 16) | (green << 8) | blue;
    }

    private static int blendChannel(int from, int to, float blend) {
        return Math.round(from + (to - from) * blend);
    }
}