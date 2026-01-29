package dev.thefern.buildinggadgets2gui.client.schematics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.thefern.buildinggadgets2gui.Config;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrashManager {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File trashMetaFile;
    private static List<TrashEntry> entries = new ArrayList<>();
    private static boolean initialized = false;
    
    public static void init() {
        if (initialized) return;
        
        Path configPath = FMLPaths.CONFIGDIR.get();
        File configDir = configPath.resolve("buildinggadgets2gui").toFile();
        
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        trashMetaFile = new File(configDir, "trash.json");
        loadTrashData();
        validateEntries();
        performAutoCleanup();
        initialized = true;
        System.out.println("TrashManager initialized with " + entries.size() + " entries");
    }
    
    private static void loadTrashData() {
        if (trashMetaFile == null || !trashMetaFile.exists()) {
            entries = new ArrayList<>();
            return;
        }
        
        try (FileReader reader = new FileReader(trashMetaFile)) {
            TrashData data = GSON.fromJson(reader, TrashData.class);
            if (data != null && data.entries != null) {
                entries = new ArrayList<>(data.entries);
            } else {
                entries = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Failed to load trash data: " + e.getMessage());
            entries = new ArrayList<>();
        }
    }
    
    private static void saveTrashData() {
        if (trashMetaFile == null) return;
        
        try (FileWriter writer = new FileWriter(trashMetaFile)) {
            TrashData data = new TrashData();
            data.version = 1;
            data.entries = new ArrayList<>(entries);
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save trash data: " + e.getMessage());
        }
    }
    
    private static void validateEntries() {
        File trashRoot = SchematicManager.getTrashRoot();
        if (trashRoot == null) {
            System.out.println("TrashManager: trashRoot is null, skipping validation");
            return;
        }
        
        Iterator<TrashEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            TrashEntry entry = iterator.next();
            File trashFile = new File(trashRoot, entry.fileName);
            if (!trashFile.exists()) {
                System.out.println("Removing orphaned trash entry: " + entry.fileName);
                iterator.remove();
            }
        }
        
        File[] trashFiles = trashRoot.listFiles((dir, name) -> name.endsWith(".bg2schem"));
        if (trashFiles != null) {
            for (File file : trashFiles) {
                if (!hasEntry(file.getName())) {
                    System.out.println("Adding untracked trash file: " + file.getName());
                    TrashEntry entry = new TrashEntry();
                    entry.fileName = file.getName();
                    entry.originalPath = "";
                    entry.deletedAt = file.lastModified();
                    entries.add(entry);
                }
            }
        }
        
        saveTrashData();
    }
    
    public static void refresh() {
        loadTrashData();
        validateEntries();
    }
    
    private static boolean hasEntry(String fileName) {
        for (TrashEntry entry : entries) {
            if (entry.fileName.equals(fileName)) {
                return true;
            }
        }
        return false;
    }
    
    public static void addToTrash(String fileName, String originalPath) {
        TrashEntry entry = new TrashEntry();
        entry.fileName = fileName;
        entry.originalPath = originalPath != null ? originalPath : "";
        entry.deletedAt = System.currentTimeMillis();
        
        entries.add(entry);
        saveTrashData();
        
        performAutoCleanup();
        
        System.out.println("Added to trash: " + fileName + " (from: " + originalPath + ")");
    }
    
    public static List<TrashEntry> getTrashEntries() {
        return Collections.unmodifiableList(entries);
    }
    
    public static int getTrashCount() {
        return entries.size();
    }
    
    public static boolean restoreFile(TrashEntry entry) {
        File trashRoot = SchematicManager.getTrashRoot();
        File schematicsRoot = SchematicManager.getSchematicsRoot();
        
        if (trashRoot == null || schematicsRoot == null) return false;
        
        File trashFile = new File(trashRoot, entry.fileName);
        if (!trashFile.exists()) {
            entries.remove(entry);
            saveTrashData();
            return false;
        }
        
        File targetDir;
        if (entry.originalPath == null || entry.originalPath.isEmpty()) {
            targetDir = schematicsRoot;
        } else {
            targetDir = new File(schematicsRoot, entry.originalPath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }
        
        String originalName = entry.fileName;
        if (originalName.matches(".*_\\d+\\.bg2schem$")) {
            String baseName = originalName.substring(0, originalName.lastIndexOf('_'));
            originalName = baseName + ".bg2schem";
        }
        
        File targetFile = new File(targetDir, originalName);
        
        int counter = 1;
        while (targetFile.exists()) {
            String baseName = originalName;
            if (baseName.endsWith(".bg2schem")) {
                baseName = baseName.substring(0, baseName.length() - 9);
            }
            targetFile = new File(targetDir, baseName + "_" + counter + ".bg2schem");
            counter++;
        }
        
        boolean success = trashFile.renameTo(targetFile);
        if (success) {
            entries.remove(entry);
            saveTrashData();
            System.out.println("Restored from trash: " + entry.fileName + " -> " + targetFile.getAbsolutePath());
        }
        
        return success;
    }
    
    public static boolean permanentlyDelete(TrashEntry entry) {
        File trashRoot = SchematicManager.getTrashRoot();
        if (trashRoot == null) return false;
        
        File trashFile = new File(trashRoot, entry.fileName);
        boolean success = true;
        
        if (trashFile.exists()) {
            success = trashFile.delete();
        }
        
        if (success) {
            entries.remove(entry);
            saveTrashData();
            System.out.println("Permanently deleted: " + entry.fileName);
        }
        
        return success;
    }
    
    public static void emptyTrash() {
        File trashRoot = SchematicManager.getTrashRoot();
        if (trashRoot == null) return;
        
        for (TrashEntry entry : new ArrayList<>(entries)) {
            File trashFile = new File(trashRoot, entry.fileName);
            if (trashFile.exists()) {
                trashFile.delete();
            }
        }
        
        entries.clear();
        saveTrashData();
        System.out.println("Trash emptied");
    }
    
    public static void performAutoCleanup() {
        boolean changed = false;
        
        int retentionDays = Config.TRASH_RETENTION_DAYS.get();
        if (retentionDays > 0) {
            long cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays);
            Iterator<TrashEntry> iterator = entries.iterator();
            
            while (iterator.hasNext()) {
                TrashEntry entry = iterator.next();
                if (entry.deletedAt < cutoffTime) {
                    File trashRoot = SchematicManager.getTrashRoot();
                    if (trashRoot != null) {
                        File trashFile = new File(trashRoot, entry.fileName);
                        if (trashFile.exists()) {
                            trashFile.delete();
                        }
                    }
                    iterator.remove();
                    changed = true;
                    System.out.println("Auto-deleted (age): " + entry.fileName);
                }
            }
        }
        
        int maxItems = Config.MAX_TRASH_ITEMS.get();
        if (maxItems > 0 && entries.size() > maxItems) {
            entries.sort((a, b) -> Long.compare(a.deletedAt, b.deletedAt));
            
            while (entries.size() > maxItems) {
                TrashEntry oldest = entries.get(0);
                File trashRoot = SchematicManager.getTrashRoot();
                if (trashRoot != null) {
                    File trashFile = new File(trashRoot, oldest.fileName);
                    if (trashFile.exists()) {
                        trashFile.delete();
                    }
                }
                entries.remove(0);
                changed = true;
                System.out.println("Auto-deleted (max items): " + oldest.fileName);
            }
        }
        
        if (changed) {
            saveTrashData();
        }
    }
    
    public static String getRelativeTimeSince(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        }
        
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        
        return "just now";
    }
    
    public static void simulateRetentionExpired() {
        long sixtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60);
        
        for (TrashEntry entry : entries) {
            entry.deletedAt = sixtyDaysAgo;
            System.out.println("Set " + entry.fileName + " deletion date to 60 days ago");
        }
        
        saveTrashData();
        performAutoCleanup();
    }
    
    public static class TrashEntry {
        public String fileName;
        public String originalPath;
        public long deletedAt;
        
        public SchematicFile getSchematicFile() {
            File trashRoot = SchematicManager.getTrashRoot();
            if (trashRoot == null) return null;
            return new SchematicFile(new File(trashRoot, fileName));
        }
    }
    
    private static class TrashData {
        int version;
        List<TrashEntry> entries;
    }
}
