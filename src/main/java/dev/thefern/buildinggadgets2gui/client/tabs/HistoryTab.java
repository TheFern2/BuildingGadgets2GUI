package dev.thefern.buildinggadgets2gui.client.tabs;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import dev.thefern.buildinggadgets2gui.Config;
import dev.thefern.buildinggadgets2gui.client.HistoryManager;
import dev.thefern.buildinggadgets2gui.network.SendClipboardToGadgetPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HistoryTab extends TabPanel {
    
    private static final int HISTORY_ROW_HEIGHT = 22;
    private static final int HISTORY_START_Y = 30;
    private static final int BOTTOM_PADDING = 10;
    private static final int MAX_HISTORY_BUTTONS = 20;
    
    private static ArrayList<StatePos> clipboardBlocks = null;
    private static UUID clipboardCopyUUID = null;
    private static int clipboardBlockCount = 0;
    
    private static List<HistoryEntry> copyHistory = new ArrayList<>();
    private List<Button> historyButtons = new ArrayList<>();
    private List<Button> deleteButtons = new ArrayList<>();
    private Button clearAllButton;
    
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int maxVisibleEntries = 10;
    
    public static class HistoryEntry {
        public ArrayList<StatePos> blocks;
        public UUID copyUUID;
        public int blockCount;
        public String timestamp;
        
        public HistoryEntry(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount) {
            this.blocks = blocks;
            this.copyUUID = copyUUID;
            this.blockCount = blockCount;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            this.timestamp = sdf.format(new Date());
        }
        
        public HistoryEntry(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount, String timestamp) {
            this.blocks = blocks;
            this.copyUUID = copyUUID;
            this.blockCount = blockCount;
            this.timestamp = timestamp;
        }
    }
    
    public HistoryTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        createClearAllButton();
        createHistoryButtons();
    }
    
    private void createClearAllButton() {
        clearAllButton = Button.builder(
            Component.literal("Clear All History"),
            button -> clearAllHistory()
        )
        .bounds(x + width - 130, y + 10, 110, 18)
        .build();
        
        clearAllButton.visible = false;
        widgets.add(clearAllButton);
    }
    
    private void createHistoryButtons() {
        historyButtons.clear();
        deleteButtons.clear();
        
        calculateMaxVisibleEntries();
        
        int historyY = y + HISTORY_START_Y;
        
        for (int i = 0; i < MAX_HISTORY_BUTTONS; i++) {
            final int index = i;
            
            Button deleteButton = Button.builder(
                Component.literal("X"),
                button -> deleteHistoryEntry(scrollOffset + index)
            )
            .bounds(x + 220, historyY + (i * HISTORY_ROW_HEIGHT), 20, 18)
            .build();
            
            deleteButton.visible = false;
            deleteButtons.add(deleteButton);
            widgets.add(deleteButton);
            
            Button sendButton = Button.builder(
                Component.literal("Send to Clipboard"),
                button -> sendHistoryToClipboard(scrollOffset + index)
            )
            .bounds(x + 245, historyY + (i * HISTORY_ROW_HEIGHT), 120, 18)
            .build();
            
            sendButton.visible = false;
            historyButtons.add(sendButton);
            widgets.add(sendButton);
        }
    }
    
    private void calculateMaxVisibleEntries() {
        int availableHeight = height - HISTORY_START_Y - BOTTOM_PADDING;
        maxVisibleEntries = Math.max(1, Math.min(MAX_HISTORY_BUTTONS, availableHeight / HISTORY_ROW_HEIGHT));
    }
    
    private void updateButtonVisibility() {
        calculateMaxVisibleEntries();
        updateScrollLimits();
        
        clearAllButton.visible = isActive && !copyHistory.isEmpty();
        clearAllButton.active = isActive && !copyHistory.isEmpty();
        
        int entriesToShow = Math.min(copyHistory.size() - scrollOffset, maxVisibleEntries);
        
        for (int i = 0; i < MAX_HISTORY_BUTTONS; i++) {
            if (i < entriesToShow && isActive) {
                deleteButtons.get(i).visible = true;
                deleteButtons.get(i).active = true;
                historyButtons.get(i).visible = true;
                historyButtons.get(i).active = true;
            } else {
                deleteButtons.get(i).visible = false;
                deleteButtons.get(i).active = false;
                historyButtons.get(i).visible = false;
                historyButtons.get(i).active = false;
            }
        }
    }
    
    private void updateScrollLimits() {
        maxScrollOffset = Math.max(0, copyHistory.size() - maxVisibleEntries);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        scrollOffset = 0;
        updateButtonVisibility();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        renderHistory(guiGraphics);
    }
    
    private void renderHistory(GuiGraphics guiGraphics) {
        int historyY = y + HISTORY_START_Y;
        
        String headerText = "History (" + copyHistory.size() + " entries):";
        if (copyHistory.size() > maxVisibleEntries) {
            headerText += " [Scroll: " + (scrollOffset + 1) + "-" + 
                         Math.min(scrollOffset + maxVisibleEntries, copyHistory.size()) + "]";
        }
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            headerText,
            x + 20,
            historyY - 15,
            0xFFFFFF,
            false
        );
        
        if (copyHistory.isEmpty()) {
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                "No history yet. Use 'Copy from Tool' in Schematics tab.",
                x + 20,
                historyY + 10,
                0xAAAAAA,
                false
            );
            return;
        }
        
        int entriesToShow = Math.min(copyHistory.size() - scrollOffset, maxVisibleEntries);
        
        for (int i = 0; i < entriesToShow; i++) {
            HistoryEntry entry = copyHistory.get(scrollOffset + i);
            int rowY = historyY + (i * HISTORY_ROW_HEIGHT);
            int entryNumber = scrollOffset + i + 1;
            
            String numberText = String.format("#%d", entryNumber);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                numberText,
                x + 10,
                rowY + 5,
                0xFFFFFF,
                false
            );
            
            String entryText = String.format("[%s] %d blocks", entry.timestamp, entry.blockCount);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                entryText,
                x + 45,
                rowY + 5,
                0xAAAAAA,
                false
            );
        }
    }
    
    private void deleteHistoryEntry(int index) {
        if (index < 0 || index >= copyHistory.size()) {
            System.out.println("Invalid history index: " + index);
            return;
        }
        
        HistoryEntry entry = copyHistory.remove(index);
        System.out.println("Deleted history entry: [" + entry.timestamp + "] " + entry.blockCount + " blocks");
        
        HistoryManager.saveHistory(copyHistory);
        updateButtonVisibility();
    }
    
    private void clearAllHistory() {
        int count = copyHistory.size();
        copyHistory.clear();
        scrollOffset = 0;
        
        System.out.println("Cleared all history (" + count + " entries)");
        
        HistoryManager.saveHistory(copyHistory);
        updateButtonVisibility();
    }
    
    private void sendHistoryToClipboard(int index) {
        if (index < 0 || index >= copyHistory.size()) {
            System.out.println("Invalid history index: " + index);
            return;
        }
        
        HistoryEntry entry = copyHistory.get(index);
        
        clipboardBlocks = new ArrayList<>();
        for (StatePos statePos : entry.blocks) {
            clipboardBlocks.add(new StatePos(statePos.state, statePos.pos.immutable()));
        }
        clipboardCopyUUID = entry.copyUUID;
        clipboardBlockCount = entry.blockCount;
        
        System.out.println("==============================================");
        System.out.println("Sent history entry #" + (index + 1) + " to clipboard");
        System.out.println("Timestamp: " + entry.timestamp);
        System.out.println("Blocks: " + clipboardBlockCount);
        System.out.println("Copy UUID: " + (clipboardCopyUUID != null ? clipboardCopyUUID.toString().substring(0, 8) + "..." : "null"));
        System.out.println("Use 'Send to Tool' in Schematics tab to apply to gadget");
        System.out.println("==============================================");
    }
    
    public static void addToHistory(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount) {
        HistoryEntry newEntry = new HistoryEntry(
            new ArrayList<>(blocks),
            copyUUID,
            blockCount
        );
        copyHistory.add(0, newEntry);
        
        trimHistoryToLimit();
        
        System.out.println("Added to history (total entries: " + copyHistory.size() + ")");
        
        HistoryManager.saveHistory(copyHistory);
    }
    
    public static void trimHistoryToLimit() {
        int maxEntries = Config.MAX_HISTORY_ENTRIES.get();
        boolean removed = false;
        while (copyHistory.size() > maxEntries) {
            HistoryEntry removedEntry = copyHistory.remove(copyHistory.size() - 1);
            System.out.println("Removed oldest history entry: [" + removedEntry.timestamp + "] " + removedEntry.blockCount + " blocks (FIFO limit: " + maxEntries + ")");
            removed = true;
        }
        if (removed) {
            HistoryManager.saveHistory(copyHistory);
        }
    }
    
    public static void loadHistory() {
        List<HistoryEntry> loadedHistory = HistoryManager.loadHistory();
        copyHistory.clear();
        copyHistory.addAll(loadedHistory);
        trimHistoryToLimit();
        System.out.println("History loaded: " + copyHistory.size() + " entries");
    }
    
    public static void setClipboard(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount) {
        clipboardBlocks = blocks;
        clipboardCopyUUID = copyUUID;
        clipboardBlockCount = blockCount;
    }
    
    public static ArrayList<StatePos> getClipboardBlocks() {
        return clipboardBlocks;
    }
    
    public static UUID getClipboardCopyUUID() {
        return clipboardCopyUUID;
    }
    
    public static int getClipboardBlockCount() {
        return clipboardBlockCount;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isActive) return false;
        
        if (copyHistory.size() <= maxVisibleEntries) {
            return false;
        }
        
        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScrollOffset, scrollOffset + 1);
        }
        
        updateButtonVisibility();
        return true;
    }
}

