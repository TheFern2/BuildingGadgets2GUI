package dev.thefern.buildinggadgets2gui.client.dialogs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SaveSchematicDialog extends Screen {
    
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 150;
    
    private final Screen parent;
    private final Consumer<SaveResult> onSave;
    private final Consumer<Void> onCancel;
    
    private EditBox nameInput;
    private EditBox descriptionInput;
    private Button saveButton;
    private Button cancelButton;
    
    private int dialogX;
    private int dialogY;
    
    public SaveSchematicDialog(Screen parent, Consumer<SaveResult> onSave, Consumer<Void> onCancel) {
        super(Component.literal("Save Schematic"));
        this.parent = parent;
        this.onSave = onSave;
        this.onCancel = onCancel;
    }
    
    @Override
    protected void init() {
        dialogX = (this.width - DIALOG_WIDTH) / 2;
        dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        int inputWidth = DIALOG_WIDTH - 40;
        int inputX = dialogX + 20;
        int inputY = dialogY + 30;
        
        nameInput = new EditBox(
            this.font,
            inputX,
            inputY,
            inputWidth,
            20,
            Component.literal("Schematic Name")
        );
        nameInput.setMaxLength(50);
        nameInput.setHint(Component.literal("Enter schematic name..."));
        nameInput.setResponder(text -> updateSaveButton());
        this.addRenderableWidget(nameInput);
        
        descriptionInput = new EditBox(
            this.font,
            inputX,
            inputY + 30,
            inputWidth,
            20,
            Component.literal("Description")
        );
        descriptionInput.setMaxLength(100);
        descriptionInput.setHint(Component.literal("Optional description..."));
        this.addRenderableWidget(descriptionInput);
        
        int buttonY = dialogY + DIALOG_HEIGHT - 35;
        int buttonWidth = 120;
        
        saveButton = Button.builder(
            Component.literal("Save"),
            button -> onSavePressed()
        )
        .bounds(dialogX + 20, buttonY, buttonWidth, 20)
        .build();
        saveButton.active = false;
        this.addRenderableWidget(saveButton);
        
        cancelButton = Button.builder(
            Component.literal("Cancel"),
            button -> onCancelPressed()
        )
        .bounds(dialogX + DIALOG_WIDTH - buttonWidth - 20, buttonY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(cancelButton);
        
        setInitialFocus(nameInput);
    }
    
    private void updateSaveButton() {
        saveButton.active = !nameInput.getValue().trim().isEmpty();
    }
    
    private void onSavePressed() {
        String name = nameInput.getValue().trim();
        String description = descriptionInput.getValue().trim();
        
        if (!name.isEmpty()) {
            SaveResult result = new SaveResult(
                name,
                description.isEmpty() ? null : description,
                new ArrayList<>()
            );
            onSave.accept(result);
            minecraft.setScreen(parent);
        }
    }
    
    private void onCancelPressed() {
        onCancel.accept(null);
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
        
        graphics.drawString(
            this.font,
            "Save Schematic",
            dialogX + 20,
            dialogY + 10,
            0xFFFFFF,
            false
        );
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onCancelPressed();
            return true;
        }
        if (keyCode == 257 && saveButton.active) {
            onSavePressed();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public static class SaveResult {
        public final String name;
        public final String description;
        public final List<String> tags;
        
        public SaveResult(String name, String description, List<String> tags) {
            this.name = name;
            this.description = description;
            this.tags = tags;
        }
    }
}

