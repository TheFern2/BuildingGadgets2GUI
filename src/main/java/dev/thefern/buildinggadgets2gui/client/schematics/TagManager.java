package dev.thefern.buildinggadgets2gui.client.schematics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagManager {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File tagsFile;
    private static List<String> tags = new ArrayList<>();
    private static Map<String, Integer> tagUsageCounts = new HashMap<>();
    private static boolean initialized = false;
    
    public static void init() {
        if (initialized) return;
        
        Path configPath = FMLPaths.CONFIGDIR.get();
        File configDir = configPath.resolve("buildinggadgets2gui").toFile();
        
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        tagsFile = new File(configDir, "tags.json");
        loadTags();
        initialized = true;
        System.out.println("TagManager initialized with " + tags.size() + " tags");
    }
    
    private static void loadTags() {
        if (tagsFile == null || !tagsFile.exists()) {
            tags = new ArrayList<>();
            return;
        }
        
        try (FileReader reader = new FileReader(tagsFile)) {
            TagsData data = GSON.fromJson(reader, TagsData.class);
            if (data != null && data.tags != null) {
                tags = new ArrayList<>(data.tags);
            } else {
                tags = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Failed to load tags: " + e.getMessage());
            tags = new ArrayList<>();
        }
    }
    
    private static void saveTags() {
        if (tagsFile == null) return;
        
        try (FileWriter writer = new FileWriter(tagsFile)) {
            TagsData data = new TagsData();
            data.tags = new ArrayList<>(tags);
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save tags: " + e.getMessage());
        }
    }
    
    public static List<String> getAllTags() {
        return Collections.unmodifiableList(tags);
    }
    
    public static boolean addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return false;
        }
        
        String normalizedTag = tag.trim().toLowerCase();
        
        if (tags.contains(normalizedTag)) {
            return false;
        }
        
        tags.add(normalizedTag);
        Collections.sort(tags);
        saveTags();
        System.out.println("Added tag: " + normalizedTag);
        return true;
    }
    
    public static boolean removeTag(String tag) {
        if (tag == null) return false;
        
        String normalizedTag = tag.trim().toLowerCase();
        boolean removed = tags.remove(normalizedTag);
        
        if (removed) {
            saveTags();
            System.out.println("Removed tag: " + normalizedTag);
        }
        
        return removed;
    }
    
    public static boolean renameTag(String oldTag, String newTag) {
        if (oldTag == null || newTag == null) return false;
        
        String normalizedOld = oldTag.trim().toLowerCase();
        String normalizedNew = newTag.trim().toLowerCase();
        
        if (normalizedNew.isEmpty()) return false;
        if (normalizedOld.equals(normalizedNew)) return true;
        if (tags.contains(normalizedNew)) return false;
        
        int index = tags.indexOf(normalizedOld);
        if (index == -1) return false;
        
        tags.set(index, normalizedNew);
        Collections.sort(tags);
        saveTags();
        System.out.println("Renamed tag: " + normalizedOld + " -> " + normalizedNew);
        return true;
    }
    
    public static boolean hasTag(String tag) {
        if (tag == null) return false;
        return tags.contains(tag.trim().toLowerCase());
    }
    
    public static void ensureTagExists(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            addTag(tag);
        }
    }
    
    public static void ensureTagsExist(List<String> tagList) {
        if (tagList == null) return;
        for (String tag : tagList) {
            ensureTagExists(tag);
        }
    }
    
    public static int getTagUsageCount(String tag) {
        if (tag == null) return 0;
        return tagUsageCounts.getOrDefault(tag.trim().toLowerCase(), 0);
    }
    
    public static void refreshUsageCounts() {
        tagUsageCounts.clear();
        
        File schematicsRoot = SchematicManager.getSchematicsRoot();
        if (schematicsRoot != null && schematicsRoot.exists()) {
            countTagsInFolder(schematicsRoot);
        }
        
        System.out.println("Tag usage counts refreshed: " + tagUsageCounts);
    }
    
    private static void countTagsInFolder(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                countTagsInFolder(file);
            } else if (file.getName().endsWith(".bg2schem")) {
                SchematicFile schematic = new SchematicFile(file);
                List<String> schematicTags = schematic.getTags();
                for (String tag : schematicTags) {
                    String normalizedTag = tag.trim().toLowerCase();
                    tagUsageCounts.merge(normalizedTag, 1, Integer::sum);
                }
            }
        }
    }
    
    private static class TagsData {
        List<String> tags;
    }
}
