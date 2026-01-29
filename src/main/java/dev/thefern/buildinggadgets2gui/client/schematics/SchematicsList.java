package dev.thefern.buildinggadgets2gui.client.schematics;

import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.MaterialListDialog;
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
            
            int materialButtonSize = 12;
            int materialButtonX = left + width - materialButtonSize - 8;
            int materialButtonY = top + 6;
            
            boolean isHoveringMaterial = mouseX >= materialButtonX && mouseX <= materialButtonX + materialButtonSize &&
                                         mouseY >= materialButtonY && mouseY <= materialButtonY + materialButtonSize;
            
            graphics.fill(
                materialButtonX,
                materialButtonY,
                materialButtonX + materialButtonSize,
                materialButtonY + materialButtonSize,
                isHoveringMaterial ? 0xFF6688FF : 0xFF4466AA
            );
            
            String mText = "M";
            int mTextWidth = Minecraft.getInstance().font.width(mText);
            graphics.drawString(
                Minecraft.getInstance().font,
                mText,
                materialButtonX + (materialButtonSize - mTextWidth) / 2,
                materialButtonY + 2,
                0xFFFFFF,
                false
            );
            
            if (isHoveringMaterial) {
                setPendingTooltip(Component.literal("Material list"), mouseX, mouseY);
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
                
                int materialButtonSize = 12;
                int materialButtonX = lastRenderedLeft + getRowWidth() - materialButtonSize - 8;
                int materialButtonY = lastRenderedTop + 6;
                
                if (mouseX >= materialButtonX && mouseX <= materialButtonX + materialButtonSize &&
                    mouseY >= materialButtonY && mouseY <= materialButtonY + materialButtonSize) {
                    
                    onMaterialButtonClicked();
                    return true;
                }
                
                setSelected(this);
                return true;
            }
            return false;
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

