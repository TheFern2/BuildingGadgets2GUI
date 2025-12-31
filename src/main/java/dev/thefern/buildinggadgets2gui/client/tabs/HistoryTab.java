package dev.thefern.buildinggadgets2gui.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class HistoryTab extends TabPanel {
    
    public HistoryTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        
        String text = "History Tab";
        int textWidth = Minecraft.getInstance().font.width(text);
        
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            text,
            centerX - textWidth / 2,
            centerY - 10,
            0xFFFFFF,
            false
        );
        
        String subText = "(Coming Soon)";
        int subTextWidth = Minecraft.getInstance().font.width(subText);
        
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            subText,
            centerX - subTextWidth / 2,
            centerY + 10,
            0xAAAAAA,
            false
        );
    }
}

