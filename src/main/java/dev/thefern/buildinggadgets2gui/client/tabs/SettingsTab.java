package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.Config;
import dev.thefern.buildinggadgets2gui.client.schematics.TrashManager;
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
    private Button decreaseTrashItemsButton;
    private Button increaseTrashItemsButton;
    private Button decreaseTrashDaysButton;
    private Button increaseTrashDaysButton;
    
    public SettingsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        createHistoryLimitButtons();
        createTrashSettingsButtons();
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
    
    private void createTrashSettingsButtons() {
        int settingY = y + SETTING_START_Y + SETTING_ROW_HEIGHT;
        
        decreaseTrashItemsButton = Button.builder(
            Component.literal("-"),
            button -> adjustTrashItems(-10)
        )
        .bounds(x + 200, settingY, 30, 20)
        .build();
        
        increaseTrashItemsButton = Button.builder(
            Component.literal("+"),
            button -> adjustTrashItems(10)
        )
        .bounds(x + 280, settingY, 30, 20)
        .build();
        
        decreaseTrashItemsButton.visible = false;
        increaseTrashItemsButton.visible = false;
        
        widgets.add(decreaseTrashItemsButton);
        widgets.add(increaseTrashItemsButton);
        
        settingY += SETTING_ROW_HEIGHT;
        
        decreaseTrashDaysButton = Button.builder(
            Component.literal("-"),
            button -> adjustTrashDays(-5)
        )
        .bounds(x + 200, settingY, 30, 20)
        .build();
        
        increaseTrashDaysButton = Button.builder(
            Component.literal("+"),
            button -> adjustTrashDays(5)
        )
        .bounds(x + 280, settingY, 30, 20)
        .build();
        
        decreaseTrashDaysButton.visible = false;
        increaseTrashDaysButton.visible = false;
        
        widgets.add(decreaseTrashDaysButton);
        widgets.add(increaseTrashDaysButton);
    }
    
    private void adjustHistoryLimit(int delta) {
        int currentValue = Config.MAX_HISTORY_ENTRIES.get();
        int newValue = Math.max(1, Math.min(500, currentValue + delta));
        Config.MAX_HISTORY_ENTRIES.set(newValue);
        
        HistoryTab.trimHistoryToLimit();
        
        System.out.println("Max history entries set to: " + newValue);
    }
    
    private void adjustTrashItems(int delta) {
        int currentValue = Config.MAX_TRASH_ITEMS.get();
        int newValue = Math.max(0, Math.min(500, currentValue + delta));
        Config.MAX_TRASH_ITEMS.set(newValue);
        
        TrashManager.performAutoCleanup();
        
        System.out.println("Max trash items set to: " + newValue);
    }
    
    private void adjustTrashDays(int delta) {
        int currentValue = Config.TRASH_RETENTION_DAYS.get();
        int newValue = Math.max(0, Math.min(365, currentValue + delta));
        Config.TRASH_RETENTION_DAYS.set(newValue);
        
        TrashManager.performAutoCleanup();
        
        System.out.println("Trash retention days set to: " + newValue);
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        setAllButtonsVisible(true);
    }
    
    @Override
    public void onTabDeactivated() {
        super.onTabDeactivated();
        setAllButtonsVisible(false);
    }
    
    private void setAllButtonsVisible(boolean visible) {
        decreaseHistoryButton.visible = visible;
        decreaseHistoryButton.active = visible;
        increaseHistoryButton.visible = visible;
        increaseHistoryButton.active = visible;
        
        decreaseTrashItemsButton.visible = visible;
        decreaseTrashItemsButton.active = visible;
        increaseTrashItemsButton.visible = visible;
        increaseTrashItemsButton.active = visible;
        
        decreaseTrashDaysButton.visible = visible;
        decreaseTrashDaysButton.active = visible;
        increaseTrashDaysButton.visible = visible;
        increaseTrashDaysButton.active = visible;
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
        
        settingY += SETTING_ROW_HEIGHT;
        
        String trashItemsLabel = "Max Trash Items:";
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            trashItemsLabel,
            x + 20,
            settingY + 6,
            0xFFFFFF,
            false
        );
        
        int trashItemsValue = Config.MAX_TRASH_ITEMS.get();
        String trashItemsText = trashItemsValue == 0 ? "∞" : String.valueOf(trashItemsValue);
        int trashItemsWidth = Minecraft.getInstance().font.width(trashItemsText);
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            trashItemsText,
            x + 245 - trashItemsWidth / 2,
            settingY + 6,
            0xFFFF00,
            false
        );
        
        settingY += SETTING_ROW_HEIGHT;
        
        String trashDaysLabel = "Trash Retention (days):";
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            trashDaysLabel,
            x + 20,
            settingY + 6,
            0xFFFFFF,
            false
        );
        
        int trashDaysValue = Config.TRASH_RETENTION_DAYS.get();
        String trashDaysText = trashDaysValue == 0 ? "∞" : String.valueOf(trashDaysValue);
        int trashDaysWidth = Minecraft.getInstance().font.width(trashDaysText);
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            trashDaysText,
            x + 245 - trashDaysWidth / 2,
            settingY + 6,
            0xFFFF00,
            false
        );
    }
}

