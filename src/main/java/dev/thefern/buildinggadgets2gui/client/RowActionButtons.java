package dev.thefern.buildinggadgets2gui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RowActionButtons {
    
    public static final int BUTTON_SIZE = 14;
    public static final int BUTTON_SIZE_SMALL = 12;
    public static final int BUTTON_SPACING = 2;
    
    public enum ButtonType {
        MATERIAL("M", 0xFF4466AA, 0xFF6688FF, "Show Materials"),
        CLIPBOARD("C", 0xFF44AA66, 0xFF66FF88, "Send to Clipboard"),
        TOOL("T", 0xFFAA6644, 0xFFFF8866, "Send to Tool"),
        TAGS("#", 0xFF8844AA, 0xFFAA66FF, "Edit Tags"),
        RESTORE("R", 0xFF44AA44, 0xFF66FF66, "Restore"),
        DELETE_PERM("X", 0xFFAA4444, 0xFFFF6666, "Delete Permanently");
        
        public final String label;
        public final int normalColor;
        public final int hoverColor;
        public final String tooltip;
        
        ButtonType(String label, int normalColor, int hoverColor, String tooltip) {
            this.label = label;
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.tooltip = tooltip;
        }
    }
    
    public static class ButtonBounds {
        public final int x;
        public final int y;
        public final int size;
        public final ButtonType type;
        
        public ButtonBounds(int x, int y, int size, ButtonType type) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.type = type;
        }
        
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + size &&
                   mouseY >= y && mouseY <= y + size;
        }
    }
    
    public static List<ButtonBounds> calculateButtonBounds(int rightX, int y, ButtonType... types) {
        return calculateButtonBounds(rightX, y, BUTTON_SIZE, types);
    }
    
    public static List<ButtonBounds> calculateButtonBounds(int rightX, int y, int buttonSize, ButtonType... types) {
        List<ButtonBounds> bounds = new ArrayList<>();
        int currentX = rightX;
        
        for (int i = types.length - 1; i >= 0; i--) {
            currentX -= buttonSize;
            bounds.add(0, new ButtonBounds(currentX, y, buttonSize, types[i]));
            if (i > 0) {
                currentX -= BUTTON_SPACING;
            }
        }
        
        return bounds;
    }
    
    public static void renderButton(GuiGraphics guiGraphics, ButtonBounds bounds, boolean isHovering) {
        guiGraphics.fill(
            bounds.x,
            bounds.y,
            bounds.x + bounds.size,
            bounds.y + bounds.size,
            isHovering ? bounds.type.hoverColor : bounds.type.normalColor
        );
        
        int textWidth = Minecraft.getInstance().font.width(bounds.type.label);
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            bounds.type.label,
            bounds.x + (bounds.size - textWidth) / 2,
            bounds.y + 3,
            0xFFFFFF,
            false
        );
    }
    
    public static void renderButtons(GuiGraphics guiGraphics, List<ButtonBounds> buttonsList, double mouseX, double mouseY) {
        for (ButtonBounds bounds : buttonsList) {
            boolean isHovering = bounds.contains(mouseX, mouseY);
            renderButton(guiGraphics, bounds, isHovering);
        }
    }
    
    public static void renderTooltip(GuiGraphics guiGraphics, List<ButtonBounds> buttonsList, int mouseX, int mouseY) {
        for (ButtonBounds bounds : buttonsList) {
            if (bounds.contains(mouseX, mouseY)) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal(bounds.type.tooltip),
                    mouseX,
                    mouseY
                );
                break;
            }
        }
    }
    
    public static ButtonType getClickedButton(List<ButtonBounds> buttonsList, double mouseX, double mouseY) {
        for (ButtonBounds bounds : buttonsList) {
            if (bounds.contains(mouseX, mouseY)) {
                return bounds.type;
            }
        }
        return null;
    }
}
