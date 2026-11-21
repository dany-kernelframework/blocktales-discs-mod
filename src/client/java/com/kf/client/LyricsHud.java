package com.kf.client;

import com.kf.DiscLyrics;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

@NullMarked
public class LyricsHud implements HudElement {

    private static final double fade_sec = 0.5;
    private static final double hold_sec = 8.0;
    private static final double end_fade_sec = 2.0;
    private static final double prev_window = 4.0;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        LyricsConfig.Settings cfg = LyricsConfig.get();
        if (!cfg.enabled) return;

        LyricsTracker.View view = LyricsTracker.view();
        if (view == null) return;

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        long nowMs = System.currentTimeMillis();

        float base = Mth.clamp(cfg.opacity, 0.0f, 1.0f) * view.distanceAlpha();
        if (base <= 0.0f) return;

        int cx = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - cfg.yOffset;

        DiscLyrics.Song song = view.song();
        DiscLyrics.LyricLine line = view.line();
        DiscLyrics.LyricLine next = view.next();

        // current line
        boolean lineShown = false;
        if (song != null && line != null) {
            float alpha = lineAlpha(view, line) * instrumentalFactor(cfg, line);
            if (alpha > 0.0f) {
                int rgb = pick(line.color(), song.color(), 0xFFFFFF);
                DiscLyrics.Effect effect = line.effect() != null ? line.effect() : song.effect();
                boolean fx = cfg.effects && !line.instrumental();

                Component text = LyricsText.compose(line, view.elapsed(), effect, fx, rgb, nowMs);
                int offsetX = 0;
                int offsetY = 0;
                if (fx && effect == DiscLyrics.Effect.shake) {
                    int[] shake = LyricsText.shake(line, nowMs);
                    offsetX = shake[0];
                    offsetY = shake[1];
                }

                drawLine(graphics, font, text, cx + offsetX, y + offsetY, cfg.scale, alpha * base, cfg);
                lineShown = true;
            }
        }

        if (cfg.showNextLine && song != null && next != null && !next.instrumental() && (lineShown || line == null)) {
            double until = next.time() - view.elapsed();
            if (until <= prev_window) {
                float fade = (float) Math.min(1.0, (prev_window - until) / fade_sec);
                int rgb = LyricsText.dim(pick(next.color(), song.color(), 0xFFFFFF));
                int previewY = lineShown ? y + Math.round(11 * cfg.scale) : y;
                drawLine(graphics, font, LyricsText.plain(next, rgb), cx, previewY, cfg.scale, 0.6f * fade * base, cfg);
            }
        }


    }

    private static float lineAlpha(LyricsTracker.View view, DiscLyrics.LyricLine line) {
        double age = view.elapsed() - line.time();
        float alpha = (float) Math.min(1.0, age / fade_sec);

        DiscLyrics.LyricLine next = view.next();
        double end = line.end() > 0 ? line.end() : (next != null ? next.time() : -1);

        if (end > 0) {
            double until = end - view.elapsed();
            if (until <= 0) return 0.0f;
            alpha = Math.min(alpha, (float) Math.min(1.0, until / fade_sec));
        } else if (age > hold_sec) {
            alpha *= (float) Math.max(0.0, 1.0 - (age - hold_sec) / end_fade_sec);
        }
        return Math.max(0.0f, alpha);
    }

    private static float instrumentalFactor(LyricsConfig.Settings cfg, DiscLyrics.LyricLine line) {
        if (!line.instrumental()) return 1.0f;
        String mode = cfg.instrumental == null ? "dim" : cfg.instrumental.toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "hide" -> 0.0f;
            case "show" -> 1.0f;
            default -> 0.5f;
        };
    }

    private static int pick(@Nullable Integer first, @Nullable Integer second, int fallback) { // tfym Value of parameter 'fallback' is always '16777215'
        if (first != null) return first;
        if (second != null) return second;
        return fallback;
    }

    private static void drawLine(GuiGraphicsExtractor graphics, Font font, Component text,
                                 int centerX, int y, float scale, float alpha, LyricsConfig.Settings cfg) {
        int alphaInt = Math.clamp((int) (alpha * 255.0f), 0, 255);
        if (alphaInt < 8) return; // very low alpha text can explode

        int width = font.width(text);

        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, y);
        graphics.pose().scale(scale, scale);

        if (cfg.background) {
            int boxAlpha = Math.clamp((int) (alpha * 0.5f * 255.0f), 0, 255);
            graphics.fill(-width / 2 - 3, -2, width / 2 + 3, 10, boxAlpha << 24);
        }
        graphics.text(font, text, -width / 2, 0, (alphaInt << 24) | 0xFFFFFF, cfg.shadow);

        graphics.pose().popMatrix();
    }
}
