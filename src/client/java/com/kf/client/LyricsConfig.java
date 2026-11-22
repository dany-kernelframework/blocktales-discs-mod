package com.kf.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

// hardcoded configs.. wow, what an amazing programmer am i!
public final class LyricsConfig {

    public static final class Settings {
        public boolean enabled = true;
        public int yOffset = 60;
        public float scale = 1.0f;
        public float opacity = 1.0f;
        public boolean shadow = true;
        public boolean background = false;
        public boolean showNextLine = true;
        public boolean effects = true;
        public int shakeStrength = 2;
        public String instrumental = "dim";
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile Settings current = new Settings();

    public static Settings get() {
        return current;
    }

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("discs-lyrics.json");
        Settings loaded = new Settings();

        if (Files.exists(file)) {
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                Settings parsed = GSON.fromJson(reader, Settings.class);
                if (parsed != null) loaded = parsed;
            } catch (Exception e) {
                System.err.println("[Discs] failed reading discs-lyrics.json, using defaults (" + e.getMessage() + ")");
            }
        }

        loaded.scale = Math.clamp(loaded.scale, 0.5f, 3.0f);
        loaded.opacity = Math.clamp(loaded.opacity, 0.0f, 1.0f);
        loaded.shakeStrength = Math.clamp(loaded.shakeStrength, 1, 8);
        current = loaded;

        // tries to write the file itself
        try {
            Files.writeString(file, GSON.toJson(loaded));
        } catch (Exception e) {
            System.err.println("[Discs] failed writing discs-lyrics.json (" + e.getMessage() + ")");
        }
    }
}