package com.kf.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kf.Discs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;

// technically rps could change this but uh yes
public final class DiscTabStyle {

    public record Style(int panel, int line, int text, boolean shadow) {}

    private static final Style DEFAULT = new Style(0xFFC6C6C6, 0xFF8B8B8B, 0xFF404040, false);
    private static final String file = "tab_style.json";

    private static volatile Style current = DEFAULT;

    public static Style get() {
        return current;
    }

    public static Style load(ResourceManager manager) {
        var resource = manager.getResource(Identifier.fromNamespaceAndPath(Discs.MOD_ID, file));
        if (resource.isEmpty()) return DEFAULT;

        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return new Style(
                    color(json, "panel", DEFAULT.panel()),
                    color(json, "line", DEFAULT.line()),
                    color(json, "text", DEFAULT.text()),
                    json.has("shadow") ? json.get("shadow").getAsBoolean() : DEFAULT.shadow());
        } catch (Exception e) {
            System.err.println("[Discs] failed loading " + file + ", using defaults (" + e.getMessage() + ")");
            return DEFAULT;
        }
    }

    public static void install(Style style) {
        current = style;
    }

    private static int color(JsonObject json, String key, int fallback) {
        if (!json.has(key)) return fallback;
        String hex = json.get(key).getAsString().replace("#", "").trim();
        try {
            long value = Long.parseLong(hex, 16);
            return hex.length() <= 6 ? (int) (0xFF000000L | value) : (int) value;
        } catch (NumberFormatException e) {
            System.err.println("[Discs] invalid color '" + hex + "' for '" + key + "' in " + file);
            return fallback;
        }
    }
}