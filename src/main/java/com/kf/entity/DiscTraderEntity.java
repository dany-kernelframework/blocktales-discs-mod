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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class DiscTraderEntity extends WanderingTrader {

    // how likely each category is for a single trade slot
    // these are relative, they don't need to add up to 100
    // lower = rarer, these are per category, not per disc
    private static final double boss = 5.0;
    private static final double template = 3.0;
    private static final double normal = 92.0;

    private static final int maxBossPerTrader = 1;
    private static final int maxTemplatePerTrader = 1;

    public DiscTraderEntity(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        if (!offers.isEmpty()) {
            return;
        }

        Item tixItem = Discs.REGISTERED_MATERIALS.get("tix");
        if (tixItem != null) {
            int tixAmount = 1 + this.random.nextInt(8);
            int emeraldCost = tixAmount * 2;

            offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    new ItemStack(tixItem, tixAmount),
                    1,
                    2,
                    0.0f
            ));
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

        List<Category> categories = List.of(
                new Category(boss, maxBossPerTrader, bossDiscs),
                new Category(template, maxTemplatePerTrader, templateItems),
                new Category(normal, Integer.MAX_VALUE, normalDiscs)
        );

        Item currency = tixItem != null ? tixItem : Items.EMERALD;

        int tradeCount = 1 + this.random.nextInt(5);

        for (int i = 0; i < tradeCount; i++) {
            Category category = pickCategory(categories);
            if (category == null) {
                break; // nothing left to sell
            }

            Item item = category.items.remove(this.random.nextInt(category.items.size()));
            category.picked++;

            int price = Discs.discPrices.getOrDefault(item, 5);

            offers.add(new MerchantOffer(
                    new ItemCost(currency, price),
                    new ItemStack(item),
                    1,
                    2,
                    0.0f
            ));
        }
    }

    private @Nullable Category pickCategory(List<Category> categories) {
        double totalWeight = 0.0;
        for (Category c : categories) {
            if (c.canPick()) {
                totalWeight += c.weight;
            }
        }

        if (totalWeight <= 0.0) {
            return null;
        }

        double roll = this.random.nextDouble() * totalWeight;
        double running = 0.0;
        Category lastValid = null;

        for (Category c : categories) {
            if (!c.canPick()) continue;

            running += c.weight;
            lastValid = c;
            if (roll < running) {
                return c;
            }
        }

        return lastValid;
    }

    private static class Category {
        final double weight;
        final int maxPicks;
        final List<Item> items;
        int picked = 0;

        Category(double weight, int maxPicks, List<Item> items) {
            this.weight = weight;
            this.maxPicks = maxPicks;
            this.items = items;
        }

        boolean canPick() {
            return picked < maxPicks && !items.isEmpty();
        }
    }
}