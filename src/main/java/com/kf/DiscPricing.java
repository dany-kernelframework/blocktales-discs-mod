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

    private static final Map<String, String> BOSS_GRADIENTS = Map.ofEntries(
            Map.entry("prologue/noobador", "<#FFC000>M<#F7A30C>u<#F08719>s<#E86A25>i<#E14E32>c <#E3552F>D<#EC791F>i<#F69C10>s<#FFC000>c"),
            Map.entry("demo1/cruelking", "<#DCCAFF>M<#E3D5FF>u<#EADFFF>s<#F1EAFF>i<#F8F4FF>c <#F6F2FF>D<#EEE5FF>i<#E5D7FF>s<#DCCAFF>c"),
            Map.entry("demo2/bubonicplant", "<#A47215>M<#926219>u<#80521D>s<#6E4121>i<#5C3125>c <#613524>D<#774A1F>i<#8E5E1A>s<#A47215>c"),
            Map.entry("demo2/suprememosquito", "<#946B73>M<#8C7372>u<#847B70>s<#7B846F>i<#738C6D>c <#758A6E>D<#808070>i<#8A7571>s<#946B73>c"),
            Map.entry("demo3/greed", "<#C8FFAB>M<#CDF799>u<#D2EE88>s<#D6E676>i<#DBDD64>c <#E5CC41>D<#E9C42F>i<#EEBB1E>s<#F3B30C>c"),
            Map.entry("demo3/solitude", "<#6E69B0>M<#605C99>u<#534F82>s<#45436A>i<#383653>c <#3B3959>D<#4C4976>i<#5D5993>s<#6E69B0>c"),
            Map.entry("demo3/fear", "<#9500BE>M<#7F00A1>u<#6A0084>s<#540067>i<#3F004A>c <#440051>D<#5F0076>i<#7A009A>s<#9500BE>c"),
            Map.entry("demo3/hatred", "<#7A0000>M<#950606>u<#AF0D0D>s<#CA1313>i<#E41A1A>c <#DE1818>D<#BD1010>i<#9B0808>s<#7A0000>c"),
            Map.entry("demo4/theancients", "<#AB8000>M<#BC6604>u<#CD4D08>s<#DD330C>i<#EE1A10>c <#EA200F>D<#D5400A>i<#C06005>s<#AB8000>c")
    );

    // the style name has to match the one used in TooltipFrames.add(...) in DiscsClient
    private static final Map<String, String> TOOLTIP_STYLES = Map.ofEntries(
            Map.entry("prologue/noobador", "noobador"),
            Map.entry("demo1/cruelking", "cruelking"),
            Map.entry("demo2/bubonicplant", "bubonicplant"),
            Map.entry("demo2/suprememosquito", "suprememosquito"),
            Map.entry("demo3/hatred", "hatred"),
            Map.entry("demo3/fear", "fear"),
            Map.entry("demo3/solitude", "solitude"),
            Map.entry("demo3/greed", "greed"),
            Map.entry("demo4/theancients", "theancients")
    );

    public static int getPrice(String chapter, String trackName) {
        String fullPath = chapter + "/" + trackName;

        if ("materials".equals(chapter) && trackName.endsWith("template")) {
            return TEMPLATE_PRICE;
        }

        boolean boss = BOSS_GRADIENTS.containsKey(fullPath);
        int base = BASE_PRICE.getOrDefault(chapter, 5);
        return boss ? base + 2 : base;
    }

    public static boolean isBoss(String chapter, String trackName) {
        return BOSS_GRADIENTS.containsKey(chapter + "/" + trackName);
    }

    public static String getBossGradient(String chapter, String trackName) {
        return BOSS_GRADIENTS.get(chapter + "/" + trackName);
    }

    public static String getTooltipStyle(String chapter, String trackName) {
        return TOOLTIP_STYLES.get(chapter + "/" + trackName);
    }
}