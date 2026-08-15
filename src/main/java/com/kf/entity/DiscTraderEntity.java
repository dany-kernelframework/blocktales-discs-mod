package com.kf.entity;

import com.kf.Discs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class DiscTraderEntity extends WanderingTrader {

// calcs in category and not per item
    private static final double boss = 15.0;
    private static final double template = 5.0;
    private static final double normal = 80.0;

    public DiscTraderEntity(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        if (!offers.isEmpty()) {
            return;
        }

        List<Item> normalDiscs = new ArrayList<>();
        List<Item> bossDiscs = new ArrayList<>();
        List<Item> templateItems = new ArrayList<>(Discs.TEMPLATES);

        Discs.discsPerChapter.values().forEach(chapterDiscs -> {
            for (Item disc : chapterDiscs) {
                if (Discs.bossDiscs.contains(disc)) {
                    bossDiscs.add(disc);
                } else {
                    normalDiscs.add(disc);
                }
            }
        });

        List<WeightedItem> pool = new ArrayList<>(bossDiscs.size() + templateItems.size() + normalDiscs.size());
        addWeighted(pool, bossDiscs, boss);
        addWeighted(pool, templateItems, template);
        addWeighted(pool, normalDiscs, normal);

        int tradeCount = 1 + this.random.nextInt(5);

        for (int i = 0; i < tradeCount && !pool.isEmpty(); i++) {
            Item chosenItem = drawWithoutReplacement(pool);
            int price = Discs.discPrices.getOrDefault(chosenItem, 5);

            offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, price),
                    new ItemStack(chosenItem),
                    1,
                    2,
                    0.0f
            ));
        }
    }

    private static void addWeighted(List<WeightedItem> pool, List<Item> items, double categoryWeight) {
        if (items.isEmpty()) return;
        double perItemWeight = categoryWeight / items.size();
        for (Item item : items) {
            pool.add(new WeightedItem(item, perItemWeight));
        }
    }

    private Item drawWithoutReplacement(List<WeightedItem> pool) {
        double totalWeight = 0.0;
        for (WeightedItem entry : pool) {
            totalWeight += entry.weight();
        }

        double roll = this.random.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (int i = 0; i < pool.size(); i++) {
            cumulative += pool.get(i).weight();
            if (roll < cumulative) {
                return pool.remove(i).item();
            }
        }

        return pool.removeLast().item();
    }

    private record WeightedItem(Item item, double weight) {}
}