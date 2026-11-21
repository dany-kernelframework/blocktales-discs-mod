package com.kf;

import java.util.Map;

public class DiscPricing {

    private static final int TEMPLATE_PRICE = 30;

    private static final Map<String, Integer> BASE_PRICE = Map.ofEntries(
            Map.entry("preprologue", 2),
            Map.entry("prologue", 3),
            Map.entry("demo1", 4),
            Map.entry("demo2", 5),
            Map.entry("demo3", 6),
            Map.entry("demo4", 7),
            Map.entry("demo5", 8),
            Map.entry("demo6", 9),
            Map.entry("demo7", 10)
    );

    private static final Map<String, Integer> buxprice = Map.ofEntries(
            Map.entry("preprologue", 1),
            Map.entry("prologue", 1),
            Map.entry("demo1", 1),
            Map.entry("demo2", 2),
            Map.entry("demo3", 3),
            Map.entry("demo4", 4),
            Map.entry("demo5", 5),
            Map.entry("demo6", 6),
            Map.entry("demo7", 7)
    );

    public static int getPrice(String chapter, String trackName) {
        if ("materials".equals(chapter) && trackName.endsWith("template")) {
            return TEMPLATE_PRICE;
        }

        if (LabelData.isBuxDisc(chapter + "/" + trackName)) {
            return buxprice.getOrDefault(chapter, 1);
        }

        boolean boss = isBoss(chapter, trackName);
        int base = BASE_PRICE.getOrDefault(chapter, 5);
        return boss ? base + 2 : base;
    }

    // bosses come from assets/discs/bosses/*.json (BossData must be loaded before discs register)
    public static boolean isBoss(String chapter, String trackName) {
        return BossData.isBoss(chapter + "/" + trackName);
    }
}