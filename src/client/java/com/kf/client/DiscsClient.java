package com.kf.client;

import com.kf.Discs;
import com.kf.TooltipFrames;
import com.kf.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;

public class DiscsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.DISC_TRADER, DiscTraderRenderer::new);
		ClientTickEvents.END_CLIENT_TICK.register(_ -> LyricsTracker.tick());

		HudElementRegistry.attachElementAfter(
				VanillaHudElements.HOTBAR,
				Identifier.fromNamespaceAndPath(Discs.MOD_ID, "lyrics"),
				new LyricsHud()
		);




		// for animated tooltips

		// add(...) must be used per every disc which needs animated thingy (i heart hardcoding)
		AnimatedDiscText.add(
				"nicopatty, Street Wise Rhapsody - Hatred",
				AnimatedDiscText.Gradient.of(0x7A0000, 0xFF2020),
				"YOU DON'T DESERVE TO RIDE THE SWING.",
				"YOU CARVED YOUR WAY THROUGH THEM. PIECE BY PIECE. SEARCHING FOR A WAY OUT. AND FOUND NOTHING.",
				"FOR I. AM THE SCOURGE OF EVERYONE. THAT YOU CAN NO LONGER HIDE.",
				"LET RUIN BEFALL THEIR CREATION."
		);
		AnimatedDiscText.add(
				"nicopatty - Fear",
				AnimatedDiscText.Gradient.of(0x9500BE, 0x29002D),
				"YOU'RE BEING USED!",
				"Please... P-Please listen...",
				"IT is m-manipulating you...",
				"KILLING ME WON'T SAVE YOU!!"
		);
		AnimatedDiscText.add(
				"Dragkun, Campyfire - Solitude",
				AnimatedDiscText.Gradient.of(0x6E69B0, 0x2A293C),
				"Do you miss your grandparents? Do you miss your home? Do you miss your future?",
				"Please... Step away from HIM... he just wants to hurt you...",
				"He's tricking you... So you won't...",
				"Oh.. You're here..."
		);
		AnimatedDiscText.add(
				"Campyfire - Greed",
				AnimatedDiscText.Gradient.of(0xC8FFAB, 0xF3B30C),
				"Just slap together a cheap copy, leave it out on the ground, and BAM! Easy catch!",
				"Hahahahahahaaaaa! Geez, it's just too easy!",
				"JACKPOT!",
				"So c'mon! Let's forget about these nerds, grab those SWORDS, and scram!"
		);

		// custom tooltip frames: frame top color, frame bottom color, slide speed (0 = still),
		// then the two background colors (top left half, bottom right half), keep those dark
		// the name has to match the style name in DiscPricing's TOOLTIP_STYLES
		TooltipFrames.add("hatred", 0x7A0000, 0xFF2020, 0.15, 0x3A0505, 0x0E0008);
		TooltipFrames.add("fear", 0xB84DFF, 0x5A1A7A, 0.15, 0x2A0033, 0x0E0012);
		TooltipFrames.add("solitude", 0x8F8AD6, 0x4A4880, 0.1, 0x24233A, 0x101018);
		TooltipFrames.add("greed", 0xC8FFAB, 0xF3B30C, 0.2, 0x1E2A0F, 0x2E2306);
		TooltipFrames.add("noobador", 0xFFC000, 0xE14E32, 0.15, 0x2E2106, 0x1A0A06);
		TooltipFrames.add("cruelking", 0xDCCAFF, 0x8A6FD6, 0.1, 0x241E33, 0x0F0D16);
		TooltipFrames.add("bubonicplant", 0xA47215, 0x6E4121, 0.12, 0x2A1D08, 0x140C0A);
		TooltipFrames.add("suprememosquito", 0x946B73, 0x738C6D, 0.18, 0x241B1E, 0x121A12);
		TooltipFrames.add("theancients", 0xAB8000, 0xEE1A10, 0.2, 0x2B2000, 0x2A0806);

		AnimatedDiscText.init();
	}
}