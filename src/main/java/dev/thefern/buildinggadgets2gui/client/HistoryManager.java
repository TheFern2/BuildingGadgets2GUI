package dev.thefern.buildinggadgets2gui.client;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import dev.thefern.buildinggadgets2gui.client.tabs.HistoryTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HistoryManager {
    private static final String HISTORY_FILE_NAME = "buildinggadgets2gui-history.dat";
    private static File historyFile;
    
    public static void init() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        historyFile = configPath.resolve(HISTORY_FILE_NAME).toFile();
        System.out.println("History file location: " + historyFile.getAbsolutePath());
    }
    
    public static void saveHistory(List<HistoryTab.HistoryEntry> history) {
        if (historyFile == null) {
            System.err.println("History file not initialized!");
            return;
        }
        
        try {
            CompoundTag rootTag = new CompoundTag();
            ListTag historyList = new ListTag();
            
            for (HistoryTab.HistoryEntry entry : history) {
                CompoundTag entryTag = new CompoundTag();
                
                if (entry.copyUUID != null) {
                    entryTag.putUUID("copyUUID", entry.copyUUID);
                }
                entryTag.putInt("blockCount", entry.blockCount);
                entryTag.putString("timestamp", entry.timestamp);
                
                ListTag blocksList = new ListTag();
                for (StatePos statePos : entry.blocks) {
                    blocksList.add(statePos.getTag());
                }
                entryTag.put("blocks", blocksList);
                
                historyList.add(entryTag);
            }
            
            rootTag.put("history", historyList);
            rootTag.putInt("version", 1);
            
            NbtIo.writeCompressed(rootTag, historyFile.toPath());
            System.out.println("Saved " + history.size() + " history entries to " + historyFile.getName());
            
        } catch (IOException e) {
            System.err.println("Failed to save history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static List<HistoryTab.HistoryEntry> loadHistory() {
        List<HistoryTab.HistoryEntry> history = new ArrayList<>();
        
        if (historyFile == null) {
            System.err.println("History file not initialized!");
            return history;
        }
        
        if (!historyFile.exists()) {
            System.out.println("No history file found, starting fresh");
            return history;
        }
        
        try {
            CompoundTag rootTag = NbtIo.readCompressed(historyFile.toPath(), NbtAccounter.unlimitedHeap());
            
            int version = rootTag.getInt("version");
            System.out.println("Loading history file version: " + version);
            
            ListTag historyList = rootTag.getList("history", Tag.TAG_COMPOUND);
            
            for (int i = 0; i < historyList.size(); i++) {
                CompoundTag entryTag = historyList.getCompound(i);
                
                UUID copyUUID = entryTag.hasUUID("copyUUID") ? entryTag.getUUID("copyUUID") : null;
                int blockCount = entryTag.getInt("blockCount");
                String timestamp = entryTag.getString("timestamp");
                
                ArrayList<StatePos> blocks = new ArrayList<>();
                ListTag blocksList = entryTag.getList("blocks", Tag.TAG_COMPOUND);
                for (int j = 0; j < blocksList.size(); j++) {
                    CompoundTag blockTag = blocksList.getCompound(j);
                    blocks.add(new StatePos(blockTag));
                }
                
                HistoryTab.HistoryEntry entry = new HistoryTab.HistoryEntry(blocks, copyUUID, blockCount, timestamp);
                history.add(entry);
            }
            
            System.out.println("Loaded " + history.size() + " history entries from " + historyFile.getName());
            
        } catch (IOException e) {
            System.err.println("Failed to load history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    public static void deleteHistoryFile() {
        if (historyFile != null && historyFile.exists()) {
            if (historyFile.delete()) {
                System.out.println("Deleted history file: " + historyFile.getName());
            } else {
                System.err.println("Failed to delete history file: " + historyFile.getName());
            }
        }
    }
}

