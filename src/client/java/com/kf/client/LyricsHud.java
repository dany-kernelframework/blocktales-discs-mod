package com.kf.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public class LyricsHud implements HudElement {

    private static final int Y_OFFSET_ABOVE_HOTBAR = 60; //should be right above armor points and under now playing - <songname>

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        @Nullable String line = LyricsTracker.getCurrentLine();

        if (line == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        float alpha = 1.0f;

        BlockPos pos = LyricsTracker.getTrackedPos();
        if (pos != null && client.player != null) {
            double distSq = client.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double maxDist = 64.0;
            double fadeStartDist = 48.0;

            if (distSq > maxDist * maxDist) {
                return;
            }
            // hmhm yes i understand math (i dont i just pray ts actually works)
            double dist = Math.sqrt(distSq);
            if (dist > fadeStartDist) {
                alpha *= (float) (1.0 - ((dist - fadeStartDist) / (maxDist - fadeStartDist)));
            }
        }

        // --- Final Lyric Fading Logic ---
        if (LyricsTracker.isCurrentLineLast()) {
            long age = LyricsTracker.getTicksSinceLineChanged();

            float holdTicks = 160.0f;
            float fadeTicks = 40.0f;

            if (age > holdTicks) {
                alpha *= 1.0f - ((age - holdTicks) / fadeTicks);
            }
        }
        if (alpha <= 0.0f) {
            return;
        }

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int textWidth = client.font.width(line);

        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - Y_OFFSET_ABOVE_HOTBAR;

        int alphaInt = (int) (alpha * 255.0f);
        alphaInt = Math.max(0, Math.min(255, alphaInt)); // bounded if anything breaks
        int color = (alphaInt << 24) | 0x00FFFFFF;

        graphics.text(client.font, line, x, y, color);
    }
}