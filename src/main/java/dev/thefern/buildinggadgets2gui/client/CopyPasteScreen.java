package dev.thefern.buildinggadgets2gui.client;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import dev.thefern.buildinggadgets2gui.network.SendClipboardToGadgetPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.UUID;

public class CopyPasteScreen extends Screen {
    
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 260;
    
    private int leftPos;
    private int topPos;
    
    private boolean hasCopyData = false;
    private int blockCount = 0;
    private UUID gadgetUUID = null;
    private UUID copyUUID = null;
    private ArrayList<StatePos> copiedBlocks = null;
    
    private static ArrayList<StatePos> clipboardBlocks = null;
    private static UUID clipboardCopyUUID = null;
    private static int clipboardBlockCount = 0;
    
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
                Component.literal("Copy from Tool"),
                button -> onCopyFromTool()
            )
            .bounds(leftPos + 20, topPos + 160, 125, 20)
            .build()
        );
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Send to Tool"),
                button -> onSendToTool()
            )
            .bounds(leftPos + 155, topPos + 160, 125, 20)
            .build()
        );
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Print Clipboard Data"),
                button -> onPrintClipboard()
            )
            .bounds(leftPos + 20, topPos + 190, 125, 20)
            .build()
        );
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Clear Clipboard"),
                button -> onClearClipboard()
            )
            .bounds(leftPos + 155, topPos + 190, 125, 20)
            .build()
        );
        
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Close"),
                button -> this.onClose()
            )
            .bounds(leftPos + 20, topPos + 220, 260, 20)
            .build()
        );
    }
    
    private void checkCopyData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            hasCopyData = false;
            blockCount = 0;
            gadgetUUID = null;
            copyUUID = null;
            copiedBlocks = null;
            return;
        }
        
        ItemStack heldItem = mc.player.getMainHandItem();
        
        if (heldItem.getItem() instanceof GadgetCopyPaste) {
            gadgetUUID = GadgetNBT.getUUID(heldItem);
            copyUUID = GadgetNBT.hasCopyUUID(heldItem) ? GadgetNBT.getCopyUUID(heldItem) : null;
            
            BG2DataClient.isClientUpToDate(heldItem);
            
            copiedBlocks = BG2DataClient.getLookupFromUUID(gadgetUUID);
            
            if (copiedBlocks != null && !copiedBlocks.isEmpty()) {
                hasCopyData = true;
                blockCount = copiedBlocks.size();
            } else {
                hasCopyData = false;
                blockCount = 0;
                copiedBlocks = null;
            }
        } else {
            hasCopyData = false;
            blockCount = 0;
            gadgetUUID = null;
            copyUUID = null;
            copiedBlocks = null;
        }
    }
    
    private void onTestButtonPressed() {
        System.out.println("==============================================");
        System.out.println("Test button pressed!");
        System.out.println("Has copy data: " + hasCopyData);
        System.out.println("Block count: " + blockCount);
        System.out.println("Gadget UUID: " + (gadgetUUID != null ? gadgetUUID.toString().substring(0, 8) + "..." : "null"));
        System.out.println("Copy UUID: " + (copyUUID != null ? copyUUID.toString().substring(0, 8) + "..." : "null"));
        System.out.println("==============================================");
    }
    
    private void onCopyFromTool() {
        if (!hasCopyData || copiedBlocks == null || copiedBlocks.isEmpty()) {
            System.out.println("No copy data available on tool to copy!");
            return;
        }
        
        clipboardBlocks = new ArrayList<>();
        for (StatePos statePos : copiedBlocks) {
            clipboardBlocks.add(new StatePos(statePos.state, statePos.pos.immutable()));
        }
        clipboardCopyUUID = copyUUID;
        clipboardBlockCount = clipboardBlocks.size();
        
        System.out.println("==============================================");
        System.out.println("Copied data from tool to clipboard!");
        System.out.println("Clipboard blocks: " + clipboardBlockCount);
        System.out.println("Clipboard Copy UUID: " + (clipboardCopyUUID != null ? clipboardCopyUUID.toString().substring(0, 8) + "..." : "null"));
        System.out.println("==============================================");
    }
    
    private void onSendToTool() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        ItemStack heldItem = mc.player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GadgetCopyPaste)) {
            System.out.println("Not holding a Copy/Paste gadget!");
            return;
        }
        
        if (clipboardBlocks == null || clipboardBlocks.isEmpty()) {
            System.out.println("Clipboard is empty! Use 'Copy from Tool' first.");
            return;
        }
        
        UUID targetGadgetUUID = GadgetNBT.getUUID(heldItem);
        UUID newCopyUUID = UUID.randomUUID();
        
        CompoundTag tag = BG2Data.statePosListToNBTMapArray(clipboardBlocks);
        
        PacketDistributor.sendToServer(new SendClipboardToGadgetPayload(targetGadgetUUID, newCopyUUID, tag));
        
        System.out.println("==============================================");
        System.out.println("Sent clipboard data to tool!");
        System.out.println("Target Gadget UUID: " + targetGadgetUUID.toString().substring(0, 8) + "...");
        System.out.println("New Copy UUID: " + newCopyUUID.toString().substring(0, 8) + "...");
        System.out.println("Blocks sent: " + clipboardBlocks.size());
        System.out.println("==============================================");
    }
    
    private void onPrintClipboard() {
        System.out.println("==============================================");
        System.out.println("Clipboard Data:");
        
        if (clipboardBlocks == null || clipboardBlocks.isEmpty()) {
            System.out.println("Clipboard is EMPTY");
        } else {
            System.out.println("Block count: " + clipboardBlockCount);
            System.out.println("Copy UUID: " + (clipboardCopyUUID != null ? clipboardCopyUUID.toString().substring(0, 8) + "..." : "null"));
            System.out.println("First 5 blocks:");
            int count = 0;
            for (StatePos statePos : clipboardBlocks) {
                if (count >= 5) break;
                System.out.println("  - " + statePos.state.getBlock().getName().getString() + " at " + statePos.pos);
                count++;
            }
            if (clipboardBlocks.size() > 5) {
                System.out.println("  ... and " + (clipboardBlocks.size() - 5) + " more blocks");
            }
        }
        
        System.out.println("==============================================");
    }
    
    private void onClearClipboard() {
        clipboardBlocks = null;
        clipboardCopyUUID = null;
        clipboardBlockCount = 0;
        
        System.out.println("==============================================");
        System.out.println("Clipboard cleared!");
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
        
        int yOffset = 40;
        
        String statusText = "Copy Data Status: " + (hasCopyData ? "YES" : "NO");
        guiGraphics.drawString(
            this.font,
            statusText,
            leftPos + 20,
            topPos + yOffset,
            hasCopyData ? 0x00FF00 : 0xFF0000,
            false
        );
        yOffset += 20;
        
        if (hasCopyData) {
            String countText = "Blocks: " + blockCount;
            guiGraphics.drawString(
                this.font,
                countText,
                leftPos + 20,
                topPos + yOffset,
                0xFFFFFF,
                false
            );
            yOffset += 15;
            
            if (gadgetUUID != null) {
                String gadgetText = "Gadget: " + formatUUID(gadgetUUID);
                guiGraphics.drawString(
                    this.font,
                    gadgetText,
                    leftPos + 20,
                    topPos + yOffset,
                    0xAAAAAA,
                    false
                );
                yOffset += 12;
            }
            
            if (copyUUID != null) {
                String copyText = "Copy: " + formatUUID(copyUUID);
                guiGraphics.drawString(
                    this.font,
                    copyText,
                    leftPos + 20,
                    topPos + yOffset,
                    0xAAAAAA,
                    false
                );
            }
        }
    }
    
    private String formatUUID(UUID uuid) {
        if (uuid == null) return "null";
        return uuid.toString().substring(0, 8) + "...";
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

