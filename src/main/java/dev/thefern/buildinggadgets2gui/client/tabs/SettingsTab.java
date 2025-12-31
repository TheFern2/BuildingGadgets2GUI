package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsTab extends TabPanel {
    
    private static final int SETTING_START_Y = 30;
    private static final int SETTING_ROW_HEIGHT = 30;
    
    private Button decreaseHistoryButton;
    private Button increaseHistoryButton;
    
    public SettingsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        createHistoryLimitButtons();
    }
    
    private void createHistoryLimitButtons() {
        int settingY = y + SETTING_START_Y;
        
        decreaseHistoryButton = Button.builder(
            Component.literal("-"),
            button -> adjustHistoryLimit(-10)
        )
        .bounds(x + 200, settingY, 30, 20)
        .build();
        
        increaseHistoryButton = Button.builder(
            Component.literal("+"),
            button -> adjustHistoryLimit(10)
        )
        .bounds(x + 280, settingY, 30, 20)
        .build();
        
        decreaseHistoryButton.visible = false;
        increaseHistoryButton.visible = false;
        
        widgets.add(decreaseHistoryButton);
        widgets.add(increaseHistoryButton);
    }
    
    private void adjustHistoryLimit(int delta) {
        int currentValue = Config.MAX_HISTORY_ENTRIES.get();
        int newValue = Math.max(1, Math.min(500, currentValue + delta));
        Config.MAX_HISTORY_ENTRIES.set(newValue);
        
        HistoryTab.trimHistoryToLimit();
        
        System.out.println("Max history entries set to: " + newValue);
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        decreaseHistoryButton.visible = true;
        decreaseHistoryButton.active = true;
        increaseHistoryButton.visible = true;
        increaseHistoryButton.active = true;
    }
    
    @Override
    public void onTabDeactivated() {
        super.onTabDeactivated();
        decreaseHistoryButton.visible = false;
        decreaseHistoryButton.active = false;
        increaseHistoryButton.visible = false;
        increaseHistoryButton.active = false;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        int settingY = y + SETTING_START_Y;
        
        String labelText = "Max History Entries:";
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            labelText,
            x + 20,
            settingY + 6,
            0xFFFFFF,
            false
        );
        
        String valueText = String.valueOf(Config.MAX_HISTORY_ENTRIES.get());
        int valueWidth = Minecraft.getInstance().font.width(valueText);
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            valueText,
            x + 245 - valueWidth / 2,
            settingY + 6,
            0xFFFF00,
            false
        );
    }
}

