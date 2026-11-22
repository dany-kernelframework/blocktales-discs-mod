package com.kf.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kf.Discs;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.InputStream;

public final class DiscTabStyle {

    public record Style(int panel, int line, int text, boolean shadow, boolean custom) {}

    public static final Identifier ctt_texture =
            Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");

    private static final Style DEFAULT = new Style(0xFFC6C6C6, 0xFF8B8B8B, 0xFF404040, false, false);
    private static final String file = "tab_style.json";

    private static volatile Style current = DEFAULT;

    public static Style get() {
        return current;
    }

    public static Style load(ResourceManager manager) {
        var resource = manager.getResource(Identifier.fromNamespaceAndPath(Discs.MOD_ID, file));
        if (resource.isEmpty()) {
            return sampleFromVanillaTexture(manager);
        }

        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("auto") && json.get("auto").getAsBoolean()) {
                return sampleFromVanillaTexture(manager);
            }
            return new Style(
                    color(json, "panel", DEFAULT.panel()),
                    color(json, "line", DEFAULT.line()),
                    color(json, "text", DEFAULT.text()),
                    json.has("shadow") ? json.get("shadow").getAsBoolean() : DEFAULT.shadow(),
                    true);
        } catch (Exception e) {
            System.err.println("[Discs] failed loading " + file + ", using defaults (" + e.getMessage() + ")");
            return sampleFromVanillaTexture(manager);
        }
    }

    private static Style sampleFromVanillaTexture(ResourceManager manager) {
        var tex = manager.getResource(ctt_texture);
        if (tex.isEmpty()) return DEFAULT;

        try (InputStream in = tex.get().open(); NativeImage img = NativeImage.read(in)) {
            int panel = sample(img, 175, 10);
            int line = sample(img, 12, 22);
            return new Style(panel, line, DEFAULT.text(), DEFAULT.shadow(), false);
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    private static int sample(NativeImage img, int u, int v) {
        int x = Math.clamp(Math.round(u * img.getWidth() / 256.0f), 0, img.getWidth() - 1);
        int y = Math.clamp(Math.round(v * img.getHeight() / 256.0f), 0, img.getHeight() - 1);
        return img.getPixel(x, y) | 0xFF000000;
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