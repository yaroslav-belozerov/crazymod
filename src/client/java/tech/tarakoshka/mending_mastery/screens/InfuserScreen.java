package tech.tarakoshka.mending_mastery.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tech.tarakoshka.mending_mastery.MyMod;
import tech.tarakoshka.mending_mastery.blocks.infuser.InfuserMenu;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {

    public InfuserScreen(InfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF8B8B8B);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);

        if (this.menu.isLit()) {
            int burnProgress = this.menu.getBurnProgress();
            guiGraphics.fill(x + 57, y + 37 + 13 - burnProgress, x + 70, y + 50, 0xFFFF6600);
        }
        int cookProgress = this.menu.getCookProgress();
        if (cookProgress > 0) {
            guiGraphics.fill(x + 79, y + 34, x + 79 + cookProgress, y + 38, 0xFF00FF00);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}