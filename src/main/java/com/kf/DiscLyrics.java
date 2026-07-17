package com.kf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

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
                lines.add(new LyricLine(obj.get("time").getAsFloat(), obj.get("text").getAsString()));
            });
            lines.sort((a, b) -> Float.compare(a.time(), b.time()));

            LYRICS.put(track, Collections.unmodifiableList(lines));
        } catch (Exception e) {
            System.err.println("[Discs] Failed loading lyrics file: " + file + " (" + e.getMessage() + ")");
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

    public record LyricLine(float time, String text) {}
}