package com.kf;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PulsingName {
    private final String text;
    private final int[] colors;

    private PulsingName(String text, int[] colors) {
        this.text = text;
        this.colors = colors;
    }

    public static @Nullable PulsingName fromBirdflop(String birdflop) {
        StringBuilder text = new StringBuilder();
        List<Integer> colors = new ArrayList<>();

        for (String part : birdflop.split("<#")) {
            if (part.isEmpty()) continue;
            if (part.length() < 7 || part.charAt(6) != '>') return null;

            int color;
            try {
                color = Integer.parseInt(part.substring(0, 6), 16);
            } catch (NumberFormatException e) {
                return null;
            }

            for (char letter : part.substring(7).toCharArray()) {
                text.append(letter);
                colors.add(color);
            }
        }

        if (text.isEmpty()) return null;
        return new PulsingName(text.toString(), colors.stream().mapToInt(Integer::intValue).toArray());
    }

    public Component at(double speed, long nowMs) {
        MutableComponent result = Component.empty();

        for (int i = 0; i < text.length(); i++) {
            float position = text.length() == 1 ? 0f : (float) i / (text.length() - 1);
            int color = TooltipFrames.slidingColor(colors, position, speed, nowMs);

            result.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }

        return result;
    }
}