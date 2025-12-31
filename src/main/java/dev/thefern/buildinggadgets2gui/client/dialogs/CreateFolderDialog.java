package dev.thefern.buildinggadgets2gui.client.dialogs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class CreateFolderDialog extends Screen {
    
    private final Screen parent;
    private final Consumer<String> onConfirm;
    private final Runnable onCancel;
    
    private EditBox folderNameField;
    private Button createButton;
    private Button cancelButton;
    
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 120;
    
    public CreateFolderDialog(Screen parent, Consumer<String> onConfirm, Runnable onCancel) {
        super(Component.literal("Create Folder"));
        this.parent = parent;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int dialogX = centerX - DIALOG_WIDTH / 2;
        int dialogY = centerY - DIALOG_HEIGHT / 2;
        
        folderNameField = new EditBox(
            this.font,
            dialogX + 20,
            dialogY + 45,
            DIALOG_WIDTH - 40,
            20,
            Component.literal("Folder Name")
        );
        folderNameField.setMaxLength(50);
        folderNameField.setValue("");
        folderNameField.setHint(Component.literal("Enter folder name..."));
        this.addRenderableWidget(folderNameField);
        this.setInitialFocus(folderNameField);
        
        createButton = Button.builder(
            Component.literal("Create"),
            button -> onCreatePressed()
        )
        .bounds(dialogX + 20, dialogY + 80, 120, 20)
        .build();
        this.addRenderableWidget(createButton);
        
        cancelButton = Button.builder(
            Component.literal("Cancel"),
            button -> onCancelPressed()
        )
        .bounds(dialogX + 160, dialogY + 80, 120, 20)
        .build();
        this.addRenderableWidget(cancelButton);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int dialogX = centerX - DIALOG_WIDTH / 2;
        int dialogY = centerY - DIALOG_HEIGHT / 2;
        
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
        
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF2A2A2A);
        graphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + 1, 0xFF555555);
        graphics.fill(dialogX, dialogY, dialogX + 1, dialogY + DIALOG_HEIGHT, 0xFF555555);
        graphics.fill(dialogX + DIALOG_WIDTH - 1, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF000000);
        graphics.fill(dialogX, dialogY + DIALOG_HEIGHT - 1, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF000000);
        
        String title = "Create New Folder";
        int titleWidth = this.font.width(title);
        graphics.drawString(
            this.font,
            title,
            dialogX + (DIALOG_WIDTH - titleWidth) / 2,
            dialogY + 15,
            0xFFFFFF,
            false
        );
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private void onCreatePressed() {
        String folderName = folderNameField.getValue().trim();
        if (!folderName.isEmpty()) {
            if (isValidFolderName(folderName)) {
                onConfirm.accept(folderName);
                this.minecraft.setScreen(parent);
            }
        }
    }
    
    private void onCancelPressed() {
        this.minecraft.setScreen(parent);
        if (onCancel != null) {
            onCancel.run();
        }
    }
    
    private boolean isValidFolderName(String name) {
        if (name.matches(".*[/\\\\:*?\"<>|].*")) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onCancelPressed();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            onCreatePressed();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

