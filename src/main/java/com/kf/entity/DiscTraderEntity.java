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

    private static final int boss = 10;
    private static final int template = 20;
    private static final int normal = 70;

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
        boolean templateAvailable = (Discs.templateDisc != null);

        Discs.discsPerChapter.values().forEach(chapterDiscs -> {
            for (Item disc : chapterDiscs) {
                if (Discs.bossDiscs.contains(disc)) {
                    bossDiscs.add(disc);
                } else {
                    normalDiscs.add(disc);
                }
            }
        });

        int tradeCount = 1 + this.random.nextInt(5);

        for (int i = 0; i < tradeCount; i++) {
            int currentBossWeight = bossDiscs.isEmpty() ? 0 : boss;
            int currentTemplateWeight = templateAvailable ? template : 0;
            int currentNormalWeight = normalDiscs.isEmpty() ? 0 : normal;

            int totalWeight = currentBossWeight + currentTemplateWeight + currentNormalWeight;
            if (totalWeight == 0) {
                break;
            }

            int roll = this.random.nextInt(totalWeight);
            Item chosenItem;

            if (roll < currentBossWeight) {
                chosenItem = bossDiscs.remove(this.random.nextInt(bossDiscs.size()));
            } else if (roll < currentBossWeight + currentTemplateWeight) {
                chosenItem = Discs.templateDisc;
                templateAvailable = false;
            } else {
                chosenItem = normalDiscs.remove(this.random.nextInt(normalDiscs.size()));
            }

            if (chosenItem != null) {
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
    }
}