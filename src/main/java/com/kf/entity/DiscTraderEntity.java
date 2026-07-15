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
import java.util.Collections;
import java.util.List;

@NullMarked
public class DiscTraderEntity extends WanderingTrader {

    private static final int NORMAL_WEIGHT = 4;
    private static final int BOSS_WEIGHT = 1; // tells the mod to make bosses (e.g. noobador 1:4 times rarer)
    private static final int TRADE_COUNT = 3;

    public DiscTraderEntity(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        if (!offers.isEmpty()) {
            return;
        }

        List<Item> weightedPool = generateWeightedPool();
        if (weightedPool.isEmpty()) {
            return;
        }

        for (int i = 0; i < TRADE_COUNT && !weightedPool.isEmpty(); i++) {
            Item chosenItem = weightedPool.get(this.random.nextInt(weightedPool.size()));
            int price = Discs.discPrices.getOrDefault(chosenItem, 5);

            MerchantOffer offer = new MerchantOffer(
                    new ItemCost(Items.EMERALD, price),
                    new ItemStack(chosenItem),
                    1,
                    2,
                    0.0f
            );

            offers.add(offer);

            // Massively faster than .removeIf() inside a loop
            weightedPool.removeAll(Collections.singleton(chosenItem));
        }
    }

    private List<Item> generateWeightedPool() {
        // Pre-sizing array to ~400 to prevent memory resizing during loop
        List<Item> pool = new ArrayList<>(400);

        Discs.discsPerChapter.values().forEach(chapterDiscs -> {
            for (Item disc : chapterDiscs) {
                int weight = Discs.bossDiscs.contains(disc) ? BOSS_WEIGHT : NORMAL_WEIGHT;
                for (int i = 0; i < weight; i++) {
                    pool.add(disc);
                }
            }
        });

        return pool;
    }
}