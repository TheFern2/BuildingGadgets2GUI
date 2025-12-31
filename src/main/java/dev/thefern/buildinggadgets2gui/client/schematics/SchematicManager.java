package dev.thefern.buildinggadgets2gui.client.schematics;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Path;

public class SchematicManager {
    
    private static File schematicsRoot;
    private static File trashRoot;
    private static SchematicFolder currentFolder;
    
    public static void init() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        schematicsRoot = configPath.resolve("buildinggadgets2gui/schematics").toFile();
        trashRoot = configPath.resolve("buildinggadgets2gui/trash").toFile();
        
        if (!schematicsRoot.exists()) {
            schematicsRoot.mkdirs();
            System.out.println("Created schematics directory: " + schematicsRoot.getAbsolutePath());
        }
        
        if (!trashRoot.exists()) {
            trashRoot.mkdirs();
            System.out.println("Created trash directory: " + trashRoot.getAbsolutePath());
        }
        
        currentFolder = new SchematicFolder(schematicsRoot);
        System.out.println("SchematicManager initialized");
        System.out.println("Schematics root: " + schematicsRoot.getAbsolutePath());
    }
    
    public static File getSchematicsRoot() {
        return schematicsRoot;
    }
    
    public static File getTrashRoot() {
        return trashRoot;
    }
    
    public static SchematicFolder getCurrentFolder() {
        return currentFolder;
    }
    
    public static void setCurrentFolder(SchematicFolder folder) {
        currentFolder = folder;
        System.out.println("Current folder changed to: " + folder.getPath());
    }
    
    public static void navigateToRoot() {
        currentFolder = new SchematicFolder(schematicsRoot);
    }
    
    public static void navigateToFolder(SchematicFolder folder) {
        setCurrentFolder(folder);
    }
    
    public static void navigateUp() {
        if (currentFolder.hasParent()) {
            setCurrentFolder(currentFolder.getParent());
        }
    }
    
    public static boolean isAtRoot() {
        return currentFolder.getDirectory().equals(schematicsRoot);
    }
    
    public static String getCurrentPath() {
        return currentFolder.getRelativePath(schematicsRoot);
    }
    
    public static boolean createFolder(String name) {
        return currentFolder.createSubFolder(name);
    }
    
    public static boolean deleteFile(SchematicFile file) {
        File sourceFile = file.getFile();
        if (!sourceFile.exists()) {
            return false;
        }
        
        File trashFile = new File(trashRoot, sourceFile.getName());
        
        int counter = 1;
        while (trashFile.exists()) {
            String baseName = sourceFile.getName();
            if (baseName.endsWith(".bg2schem")) {
                baseName = baseName.substring(0, baseName.length() - 9);
            }
            trashFile = new File(trashRoot, baseName + "_" + counter + ".bg2schem");
            counter++;
        }
        
        boolean success = sourceFile.renameTo(trashFile);
        if (success) {
            System.out.println("Moved to trash: " + sourceFile.getName() + " -> " + trashFile.getName());
        }
        return success;
    }
    
    public static boolean deleteFolder(SchematicFolder folder) {
        if (folder.isEmpty()) {
            return folder.delete();
        }
        return false;
    }
    
    public static boolean renameFile(SchematicFile file, String newName) {
        if (!newName.endsWith(".bg2schem")) {
            newName += ".bg2schem";
        }
        
        File oldFile = file.getFile();
        File newFile = new File(oldFile.getParent(), newName);
        
        if (newFile.exists()) {
            return false;
        }
        
        return oldFile.renameTo(newFile);
    }
    
    public static boolean renameFolder(SchematicFolder folder, String newName) {
        return folder.rename(newName);
    }
    
    public static File createSchematicFile(String name) {
        if (!name.endsWith(".bg2schem")) {
            name += ".bg2schem";
        }
        
        File file = new File(currentFolder.getDirectory(), name);
        
        int counter = 1;
        while (file.exists()) {
            String baseName = name.substring(0, name.length() - 9);
            file = new File(currentFolder.getDirectory(), baseName + "_" + counter + ".bg2schem");
            counter++;
        }
        
        return file;
    }
}

