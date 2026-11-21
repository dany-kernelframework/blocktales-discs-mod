package com.kf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jspecify.annotations.NullMarked;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@NullMarked
public final class LabelData {

    private static final Set<String> buxtag = Set.of("scrapped", "removed", "placeholder");
    private static final Set<String> buxdisc = new HashSet<>();

    private LabelData() {}

    public static void loadFromJar(Path labelsFile) {
        buxdisc.clear();
        if (!Files.isRegularFile(labelsFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(labelsFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("discs")) {
                return;
            }

            JsonObject discs = json.getAsJsonObject("discs");
            for (var entry : discs.entrySet()) {
                for (JsonElement tag : entry.getValue().getAsJsonArray()) {
                    if (buxtag.contains(tag.getAsString())) {
                        buxdisc.add(entry.getKey());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Discs] failed to load labels.json for BUX discs: " + e.getMessage());
        }
    }

    public static boolean isBuxDisc(String registryPath) {
        return buxdisc.contains(registryPath);
    }
}