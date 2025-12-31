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

public class SchematicsTab extends TabPanel {
    
    private static final int TOGGLE_BUTTON_WIDTH = 60;
    private static final int TOGGLE_BUTTON_HEIGHT = 20;
    private static final int TOGGLE_BUTTON_SPACING = 5;
    private static final int TOP_MARGIN = 10;
    
    private Button allButton;
    private Button deletedButton;
    private boolean showingTrash = false;
    
    private boolean hasCopyData = false;
    private int blockCount = 0;
    private UUID gadgetUUID = null;
    private UUID copyUUID = null;
    private ArrayList<StatePos> copiedBlocks = null;
    
    public SchematicsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        checkCopyData();
        
        int toggleY = y + TOP_MARGIN;
        int toggleX = x + 10;
        
        allButton = Button.builder(
            Component.literal("All"),
            button -> setShowingTrash(false)
        )
        .bounds(toggleX, toggleY, TOGGLE_BUTTON_WIDTH, TOGGLE_BUTTON_HEIGHT)
        .build();
        
        deletedButton = Button.builder(
            Component.literal("Deleted"),
            button -> setShowingTrash(true)
        )
        .bounds(toggleX + TOGGLE_BUTTON_WIDTH + TOGGLE_BUTTON_SPACING, toggleY, TOGGLE_BUTTON_WIDTH, TOGGLE_BUTTON_HEIGHT)
        .build();
        
        widgets.add(allButton);
        widgets.add(deletedButton);
        
        int buttonY = y + 90;
        
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
            Component.literal("Print Clipboard"),
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
    
    private void setShowingTrash(boolean showTrash) {
        this.showingTrash = showTrash;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        renderToggleButtonBackgrounds(guiGraphics);
        
        if (!showingTrash) {
            int yOffset = y + 50;
            
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
            }
        } else {
            int contentY = y + TOP_MARGIN + TOGGLE_BUTTON_HEIGHT + 20;
            int centerX = x + width / 2;
            int centerY = contentY + (height - contentY + y) / 2;
            
            String text = "Deleted Schematics";
            int textWidth = Minecraft.getInstance().font.width(text);
            
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                text,
                centerX - textWidth / 2,
                centerY - 10,
                0xFFFFFF,
                false
            );
            
            String subText = "(Coming Soon)";
            int subTextWidth = Minecraft.getInstance().font.width(subText);
            
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                subText,
                centerX - subTextWidth / 2,
                centerY + 10,
                0xAAAAAA,
                false
            );
        }
    }
    
    private void renderToggleButtonBackgrounds(GuiGraphics guiGraphics) {
        int activeColor = 0xFF4A4A4A;
        int inactiveColor = 0xFF2A2A2A;
        
        int toggleY = y + TOP_MARGIN;
        int toggleX = x + 10;
        
        guiGraphics.fill(
            toggleX, 
            toggleY, 
            toggleX + TOGGLE_BUTTON_WIDTH, 
            toggleY + TOGGLE_BUTTON_HEIGHT,
            showingTrash ? inactiveColor : activeColor
        );
        
        guiGraphics.fill(
            toggleX + TOGGLE_BUTTON_WIDTH + TOGGLE_BUTTON_SPACING, 
            toggleY, 
            toggleX + TOGGLE_BUTTON_WIDTH * 2 + TOGGLE_BUTTON_SPACING, 
            toggleY + TOGGLE_BUTTON_HEIGHT,
            showingTrash ? activeColor : inactiveColor
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
    
    private void onCopyFromTool() {
        if (!hasCopyData || copiedBlocks == null || copiedBlocks.isEmpty()) {
            System.out.println("No copy data available on tool to copy!");
            return;
        }
        
        ArrayList<StatePos> clipboardBlocks = new ArrayList<>();
        for (StatePos statePos : copiedBlocks) {
            clipboardBlocks.add(new StatePos(statePos.state, statePos.pos.immutable()));
        }
        UUID clipboardCopyUUID = copyUUID;
        int clipboardBlockCount = clipboardBlocks.size();
        
        HistoryTab.setClipboard(clipboardBlocks, clipboardCopyUUID, clipboardBlockCount);
        HistoryTab.addToHistory(clipboardBlocks, clipboardCopyUUID, clipboardBlockCount);
        
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
        
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
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
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        UUID clipboardCopyUUID = HistoryTab.getClipboardCopyUUID();
        int clipboardBlockCount = HistoryTab.getClipboardBlockCount();
        
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
        HistoryTab.setClipboard(null, null, 0);
        
        System.out.println("==============================================");
        System.out.println("Clipboard cleared!");
        System.out.println("==============================================");
    }
    
    @Override
    public void tick() {
        checkCopyData();
    }
}

