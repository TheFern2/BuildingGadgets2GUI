package dev.thefern.buildinggadgets2gui.client.schematics;

import dev.thefern.buildinggadgets2gui.client.RowActionButtons;
import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.tabs.TrashTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TrashList extends ObjectSelectionList<TrashList.TrashEntry> {
    
    private final TrashTab parent;
    
    private Component pendingTooltip = null;
    private int tooltipX = 0;
    private int tooltipY = 0;
    
    public TrashList(Minecraft minecraft, int width, int height, int y, int itemHeight, TrashTab parent) {
        super(minecraft, width, height, y, itemHeight);
        this.parent = parent;
        refreshList();
    }
    
    public void setPendingTooltip(Component tooltip, int x, int y) {
        this.pendingTooltip = tooltip;
        this.tooltipX = x;
        this.tooltipY = y;
    }
    
    public void clearPendingTooltip() {
        this.pendingTooltip = null;
    }
    
    public void renderPendingTooltip(GuiGraphics graphics) {
        if (pendingTooltip != null) {
            graphics.renderTooltip(Minecraft.getInstance().font, pendingTooltip, tooltipX, tooltipY);
            pendingTooltip = null;
        }
    }
    
    public void refreshList() {
        this.clearEntries();
        
        List<TrashManager.TrashEntry> trashEntries = TrashManager.getTrashEntries();
        for (TrashManager.TrashEntry entry : trashEntries) {
            this.addEntry(new TrashEntry(entry));
        }
    }
    
    public void setSelected(TrashEntry entry) {
        super.setSelected(entry);
        if (entry != null) {
            parent.onEntrySelected(entry.getTrashEntry());
        }
    }
    
    @Override
    public int getRowWidth() {
        return this.width - 20;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }
    
    public class TrashEntry extends ObjectSelectionList.Entry<TrashEntry> {
        private final TrashManager.TrashEntry trashEntry;
        private int lastRenderedTop;
        private int lastRenderedLeft;
        private int lastRenderedWidth;
        
        public TrashEntry(TrashManager.TrashEntry trashEntry) {
            this.trashEntry = trashEntry;
        }
        
        public TrashManager.TrashEntry getTrashEntry() {
            return trashEntry;
        }
        
        @Override
        public Component getNarration() {
            return Component.literal("Trash: " + trashEntry.fileName);
        }
        
        private List<RowActionButtons.ButtonBounds> getActionBounds(int left, int top, int width) {
            int buttonY = top + 6;
            int rightX = left + width - 8;
            return RowActionButtons.calculateButtonBounds(
                rightX, 
                buttonY, 
                RowActionButtons.BUTTON_SIZE_SMALL,
                RowActionButtons.ButtonType.RESTORE,
                RowActionButtons.ButtonType.DELETE_PERM
            );
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                          int mouseX, int mouseY, boolean hovered, float partialTick) {
            
            this.lastRenderedTop = top;
            this.lastRenderedLeft = left;
            this.lastRenderedWidth = width;
            
            List<RowActionButtons.ButtonBounds> actionButtons = getActionBounds(left, top, width);
            RowActionButtons.renderButtons(graphics, actionButtons, mouseX, mouseY);
            
            for (RowActionButtons.ButtonBounds bounds : actionButtons) {
                if (bounds.contains(mouseX, mouseY)) {
                    setPendingTooltip(Component.literal(bounds.type.tooltip), mouseX, mouseY);
                    break;
                }
            }
            
            String fileIcon = "📄";
            String fileName = trashEntry.fileName;
            if (fileName.endsWith(".bg2schem")) {
                fileName = fileName.substring(0, fileName.length() - 9);
            }
            
            graphics.drawString(
                Minecraft.getInstance().font,
                fileIcon + " " + fileName,
                left + 6,
                top + 2,
                hovered ? 0xFFFFFF : 0xCCCCCC,
                false
            );
            
            String originalPath = trashEntry.originalPath;
            String fromText = "From: " + (originalPath == null || originalPath.isEmpty() ? "root" : originalPath);
            String timeText = " - " + TrashManager.getRelativeTimeSince(trashEntry.deletedAt);
            
            graphics.drawString(
                Minecraft.getInstance().font,
                fromText + timeText,
                left + 6,
                top + 12,
                0x888888,
                false
            );
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                List<RowActionButtons.ButtonBounds> actionButtons = getActionBounds(lastRenderedLeft, lastRenderedTop, lastRenderedWidth);
                RowActionButtons.ButtonType clickedType = RowActionButtons.getClickedButton(actionButtons, mouseX, mouseY);
                if (clickedType != null) {
                    handleActionButtonClick(clickedType);
                    return true;
                }
                
                setSelected(this);
                return true;
            }
            return false;
        }
        
        private void handleActionButtonClick(RowActionButtons.ButtonType buttonType) {
            switch (buttonType) {
                case RESTORE:
                    onRestoreButtonClicked();
                    break;
                case DELETE_PERM:
                    onDeleteButtonClicked();
                    break;
                default:
                    break;
            }
        }
        
        private void onRestoreButtonClicked() {
            String rawFileName = trashEntry.fileName;
            final String displayName = rawFileName.endsWith(".bg2schem") 
                ? rawFileName.substring(0, rawFileName.length() - 9) 
                : rawFileName;
            
            String destination = trashEntry.originalPath;
            if (destination == null || destination.isEmpty()) {
                destination = "schematics root";
            }
            
            ConfirmationDialog dialog = new ConfirmationDialog(
                parent.getParentScreen(),
                "Restore Schematic",
                "Restore '" + displayName + "' to " + destination + "?",
                confirmed -> {
                    if (confirmed) {
                        boolean success = TrashManager.restoreFile(trashEntry);
                        if (success) {
                            System.out.println("Restored: " + displayName);
                            refreshList();
                            parent.onTrashChanged();
                        } else {
                            System.err.println("Failed to restore: " + displayName);
                        }
                    }
                }
            );
            Minecraft.getInstance().setScreen(dialog);
        }
        
        private void onDeleteButtonClicked() {
            String rawFileName = trashEntry.fileName;
            final String displayName = rawFileName.endsWith(".bg2schem") 
                ? rawFileName.substring(0, rawFileName.length() - 9) 
                : rawFileName;
            
            ConfirmationDialog dialog = new ConfirmationDialog(
                parent.getParentScreen(),
                "Delete Permanently",
                "Permanently delete '" + displayName + "'? This cannot be undone.",
                confirmed -> {
                    if (confirmed) {
                        boolean success = TrashManager.permanentlyDelete(trashEntry);
                        if (success) {
                            System.out.println("Permanently deleted: " + displayName);
                            refreshList();
                            parent.onTrashChanged();
                        } else {
                            System.err.println("Failed to permanently delete: " + displayName);
                        }
                    }
                }
            );
            Minecraft.getInstance().setScreen(dialog);
        }
    }
}
