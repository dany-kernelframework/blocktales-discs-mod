package com.kf.client.mixin;

import com.kf.Discs;
import com.kf.client.DiscTabLayout;
import com.kf.client.DiscTabStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    private float scrollOffs;

    @Unique
    private static final int discs$gridLeft = 8;
    @Unique
    private static final int discs$gridRight = 170;
    @Unique
    private static final int discs$firstRowTop = 17;
    @Unique
    private static final int discs$rowHeight = 18;
    @Unique
    private static final int discs$visibleRows = 5;

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 195, 136);
    }

    @Redirect(
            method = "selectTab",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z", ordinal = 1) // it took me hours to find whatever the fuck this is
    )
    private boolean discs$addItemsOnSelectTab(NonNullList<ItemStack> list, Collection<ItemStack> items) {
        return discs$addItems(list, items);
    }

    @Redirect(
            method = "refreshCurrentTabContents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z", ordinal = 0)
    )
    private boolean discs$addItemsOnRefresh(NonNullList<ItemStack> list, Collection<ItemStack> items) {
        return discs$addItems(list, items);
    }
    // uhh basically the 3 calls would result in a crash of the game because minecraft is stupid (or i am... maybe both)
    @Unique
    private boolean discs$addItems(NonNullList<ItemStack> list, Collection<ItemStack> items) {
        if (selectedTab == Discs.discsTab) {
            return list.addAll(DiscTabLayout.build(items));
        }
        return list.addAll(items);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (selectedTab == Discs.discsTab && width == 16 && height == 16 && x >= 9 && x <= 153 && (x - 9) % 18 == 0 && y >= 18 && y <= 90 && (y - 18) % 18 == 0) {
            int col = (x - 9) / 18;
            int visibleRow = (y - 18) / 18;
            int totalRows = DiscTabLayout.rowCount();
            int rowsToScroll = Math.max(0, totalRows - discs$visibleRows);
            int firstRow = Math.max((int) (scrollOffs * rowsToScroll + 0.5), 0);
            if (DiscTabLayout.isHeaderSlot(firstRow + visibleRow, col)) {
                return false;
            }
        }
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void discs$onSlotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if (selectedTab == Discs.discsTab && discs$isHeaderSlot(slot)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean discs$isHeaderSlot(Slot slot) {
        if (slot == null || slot.index < 0 || slot.index >= 45) {
            return false;
        }
        int totalRows = DiscTabLayout.rowCount();
        int rowsToScroll = Math.max(0, totalRows - discs$visibleRows);
        int firstRow = Math.max((int) (scrollOffs * rowsToScroll + 0.5), 0);
        int visibleRow = slot.index / 9;
        int col = slot.index % 9;
        return DiscTabLayout.isHeaderSlot(firstRow + visibleRow, col);
    }

    @Inject(method = "extractLabels", at = @At("TAIL"))
    private void discs$drawSectionHeaders(GuiGraphicsExtractor graphics, int xm, int ym, CallbackInfo ci) {
        if (selectedTab != Discs.discsTab) {
            return;
        }

        DiscTabStyle.Style style = DiscTabStyle.get();

        int totalRows = DiscTabLayout.rowCount();
        int rowsToScroll = Math.max(0, totalRows - discs$visibleRows);
        int firstRow = Math.max((int) (scrollOffs * rowsToScroll + 0.5), 0);

        for (int visibleRow = 0; visibleRow < discs$visibleRows; visibleRow++) {
            int row = firstRow + visibleRow;
            DiscTabLayout.Header header = DiscTabLayout.headerAt(row);
            if (header == null) {
                continue;
            }

            int top = discs$firstRowTop + visibleRow * discs$rowHeight;
            int titleWidth = header.slots() * discs$rowHeight;

            if (style.custom()) {
                graphics.fill(discs$gridLeft, top, discs$gridLeft + titleWidth, top + discs$rowHeight, style.panel());
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, DiscTabStyle.ctt_texture,
                        discs$gridLeft, top, (float) discs$gridLeft, 5.0F, titleWidth, 9, 256, 256);
                graphics.blit(RenderPipelines.GUI_TEXTURED, DiscTabStyle.ctt_texture,
                        discs$gridLeft, top + 9, (float) discs$gridLeft, 5.0F, titleWidth, 9, 256, 256);
            }
            if (row > 0) {
                graphics.fill(discs$gridLeft, top, discs$gridRight, top + 1, style.line());
            }
            float textY = top + (discs$rowHeight - 9 * DiscTabLayout.TITLE_SCALE) / 2f;
            graphics.pose().pushMatrix();
            graphics.pose().translate(discs$gridLeft + 2, textY);
            graphics.pose().scale(DiscTabLayout.TITLE_SCALE, DiscTabLayout.TITLE_SCALE);
            graphics.text(Minecraft.getInstance().font, header.title(), 0, 0, style.text(), style.shadow());
            graphics.pose().popMatrix();
        }
    }
}