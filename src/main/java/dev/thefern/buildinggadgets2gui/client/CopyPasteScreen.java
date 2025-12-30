package dev.thefern.buildinggadgets2gui.client;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class CopyPasteScreen extends Screen {
    
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 200;
    
    private int leftPos;
    private int topPos;
    
    private boolean hasCopyData = false;
    private int blockCount = 0;
    
    public CopyPasteScreen() {
        super(Component.literal("Copy/Paste Tool Manager"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = (this.height - WINDOW_HEIGHT) / 2;
        
        checkCopyData();
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Test Button"),
                button -> onTestButtonPressed()
            )
            .bounds(leftPos + 20, topPos + 100, 260, 20)
            .build()
        );
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Refresh Status"),
                button -> checkCopyData()
            )
            .bounds(leftPos + 20, topPos + 130, 260, 20)
            .build()
        );
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Close"),
                button -> this.onClose()
            )
            .bounds(leftPos + 20, topPos + 160, 260, 20)
            .build()
        );
    }
    
    private void checkCopyData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            hasCopyData = false;
            blockCount = 0;
            return;
        }
        
        ItemStack heldItem = mc.player.getMainHandItem();
        
        if (heldItem.getItem() instanceof GadgetCopyPaste) {
            UUID gadgetUUID = GadgetNBT.getUUID(heldItem);
            ArrayList<StatePos> copiedBlocks = BG2DataClient.getLookupFromUUID(gadgetUUID);
            
            if (copiedBlocks != null && !copiedBlocks.isEmpty()) {
                hasCopyData = true;
                blockCount = copiedBlocks.size();
            } else {
                hasCopyData = false;
                blockCount = 0;
            }
        } else {
            hasCopyData = false;
            blockCount = 0;
        }
    }
    
    private void onTestButtonPressed() {
        System.out.println("==============================================");
        System.out.println("Test button pressed!");
        System.out.println("Has copy data: " + hasCopyData);
        System.out.println("Block count: " + blockCount);
        System.out.println("==============================================");
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xC0101010);
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + 20, 0xFF303030);
        
        guiGraphics.drawString(
            this.font,
            this.title,
            leftPos + 10,
            topPos + 6,
            0xFFFFFF,
            false
        );
        
        String statusText = "Copy Data Status: " + (hasCopyData ? "YES" : "NO");
        guiGraphics.drawString(
            this.font,
            statusText,
            leftPos + 20,
            topPos + 40,
            hasCopyData ? 0x00FF00 : 0xFF0000,
            false
        );
        
        if (hasCopyData) {
            String countText = "Blocks: " + blockCount;
            guiGraphics.drawString(
                this.font,
                countText,
                leftPos + 20,
                topPos + 60,
                0xFFFFFF,
                false
            );
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

