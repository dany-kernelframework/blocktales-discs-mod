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
import org.jspecify.annotations.Nullable;

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

	public static final String[] CHAPTER_ORDER = {
			"preprologue", "prologue", "demo1", "demo2", "demo3", "demo4", "demo5", "demo6", "demo7"
	};

	public static final Map<String, List<Item>> discsPerChapter = new LinkedHashMap<>(16);
	public static final List<Item> modMaterials = new ArrayList<>();
	public static final Map<Item, Integer> discPrices = new HashMap<>(128);
	public static final Set<Item> bossDiscs = new HashSet<>(32);
	public static final Map<String, Item> REGISTERED_DISCS = new HashMap<>(128);

	public static @Nullable Item tabIcon = null;
	public static @Nullable Item templateDisc = null;

	static {
		for (String chapter : CHAPTER_ORDER) {
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
			Map<String, List<String>> discoveredItems = new HashMap<>();

			try (var files = Files.walk(itemAssets)) {
				files.filter(Files::isRegularFile)
						.filter(file -> file.toString().endsWith(".json"))
						.sorted()
						.forEach(file -> {
							Path relativePath = itemAssets.relativize(file);

							if (relativePath.getNameCount() >= 2) {
								String folderDir = relativePath.getName(0).toString();
								String itemName = relativePath.getFileName().toString().replace(".json", "");

								discoveredItems.computeIfAbsent(folderDir, _ -> new ArrayList<>()).add(itemName);
							}
						});
			} catch (Exception e) {
				System.err.println("[Discs] failed to scan for item files: " + e.getMessage());
			}
			if (discoveredItems.containsKey("materials")) {
				for (String itemName : discoveredItems.get("materials")) {
					registerMaterial(itemName);
				}
			}

			for (String chapter : CHAPTER_ORDER) {
				if (discoveredItems.containsKey(chapter)) {
					for (String trackName : discoveredItems.get(chapter)) {
						registerDisc(trackName, chapter);
					}
				}
			}
			for (String folder : discoveredItems.keySet()) {
				if (!folder.equals("materials") && !List.of(CHAPTER_ORDER).contains(folder)) {
					for (String trackName : discoveredItems.get(folder)) {
						registerDisc(trackName, folder);
					}
				}
			}
		});

		CreativeModeTab mainTab = FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.discs.main_tab"))
				.icon(() -> new ItemStack(tabIcon != null ? tabIcon : Items.JUKEBOX))
				.displayItems((_, output) -> {
					modMaterials.forEach(output::accept);

					for (String chapter : CHAPTER_ORDER) {
						List<Item> chapterDiscs = discsPerChapter.get(chapter);
						if (chapterDiscs != null) {
							chapterDiscs.forEach(output::accept);
						}
					}

					discsPerChapter.forEach((chapter, discs) -> {
						if (!List.of(CHAPTER_ORDER).contains(chapter)) {
							discs.forEach(output::accept);
						}
					});
				})
				.build();

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY.identifier(), mainTab);

		ModEntities.register();
		FabricDefaultAttributeRegistry.register(ModEntities.DISC_TRADER, WanderingTrader.createMobAttributes()); // please stop being bitchy i tried to fix you for the last hour
		ModCommands.register();

		DiscLoot.register();
		DiscLyrics.register();
	}

	private static void registerMaterial(String itemName) {
		String registryPath = "materials/" + itemName;
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, registryPath));

		Item materialItem = new Item(new Item.Properties().setId(itemKey));

		Registry.register(BuiltInRegistries.ITEM, itemKey, materialItem);

		modMaterials.add(materialItem);

		if (itemName.equals("template")) {
			templateDisc = materialItem;
		}

		discPrices.put(materialItem, DiscPricing.getPrice("materials", itemName));
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

		if ("demo4".equals(chapter) && "theancients".equals(trackName)) {
			tabIcon = vinyl;
		} else if (tabIcon == null) {
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