package com.kf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DiscLyrics {

    public enum Effect {
        none, glitch, shake, scramble;

        static Effect parse(@Nullable String name, Effect fallback) { // fuck you
            if (name == null) return fallback;
            try {
                return valueOf(name.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                System.err.println("[Discs] unknown lyric effect '" + name + "'");
                return fallback;
            }
        }
    }

    public record Word(float time, String text) {}

    // end is -1 when not set, words empty for plain lines
    public record LyricLine(float time, float end, String text, List<Word> words,
                            @Nullable Integer color, @Nullable Effect effect, boolean instrumental) {}

    public record Song(Effect effect, @Nullable Integer color, float offset, List<LyricLine> lines) {}

    private static volatile Map<String, Song> songs = Map.of();

    public static boolean hasLyrics(String combinedKey) {
        return songs.containsKey(combinedKey);
    }

    public static @Nullable Song song(String combinedKey) {
        return songs.get(combinedKey);
    }

    public static List<LyricLine> get(String combinedKey) {
        Song song = songs.get(combinedKey);
        return song == null ? List.of() : song.lines();
    }

    // reload step 1 (off-thread)
    public static Map<String, Song> parseAll(ResourceManager manager) {
        FileToIdConverter converter = FileToIdConverter.json("lyrics");
        Map<String, Song> parsed = new HashMap<>();

        converter.listMatchingResources(manager).forEach((file, resource) -> {
            if (!file.getNamespace().equals(Discs.MOD_ID)) return;
            try (BufferedReader reader = resource.openAsReader()) {
                parseSong(file.toString(), reader, parsed);
            } catch (Exception e) {
                System.err.println("[Discs] failed loading lyrics file " + file + " (" + e.getMessage() + ")");
            }
        });

        return parsed;
    }

    // reload step 2
    public static void install(Map<String, Song> parsed) {
        songs = Map.copyOf(parsed);
    }

    private static void parseSong(String fileName, Reader reader, Map<String, Song> out) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        String track = json.get("track").getAsString();

        Effect songEffect = Effect.parse(str(json, "effect"), Effect.none);
        Integer songColor = color(json, fileName);
        float offset = json.has("offset") ? json.get("offset").getAsFloat() : 0f;

        List<LyricLine> lines = new ArrayList<>();
        for (JsonElement el : json.getAsJsonArray("lines")) {
            JsonObject obj = el.getAsJsonObject();
            float time = obj.get("time").getAsFloat();
            float end = obj.has("end") ? obj.get("end").getAsFloat() : -1f;

            List<Word> words = new ArrayList<>();
            if (obj.has("words")) {
                for (JsonElement w : obj.getAsJsonArray("words")) {
                    JsonObject wo = w.getAsJsonObject();
                    words.add(new Word(wo.get("time").getAsFloat(), wo.get("text").getAsString()));
                }
                words.sort((a, b) -> Float.compare(a.time(), b.time()));
            }

            String text = str(obj, "text");
            if (text == null) {
                StringBuilder joined = new StringBuilder();
                for (Word w : words) joined.append(w.text());
                text = joined.toString();
            }

            boolean instrumental = "instrumental".equals(str(obj, "type")) || text.trim().equals("\u266B");
            if (instrumental && text.isBlank()) text = "\u266B"; // this is just because intelliJ is stupid. i keep the unicode char because who knows

            Effect effect = obj.has("effect") ? Effect.parse(str(obj, "effect"), Effect.none) : null;
            lines.add(new LyricLine(time, end, text, List.copyOf(words), color(obj, fileName), effect, instrumental));
        }
        lines.sort((a, b) -> Float.compare(a.time(), b.time()));

        out.put(track, new Song(songEffect, songColor, offset, List.copyOf(lines)));
    }

    private static @Nullable String str(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : null;
    }

    private static @Nullable Integer color(JsonObject obj, String fileName) {
        if (!obj.has("color")) return null;
        String hex = obj.get("color").getAsString().replace("#", "").trim();
        try {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            System.err.println("[Discs] invalid lyric color '" + hex + "' in " + fileName);
            return null;
        }
    }
}
