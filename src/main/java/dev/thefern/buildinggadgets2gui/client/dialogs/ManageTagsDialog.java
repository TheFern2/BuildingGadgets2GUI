package dev.thefern.buildinggadgets2gui.client.dialogs;

import dev.thefern.buildinggadgets2gui.client.schematics.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ManageTagsDialog extends Screen {
    
    private static final int DIALOG_WIDTH = 260;
    private static final int DIALOG_HEIGHT = 200;
    
    private static final int TAG_ROW_HEIGHT = 18;
    private static final int TAG_PADDING = 4;
    
    private final Screen parent;
    
    private EditBox newTagInput;
    private Button addTagButton;
    private Button closeButton;
    
    private List<TagRow> tagRows = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxVisibleTags = 5;
    
    private int dialogX;
    private int dialogY;
    private int tagsListY;
    private int tagsListHeight;
    
    private String editingTag = null;
    private EditBox renameInput = null;
    private int saveButtonX, saveButtonY;
    private int cancelButtonX, cancelButtonY;
    private static final int EDIT_BUTTON_SIZE = 16;
    
    public ManageTagsDialog(Screen parent) {
        super(Component.literal("Manage Tags"));
        this.parent = parent;
        TagManager.refreshUsageCounts();
    }
    
    @Override
    protected void init() {
        dialogX = (this.width - DIALOG_WIDTH) / 2;
        dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        int inputX = dialogX + 15;
        int inputWidth = DIALOG_WIDTH - 30;
        int inputY = dialogY + 30;
        
        int newTagInputWidth = inputWidth - 45;
        newTagInput = new EditBox(
            this.font,
            inputX,
            inputY,
            newTagInputWidth,
            18,
            Component.literal("New Tag")
        );
        newTagInput.setMaxLength(30);
        newTagInput.setHint(Component.literal("Enter new tag..."));
        this.addRenderableWidget(newTagInput);
        
        addTagButton = Button.builder(
            Component.literal("+"),
            button -> onAddTagPressed()
        )
        .bounds(inputX + newTagInputWidth + 5, inputY, 35, 18)
        .build();
        this.addRenderableWidget(addTagButton);
        
        tagsListY = inputY + 30;
        tagsListHeight = DIALOG_HEIGHT - 100;
        maxVisibleTags = tagsListHeight / TAG_ROW_HEIGHT;
        
        rebuildTagRows();
        
        int buttonY = dialogY + DIALOG_HEIGHT - 35;
        closeButton = Button.builder(
            Component.literal("Close"),
            button -> onClosePressed()
        )
        .bounds(dialogX + (DIALOG_WIDTH - 80) / 2, buttonY, 80, 20)
        .build();
        this.addRenderableWidget(closeButton);
        
        setInitialFocus(newTagInput);
    }
    
    private void rebuildTagRows() {
        tagRows.clear();
        
        List<String> allTags = TagManager.getAllTags();
        int y = tagsListY;
        
        for (int i = 0; i < allTags.size(); i++) {
            String tag = allTags.get(i);
            tagRows.add(new TagRow(tag, dialogX + 15, y + (i * TAG_ROW_HEIGHT), DIALOG_WIDTH - 30));
        }
    }
    
    private void onAddTagPressed() {
        String tag = newTagInput.getValue().trim().toLowerCase();
        if (!tag.isEmpty()) {
            boolean added = TagManager.addTag(tag);
            if (added) {
                newTagInput.setValue("");
                rebuildTagRows();
            }
        }
    }
    
    private void onClosePressed() {
        minecraft.setScreen(parent);
    }
    
    private void startRenaming(String tag) {
        editingTag = tag;
        
        clearRenameWidgets();
        
        for (TagRow row : tagRows) {
            if (row.tag.equals(tag)) {
                int inputWidth = row.width - 60;
                int inputY = row.y - scrollOffset * TAG_ROW_HEIGHT + 1;
                
                renameInput = new EditBox(
                    this.font,
                    row.x + 2,
                    inputY,
                    inputWidth,
                    TAG_ROW_HEIGHT - 2,
                    Component.literal("Rename")
                );
                renameInput.setMaxLength(30);
                renameInput.setValue(tag);
                this.addRenderableWidget(renameInput);
                
                saveButtonX = row.x + inputWidth + 4;
                saveButtonY = inputY;
                cancelButtonX = saveButtonX + EDIT_BUTTON_SIZE + 4;
                cancelButtonY = inputY;
                
                setFocused(renameInput);
                break;
            }
        }
    }
    
    private void clearRenameWidgets() {
        if (renameInput != null) {
            this.removeWidget(renameInput);
            renameInput = null;
        }
    }
    
    private void finishRenaming(boolean save) {
        if (editingTag != null && renameInput != null) {
            if (save) {
                String newName = renameInput.getValue().trim().toLowerCase();
                if (!newName.isEmpty() && !newName.equals(editingTag)) {
                    TagManager.renameTag(editingTag, newName);
                }
            }
            
            clearRenameWidgets();
            editingTag = null;
            rebuildTagRows();
        }
    }
    
    private void deleteTag(String tag) {
        ConfirmationDialog dialog = new ConfirmationDialog(
            this,
            "Delete Tag",
            "Delete tag '" + tag + "'? This will not remove it from existing schematics.",
            confirmed -> {
                if (confirmed) {
                    TagManager.removeTag(tag);
                    rebuildTagRows();
                }
            }
        );
        minecraft.setScreen(dialog);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF2A2A2A);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + 2, 0xFF4A4A4A);
        graphics.fill(dialogX, dialogY + DIALOG_HEIGHT - 2, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        graphics.fill(dialogX, dialogY, dialogX + 2, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        graphics.fill(dialogX + DIALOG_WIDTH - 2, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        
        graphics.fill(dialogX + 10, tagsListY - 5, dialogX + DIALOG_WIDTH - 10, tagsListY + tagsListHeight + 5, 0xFF1A1A1A);
        
        super.render(graphics, mouseX, mouseY, partialTick);
        
        graphics.drawString(
            this.font,
            "Manage Tags",
            dialogX + 15,
            dialogY + 10,
            0xFFFFFF,
            true
        );
        
        if (tagRows.isEmpty()) {
            graphics.drawString(
                this.font,
                "No tags yet. Add one above!",
                dialogX + 15,
                tagsListY + 20,
                0x888888,
                true
            );
        } else {
            graphics.enableScissor(dialogX + 10, tagsListY, dialogX + DIALOG_WIDTH - 10, tagsListY + tagsListHeight);
            
            for (int i = scrollOffset; i < Math.min(tagRows.size(), scrollOffset + maxVisibleTags + 1); i++) {
                TagRow row = tagRows.get(i);
                int renderY = row.y - scrollOffset * TAG_ROW_HEIGHT;
                
                if (renderY >= tagsListY - TAG_ROW_HEIGHT && renderY < tagsListY + tagsListHeight) {
                    renderTagRow(graphics, row, renderY, mouseX, mouseY);
                }
            }
            
            graphics.disableScissor();
            
            if (tagRows.size() > maxVisibleTags) {
                int scrollbarX = dialogX + DIALOG_WIDTH - 16;
                int scrollbarY = tagsListY;
                int scrollbarHeight = tagsListHeight;
                
                graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF333333);
                
                int maxScroll = Math.max(1, tagRows.size() - maxVisibleTags);
                float scrollRatio = (float) scrollOffset / maxScroll;
                int thumbHeight = Math.max(20, (int) ((float) maxVisibleTags / tagRows.size() * scrollbarHeight));
                int thumbY = scrollbarY + (int) (scrollRatio * (scrollbarHeight - thumbHeight));
                
                graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF666666);
            }
        }
    }
    
    private void renderTagRow(GuiGraphics graphics, TagRow row, int y, int mouseX, int mouseY) {
        boolean isEditing = editingTag != null && editingTag.equals(row.tag);
        
        if (isEditing) {
            boolean hoverSave = mouseX >= saveButtonX && mouseX <= saveButtonX + EDIT_BUTTON_SIZE &&
                               mouseY >= saveButtonY && mouseY <= saveButtonY + EDIT_BUTTON_SIZE;
            graphics.fill(saveButtonX, saveButtonY, saveButtonX + EDIT_BUTTON_SIZE, saveButtonY + EDIT_BUTTON_SIZE, 
                         hoverSave ? 0xFF33AA33 : 0xFF226622);
            graphics.drawString(this.font, "✓", saveButtonX + 4, saveButtonY + 4, 0xFFFFFF, true);
            
            boolean hoverCancel = mouseX >= cancelButtonX && mouseX <= cancelButtonX + EDIT_BUTTON_SIZE &&
                                 mouseY >= cancelButtonY && mouseY <= cancelButtonY + EDIT_BUTTON_SIZE;
            graphics.fill(cancelButtonX, cancelButtonY, cancelButtonX + EDIT_BUTTON_SIZE, cancelButtonY + EDIT_BUTTON_SIZE, 
                         hoverCancel ? 0xFFAA3333 : 0xFF663333);
            graphics.drawString(this.font, "X", cancelButtonX + 5, cancelButtonY + 4, 0xFFFFFF, true);
        } else {
            boolean hovered = mouseX >= row.x && mouseX <= row.x + row.width &&
                             mouseY >= y && mouseY <= y + TAG_ROW_HEIGHT;
            
            int bgColor = hovered ? 0xFF3A3A4A : 0xFF2A2A3A;
            graphics.fill(row.x, y, row.x + row.width, y + TAG_ROW_HEIGHT, bgColor);
            
            graphics.drawString(
                this.font,
                row.tag,
                row.x + TAG_PADDING,
                y + 5,
                0xFFFFFF,
                true
            );
            
            int deleteX = row.x + row.width - 20;
            boolean hoverDelete = mouseX >= deleteX && mouseX <= deleteX + 16 &&
                                 mouseY >= y + 2 && mouseY <= y + TAG_ROW_HEIGHT - 2;
            graphics.fill(deleteX, y + 2, deleteX + 16, y + TAG_ROW_HEIGHT - 2, hoverDelete ? 0xFFAA3333 : 0xFF663333);
            graphics.drawString(this.font, "X", deleteX + 5, y + 5, 0xFFFFFF, true);
            
            int renameX = deleteX - 22;
            boolean hoverRename = mouseX >= renameX && mouseX <= renameX + 18 &&
                                 mouseY >= y + 2 && mouseY <= y + TAG_ROW_HEIGHT - 2;
            graphics.fill(renameX, y + 2, renameX + 18, y + TAG_ROW_HEIGHT - 2, hoverRename ? 0xFF4466AA : 0xFF334488);
            graphics.drawString(this.font, "✎", renameX + 5, y + 5, 0xFFFFFF, true);
            
            int usageCount = TagManager.getTagUsageCount(row.tag);
            String countText = "(" + usageCount + ")";
            int countWidth = this.font.width(countText);
            int countX = renameX - countWidth - 6;
            graphics.drawString(
                this.font,
                countText,
                countX,
                y + 5,
                0x888888,
                true
            );
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (editingTag != null) {
                if (mouseX >= saveButtonX && mouseX <= saveButtonX + EDIT_BUTTON_SIZE &&
                    mouseY >= saveButtonY && mouseY <= saveButtonY + EDIT_BUTTON_SIZE) {
                    finishRenaming(true);
                    return true;
                }
                
                if (mouseX >= cancelButtonX && mouseX <= cancelButtonX + EDIT_BUTTON_SIZE &&
                    mouseY >= cancelButtonY && mouseY <= cancelButtonY + EDIT_BUTTON_SIZE) {
                    finishRenaming(false);
                    return true;
                }
            } else {
                for (int i = scrollOffset; i < Math.min(tagRows.size(), scrollOffset + maxVisibleTags + 1); i++) {
                    TagRow row = tagRows.get(i);
                    int renderY = row.y - scrollOffset * TAG_ROW_HEIGHT;
                    
                    if (mouseY >= renderY && mouseY <= renderY + TAG_ROW_HEIGHT &&
                        mouseX >= row.x && mouseX <= row.x + row.width) {
                        
                        int deleteX = row.x + row.width - 20;
                        if (mouseX >= deleteX && mouseX <= deleteX + 16) {
                            deleteTag(row.tag);
                            return true;
                        }
                        
                        int renameX = deleteX - 22;
                        if (mouseX >= renameX && mouseX <= renameX + 18) {
                            startRenaming(row.tag);
                            return true;
                        }
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= dialogX + 10 && mouseX <= dialogX + DIALOG_WIDTH - 10 &&
            mouseY >= tagsListY && mouseY <= tagsListY + tagsListHeight) {
            
            int maxScroll = Math.max(0, tagRows.size() - maxVisibleTags);
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (editingTag != null) {
                finishRenaming(false);
                return true;
            }
            onClosePressed();
            return true;
        }
        
        if (keyCode == 257) {
            if (editingTag != null) {
                finishRenaming(true);
                return true;
            }
            if (newTagInput.isFocused() && !newTagInput.getValue().trim().isEmpty()) {
                onAddTagPressed();
                return true;
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }
    
    private static class TagRow {
        final String tag;
        final int x;
        final int y;
        final int width;
        
        TagRow(String tag, int x, int y, int width) {
            this.tag = tag;
            this.x = x;
            this.y = y;
            this.width = width;
        }
    }
}
