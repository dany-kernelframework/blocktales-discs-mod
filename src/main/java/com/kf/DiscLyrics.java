package com.kf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * loads lyric timing data from data/discs/lyrics/z.json (used the same scanning pattern as discloot)
 * only sounds that have a matching file here will show lyric, everything else plays normally
 * keyed: "<chapter>/<trackName>".
 *
 * each line's "color" is optional and can be a hex RGB string, with or without a #
 * (e.g. AA6767 or #AA6161). lines without one just use the default white/null
 *
 **/
public class DiscLyrics {

    private static final Map<String, List<LyricLine>> LYRICS = new HashMap<>();

    public static void register() {
        FabricLoader.getInstance().getModContainer(Discs.MOD_ID).ifPresent(container -> {
            var path = container.findPath("data/" + Discs.MOD_ID + "/lyrics");
            if (path.isEmpty()) return;

            try (var files = Files.walk(path.get())) {
                files.filter(f -> f.toString().endsWith(".json")).forEach(DiscLyrics::loadFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String track = json.get("track").getAsString();

            List<LyricLine> lines = new ArrayList<>();
            json.getAsJsonArray("lines").forEach(el -> {
                JsonObject obj = el.getAsJsonObject();
                float time = obj.get("time").getAsFloat();
                String text = obj.get("text").getAsString();
                Integer color = parseColor(obj, file);
                lines.add(new LyricLine(time, text, color));
            });
            lines.sort((a, b) -> Float.compare(a.time(), b.time()));

            LYRICS.put(track, Collections.unmodifiableList(lines));
        } catch (Exception e) {
            System.err.println("[Discs] Failed loading lyrics file: " + file + " (" + e.getMessage() + ")");
        }
    }

    private static @Nullable Integer parseColor(JsonObject lineObj, Path file) {
        if (!lineObj.has("color")) {
            return null;
        }
        String hex = lineObj.get("color").getAsString().replace("#", "").trim();
        try {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            System.err.println("[Discs] invalid lyric color '" + hex + "' in " + file + " — using default white"); //fallback
            return null;
        }
    }

    public static boolean hasLyrics(String chapter, String trackName) {
        return LYRICS.containsKey(chapter + "/" + trackName);
    }

    public static List<LyricLine> get(String chapter, String trackName) {
        return LYRICS.getOrDefault(chapter + "/" + trackName, List.of());
    }

    public static boolean hasLyrics(String combinedKey) {
        return LYRICS.containsKey(combinedKey);
    }

    public static List<LyricLine> get(String combinedKey) {
        return LYRICS.getOrDefault(combinedKey, List.of());
    }

    // color is a 0xRRGGBB value (no alpha ) use null to use white aka null (see lyricshud)
    public record LyricLine(float time, String text, @Nullable Integer color) {}
}