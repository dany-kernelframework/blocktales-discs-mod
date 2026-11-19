package com.kf.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kf.Discs;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiscLabels {

    private static final int def_color = 0xAAAAAA;
    private static final String file = "labels.json";

    public record Data(@Nullable Component header, Map<String, List<Component>> byDisc) {}

    private static volatile Data data = new Data(null, Map.of());

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, _, _, lines) -> {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!itemId.getNamespace().equals(Discs.MOD_ID)) return;

            Data current = data;
            List<Component> extra = current.byDisc().get(itemId.getPath());
            if (extra == null) return;

            List<Component> block = new ArrayList<>();
            // block.add(Component.empty());
                // basically it adds another line but uhh yes
            if (current.header() != null) {
                block.add(current.header());
            }
            block.addAll(extra);

            int index = lines.size();
            String description = AnimatedDiscText.songDescription(itemId.getPath());
            if (description != null) {
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).getString().equals(description)) {
                        index = i + 1;
                        break;
                    }
                }
            }
            lines.addAll(index, block);
        });
    }

    public static Data load(ResourceManager manager) {
        var resource = manager.getResource(Identifier.fromNamespaceAndPath(Discs.MOD_ID, file));
        if (resource.isEmpty()) return new Data(null, Map.of());

        try (BufferedReader reader = resource.get().openAsReader()) {
            return parse(reader);
        } catch (Exception e) {
            System.err.println("[Discs] failed loading " + file + " (" + e.getMessage() + ")");
            return new Data(null, Map.of());
        }
    }

    public static void install(Data parsed) {
        data = parsed;
    }

    private static Data parse(Reader reader) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

        Component header = json.has("header") ? styled(json.getAsJsonObject("header")) : null;

        Map<String, Component> tags = new HashMap<>();
        for (var entry : json.getAsJsonObject("tags").entrySet()) {
            tags.put(entry.getKey(), styled(entry.getValue().getAsJsonObject()));
        }

        Map<String, List<Component>> byDisc = new HashMap<>();
        for (var entry : json.getAsJsonObject("discs").entrySet()) {
            List<Component> lines = new ArrayList<>();
            for (JsonElement el : entry.getValue().getAsJsonArray()) {
                Component line = tags.get(el.getAsString());
                if (line == null) {
                    System.err.println("[Discs] unknown label tag '" + el.getAsString() + "' on " + entry.getKey());
                } else {
                    lines.add(line);
                }
            }
            if (!lines.isEmpty()) byDisc.put(entry.getKey(), List.copyOf(lines));
        }
        return new Data(header, Map.copyOf(byDisc));
    }

    private static Component styled(JsonObject obj) {
        int color = obj.has("color")
                ? Integer.parseInt(obj.get("color").getAsString().replace("#", "").trim(), 16) & 0xFFFFFF
                : def_color;
        boolean italic = obj.has("italic") && obj.get("italic").getAsBoolean();

        return Component.literal(obj.get("text").getAsString())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withItalic(italic));
    }
}