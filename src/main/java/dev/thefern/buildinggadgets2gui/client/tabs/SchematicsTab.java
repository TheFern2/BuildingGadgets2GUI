package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.client.ClipboardUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SchematicsTab extends TabPanel {
    
    private ClipboardUtils.CopyData copyData = new ClipboardUtils.CopyData();
    
    public SchematicsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        copyData = ClipboardUtils.checkCopyData();
        
        int buttonY = y + 20;
        
        widgets.add(Button.builder(
            Component.literal("Copy from Tool"),
            button -> ClipboardUtils.copyFromTool()
        )
        .bounds(x + 20, buttonY, 175, 20)
        .build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
    }
    
    @Override
    public void tick() {
        copyData = ClipboardUtils.checkCopyData();
    }
}

