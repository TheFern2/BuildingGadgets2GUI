package dev.thefern.buildinggadgets2gui.client.schematics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SchematicFolder {
    
    private File directory;
    private SchematicFolder parent;
    
    public SchematicFolder(File directory) {
        this(directory, null);
    }
    
    public SchematicFolder(File directory, SchematicFolder parent) {
        this.directory = directory;
        this.parent = parent;
        
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    public String getName() {
        return directory.getName();
    }
    
    public File getDirectory() {
        return directory;
    }
    
    public SchematicFolder getParent() {
        return parent;
    }
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public String getPath() {
        return directory.getAbsolutePath();
    }
    
    public String getRelativePath(File root) {
        String rootPath = root.getAbsolutePath();
        String currentPath = directory.getAbsolutePath();
        
        if (currentPath.startsWith(rootPath)) {
            String relative = currentPath.substring(rootPath.length());
            if (relative.startsWith(File.separator)) {
                relative = relative.substring(1);
            }
            return relative.isEmpty() ? "/" : "/" + relative.replace(File.separator, "/");
        }
        return currentPath;
    }
    
    public List<SchematicFolder> getSubFolders() {
        List<SchematicFolder> folders = new ArrayList<>();
        
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            
            for (File file : files) {
                if (file.isDirectory()) {
                    folders.add(new SchematicFolder(file, this));
                }
            }
        }
        
        return folders;
    }
    
    public List<SchematicFile> getSchematicFiles() {
        List<SchematicFile> schematics = new ArrayList<>();
        
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".bg2schem")) {
                    schematics.add(new SchematicFile(file));
                }
            }
        }
        
        return schematics;
    }
    
    public List<Object> getContents() {
        List<Object> contents = new ArrayList<>();
        
        contents.addAll(getSubFolders());
        contents.addAll(getSchematicFiles());
        
        return contents;
    }
    
    public boolean createSubFolder(String name) {
        File newFolder = new File(directory, name);
        if (newFolder.exists()) {
            return false;
        }
        return newFolder.mkdirs();
    }
    
    public boolean delete() {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length == 0) {
                return directory.delete();
            }
        }
        return false;
    }
    
    public boolean rename(String newName) {
        File newFile = new File(directory.getParent(), newName);
        if (newFile.exists()) {
            return false;
        }
        boolean success = directory.renameTo(newFile);
        if (success) {
            this.directory = newFile;
        }
        return success;
    }
    
    public boolean isEmpty() {
        File[] files = directory.listFiles();
        return files == null || files.length == 0;
    }
    
    public int getFileCount() {
        return getSchematicFiles().size();
    }
    
    public int getFolderCount() {
        return getSubFolders().size();
    }
}

