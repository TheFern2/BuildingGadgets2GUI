package dev.thefern.buildinggadgets2gui.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class HelpTab extends TabPanel {
    
    private static final int CONTENT_PADDING = 15;
    private static final int LINE_HEIGHT = 11;
    private static final int SECTION_SPACING = 8;
    private static final int HEADER_COLOR = 0xFFFF00;
    private static final int SUBHEADER_COLOR = 0x55FFFF;
    private static final int TEXT_COLOR = 0xAAAAAA;
    private static final int HIGHLIGHT_COLOR = 0x55FF55;
    
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private List<HelpLine> helpContent;
    private int totalContentHeight = 0;
    
    private enum LineType {
        HEADER,
        SUBHEADER,
        TEXT,
        BULLET,
        EMPTY
    }
    
    private record HelpLine(LineType type, String text) {}
    
    public HelpTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
        buildHelpContent();
    }
    
    private void buildHelpContent() {
        helpContent = new ArrayList<>();
        
        addHeader("Getting Started");
        addBullet("Open GUI: Press ` (backtick) while holding a BG2 Copy/Paste gadget");
        addBullet("The GUI has tabs: Schematics, History, Trash, Settings, Help");
        addEmpty();
        
        addHeader("Schematics Tab");
        addText("Manage your schematic files and clipboard.");
        addEmpty();
        addSubheader("Top Buttons:");
        addBullet("Copy from Tool - Import blocks from your BG2 gadget to clipboard");
        addBullet("Save Schematic - Save clipboard to a file");
        addEmpty();
        addSubheader("Clipboard Row:");
        addBullet("Shows block count, dimensions, and timestamp");
        addBullet("Material icon - View required materials list");
        addBullet("Tool icon - Send clipboard back to gadget");
        addBullet("Clear - Empty the clipboard");
        addEmpty();
        addSubheader("Navigation:");
        addBullet("/ (Root) - Go to root schematics folder");
        addBullet("Up Arrow - Go to parent folder");
        addBullet("+ (Plus) - Create new folder");
        addEmpty();
        addSubheader("File List:");
        addBullet("Click file to select and view info");
        addBullet("Double-click folder to enter it");
        addBullet("Use row buttons: Material, Clipboard, Tool, Delete");
        addEmpty();
        
        addHeader("History Tab");
        addText("Automatically saves all copy operations.");
        addEmpty();
        addSubheader("Entry Actions:");
        addBullet("X - Delete entry from history");
        addBullet("Material icon - View materials for entry");
        addBullet("Clipboard icon - Send to clipboard");
        addBullet("Tool icon - Send directly to gadget");
        addEmpty();
        addBullet("Scroll with mouse wheel when many entries");
        addBullet("Clear All History button removes everything");
        addEmpty();
        
        addHeader("Trash Tab");
        addText("Deleted schematics go here for recovery.");
        addEmpty();
        addBullet("Restore - Recover schematic to original location");
        addBullet("Delete - Permanently remove from trash");
        addBullet("Empty Trash - Delete all trashed items");
        addEmpty();
        
        addHeader("Settings Tab");
        addBullet("Max History Entries - Use +/- to adjust limit");
        addEmpty();
        
        addHeader("Basic Workflow");
        addText("1. Use BG2 gadget to copy blocks in-game");
        addText("2. Open this GUI with ` key");
        addText("3. Click 'Copy from Tool' to import selection");
        addText("4. Click 'Save Schematic' to save with a name");
        addText("5. Later: Select schematic, use Tool icon to load");
        addEmpty();
        
        addHeader("Quick Tips");
        addBullet("Hold the gadget when opening GUI for 'Copy from Tool'");
        addBullet("Use folders to organize by project/category");
        addBullet("History is great for undo/versioning");
        addBullet("Material list helps plan resource gathering");
        
        calculateContentHeight();
    }
    
    private void addHeader(String text) {
        helpContent.add(new HelpLine(LineType.HEADER, text));
    }
    
    private void addSubheader(String text) {
        helpContent.add(new HelpLine(LineType.SUBHEADER, text));
    }
    
    private void addText(String text) {
        helpContent.add(new HelpLine(LineType.TEXT, text));
    }
    
    private void addBullet(String text) {
        helpContent.add(new HelpLine(LineType.BULLET, "• " + text));
    }
    
    private void addEmpty() {
        helpContent.add(new HelpLine(LineType.EMPTY, ""));
    }
    
    private void calculateContentHeight() {
        totalContentHeight = 0;
        for (HelpLine line : helpContent) {
            if (line.type == LineType.HEADER) {
                totalContentHeight += LINE_HEIGHT + SECTION_SPACING;
            } else if (line.type == LineType.EMPTY) {
                totalContentHeight += SECTION_SPACING;
            } else {
                totalContentHeight += LINE_HEIGHT;
            }
        }
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        updateScrollLimits();
    }
    
    private void updateScrollLimits() {
        int visibleHeight = height - CONTENT_PADDING * 2;
        maxScrollOffset = Math.max(0, totalContentHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        int contentX = x + CONTENT_PADDING;
        int contentY = y + CONTENT_PADDING;
        int contentWidth = width - CONTENT_PADDING * 2;
        int contentHeight = height - CONTENT_PADDING * 2;
        
        guiGraphics.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
        
        int currentY = contentY - scrollOffset;
        
        for (HelpLine line : helpContent) {
            int color = switch (line.type) {
                case HEADER -> HEADER_COLOR;
                case SUBHEADER -> SUBHEADER_COLOR;
                case BULLET -> HIGHLIGHT_COLOR;
                default -> TEXT_COLOR;
            };
            
            if (line.type == LineType.HEADER) {
                if (currentY >= contentY - LINE_HEIGHT && currentY <= contentY + contentHeight) {
                    guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        line.text,
                        contentX,
                        currentY,
                        color,
                        false
                    );
                }
                currentY += LINE_HEIGHT + SECTION_SPACING;
            } else if (line.type == LineType.EMPTY) {
                currentY += SECTION_SPACING;
            } else {
                if (currentY >= contentY - LINE_HEIGHT && currentY <= contentY + contentHeight) {
                    int indentX = contentX;
                    if (line.type == LineType.BULLET) {
                        indentX += 10;
                    } else if (line.type == LineType.SUBHEADER) {
                        indentX += 5;
                    }
                    
                    guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        line.text,
                        indentX,
                        currentY,
                        color,
                        false
                    );
                }
                currentY += LINE_HEIGHT;
            }
        }
        
        guiGraphics.disableScissor();
        
        if (maxScrollOffset > 0) {
            renderScrollbar(guiGraphics, contentX + contentWidth - 6, contentY, 4, contentHeight);
        }
    }
    
    private void renderScrollbar(GuiGraphics guiGraphics, int scrollX, int scrollY, int scrollWidth, int scrollHeight) {
        guiGraphics.fill(scrollX, scrollY, scrollX + scrollWidth, scrollY + scrollHeight, 0xFF333333);
        
        float scrollPercent = (float) scrollOffset / maxScrollOffset;
        float thumbHeight = Math.max(20, (float) scrollHeight * scrollHeight / totalContentHeight);
        int thumbY = scrollY + (int) ((scrollHeight - thumbHeight) * scrollPercent);
        
        guiGraphics.fill(scrollX, thumbY, scrollX + scrollWidth, thumbY + (int) thumbHeight, 0xFF888888);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isActive) return false;
        
        if (maxScrollOffset <= 0) return false;
        
        int scrollAmount = 20;
        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - scrollAmount);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScrollOffset, scrollOffset + scrollAmount);
        }
        
        return true;
    }
}
