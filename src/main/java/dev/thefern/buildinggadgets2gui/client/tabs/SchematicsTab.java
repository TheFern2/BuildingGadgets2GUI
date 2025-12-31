package dev.thefern.buildinggadgets2gui.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SchematicsTab extends TabPanel {
    
    private static final int TOGGLE_BUTTON_WIDTH = 60;
    private static final int TOGGLE_BUTTON_HEIGHT = 20;
    private static final int TOGGLE_BUTTON_SPACING = 5;
    private static final int TOP_MARGIN = 10;
    
    private Button allButton;
    private Button deletedButton;
    private boolean showingTrash = false;
    
    public SchematicsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        int toggleY = y + TOP_MARGIN;
        int toggleX = x + 10;
        
        allButton = Button.builder(
            Component.literal("All"),
            button -> setShowingTrash(false)
        )
        .bounds(toggleX, toggleY, TOGGLE_BUTTON_WIDTH, TOGGLE_BUTTON_HEIGHT)
        .build();
        
        deletedButton = Button.builder(
            Component.literal("Deleted"),
            button -> setShowingTrash(true)
        )
        .bounds(toggleX + TOGGLE_BUTTON_WIDTH + TOGGLE_BUTTON_SPACING, toggleY, TOGGLE_BUTTON_WIDTH, TOGGLE_BUTTON_HEIGHT)
        .build();
        
        widgets.add(allButton);
        widgets.add(deletedButton);
    }
    
    private void setShowingTrash(boolean showTrash) {
        this.showingTrash = showTrash;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderToggleButtonBackgrounds(guiGraphics);
        
        int contentY = y + TOP_MARGIN + TOGGLE_BUTTON_HEIGHT + 20;
        int centerX = x + width / 2;
        int centerY = contentY + (height - contentY + y) / 2;
        
        String text = showingTrash ? "Deleted Schematics" : "All Schematics";
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
    
    private void renderToggleButtonBackgrounds(GuiGraphics guiGraphics) {
        int activeColor = 0xFF4A4A4A;
        int inactiveColor = 0xFF2A2A2A;
        
        int toggleY = y + TOP_MARGIN;
        int toggleX = x + 10;
        
        guiGraphics.fill(
            toggleX, 
            toggleY, 
            toggleX + TOGGLE_BUTTON_WIDTH, 
            toggleY + TOGGLE_BUTTON_HEIGHT,
            showingTrash ? inactiveColor : activeColor
        );
        
        guiGraphics.fill(
            toggleX + TOGGLE_BUTTON_WIDTH + TOGGLE_BUTTON_SPACING, 
            toggleY, 
            toggleX + TOGGLE_BUTTON_WIDTH * 2 + TOGGLE_BUTTON_SPACING, 
            toggleY + TOGGLE_BUTTON_HEIGHT,
            showingTrash ? activeColor : inactiveColor
        );
    }
}

