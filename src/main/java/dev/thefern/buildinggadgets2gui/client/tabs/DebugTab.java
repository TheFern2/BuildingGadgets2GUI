package dev.thefern.buildinggadgets2gui.client.tabs;

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

public class DebugTab extends TabPanel {
    
    private boolean hasCopyData = false;
    private int blockCount = 0;
    private UUID gadgetUUID = null;
    private UUID copyUUID = null;
    private ArrayList<StatePos> copiedBlocks = null;
    
    private static ArrayList<StatePos> clipboardBlocks = null;
    private static UUID clipboardCopyUUID = null;
    private static int clipboardBlockCount = 0;
    
    public DebugTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        checkCopyData();
        
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
            button -> checkCopyData()
        )
        .bounds(x + 20, buttonY, 360, 20)
        .build());
        buttonY += 24;
        
        widgets.add(Button.builder(
            Component.literal("Copy from Tool"),
            button -> onCopyFromTool()
        )
        .bounds(x + 20, buttonY, 175, 20)
        .build());
        
        widgets.add(Button.builder(
            Component.literal("Send to Tool"),
            button -> onSendToTool()
        )
        .bounds(x + 205, buttonY, 175, 20)
        .build());
        buttonY += 24;
        
        widgets.add(Button.builder(
            Component.literal("Print Clipboard Data"),
            button -> onPrintClipboard()
        )
        .bounds(x + 20, buttonY, 175, 20)
        .build());
        
        widgets.add(Button.builder(
            Component.literal("Clear Clipboard"),
            button -> onClearClipboard()
        )
        .bounds(x + 205, buttonY, 175, 20)
        .build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        int yOffset = y + 15;
        
        String statusText = "Copy Data Status: " + (hasCopyData ? "YES" : "NO");
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            statusText,
            x + 20,
            yOffset,
            hasCopyData ? 0x00FF00 : 0xFF0000,
            false
        );
        yOffset += 20;
        
        if (hasCopyData) {
            String countText = "Blocks: " + blockCount;
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                countText,
                x + 20,
                yOffset,
                0xFFFFFF,
                false
            );
            yOffset += 15;
            
            if (gadgetUUID != null) {
                String gadgetText = "Gadget: " + formatUUID(gadgetUUID);
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
            
            if (copyUUID != null) {
                String copyText = "Copy: " + formatUUID(copyUUID);
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
        
        System.out.println("==============================================");
        System.out.println("[CLIENT] Sending clipboard data to server...");
        System.out.println("Target Gadget UUID: " + targetGadgetUUID.toString().substring(0, 8) + "...");
        System.out.println("New Copy UUID: " + newCopyUUID.toString().substring(0, 8) + "...");
        System.out.println("Blocks to send: " + clipboardBlocks.size());
        
        CompoundTag tag = BG2Data.statePosListToNBTMapArray(clipboardBlocks);
        System.out.println("NBT Tag size: " + tag.size());
        
        PacketDistributor.sendToServer(new SendClipboardToGadgetPayload(targetGadgetUUID, newCopyUUID, tag));
        
        System.out.println("Packet sent to server!");
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
    
    private String formatUUID(UUID uuid) {
        if (uuid == null) return "null";
        return uuid.toString().substring(0, 8) + "...";
    }
    
    @Override
    public void tick() {
        checkCopyData();
    }
}
