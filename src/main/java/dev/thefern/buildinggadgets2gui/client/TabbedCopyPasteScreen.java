package dev.thefern.buildinggadgets2gui.client;

import dev.thefern.buildinggadgets2gui.client.tabs.DebugTab;
import dev.thefern.buildinggadgets2gui.client.tabs.HistoryTab;
import dev.thefern.buildinggadgets2gui.client.tabs.SchematicsTab;
import dev.thefern.buildinggadgets2gui.client.tabs.TabPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TabbedCopyPasteScreen extends Screen {
    
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 260;
    private static final int TAB_HEIGHT = 24;
    private static final int TAB_WIDTH = 100;
    
    private int leftPos;
    private int topPos;
    
    private TabType currentTab = TabType.DEBUG;
    private TabPanel debugTab;
    private TabPanel schematicsTab;
    private TabPanel historyTab;
    
    private Button debugButton;
    private Button schematicsButton;
    private Button historyButton;
    private Button closeButton;
    
    public enum TabType {
        DEBUG,
        SCHEMATICS,
        HISTORY
    }
    
    public TabbedCopyPasteScreen() {
        super(Component.literal("Copy/Paste Manager"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = Math.max(10, (this.height - WINDOW_HEIGHT) / 2);
        
        debugTab = new DebugTab(this, leftPos, topPos + TAB_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT - TAB_HEIGHT);
        schematicsTab = new SchematicsTab(this, leftPos, topPos + TAB_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT - TAB_HEIGHT);
        historyTab = new HistoryTab(this, leftPos, topPos + TAB_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT - TAB_HEIGHT);
        
        debugButton = this.addRenderableWidget(
            Button.builder(
                Component.literal("Debug"),
                button -> switchTab(TabType.DEBUG)
            )
            .bounds(leftPos, topPos, TAB_WIDTH, TAB_HEIGHT)
            .build()
        );
        
        schematicsButton = this.addRenderableWidget(
            Button.builder(
                Component.literal("Schematics"),
                button -> switchTab(TabType.SCHEMATICS)
            )
            .bounds(leftPos + TAB_WIDTH, topPos, TAB_WIDTH, TAB_HEIGHT)
            .build()
        );
        
        historyButton = this.addRenderableWidget(
            Button.builder(
                Component.literal("History"),
                button -> switchTab(TabType.HISTORY)
            )
            .bounds(leftPos + TAB_WIDTH * 2, topPos, TAB_WIDTH, TAB_HEIGHT)
            .build()
        );
        
        closeButton = this.addRenderableWidget(
            Button.builder(
                Component.literal("X"),
                button -> this.onClose()
            )
            .bounds(leftPos + WINDOW_WIDTH - 25, topPos, 25, TAB_HEIGHT)
            .build()
        );
        
        debugTab.init();
        schematicsTab.init();
        historyTab.init();
        
        for (var widget : debugTab.getWidgets()) {
            if (widget instanceof net.minecraft.client.gui.components.AbstractWidget abstractWidget) {
                this.addRenderableWidget(abstractWidget);
            }
        }
        for (var widget : schematicsTab.getWidgets()) {
            if (widget instanceof net.minecraft.client.gui.components.AbstractWidget abstractWidget) {
                this.addRenderableWidget(abstractWidget);
            }
        }
        for (var widget : historyTab.getWidgets()) {
            if (widget instanceof net.minecraft.client.gui.components.AbstractWidget abstractWidget) {
                this.addRenderableWidget(abstractWidget);
            }
        }
        
        debugTab.onTabActivated();
        schematicsTab.onTabDeactivated();
        historyTab.onTabDeactivated();
    }
    
    private void switchTab(TabType tab) {
        if (currentTab != tab) {
            getCurrentTabPanel().onTabDeactivated();
            currentTab = tab;
            getCurrentTabPanel().onTabActivated();
        }
    }
    
    private TabPanel getCurrentTabPanel() {
        return switch (currentTab) {
            case DEBUG -> debugTab;
            case SCHEMATICS -> schematicsTab;
            case HISTORY -> historyTab;
        };
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTabButtons(guiGraphics);
        
        guiGraphics.fill(leftPos, topPos + TAB_HEIGHT, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xC0101010);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        getCurrentTabPanel().render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderTabButtons(GuiGraphics guiGraphics) {
        int activeColor = 0xFF505050;
        int inactiveColor = 0xFF303030;
        
        guiGraphics.fill(leftPos, topPos, leftPos + TAB_WIDTH, topPos + TAB_HEIGHT, 
            currentTab == TabType.DEBUG ? activeColor : inactiveColor);
        guiGraphics.fill(leftPos + TAB_WIDTH, topPos, leftPos + TAB_WIDTH * 2, topPos + TAB_HEIGHT, 
            currentTab == TabType.SCHEMATICS ? activeColor : inactiveColor);
        guiGraphics.fill(leftPos + TAB_WIDTH * 2, topPos, leftPos + TAB_WIDTH * 3, topPos + TAB_HEIGHT, 
            currentTab == TabType.HISTORY ? activeColor : inactiveColor);
        guiGraphics.fill(leftPos + TAB_WIDTH * 3, topPos, leftPos + WINDOW_WIDTH, topPos + TAB_HEIGHT, 
            0xFF202020);
    }
    
    @Override
    public void tick() {
        super.tick();
        getCurrentTabPanel().tick();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getCurrentTabPanel().mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (getCurrentTabPanel().mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (getCurrentTabPanel().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (getCurrentTabPanel().mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public int getLeftPos() {
        return leftPos;
    }
    
    public int getTopPos() {
        return topPos;
    }
    
    public int getWindowWidth() {
        return WINDOW_WIDTH;
    }
    
    public int getWindowHeight() {
        return WINDOW_HEIGHT;
    }
}

