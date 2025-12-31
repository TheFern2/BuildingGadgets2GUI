package dev.thefern.buildinggadgets2gui.client.tabs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public abstract class TabPanel {
    
    protected final Screen parentScreen;
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final List<Renderable> widgets = new ArrayList<>();
    protected boolean isActive = false;
    
    public TabPanel(Screen parentScreen, int x, int y, int width, int height) {
        this.parentScreen = parentScreen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public abstract void init();
    
    public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
    
    public void tick() {
    }
    
    public void onTabActivated() {
        isActive = true;
        for (Renderable widget : widgets) {
            if (widget instanceof net.minecraft.client.gui.components.AbstractWidget abstractWidget) {
                abstractWidget.visible = true;
            }
        }
    }
    
    public void onTabDeactivated() {
        isActive = false;
        for (Renderable widget : widgets) {
            if (widget instanceof net.minecraft.client.gui.components.AbstractWidget abstractWidget) {
                abstractWidget.visible = false;
            }
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }
    
    public Screen getParentScreen() {
        return parentScreen;
    }
    
    public List<Renderable> getWidgets() {
        return widgets;
    }
}

