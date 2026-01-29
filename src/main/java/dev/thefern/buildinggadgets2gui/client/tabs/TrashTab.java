package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFile;
import dev.thefern.buildinggadgets2gui.client.schematics.TrashList;
import dev.thefern.buildinggadgets2gui.client.schematics.TrashManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrashTab extends TabPanel {
    
    private static final int LIST_WIDTH = 240;
    private static final int LIST_HEIGHT = 140;
    private static final int INFO_PANEL_WIDTH = 140;
    private static final int PADDING = 10;
    
    private TrashList trashList;
    private Button emptyTrashButton;
    private TrashManager.TrashEntry selectedEntry = null;
    
    public TrashTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        int buttonY = y + 5;
        
        emptyTrashButton = Button.builder(
            Component.literal("Empty Trash"),
            button -> onEmptyTrashPressed()
        )
        .bounds(x + width - PADDING - 90, buttonY, 80, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Permanently delete all items in trash")
        ))
        .build();
        widgets.add(emptyTrashButton);
        
        int listY = buttonY + 30;
        trashList = new TrashList(
            Minecraft.getInstance(),
            LIST_WIDTH,
            LIST_HEIGHT,
            listY,
            24,
            this
        );
        trashList.setX(x + PADDING);
        
        updateEmptyTrashButton();
    }
    
    public Screen getParentScreen() {
        return parentScreen;
    }
    
    public void onEntrySelected(TrashManager.TrashEntry entry) {
        selectedEntry = entry;
        System.out.println("Selected trash entry: " + entry.fileName);
    }
    
    public void onTrashChanged() {
        selectedEntry = null;
        updateEmptyTrashButton();
    }
    
    private void updateEmptyTrashButton() {
        if (emptyTrashButton != null) {
            emptyTrashButton.active = TrashManager.getTrashCount() > 0;
        }
    }
    
    private void onEmptyTrashPressed() {
        int count = TrashManager.getTrashCount();
        if (count == 0) return;
        
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Empty Trash?",
            "Permanently delete " + count + " item" + (count > 1 ? "s" : "") + "? This cannot be undone.",
            confirmed -> {
                if (confirmed) {
                    TrashManager.emptyTrash();
                    if (trashList != null) {
                        trashList.refreshList();
                    }
                    selectedEntry = null;
                    updateEmptyTrashButton();
                    System.out.println("Trash emptied");
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        TrashManager.refresh();
        if (emptyTrashButton != null) {
            emptyTrashButton.visible = true;
            emptyTrashButton.active = TrashManager.getTrashCount() > 0;
        }
        if (trashList != null) {
            trashList.refreshList();
        }
        selectedEntry = null;
    }
    
    @Override
    public void onTabDeactivated() {
        super.onTabDeactivated();
        if (emptyTrashButton != null) {
            emptyTrashButton.visible = false;
            emptyTrashButton.active = false;
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        int trashCount = TrashManager.getTrashCount();
        String headerText = "Trash (" + trashCount + " item" + (trashCount != 1 ? "s" : "") + ")";
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            headerText,
            x + PADDING,
            y + 10,
            0xFFFFFF,
            false
        );
        
        int listY = y + 35;
        
        if (trashList != null) {
            trashList.setY(listY);
            trashList.render(guiGraphics, mouseX, mouseY, partialTick);
            trashList.renderPendingTooltip(guiGraphics);
        }
        
        renderInfoPanel(guiGraphics, listY);
        
        if (trashCount == 0) {
            int centerX = x + PADDING + LIST_WIDTH / 2;
            int centerY = listY + LIST_HEIGHT / 2;
            
            String emptyText = "Trash is empty";
            int textWidth = Minecraft.getInstance().font.width(emptyText);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                emptyText,
                centerX - textWidth / 2,
                centerY,
                0x888888,
                false
            );
        }
    }
    
    private void renderInfoPanel(GuiGraphics guiGraphics, int listY) {
        int infoX = x + PADDING + LIST_WIDTH + PADDING;
        int infoY = listY;
        
        guiGraphics.fill(infoX, infoY, infoX + INFO_PANEL_WIDTH, infoY + LIST_HEIGHT, 0xFF2A2A2A);
        
        if (selectedEntry != null) {
            int textY = infoY + 10;
            int textX = infoX + 5;
            
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                "Trash Info",
                textX,
                textY,
                0xFFFFFF,
                false
            );
            textY += 15;
            
            SchematicFile schematicFile = selectedEntry.getSchematicFile();
            if (schematicFile != null) {
                SchematicFile.SchematicMetadata metadata = schematicFile.getMetadata();
                if (metadata != null) {
                    guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        "Name:",
                        textX,
                        textY,
                        0xAAAAAA,
                        false
                    );
                    textY += 10;
                    
                    String name = metadata.name;
                    if (name.length() > 18) {
                        name = name.substring(0, 15) + "...";
                    }
                    guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        name,
                        textX,
                        textY,
                        0xFFFFFF,
                        false
                    );
                    textY += 15;
                    
                    SchematicFile.SchematicData data = schematicFile.loadData();
                    if (data != null) {
                        guiGraphics.drawString(
                            Minecraft.getInstance().font,
                            "Blocks: " + data.blockCount,
                            textX,
                            textY,
                            0xAAAAAA,
                            false
                        );
                        textY += 12;
                        
                        if (data.dimensions != null) {
                            guiGraphics.drawString(
                                Minecraft.getInstance().font,
                                "Size: " + data.dimensions.toString(),
                                textX,
                                textY,
                                0xAAAAAA,
                                false
                            );
                            textY += 12;
                        }
                    }
                }
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
            String deletedStr = sdf.format(new Date(selectedEntry.deletedAt));
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                "Deleted:",
                textX,
                textY,
                0xAAAAAA,
                false
            );
            textY += 10;
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                deletedStr,
                textX,
                textY,
                0xFF8888,
                false
            );
            textY += 15;
            
            String originalPath = selectedEntry.originalPath;
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                "From:",
                textX,
                textY,
                0xAAAAAA,
                false
            );
            textY += 10;
            String fromText = (originalPath == null || originalPath.isEmpty()) ? "/" : originalPath;
            if (fromText.length() > 18) {
                fromText = "..." + fromText.substring(fromText.length() - 15);
            }
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                fromText,
                textX,
                textY,
                0xFFFFFF,
                false
            );
        } else {
            int textY = infoY + LIST_HEIGHT / 2;
            String text = "Select an item";
            int textWidth = Minecraft.getInstance().font.width(text);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                text,
                infoX + (INFO_PANEL_WIDTH - textWidth) / 2,
                textY,
                0x888888,
                false
            );
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive || button != 0) return false;
        
        if (trashList != null) {
            return trashList.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (trashList != null && isActive) {
            return trashList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }
}
