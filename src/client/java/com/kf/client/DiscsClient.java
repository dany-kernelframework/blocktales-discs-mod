package com.kf.client;

import com.kf.Discs;
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

		AnimatedDiscText.init();
	}
}