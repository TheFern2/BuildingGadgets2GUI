package dev.thefern.buildinggadgets2gui.client.tabs;

import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import dev.thefern.buildinggadgets2gui.Config;
import dev.thefern.buildinggadgets2gui.client.HistoryManager;
import dev.thefern.buildinggadgets2gui.client.RowActionButtons;
import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.MaterialListDialog;
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
    private static ArrayList<TagPos> clipboardTEData = null;
    private static UUID clipboardCopyUUID = null;
    private static int clipboardBlockCount = 0;
    
    private static List<HistoryEntry> copyHistory = new ArrayList<>();
    private Button clearAllButton;
    
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int maxVisibleEntries = 10;
    
    public static class HistoryEntry {
        public ArrayList<StatePos> blocks;
        public ArrayList<TagPos> teData;
        public UUID copyUUID;
        public int blockCount;
        public String timestamp;
        
        public HistoryEntry(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount) {
            this.blocks = blocks;
            this.teData = null;
            this.copyUUID = copyUUID;
            this.blockCount = blockCount;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            this.timestamp = sdf.format(new Date());
        }
        
        public HistoryEntry(ArrayList<StatePos> blocks, ArrayList<TagPos> teData, UUID copyUUID, int blockCount) {
            this.blocks = blocks;
            this.teData = teData;
            this.copyUUID = copyUUID;
            this.blockCount = blockCount;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            this.timestamp = sdf.format(new Date());
        }
        
        public HistoryEntry(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount, String timestamp) {
            this.blocks = blocks;
            this.teData = null;
            this.copyUUID = copyUUID;
            this.blockCount = blockCount;
            this.timestamp = timestamp;
        }
        
        public HistoryEntry(ArrayList<StatePos> blocks, ArrayList<TagPos> teData, UUID copyUUID, int blockCount, String timestamp) {
            this.blocks = blocks;
            this.teData = teData;
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
        calculateMaxVisibleEntries();
    }
    
    private int[] getDeleteButtonBounds(int rowIndex) {
        int historyY = y + HISTORY_START_Y;
        int buttonSize = 14;
        int buttonX = x + 35;
        int buttonY = historyY + (rowIndex * HISTORY_ROW_HEIGHT) + 3;
        return new int[]{buttonX, buttonY, buttonSize};
    }
    
    private static final int ACTION_BUTTONS_RIGHT_X = 250;
    
    private List<RowActionButtons.ButtonBounds> getRowActionBounds(int rowIndex) {
        int historyY = y + HISTORY_START_Y;
        int buttonY = historyY + (rowIndex * HISTORY_ROW_HEIGHT) + 3;
        return RowActionButtons.calculateButtonBounds(
            x + ACTION_BUTTONS_RIGHT_X, 
            buttonY, 
            RowActionButtons.ButtonType.MATERIAL,
            RowActionButtons.ButtonType.CLIPBOARD,
            RowActionButtons.ButtonType.TOOL
        );
    }
    
    public static String formatBlockCount(int count) {
        if (count >= 1000) {
            double k = count / 1000.0;
            if (k == Math.floor(k)) {
                return String.format("%.0fK", k);
            } else {
                return String.format("%.1fK", k);
            }
        }
        return String.valueOf(count);
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
        
        renderHistory(guiGraphics, mouseX, mouseY);
    }
    
    private void renderHistory(GuiGraphics guiGraphics) {
        renderHistory(guiGraphics, 0, 0);
    }
    
    private void renderHistory(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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
            
            int[] deleteBounds = getDeleteButtonBounds(i);
            boolean isHoveringDelete = mouseX >= deleteBounds[0] && mouseX <= deleteBounds[0] + deleteBounds[2] &&
                                       mouseY >= deleteBounds[1] && mouseY <= deleteBounds[1] + deleteBounds[2];
            
            guiGraphics.fill(
                deleteBounds[0], 
                deleteBounds[1], 
                deleteBounds[0] + deleteBounds[2], 
                deleteBounds[1] + deleteBounds[2], 
                isHoveringDelete ? 0xFFFF4444 : 0xFF883333
            );
            
            String xText = "X";
            int xTextWidth = Minecraft.getInstance().font.width(xText);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                xText,
                deleteBounds[0] + (deleteBounds[2] - xTextWidth) / 2,
                deleteBounds[1] + 3,
                0xFFFFFF,
                false
            );
            
            String entryText = String.format("[%s] %s blocks", entry.timestamp, formatBlockCount(entry.blockCount));
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                entryText,
                x + 55,
                rowY + 5,
                0xAAAAAA,
                false
            );
            
            List<RowActionButtons.ButtonBounds> actionButtons = getRowActionBounds(i);
            RowActionButtons.renderButtons(guiGraphics, actionButtons, mouseX, mouseY);
        }
        
        renderActionButtonTooltips(guiGraphics, mouseX, mouseY);
    }
    
    private void renderActionButtonTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int entriesToShow = Math.min(copyHistory.size() - scrollOffset, maxVisibleEntries);
        
        for (int i = 0; i < entriesToShow; i++) {
            List<RowActionButtons.ButtonBounds> actionButtons = getRowActionBounds(i);
            RowActionButtons.renderTooltip(guiGraphics, actionButtons, mouseX, mouseY);
        }
    }
    
    private void deleteHistoryEntry(int index) {
        if (index < 0 || index >= copyHistory.size()) {
            System.out.println("Invalid history index: " + index);
            return;
        }
        
        HistoryEntry entry = copyHistory.get(index);
        String entryInfo = "#" + (index + 1) + " [" + entry.timestamp + "] " + entry.blockCount + " blocks";
        
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Delete History Entry",
            "Delete " + entryInfo + "?",
            confirmed -> {
                if (confirmed) {
                    HistoryEntry removedEntry = copyHistory.remove(index);
                    System.out.println("Deleted history entry: [" + removedEntry.timestamp + "] " + removedEntry.blockCount + " blocks");
                    HistoryManager.saveHistory(copyHistory);
                    updateButtonVisibility();
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void clearAllHistory() {
        int count = copyHistory.size();
        if (count == 0) return;
        
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Clear All History",
            "Delete all " + count + " history entries?",
            confirmed -> {
                if (confirmed) {
                    copyHistory.clear();
                    scrollOffset = 0;
                    System.out.println("Cleared all history (" + count + " entries)");
                    HistoryManager.saveHistory(copyHistory);
                    updateButtonVisibility();
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
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
        
        if (entry.teData != null && !entry.teData.isEmpty()) {
            clipboardTEData = new ArrayList<>(entry.teData);
        } else {
            clipboardTEData = null;
        }
        
        clipboardCopyUUID = entry.copyUUID;
        clipboardBlockCount = entry.blockCount;
        
        System.out.println("==============================================");
        System.out.println("Sent history entry #" + (index + 1) + " to clipboard");
        System.out.println("Timestamp: " + entry.timestamp);
        System.out.println("Blocks: " + clipboardBlockCount);
        System.out.println("TileEntities: " + (clipboardTEData != null ? clipboardTEData.size() : 0));
        System.out.println("Copy UUID: " + (clipboardCopyUUID != null ? clipboardCopyUUID.toString().substring(0, 8) + "..." : "null"));
        System.out.println("Use 'Send to Tool' in Schematics tab to apply to gadget");
        System.out.println("==============================================");
    }
    
    private void showMaterialList(int index) {
        if (index < 0 || index >= copyHistory.size()) {
            return;
        }
        
        HistoryEntry entry = copyHistory.get(index);
        if (entry.blocks != null && !entry.blocks.isEmpty()) {
            MaterialListDialog dialog = new MaterialListDialog(
                Minecraft.getInstance().screen,
                "Materials: History #" + (index + 1),
                entry.blocks
            );
            Minecraft.getInstance().setScreen(dialog);
        }
    }
    
    private void handleActionButtonClick(int index, RowActionButtons.ButtonType buttonType) {
        switch (buttonType) {
            case MATERIAL:
                showMaterialList(index);
                break;
            case CLIPBOARD:
                sendHistoryToClipboard(index);
                break;
            case TOOL:
                sendHistoryToTool(index);
                break;
        }
    }
    
    private void sendHistoryToTool(int index) {
        if (index < 0 || index >= copyHistory.size()) {
            System.out.println("Invalid history index: " + index);
            return;
        }
        
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Send to Tool",
            "This will override current tool copy data. Continue?",
            confirmed -> {
                if (confirmed) {
                    performSendHistoryToTool(index);
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void performSendHistoryToTool(int index) {
        HistoryEntry entry = copyHistory.get(index);
        
        clipboardBlocks = new ArrayList<>();
        for (StatePos statePos : entry.blocks) {
            clipboardBlocks.add(new StatePos(statePos.state, statePos.pos.immutable()));
        }
        
        if (entry.teData != null && !entry.teData.isEmpty()) {
            clipboardTEData = new ArrayList<>(entry.teData);
        } else {
            clipboardTEData = null;
        }
        
        clipboardCopyUUID = entry.copyUUID;
        clipboardBlockCount = entry.blockCount;
        
        dev.thefern.buildinggadgets2gui.client.ClipboardUtils.sendToTool();
        
        System.out.println("==============================================");
        System.out.println("Sent history entry #" + (index + 1) + " directly to tool");
        System.out.println("Timestamp: " + entry.timestamp);
        System.out.println("Blocks: " + clipboardBlockCount);
        System.out.println("TileEntities: " + (clipboardTEData != null ? clipboardTEData.size() : 0));
        System.out.println("==============================================");
    }
    
    public static void addToHistory(ArrayList<StatePos> blocks, UUID copyUUID, int blockCount) {
        addToHistory(blocks, null, copyUUID, blockCount);
    }
    
    public static void addToHistory(ArrayList<StatePos> blocks, ArrayList<TagPos> teData, UUID copyUUID, int blockCount) {
        ArrayList<TagPos> teDataCopy = null;
        if (teData != null && !teData.isEmpty()) {
            teDataCopy = new ArrayList<>(teData);
        }
        
        HistoryEntry newEntry = new HistoryEntry(
            new ArrayList<>(blocks),
            teDataCopy,
            copyUUID,
            blockCount
        );
        copyHistory.add(0, newEntry);
        
        trimHistoryToLimit();
        
        System.out.println("Added to history (total entries: " + copyHistory.size() + ", TileEntities: " + (teDataCopy != null ? teDataCopy.size() : 0) + ")");
        
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
        setClipboard(blocks, null, copyUUID, blockCount);
    }
    
    public static void setClipboard(ArrayList<StatePos> blocks, ArrayList<TagPos> teData, UUID copyUUID, int blockCount) {
        clipboardBlocks = blocks;
        clipboardTEData = teData;
        clipboardCopyUUID = copyUUID;
        clipboardBlockCount = blockCount;
    }
    
    public static ArrayList<StatePos> getClipboardBlocks() {
        return clipboardBlocks;
    }
    
    public static ArrayList<TagPos> getClipboardTEData() {
        return clipboardTEData;
    }
    
    public static UUID getClipboardCopyUUID() {
        return clipboardCopyUUID;
    }
    
    public static int getClipboardBlockCount() {
        return clipboardBlockCount;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive || button != 0) return false;
        
        int entriesToShow = Math.min(copyHistory.size() - scrollOffset, maxVisibleEntries);
        
        for (int i = 0; i < entriesToShow; i++) {
            int[] deleteBounds = getDeleteButtonBounds(i);
            if (mouseX >= deleteBounds[0] && mouseX <= deleteBounds[0] + deleteBounds[2] &&
                mouseY >= deleteBounds[1] && mouseY <= deleteBounds[1] + deleteBounds[2]) {
                deleteHistoryEntry(scrollOffset + i);
                return true;
            }
            
            List<RowActionButtons.ButtonBounds> actionButtons = getRowActionBounds(i);
            RowActionButtons.ButtonType clickedType = RowActionButtons.getClickedButton(actionButtons, mouseX, mouseY);
            if (clickedType != null) {
                handleActionButtonClick(scrollOffset + i, clickedType);
                return true;
            }
        }
        
        return false;
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

