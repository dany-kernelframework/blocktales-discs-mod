package com.kf.client;

import com.kf.TooltipFrames;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;


// usage (client side)
//  AnimatedDiscText.add("artist - song", AnimatedDiscText.Gradient.of(0x7A0000, 0xFF2020), "phrase one", "phrase two");
//  AnimatedDiscText.init();
public class AnimatedDiscText {

    private static final Gradient NORMAL_LOOK = Gradient.of(0xAAAAAA);

    private static final long ORIGINAL_STAYS = 12000;
    private static final long ORIGINAL_SCRAMBLES = 1200;
    private static final long PHRASE_APPEARS = 900;
    private static final long PHRASE_STAYS = 2500;
    private static final long PHRASE_SCRAMBLES = 800;
    private static final long ORIGINAL_RETURNS = 1400;

    private static final long CYCLE_LENGTH = ORIGINAL_STAYS + ORIGINAL_SCRAMBLES + PHRASE_APPEARS
            + PHRASE_STAYS + PHRASE_SCRAMBLES + ORIGINAL_RETURNS;

    private static final long GLITCH_TICK = 70;
    private static final float GLITCH_BURST_CHANCE = 0.12f;
    private static final float GLITCH_LETTER_CHANCE = 0.2f;
    private static final float FLICKER_AMOUNT = 0.1f;

    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    // original must be the same exact desc used in jukebox_song/.. .json, otherwise it will explode... probably
    public static void add(String original, Gradient phraseLook, String... phrases) {
        if (phrases.length == 0) {
            throw new IllegalArgumentException("need at least one phrase for: " + original);
        }
        ENTRIES.put(original, new Entry(original, phraseLook, List.of(phrases)));
    }

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, _, _, lines) -> {
            double speed = pulseSpeed(stack);
            lines.replaceAll(line -> replaceLine(line, speed));
        });
    }

    private static double pulseSpeed(ItemStack stack) {
        Identifier style = stack.get(DataComponents.TOOLTIP_STYLE);
        TooltipFrames.Frame frame = TooltipFrames.get(style);
        return frame == null ? 0 : frame.speed();
    }

    private static Component replaceLine(Component line, double speed) {
        Entry entry = ENTRIES.get(line.getString());
        if (entry == null) {
            return line;
        }
        return entry.line(System.currentTimeMillis(), speed);
    }

    public static final class Gradient {
        private final int[] stops;

        private Gradient(int[] stops) {
            this.stops = stops;
        }

        public static Gradient of(int... stops) {
            if (stops.length == 0) {
                throw new IllegalArgumentException("a gradient needs at least one color");
            }
            return new Gradient(stops);
        }

        int colorAt(float position, double speed, long nowMs) {
            return TooltipFrames.slidingColor(stops, position, speed, nowMs);
        }
    }

    private record Entry(String original, Gradient phraseLook, List<String> phrases) {

        Component line(long now, double speed) {
            long cycleNumber = now / CYCLE_LENGTH;
            long timeIntoCycle = now % CYCLE_LENGTH;

            long seed = cycleNumber * 1000 + original.hashCode();
            String phrase = pickPhrase(cycleNumber);

            if (timeIntoCycle < ORIGINAL_STAYS) {
                return sitting(original, NORMAL_LOOK, now, speed, seed + 1);
            }
            timeIntoCycle -= ORIGINAL_STAYS;

            if (timeIntoCycle < ORIGINAL_SCRAMBLES) {
                return disappearing(original, NORMAL_LOOK, (float) timeIntoCycle / ORIGINAL_SCRAMBLES, now, speed, seed + 2);
            }
            timeIntoCycle -= ORIGINAL_SCRAMBLES;

            if (timeIntoCycle < PHRASE_APPEARS) {
                return appearing(phrase, phraseLook, (float) timeIntoCycle / PHRASE_APPEARS, now, speed, seed + 3);
            }
            timeIntoCycle -= PHRASE_APPEARS;

            if (timeIntoCycle < PHRASE_STAYS) {
                return sitting(phrase, phraseLook, now, speed, seed + 4);
            }
            timeIntoCycle -= PHRASE_STAYS;

            if (timeIntoCycle < PHRASE_SCRAMBLES) {
                return disappearing(phrase, phraseLook, (float) timeIntoCycle / PHRASE_SCRAMBLES, now, speed, seed + 5);
            }
            timeIntoCycle -= PHRASE_SCRAMBLES;

            return appearing(original, NORMAL_LOOK, (float) timeIntoCycle / ORIGINAL_RETURNS, now, speed, seed + 6);
        }
        private String pickPhrase(long cycleNumber) {
            int count = phrases.size();

            if (count <= 2) {
                return phrases.get((int) Math.floorMod(cycleNumber + original.hashCode(), (long) count));
            }

            long round = cycleNumber / count;
            int slot = (int) (cycleNumber % count);

            List<Integer> order = shuffledOrder(round);
            int lastOfPreviousRound = shuffledOrder(round - 1).get(count - 1);

            if (order.getFirst() == lastOfPreviousRound) {
                Collections.swap(order, 0, 1);
            }

            return phrases.get(order.get(slot));
        }

        private List<Integer> shuffledOrder(long round) {
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < phrases.size(); i++) {
                order.add(i);
            }
            Collections.shuffle(order, new Random(mix(round * 31 + original.hashCode())));
            return order;
        }
    }

    private static Component sitting(String text, Gradient look, long now, double speed, long seed) {
        long glitchTick = now / GLITCH_TICK;
        boolean glitching = noise(seed * 7 + glitchTick) < GLITCH_BURST_CHANCE;
        return build(text, look, now, speed, letter -> glitching && noise(seed * 13 + glitchTick * 57 + letter) < GLITCH_LETTER_CHANCE);
    }

    private static Component appearing(String text, Gradient look, float progress, long now, double speed, long seed) {
        long glitchTick = now / GLITCH_TICK;
        return build(text, look, now, speed, letter -> !hasFlipped(letter, progress, glitchTick, seed));
    }

    private static Component disappearing(String text, Gradient look, float progress, long now, double speed, long seed) {
        long glitchTick = now / GLITCH_TICK;
        return build(text, look, now, speed, letter -> hasFlipped(letter, progress, glitchTick, seed));
    }

    private static boolean hasFlipped(int letter, float progress, long glitchTick, long seed) {
        float flipMoment = 0.12f + 0.76f * noise(seed * 31 + letter);
        float flicker = (noise(seed * 17 + glitchTick * 101 + letter) - 0.5f) * 2 * FLICKER_AMOUNT;
        return flipMoment + flicker <= progress;
    }
    private static Component build(String text, Gradient look, long now, double speed, IntPredicate isScrambled) {
        MutableComponent result = Component.empty();

        StringBuilder currentRun = new StringBuilder();
        int runColor = 0;
        boolean runScrambled = false;

        for (int i = 0; i < text.length(); i++) {
            float position = text.length() == 1 ? 0f : (float) i / (text.length() - 1);
            int color = look.colorAt(position, speed, now);
            boolean scrambled = isScrambled.test(i);

            boolean runChanged = color != runColor || scrambled != runScrambled;
            if (!currentRun.isEmpty() && runChanged) {
                result.append(piece(currentRun.toString(), runColor, runScrambled));
                currentRun.setLength(0);
            }

            currentRun.append(text.charAt(i));
            runColor = color;
            runScrambled = scrambled;
        }

        if (!currentRun.isEmpty()) {
            result.append(piece(currentRun.toString(), runColor, runScrambled));
        }

        return result;
    }

    private static Component piece(String text, int color, boolean scrambled) {
        MutableComponent piece = Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
        if (scrambled) {
            piece.withStyle(ChatFormatting.OBFUSCATED);
        }
        return piece;
    }

    private static float noise(long seed) {
        return (mix(seed) >>> 40) / (float) (1 << 24);
    }

    // splitmix64
    private static long mix(long value) {
        value += 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
// too much maths i think ill implode