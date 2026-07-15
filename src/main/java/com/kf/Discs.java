package com.kf;

import com.kf.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NullMarked
public class Discs implements ModInitializer {
	public static final String MOD_ID = "discs";

	public static final Map<String, List<Item>> discsPerChapter = new LinkedHashMap<>(16);
	public static final Map<Item, Integer> discPrices = new HashMap<>(128);
	public static final Set<Item> bossDiscs = new HashSet<>(32);
	public static final Map<String, Item> REGISTERED_DISCS = new HashMap<>(128);

	public static Item tabIcon = null;

	static {
		String[] chapters = {"preprologue", "prologue", "demo1", "demo2", "demo3", "demo4", "demo5", "demo6", "demo7"};
		for (String chapter : chapters) {
			discsPerChapter.put(chapter, new ArrayList<>(16));
		}
	}

	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "main_tab")
	);

	@Override
	public void onInitialize() {
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(mod -> {
			Path itemAssets = mod.findPath("assets/" + MOD_ID + "/items").orElse(null);

			if (itemAssets == null) return;

			try (var files = Files.walk(itemAssets)) {
				files.filter(Files::isRegularFile)
						.filter(file -> file.toString().endsWith(".json"))
						.forEach(file -> {
							Path relativePath = itemAssets.relativize(file);

							if (relativePath.getNameCount() >= 2) {
								String chapterDir = relativePath.getName(0).toString();
								String trackName = relativePath.getFileName().toString().replace(".json", "");
								registerDisc(trackName, chapterDir);
							}
						});
			} catch (Exception e) {
				System.err.println("[Discs] Failed to scan for disc files: " + e.getMessage());
			}
		});

		CreativeModeTab mainTab = FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.discs.main_tab"))
				.icon(() -> new ItemStack(tabIcon != null ? tabIcon : Items.JUKEBOX))
				.displayItems((_, output) ->
						discsPerChapter.values().forEach(chapterList -> chapterList.forEach(output::accept)))
				.build();

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, mainTab);

		ModEntities.register();
		FabricDefaultAttributeRegistry.register(ModEntities.DISC_TRADER, WanderingTrader.createMobAttributes());
		ModCommands.register();

		DiscLoot.register();

		System.out.println("btdiscs correctly loaded yay");
	}

	private static void registerDisc(String trackName, String chapter) {
		String registryPath = chapter + "/" + trackName;

		Identifier audioId = Identifier.fromNamespaceAndPath(MOD_ID, "music." + chapter + "." + trackName);
		SoundEvent audioEvent = SoundEvent.createVariableRangeEvent(audioId);
		Registry.register(BuiltInRegistries.SOUND_EVENT, audioId, audioEvent);

		ResourceKey<Item> discKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, registryPath));
		ResourceKey<JukeboxSong> songData = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(MOD_ID, registryPath));

		boolean isBoss = DiscPricing.isBoss(chapter, trackName);

		Item vinyl = new Item(
				new Item.Properties()
						.setId(discKey)
						.stacksTo(1)
						.jukeboxPlayable(songData)
						.rarity(Rarity.EPIC)
		) {
			@Override
			public Component getName(ItemStack stack) {
				if (isBoss) {
					String birdflopString = DiscPricing.getBossGradient(chapter, trackName);
					if (birdflopString != null) {
						return parseBirdflop(birdflopString);
					}
				}
				return Component.literal("Music Disc");
			}
		};

		Registry.register(BuiltInRegistries.ITEM, discKey, vinyl);

		if (tabIcon == null) {
			tabIcon = vinyl;
		}

		discsPerChapter.computeIfAbsent(chapter, _ -> new ArrayList<>(16)).add(vinyl);
		discPrices.put(vinyl, DiscPricing.getPrice(chapter, trackName));
		REGISTERED_DISCS.put(MOD_ID + ":" + registryPath, vinyl);

		if (isBoss) {
			bossDiscs.add(vinyl);
		}
	}

	private static Component parseBirdflop(String formattedText) {
		MutableComponent component = Component.empty();
		String[] parts = formattedText.split("<#");

		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.isEmpty()) continue;

			if (i == 0 && !formattedText.startsWith("<#")) {
				component.append(Component.literal(part));
				continue;
			}

			// Expected format: "FFFFFF>Text"
			if (part.length() >= 7 && part.charAt(6) == '>') {
				String hex = part.substring(0, 6);
				String text = part.substring(7);

				try {
					int color = Integer.parseInt(hex, 16);
					component.append(Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
				} catch (NumberFormatException e) {
					component.append(Component.literal("<#" + part));
				}
			} else {
				component.append(Component.literal("<#" + part));
			}
		}

		return component;
	}
}