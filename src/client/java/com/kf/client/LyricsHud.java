package com.kf.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LyricsHud implements HudElement {

    private static final int Y_OFFSET_ABOVE_HOTBAR = 60;
    private static final float FADE_TICKS = 10.0f;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        var line = LyricsTracker.getCurrentLine();

        if (line == null || line.trim().isEmpty()) {
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
            double dist = Math.sqrt(distSq);
            if (dist > fadeStartDist) {
                alpha *= (float) (1.0 - ((dist - fadeStartDist) / (maxDist - fadeStartDist)));
            }
        }

        long age = LyricsTracker.getTicksSinceLineChanged();
        float fadeInAlpha = Math.min(1.0f, age / FADE_TICKS);

        if (LyricsTracker.isCurrentLineLast()) {
            float holdTicks = 160.0f;
            float endFadeTicks = 40.0f;

            if (age > holdTicks) {
                float endFadeAlpha = 1.0f - ((age - holdTicks) / endFadeTicks);
                alpha *= Math.max(0.0f, endFadeAlpha);
            } else {
                alpha *= fadeInAlpha;
            }
        } else {
            long untilNext = LyricsTracker.getTicksUntilNextLine();
            float fadeOutAlpha = 1.0f;

            if (untilNext >= 0 && untilNext < FADE_TICKS) {
                fadeOutAlpha = untilNext / FADE_TICKS;
            }

            alpha *= Math.min(fadeInAlpha, fadeOutAlpha);
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
        // java 21 stuff i did not knew existed
        alphaInt = Math.clamp(alphaInt, 0, 255);

        Integer configuredRgb = LyricsTracker.getCurrentLineColor();
        int rgb = configuredRgb != null ? configuredRgb : 0xFFFFFF;
        int color = (alphaInt << 24) | (rgb & 0x00FFFFFF);

        graphics.text(client.font, line, x, y, color);
    }
}