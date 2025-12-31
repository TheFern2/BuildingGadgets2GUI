package dev.thefern.buildinggadgets2gui.client.dialogs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ConfirmationDialog extends Screen {
    
    private static final int DIALOG_WIDTH = 280;
    private static final int DIALOG_HEIGHT = 110;
    
    private final Screen parent;
    private final String message;
    private final Consumer<Boolean> callback;
    
    private Button yesButton;
    private Button noButton;
    
    private int dialogX;
    private int dialogY;
    
    public ConfirmationDialog(Screen parent, String title, String message, Consumer<Boolean> callback) {
        super(Component.literal(title));
        this.parent = parent;
        this.message = message;
        this.callback = callback;
    }
    
    @Override
    protected void init() {
        dialogX = (this.width - DIALOG_WIDTH) / 2;
        dialogY = (this.height - DIALOG_HEIGHT) / 2;
        
        int buttonY = dialogY + DIALOG_HEIGHT - 35;
        int buttonWidth = 100;
        
        yesButton = Button.builder(
            Component.literal("Yes"),
            button -> onConfirm(true)
        )
        .bounds(dialogX + 30, buttonY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(yesButton);
        
        noButton = Button.builder(
            Component.literal("No"),
            button -> onConfirm(false)
        )
        .bounds(dialogX + DIALOG_WIDTH - buttonWidth - 30, buttonY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(noButton);
    }
    
    private void onConfirm(boolean result) {
        callback.accept(result);
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
            this.getTitle().getString(),
            dialogX + 20,
            dialogY + 10,
            0xFFFFFF,
            false
        );
        
        String[] lines = wrapText(message, DIALOG_WIDTH - 40);
        int textY = dialogY + 35;
        for (String line : lines) {
            int textWidth = this.font.width(line);
            graphics.drawString(
                this.font,
                line,
                dialogX + (DIALOG_WIDTH - textWidth) / 2,
                textY,
                0xCCCCCC,
                false
            );
            textY += 12;
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private String[] wrapText(String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (this.font.width(testLine) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onConfirm(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

