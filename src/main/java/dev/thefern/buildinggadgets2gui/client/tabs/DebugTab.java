package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.client.ClipboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DebugTab extends TabPanel {
    
    private ClipboardUtils.CopyData copyData = new ClipboardUtils.CopyData();
    
    public DebugTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        copyData = ClipboardUtils.checkCopyData();
        
        int buttonY = y + 90;
        
        widgets.add(Button.builder(
            Component.literal("Test Button"),
            button -> onTestButtonPressed()
        )
        .bounds(x + 20, buttonY, 360, 20)
        .build());
        buttonY += 24;
        
        widgets.add(Button.builder(
            Component.literal("Refresh Status"),
            button -> { copyData = ClipboardUtils.checkCopyData(); }
        )
        .bounds(x + 20, buttonY, 360, 20)
        .build());
        buttonY += 24;
        
        widgets.add(Button.builder(
            Component.literal("Copy from Tool"),
            button -> ClipboardUtils.copyFromTool()
        )
        .bounds(x + 20, buttonY, 175, 20)
        .build());
        
        widgets.add(Button.builder(
            Component.literal("Send to Tool"),
            button -> ClipboardUtils.sendToTool()
        )
        .bounds(x + 205, buttonY, 175, 20)
        .build());
        buttonY += 24;
        
        widgets.add(Button.builder(
            Component.literal("Print Clipboard Data"),
            button -> ClipboardUtils.printClipboard()
        )
        .bounds(x + 20, buttonY, 175, 20)
        .build());
        
        widgets.add(Button.builder(
            Component.literal("Clear Clipboard"),
            button -> ClipboardUtils.clearClipboard()
        )
        .bounds(x + 205, buttonY, 175, 20)
        .build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        int yOffset = y + 15;
        
        String statusText = "Copy Data Status: " + (copyData.hasCopyData ? "YES" : "NO");
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            statusText,
            x + 20,
            yOffset,
            copyData.hasCopyData ? 0x00FF00 : 0xFF0000,
            false
        );
        yOffset += 20;
        
        if (copyData.hasCopyData) {
            String countText = "Blocks: " + copyData.blockCount;
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                countText,
                x + 20,
                yOffset,
                0xFFFFFF,
                false
            );
            yOffset += 15;
            
            if (copyData.gadgetUUID != null) {
                String gadgetText = "Gadget: " + ClipboardUtils.formatUUID(copyData.gadgetUUID);
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    gadgetText,
                    x + 20,
                    yOffset,
                    0xAAAAAA,
                    false
                );
                yOffset += 12;
            }
            
            if (copyData.copyUUID != null) {
                String copyText = "Copy: " + ClipboardUtils.formatUUID(copyData.copyUUID);
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    copyText,
                    x + 20,
                    yOffset,
                    0xAAAAAA,
                    false
                );
            }
        }
    }
    
    private void onTestButtonPressed() {
        System.out.println("==============================================");
        System.out.println("Test button pressed!");
        System.out.println("Has copy data: " + copyData.hasCopyData);
        System.out.println("Block count: " + copyData.blockCount);
        System.out.println("Gadget UUID: " + ClipboardUtils.formatUUID(copyData.gadgetUUID));
        System.out.println("Copy UUID: " + ClipboardUtils.formatUUID(copyData.copyUUID));
        System.out.println("==============================================");
    }
    
    @Override
    public void tick() {
        copyData = ClipboardUtils.checkCopyData();
    }
}
