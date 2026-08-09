package com.kf;

import java.util.Map;

public class DiscPricing {

    private static final int TEMPLATE_PRICE = 15;

    private static final Map<String, Integer> BASE_PRICE = Map.ofEntries(
            Map.entry("preprologue", 2),
            Map.entry("prologue", 4),
            Map.entry("demo1", 5),
            Map.entry("demo2", 6),
            Map.entry("demo3", 7),
            Map.entry("demo4", 8),
            Map.entry("demo5", 9),
            Map.entry("demo6", 10),
            Map.entry("demo7", 11)
    );

    private static final Map<String, String> BOSS_GRADIENTS = Map.of(
            "prologue/noobador", "<#FFC000>M<#F7A30C>u<#F08719>s<#E86A25>i<#E14E32>c <#E3552F>D<#EC791F>i<#F69C10>s<#FFC000>c",
            "demo1/cruelking", "<#DCCAFF>M<#E3D5FF>u<#EADFFF>s<#F1EAFF>i<#F8F4FF>c <#F6F2FF>D<#EEE5FF>i<#E5D7FF>s<#DCCAFF>c",
            "demo4/theancients", "<#AB8000>M<#BC6604>u<#CD4D08>s<#DD330C>i<#EE1A10>c <#EA200F>D<#D5400A>i<#C06005>s<#AB8000>c"
    );

    public static int getPrice(String chapter, String trackName) {
        String fullPath = chapter + "/" + trackName;

        if ("materials".equals(chapter) && trackName.endsWith("template")) {
            return TEMPLATE_PRICE;
        }

        boolean boss = BOSS_GRADIENTS.containsKey(fullPath);
        int base = BASE_PRICE.getOrDefault(chapter, 5);
        return boss ? base + 1 : base;
    }

    public static boolean isBoss(String chapter, String trackName) {
        return BOSS_GRADIENTS.containsKey(chapter + "/" + trackName);
    }

    public static String getBossGradient(String chapter, String trackName) {
        return BOSS_GRADIENTS.get(chapter + "/" + trackName);
    }
}