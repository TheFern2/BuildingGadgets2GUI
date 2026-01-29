package dev.thefern.buildinggadgets2gui.client.dialogs;

import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFile;
import dev.thefern.buildinggadgets2gui.client.schematics.TagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditTagsDialog extends Screen {
    
    private static final int DIALOG_WIDTH = 280;
    private static final int DIALOG_HEIGHT = 220;
    
    private static final int TAG_CHIP_HEIGHT = 16;
    private static final int TAG_CHIP_PADDING = 4;
    private static final int TAG_CHIP_SPACING = 4;
    private static final int TAGS_AREA_HEIGHT = 120;
    
    private final Screen parent;
    private final SchematicFile schematicFile;
    private final Consumer<Boolean> onClose;
    
    private EditBox tagInput;
    private Button addTagButton;
    private Button saveButton;
    private Button cancelButton;
    
    private List<String> currentTags = new ArrayList<>();
    private List<TagChip> selectedTagChips = new ArrayList<>();
    private List<TagChip> availableTagChips = new ArrayList<>();
    
    private int dialogX;
    private int dialogY;
    private int inputY;
    private int tagsAreaY;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    
    public EditTagsDialog(Screen parent, SchematicFile schematicFile, Consumer<Boolean> onClose) {
        super(Component.literal("Edit Tags"));
        this.parent = parent;
        this.schematicFile = schematicFile;
        this.onClose = onClose;
        
        this.currentTags = new ArrayList<>(schematicFile.getTags());
    }
    
    @Override
    protected void init() {
        dialogX = (this.width - DIALOG_WIDTH) / 2;
        dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        int inputX = dialogX + 15;
        int inputWidth = DIALOG_WIDTH - 30;
        
        inputY = dialogY + 30;
        tagsAreaY = inputY + 25;
        
        int tagInputWidth = inputWidth - 45;
        tagInput = new EditBox(
            this.font,
            inputX,
            inputY,
            tagInputWidth,
            18,
            Component.literal("Tag")
        );
        tagInput.setMaxLength(30);
        tagInput.setHint(Component.literal("Add new tag..."));
        this.addRenderableWidget(tagInput);
        
        addTagButton = Button.builder(
            Component.literal("+"),
            button -> onAddTagPressed()
        )
        .bounds(inputX + tagInputWidth + 5, inputY, 35, 18)
        .build();
        this.addRenderableWidget(addTagButton);
        
        rebuildTagChips();
        
        int buttonY = dialogY + DIALOG_HEIGHT - 35;
        int buttonWidth = 100;
        
        saveButton = Button.builder(
            Component.literal("Save"),
            button -> onSavePressed()
        )
        .bounds(dialogX + 15, buttonY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(saveButton);
        
        cancelButton = Button.builder(
            Component.literal("Cancel"),
            button -> onCancelPressed()
        )
        .bounds(dialogX + DIALOG_WIDTH - buttonWidth - 15, buttonY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(cancelButton);
        
        setInitialFocus(tagInput);
    }
    
    private void rebuildTagChips() {
        selectedTagChips.clear();
        availableTagChips.clear();
        
        int inputX = dialogX + 15;
        int maxWidth = DIALOG_WIDTH - 40;
        
        int chipY = 0;
        int currentX = inputX;
        
        if (!currentTags.isEmpty()) {
            chipY = 12;
        }
        
        for (String tag : currentTags) {
            int chipWidth = this.font.width(tag) + TAG_CHIP_PADDING * 2 + 12;
            if (currentX + chipWidth > inputX + maxWidth && currentX != inputX) {
                currentX = inputX;
                chipY += TAG_CHIP_HEIGHT + TAG_CHIP_SPACING;
            }
            selectedTagChips.add(new TagChip(tag, currentX, chipY, chipWidth, true));
            currentX += chipWidth + TAG_CHIP_SPACING;
        }
        
        if (!currentTags.isEmpty()) {
            chipY += TAG_CHIP_HEIGHT + TAG_CHIP_SPACING + 18;
        } else {
            chipY = 12;
        }
        currentX = inputX;
        
        int availableStartY = chipY;
        
        for (String tag : TagManager.getAllTags()) {
            if (currentTags.contains(tag)) continue;
            
            int chipWidth = this.font.width(tag) + TAG_CHIP_PADDING * 2 + 8;
            if (currentX + chipWidth > inputX + maxWidth && currentX != inputX) {
                currentX = inputX;
                chipY += TAG_CHIP_HEIGHT + TAG_CHIP_SPACING;
            }
            availableTagChips.add(new TagChip(tag, currentX, chipY, chipWidth, false));
            currentX += chipWidth + TAG_CHIP_SPACING;
        }
        
        int totalContentHeight = chipY + TAG_CHIP_HEIGHT + TAG_CHIP_SPACING;
        maxScrollOffset = Math.max(0, totalContentHeight - TAGS_AREA_HEIGHT);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);
    }
    
    private void onAddTagPressed() {
        String tag = tagInput.getValue().trim().toLowerCase();
        if (!tag.isEmpty() && !currentTags.contains(tag)) {
            currentTags.add(tag);
            TagManager.ensureTagExists(tag);
            tagInput.setValue("");
            rebuildTagChips();
        }
    }
    
    private void onSavePressed() {
        boolean success = schematicFile.setTags(currentTags);
        if (success) {
            System.out.println("Updated tags for: " + schematicFile.getName());
        } else {
            System.err.println("Failed to update tags for: " + schematicFile.getName());
        }
        onClose.accept(success);
        minecraft.setScreen(parent);
    }
    
    private void onCancelPressed() {
        onClose.accept(false);
        minecraft.setScreen(parent);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF2A2A2A);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + 2, 0xFF4A4A4A);
        graphics.fill(dialogX, dialogY + DIALOG_HEIGHT - 2, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        graphics.fill(dialogX, dialogY, dialogX + 2, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        graphics.fill(dialogX + DIALOG_WIDTH - 2, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF4A4A4A);
        
        graphics.fill(dialogX + 10, tagsAreaY, dialogX + DIALOG_WIDTH - 10, tagsAreaY + TAGS_AREA_HEIGHT, 0xFF1A1A1A);
        
        super.render(graphics, mouseX, mouseY, partialTick);
        
        String title = "Edit Tags: " + schematicFile.getName();
        if (title.length() > 35) {
            title = title.substring(0, 32) + "...";
        }
        graphics.drawString(
            this.font,
            title,
            dialogX + 15,
            dialogY + 10,
            0xFFFFFF,
            true
        );
        
        graphics.enableScissor(dialogX + 10, tagsAreaY, dialogX + DIALOG_WIDTH - 10, tagsAreaY + TAGS_AREA_HEIGHT);
        
        int offsetY = tagsAreaY - scrollOffset;
        
        if (!currentTags.isEmpty()) {
            graphics.drawString(
                this.font,
                "Current tags (click to remove):",
                dialogX + 15,
                offsetY + 2,
                0x888888,
                true
            );
        }
        
        for (TagChip chip : selectedTagChips) {
            renderTagChip(graphics, chip, offsetY, mouseX, mouseY);
        }
        
        if (!availableTagChips.isEmpty()) {
            int labelY = selectedTagChips.isEmpty() ? 2 : 
                selectedTagChips.get(selectedTagChips.size() - 1).y + TAG_CHIP_HEIGHT + 8;
            graphics.drawString(
                this.font,
                "Available tags (click to add):",
                dialogX + 15,
                offsetY + labelY,
                0x888888,
                true
            );
        }
        
        for (TagChip chip : availableTagChips) {
            renderTagChip(graphics, chip, offsetY, mouseX, mouseY);
        }
        
        graphics.disableScissor();
        
        if (maxScrollOffset > 0) {
            int scrollbarX = dialogX + DIALOG_WIDTH - 16;
            int scrollbarY = tagsAreaY;
            int scrollbarHeight = TAGS_AREA_HEIGHT;
            
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF333333);
            
            float scrollRatio = (float) scrollOffset / maxScrollOffset;
            int thumbHeight = Math.max(20, (int) ((float) TAGS_AREA_HEIGHT / (TAGS_AREA_HEIGHT + maxScrollOffset) * scrollbarHeight));
            int thumbY = scrollbarY + (int) (scrollRatio * (scrollbarHeight - thumbHeight));
            
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF666666);
        }
    }
    
    private void renderTagChip(GuiGraphics graphics, TagChip chip, int offsetY, int mouseX, int mouseY) {
        int renderY = offsetY + chip.y;
        
        boolean hovered = mouseX >= chip.x && mouseX <= chip.x + chip.width &&
                         mouseY >= renderY && mouseY <= renderY + TAG_CHIP_HEIGHT &&
                         mouseY >= tagsAreaY && mouseY <= tagsAreaY + TAGS_AREA_HEIGHT;
        
        int bgColor = chip.selected ? 
            (hovered ? 0xFF6A3A3A : 0xFF4A6A4A) : 
            (hovered ? 0xFF4A4A6A : 0xFF3A3A4A);
        
        graphics.fill(chip.x, renderY, chip.x + chip.width, renderY + TAG_CHIP_HEIGHT, bgColor);
        
        graphics.fill(chip.x, renderY, chip.x + chip.width, renderY + 1, 0xFF5A5A5A);
        graphics.fill(chip.x, renderY + TAG_CHIP_HEIGHT - 1, chip.x + chip.width, renderY + TAG_CHIP_HEIGHT, 0xFF2A2A2A);
        
        String displayText = chip.selected ? chip.tag + " ×" : "+" + chip.tag;
        graphics.drawString(
            this.font,
            displayText,
            chip.x + TAG_CHIP_PADDING,
            renderY + 4,
            chip.selected ? 0xFFFFFF : 0xCCCCCC,
            true
        );
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= tagsAreaY && mouseY <= tagsAreaY + TAGS_AREA_HEIGHT) {
            int offsetY = tagsAreaY - scrollOffset;
            
            for (TagChip chip : selectedTagChips) {
                int renderY = offsetY + chip.y;
                if (mouseX >= chip.x && mouseX <= chip.x + chip.width &&
                    mouseY >= renderY && mouseY <= renderY + TAG_CHIP_HEIGHT) {
                    currentTags.remove(chip.tag);
                    rebuildTagChips();
                    return true;
                }
            }
            
            for (TagChip chip : availableTagChips) {
                int renderY = offsetY + chip.y;
                if (mouseX >= chip.x && mouseX <= chip.x + chip.width &&
                    mouseY >= renderY && mouseY <= renderY + TAG_CHIP_HEIGHT) {
                    currentTags.add(chip.tag);
                    rebuildTagChips();
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= dialogX + 10 && mouseX <= dialogX + DIALOG_WIDTH - 10 &&
            mouseY >= tagsAreaY && mouseY <= tagsAreaY + TAGS_AREA_HEIGHT) {
            
            scrollOffset = (int) Math.max(0, Math.min(maxScrollOffset, scrollOffset - scrollY * 10));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onCancelPressed();
            return true;
        }
        if (keyCode == 257) {
            if (tagInput.isFocused() && !tagInput.getValue().trim().isEmpty()) {
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
    
    private static class TagChip {
        final String tag;
        final int x;
        final int y;
        final int width;
        final boolean selected;
        
        TagChip(String tag, int x, int y, int width, boolean selected) {
            this.tag = tag;
            this.x = x;
            this.y = y;
            this.width = width;
            this.selected = selected;
        }
    }
}
