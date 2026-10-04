package com.kf.client.mixin;

import com.kf.TooltipFrames;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {

    @Unique
    private static final Identifier discs$normalBackground = Identifier.withDefaultNamespace("tooltip/background");

    @Unique
    private static final int discs$frameAlpha = 0xA0;

    @Unique
    private static final int discs$backgroundAlpha = 0xF0;

    @Unique
    private static final int discs$pieceHeight = 4;

    @Unique
    private static final int discs$stripWidth = 2;

    @Inject(method = "extractTooltipBackground", at = @At("HEAD"), cancellable = true)
    private static void discs$drawCustomFrame(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Identifier style, CallbackInfo ci) {
        TooltipFrames.Frame frame = TooltipFrames.get(style);
        if (frame == null) {
            return;
        }

        TooltipFrames.Background background = frame.background();
        if (background != null) {
            discs$drawDiagonalBackground(graphics, x, y, w, h, background);
        } else {
            int backgroundX = x - 3 - 9;
            int backgroundY = y - 3 - 9;
            int backgroundWidth = w + 3 + 3 + 18;
            int backgroundHeight = h + 3 + 3 + 18;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, discs$normalBackground, backgroundX, backgroundY, backgroundWidth, backgroundHeight);
        }

        long now = System.currentTimeMillis();
        int left = x - 3;
        int top = y - 3;
        int right = x + w + 3;
        int bottom = y + h + 3;
        int totalHeight = bottom - top;

        graphics.fill(left + 1, top, right - 1, top + 1, discs$withFrameAlpha(frame.colorAt(0f, now)));
        graphics.fill(left + 1, bottom - 1, right - 1, bottom, discs$withFrameAlpha(frame.colorAt(1f, now)));

        int sidesTop = top + 1;
        int sidesBottom = bottom - 1;

        for (int pieceTop = sidesTop; pieceTop < sidesBottom; pieceTop += discs$pieceHeight) {
            int pieceBottom = Math.min(pieceTop + discs$pieceHeight, sidesBottom);

            int colorAtPieceTop = discs$withFrameAlpha(frame.colorAt((float) (pieceTop - top) / totalHeight, now));
            int colorAtPieceBottom = discs$withFrameAlpha(frame.colorAt((float) (pieceBottom - top) / totalHeight, now));

            graphics.fillGradient(left, pieceTop, left + 1, pieceBottom, colorAtPieceTop, colorAtPieceBottom);
            graphics.fillGradient(right - 1, pieceTop, right, pieceBottom, colorAtPieceTop, colorAtPieceBottom);
        }

        ci.cancel();
    }

    @Unique
    private static void discs$drawDiagonalBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h, TooltipFrames.Background background) {
        int areaLeft = x - 4;
        int areaTop = y - 4;
        int areaWidth = w + 8;
        int areaHeight = h + 8;

        for (int columnLeft = x - 3; columnLeft < x + w + 3; columnLeft += discs$stripWidth) {
            int columnRight = Math.min(columnLeft + discs$stripWidth, x + w + 3);
            discs$drawColumn(graphics, columnLeft, columnRight, areaTop, areaTop + areaHeight, areaLeft, areaTop, areaWidth, areaHeight, background);
        }

        discs$drawColumn(graphics, x - 4, x - 3, areaTop + 1, areaTop + areaHeight - 1, areaLeft, areaTop, areaWidth, areaHeight, background);
        discs$drawColumn(graphics, x + w + 3, x + w + 4, areaTop + 1, areaTop + areaHeight - 1, areaLeft, areaTop, areaWidth, areaHeight, background);
    }

    @Unique
    private static void discs$drawColumn(GuiGraphicsExtractor graphics, int columnLeft, int columnRight, int yFrom, int yTo,
                                         int areaLeft, int areaTop, int areaWidth, int areaHeight, TooltipFrames.Background background) {
        float distanceFromLeft = (columnLeft + columnRight) / 2f - areaLeft;
        float span = areaWidth + areaHeight;
        float softness = background.softness();

        float blendStartY = areaTop + (0.5f - softness) * span - distanceFromLeft;
        float blendEndY = areaTop + (0.5f + softness) * span - distanceFromLeft;

        int blendStart = Math.clamp(Math.round(blendStartY), yFrom, yTo);
        int blendEnd = Math.clamp(Math.round(blendEndY), yFrom, yTo);

        discs$drawPiece(graphics, columnLeft, columnRight, yFrom, blendStart, distanceFromLeft, areaTop, span, background);
        discs$drawPiece(graphics, columnLeft, columnRight, blendStart, blendEnd, distanceFromLeft, areaTop, span, background);
        discs$drawPiece(graphics, columnLeft, columnRight, blendEnd, yTo, distanceFromLeft, areaTop, span, background);
    }

    @Unique
    private static void discs$drawPiece(GuiGraphicsExtractor graphics, int columnLeft, int columnRight, int pieceTop, int pieceBottom,
                                        float distanceFromLeft, int areaTop, float span, TooltipFrames.Background background) {
        if (pieceBottom <= pieceTop) {
            return;
        }

        int colorAtTop = discs$backgroundColorAt(pieceTop + 0.01f, distanceFromLeft, areaTop, span, background);
        int colorAtBottom = discs$backgroundColorAt(pieceBottom - 0.01f, distanceFromLeft, areaTop, span, background);

        graphics.fillGradient(columnLeft, pieceTop, columnRight, pieceBottom, colorAtTop, colorAtBottom);
    }

    @Unique
    private static int discs$backgroundColorAt(float y, float distanceFromLeft, int areaTop, float span, TooltipFrames.Background background) {
        float position = (distanceFromLeft + (y - areaTop)) / span;
        float softness = background.softness();

        float amount;
        if (softness <= 0f) {
            amount = position < 0.5f ? 0f : 1f;
        } else {
            amount = (position - (0.5f - softness)) / (2f * softness);
            amount = Math.clamp(amount, 0f, 1f);
        }

        return (discs$backgroundAlpha << 24) | discs$blend(background.firstColor(), background.secondColor(), amount);
    }

    @Unique
    private static int discs$blend(int from, int to, float amount) {
        int red = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * amount);
        int green = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * amount);
        int blue = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * amount);
        return (red << 16) | (green << 8) | blue;
    }

    @Unique
    private static int discs$withFrameAlpha(int color) {
        return (discs$frameAlpha << 24) | (color & 0xFFFFFF);
    }
}