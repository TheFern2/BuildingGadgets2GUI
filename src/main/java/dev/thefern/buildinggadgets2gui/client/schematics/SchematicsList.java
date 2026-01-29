package dev.thefern.buildinggadgets2gui.client.schematics;

import dev.thefern.buildinggadgets2gui.client.ClipboardUtils;
import dev.thefern.buildinggadgets2gui.client.RowActionButtons;
import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.MaterialListDialog;
import dev.thefern.buildinggadgets2gui.client.tabs.HistoryTab;
import dev.thefern.buildinggadgets2gui.client.tabs.SchematicsTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SchematicsList extends ObjectSelectionList<SchematicsList.Entry> {
    
    private final SchematicsTab parent;
    private SchematicFolder currentFolder;
    
    private Component pendingTooltip = null;
    private int tooltipX = 0;
    private int tooltipY = 0;
    
    public SchematicsList(Minecraft minecraft, int width, int height, int y, int itemHeight, SchematicsTab parent) {
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
        currentFolder = SchematicManager.getCurrentFolder();
        
        List<Object> contents = currentFolder.getContents();
        for (Object obj : contents) {
            if (obj instanceof SchematicFolder) {
                this.addEntry(new FolderEntry((SchematicFolder) obj));
            } else if (obj instanceof SchematicFile) {
                this.addEntry(new FileEntry((SchematicFile) obj));
            }
        }
    }
    
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        if (entry != null) {
            entry.onSelected();
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
    
    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
        public abstract void onSelected();
        public abstract boolean isFolder();
    }
    
    public class FolderEntry extends Entry {
        private final SchematicFolder folder;
        private int lastRenderedTop;
        private int lastRenderedLeft;
        
        public FolderEntry(SchematicFolder folder) {
            this.folder = folder;
        }
        
        public SchematicFolder getFolder() {
            return folder;
        }
        
        @Override
        public void onSelected() {
            parent.onFolderSelected(folder);
        }
        
        @Override
        public boolean isFolder() {
            return true;
        }
        
        @Override
        public Component getNarration() {
            return Component.literal("Folder: " + folder.getName());
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, 
                          int mouseX, int mouseY, boolean hovered, float partialTick) {
            
            this.lastRenderedTop = top;
            this.lastRenderedLeft = left;
            
            int deleteButtonSize = 12;
            int deleteButtonX = left + 2;
            int deleteButtonY = top + 6;
            
            boolean isHoveringDelete = mouseX >= deleteButtonX && mouseX <= deleteButtonX + deleteButtonSize &&
                                       mouseY >= deleteButtonY && mouseY <= deleteButtonY + deleteButtonSize;
            
            graphics.fill(
                deleteButtonX, 
                deleteButtonY, 
                deleteButtonX + deleteButtonSize, 
                deleteButtonY + deleteButtonSize, 
                isHoveringDelete ? 0xFFFF4444 : 0xFF883333
            );
            
            String xText = "X";
            int textWidth = Minecraft.getInstance().font.width(xText);
            graphics.drawString(
                Minecraft.getInstance().font,
                xText,
                deleteButtonX + (deleteButtonSize - textWidth) / 2,
                deleteButtonY + 2,
                0xFFFFFF,
                false
            );
            
            String folderIcon = "📁";
            String folderName = folder.getName();
            
            graphics.drawString(
                Minecraft.getInstance().font,
                folderIcon + " " + folderName,
                left + 20,
                top + 2,
                hovered ? 0xFFFFFF : 0xCCCCCC,
                false
            );
            
            int fileCount = folder.getFileCount();
            int folderCount = folder.getFolderCount();
            String info = "(" + fileCount + " files, " + folderCount + " folders)";
            graphics.drawString(
                Minecraft.getInstance().font,
                info,
                left + 20,
                top + 12,
                0x888888,
                false
            );
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int deleteButtonSize = 12;
                int deleteButtonX = lastRenderedLeft + 2;
                int deleteButtonY = lastRenderedTop + 6;
                
                if (mouseX >= deleteButtonX && mouseX <= deleteButtonX + deleteButtonSize &&
                    mouseY >= deleteButtonY && mouseY <= deleteButtonY + deleteButtonSize) {
                    
                    onDeleteButtonClicked();
                    return true;
                }
                
                SchematicManager.navigateToFolder(folder);
                refreshList();
                parent.onNavigate();
                return true;
            }
            return false;
        }
        
        private void onDeleteButtonClicked() {
            String folderName = folder.getName();
            
            if (!folder.isEmpty()) {
                ConfirmationDialog dialog = new ConfirmationDialog(
                    parent.getParentScreen(),
                    "Cannot Delete Folder",
                    "Folder '" + folderName + "' is not empty. Please remove all contents first.",
                    confirmed -> {}
                );
                Minecraft.getInstance().setScreen(dialog);
                return;
            }
            
            ConfirmationDialog dialog = new ConfirmationDialog(
                parent.getParentScreen(),
                "Delete Folder",
                "Delete folder '" + folderName + "'?",
                confirmed -> {
                    if (confirmed) {
                        boolean success = SchematicManager.deleteFolder(folder);
                        if (success) {
                            System.out.println("Deleted folder: " + folderName);
                            refreshList();
                            parent.onNavigate();
                        } else {
                            System.err.println("Failed to delete folder: " + folderName);
                        }
                    }
                }
            );
            Minecraft.getInstance().setScreen(dialog);
        }
    }
    
    public class FileEntry extends Entry {
        private final SchematicFile file;
        private int lastRenderedTop;
        private int lastRenderedLeft;
        private int lastRenderedWidth;
        
        public FileEntry(SchematicFile file) {
            this.file = file;
        }
        
        public SchematicFile getFile() {
            return file;
        }
        
        @Override
        public void onSelected() {
            parent.onFileSelected(file);
        }
        
        @Override
        public boolean isFolder() {
            return false;
        }
        
        @Override
        public Component getNarration() {
            return Component.literal("File: " + file.getName());
        }
        
        private java.util.List<RowActionButtons.ButtonBounds> getActionBounds(int left, int top, int width) {
            int buttonY = top + 6;
            int rightX = left + width - 8;
            return RowActionButtons.calculateButtonBounds(
                rightX, 
                buttonY, 
                RowActionButtons.BUTTON_SIZE_SMALL,
                RowActionButtons.ButtonType.MATERIAL,
                RowActionButtons.ButtonType.TOOL
            );
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                          int mouseX, int mouseY, boolean hovered, float partialTick) {
            
            this.lastRenderedTop = top;
            this.lastRenderedLeft = left;
            this.lastRenderedWidth = width;
            
            int deleteButtonSize = 12;
            int deleteButtonX = left + 2;
            int deleteButtonY = top + 6;
            
            boolean isHoveringDelete = mouseX >= deleteButtonX && mouseX <= deleteButtonX + deleteButtonSize &&
                                       mouseY >= deleteButtonY && mouseY <= deleteButtonY + deleteButtonSize;
            
            graphics.fill(
                deleteButtonX, 
                deleteButtonY, 
                deleteButtonX + deleteButtonSize, 
                deleteButtonY + deleteButtonSize, 
                isHoveringDelete ? 0xFFFF4444 : 0xFF883333
            );
            
            String xText = "X";
            int textWidth = Minecraft.getInstance().font.width(xText);
            graphics.drawString(
                Minecraft.getInstance().font,
                xText,
                deleteButtonX + (deleteButtonSize - textWidth) / 2,
                deleteButtonY + 2,
                0xFFFFFF,
                false
            );
            
            java.util.List<RowActionButtons.ButtonBounds> actionButtons = getActionBounds(left, top, width);
            RowActionButtons.renderButtons(graphics, actionButtons, mouseX, mouseY);
            
            for (RowActionButtons.ButtonBounds bounds : actionButtons) {
                if (bounds.contains(mouseX, mouseY)) {
                    setPendingTooltip(Component.literal(bounds.type.tooltip), mouseX, mouseY);
                    break;
                }
            }
            
            String fileIcon = "📄";
            String fileName = file.getName();
            
            graphics.drawString(
                Minecraft.getInstance().font,
                fileIcon + " " + fileName,
                left + 20,
                top + 2,
                hovered ? 0xFFFFFF : 0xCCCCCC,
                false
            );
            
            SchematicFile.SchematicMetadata metadata = file.getMetadata();
            if (metadata != null) {
                String info = metadata.tags != null && !metadata.tags.isEmpty() 
                    ? String.join(", ", metadata.tags) 
                    : "No tags";
                graphics.drawString(
                    Minecraft.getInstance().font,
                    info,
                    left + 20,
                    top + 12,
                    0x888888,
                    false
                );
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int deleteButtonSize = 12;
                int deleteButtonX = lastRenderedLeft + 2;
                int deleteButtonY = lastRenderedTop + 6;
                
                if (mouseX >= deleteButtonX && mouseX <= deleteButtonX + deleteButtonSize &&
                    mouseY >= deleteButtonY && mouseY <= deleteButtonY + deleteButtonSize) {
                    
                    onDeleteButtonClicked();
                    return true;
                }
                
                java.util.List<RowActionButtons.ButtonBounds> actionButtons = getActionBounds(lastRenderedLeft, lastRenderedTop, lastRenderedWidth);
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
                case MATERIAL:
                    onMaterialButtonClicked();
                    break;
                case TOOL:
                    onToolButtonClicked();
                    break;
                default:
                    break;
            }
        }
        
        private void onToolButtonClicked() {
            ConfirmationDialog dialog = new ConfirmationDialog(
                parent.getParentScreen(),
                "Send to Tool",
                "This will override current tool copy data. Continue?",
                confirmed -> {
                    if (confirmed) {
                        SchematicFile.SchematicData data = file.loadData();
                        if (data != null && data.blocks != null) {
                            HistoryTab.setClipboard(
                                data.blocks,
                                data.teData,
                                data.copyUUID != null ? java.util.UUID.fromString(data.copyUUID) : null,
                                data.blockCount
                            );
                            ClipboardUtils.sendToTool();
                            System.out.println("Sent schematic '" + file.getName() + "' to tool");
                        }
                    }
                }
            );
            Minecraft.getInstance().setScreen(dialog);
        }
        
        private void onMaterialButtonClicked() {
            SchematicFile.SchematicData data = file.loadData();
            if (data != null && data.blocks != null) {
                MaterialListDialog dialog = new MaterialListDialog(
                    Minecraft.getInstance().screen,
                    "Materials: " + file.getName(),
                    data.blocks
                );
                Minecraft.getInstance().setScreen(dialog);
            }
        }
        
        private void onDeleteButtonClicked() {
            String fileName = file.getName();
            
            ConfirmationDialog dialog = new ConfirmationDialog(
                parent.getParentScreen(),
                "Delete Schematic",
                "Delete '" + fileName + "'?",
                confirmed -> {
                    if (confirmed) {
                        boolean success = SchematicManager.deleteFile(file);
                        if (success) {
                            System.out.println("Deleted file: " + fileName);
                            refreshList();
                            parent.onNavigate();
                        } else {
                            System.err.println("Failed to delete file: " + fileName);
                        }
                    }
                }
            );
            Minecraft.getInstance().setScreen(dialog);
        }
    }
}

