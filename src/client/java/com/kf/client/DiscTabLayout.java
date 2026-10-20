package com.kf.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscTabLayout {

    private static final boolean INLINE_TITLES = true; // basically it makes it like this: Mts (Mts) instead of Mts
    //  this sucks                                           Mts

    public static final float TITLE_SCALE = 1f;

    private static final int MIN_SLOTS = 3;

    private static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;
    private static final Pattern DEMO = Pattern.compile("demo(\\d+)");

    public record Header(String title, int slots) {}

    private static Map<Integer, Header> headers = new HashMap<>();
    private static int rowCount = 0;

    public static List<ItemStack> build(Collection<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();
        Map<Integer, Header> newHeaders = new HashMap<>();
        String currentSection = null;

        for (ItemStack stack : items) {
            String section = sectionOf(stack);

            if (!section.equals(currentSection)) {
                fillRow(result);

                String title = titleOf(section);
                int slots = INLINE_TITLES ? Math.min(slotsFor(title), COLUMNS) : COLUMNS;

                newHeaders.put(result.size() / COLUMNS, new Header(title, slots));
                for (int i = 0; i < slots; i++) {
                    result.add(ItemStack.EMPTY);
                }
                currentSection = section;
            }

            result.add(stack);
        }

        headers = newHeaders;
        rowCount = (result.size() + COLUMNS - 1) / COLUMNS;
        return result;
    }

    public static @Nullable Header headerAt(int row) {
        return headers.get(row);
    }

    public static boolean isHeaderSlot(int row, int col) {
        Header header = headers.get(row);
        return header != null && col >= 0 && col < header.slots();
    }

    public static int rowCount() {
        return rowCount;
    }

    private static int slotsFor(String title) {
        int width = Math.round(Minecraft.getInstance().font.width(title) * TITLE_SCALE);
        return Math.max(MIN_SLOTS, (width + 2 + SLOT_SIZE - 1) / SLOT_SIZE);
    }

    private static void fillRow(List<ItemStack> list) {
        while (list.size() % COLUMNS != 0) {
            list.add(ItemStack.EMPTY);
        }
    }
    private static String sectionOf(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int slash = path.indexOf('/');
        return slash > 0 ? path.substring(0, slash) : "other";
    }

    private static String titleOf(String section) {
        Matcher demo = DEMO.matcher(section);
        if (demo.matches()) {
            return "Demo " + demo.group(1);
        }
        return section.substring(0, 1).toUpperCase() + section.substring(1);
    }
}