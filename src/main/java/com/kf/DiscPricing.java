package com.kf;

import java.util.Map;

public class DiscPricing {

    private static final Map<String, Integer> BASE_PRICE = Map.of(
            "preprologue", 2,
            "prologue", 4,
            "demo1", 5,
            "demo2", 6,
            "demo3", 7,
            "demo4", 8,
            "demo5", 9,
            "demo6", 10,
            "demo7", 11
    );

    // special category for "kinda" discs
    private static final Map<String, Integer> SPECIAL_PRICES = Map.of(
            "materials/template", 15
    );

    private static final Map<String, String> BOSS_GRADIENTS = Map.of(
            "prologue/noobador", "<#FFC000>M<#F7A30C>u<#F08719>s<#E86A25>i<#E14E32>c <#E3552F>D<#EC791F>i<#F69C10>s<#FFC000>c",
            "demo1/cruelking", "<#DCCAFF>M<#E3D5FF>u<#EADFFF>s<#F1EAFF>i<#F8F4FF>c <#F6F2FF>D<#EEE5FF>i<#E5D7FF>s<#DCCAFF>c",
            "demo4/theancients", "<#AB8000>M<#BC6604>u<#CD4D08>s<#DD330C>i<#EE1A10>c <#EA200F>D<#D5400A>i<#C06005>s<#AB8000>c"
    );

    public static int getPrice(String chapter, String trackName) {
        String fullPath = chapter + "/" + trackName;

        if (SPECIAL_PRICES.containsKey(fullPath)) {
            return SPECIAL_PRICES.get(fullPath);
        }

        int base = BASE_PRICE.getOrDefault(chapter, 5);
        return isBoss(chapter, trackName) ? base + 1 : base;
    }

    public static boolean isBoss(String chapter, String trackName) {
        return BOSS_GRADIENTS.containsKey(chapter + "/" + trackName);
    }

    public static String getBossGradient(String chapter, String trackName) {
        return BOSS_GRADIENTS.get(chapter + "/" + trackName);
    }
}