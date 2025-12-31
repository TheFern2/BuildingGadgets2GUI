package dev.thefern.buildinggadgets2gui.client.schematics;

import dev.thefern.buildinggadgets2gui.client.tabs.SchematicsTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SchematicsList extends ObjectSelectionList<SchematicsList.Entry> {
    
    private final SchematicsTab parent;
    private SchematicFolder currentFolder;
    
    public SchematicsList(Minecraft minecraft, int width, int height, int y, int itemHeight, SchematicsTab parent) {
        super(minecraft, width, height, y, itemHeight);
        this.parent = parent;
        refreshList();
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
            
            String folderIcon = "📁";
            String folderName = folder.getName();
            
            graphics.drawString(
                Minecraft.getInstance().font,
                folderIcon + " " + folderName,
                left + 5,
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
                left + 5,
                top + 12,
                0x888888,
                false
            );
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                SchematicManager.navigateToFolder(folder);
                refreshList();
                parent.onNavigate();
                return true;
            }
            return false;
        }
    }
    
    public class FileEntry extends Entry {
        private final SchematicFile file;
        
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
            
            String fileIcon = "📄";
            String fileName = file.getName();
            
            graphics.drawString(
                Minecraft.getInstance().font,
                fileIcon + " " + fileName,
                left + 5,
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
                    left + 5,
                    top + 12,
                    0x888888,
                    false
                );
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                setSelected(this);
                return true;
            }
            return false;
        }
    }
}

