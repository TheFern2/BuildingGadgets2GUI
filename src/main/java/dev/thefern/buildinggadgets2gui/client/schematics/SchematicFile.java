package dev.thefern.buildinggadgets2gui.client.schematics;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchematicFile {
    
    private File file;
    private SchematicMetadata metadata;
    private boolean metadataLoaded = false;
    
    public SchematicFile(File file) {
        this.file = file;
    }
    
    public String getName() {
        String name = file.getName();
        if (name.endsWith(".bg2schem")) {
            return name.substring(0, name.length() - 9);
        }
        return name;
    }
    
    public String getFileName() {
        return file.getName();
    }
    
    public File getFile() {
        return file;
    }
    
    public long getLastModified() {
        return file.lastModified();
    }
    
    public boolean exists() {
        return file.exists();
    }
    
    public SchematicMetadata getMetadata() {
        if (!metadataLoaded) {
            loadMetadata();
        }
        return metadata;
    }
    
    private void loadMetadata() {
        metadataLoaded = true;
        try {
            CompoundTag nbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            
            if (nbt.contains("metadata")) {
                CompoundTag metaTag = nbt.getCompound("metadata");
                metadata = new SchematicMetadata();
                metadata.name = metaTag.getString("name");
                metadata.description = metaTag.contains("description") ? metaTag.getString("description") : null;
                metadata.created = metaTag.getLong("created");
                metadata.modified = metaTag.getLong("modified");
                metadata.author = metaTag.contains("author") ? metaTag.getString("author") : null;
                metadata.tags = new ArrayList<>();
                if (metaTag.contains("tags")) {
                    String tagsStr = metaTag.getString("tags");
                    if (!tagsStr.isEmpty()) {
                        for (String tag : tagsStr.split(",")) {
                            metadata.tags.add(tag.trim());
                        }
                    }
                }
            } else {
                metadata = new SchematicMetadata();
                metadata.name = getName();
            }
        } catch (IOException e) {
            System.err.println("Failed to load schematic metadata: " + file.getName());
            e.printStackTrace();
            metadata = new SchematicMetadata();
            metadata.name = getName();
        }
    }
    
    public SchematicData loadData() {
        try {
            CompoundTag nbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            
            SchematicData data = new SchematicData();
            data.version = nbt.getInt("version");
            data.blockCount = nbt.getInt("blockCount");
            data.copyUUID = nbt.contains("copyUUID") ? nbt.getString("copyUUID") : null;
            
            if (nbt.contains("metadata")) {
                CompoundTag metaTag = nbt.getCompound("metadata");
                data.metadata = new SchematicMetadata();
                data.metadata.name = metaTag.getString("name");
                data.metadata.description = metaTag.contains("description") ? metaTag.getString("description") : null;
                data.metadata.created = metaTag.getLong("created");
                data.metadata.modified = metaTag.getLong("modified");
                data.metadata.author = metaTag.contains("author") ? metaTag.getString("author") : null;
                data.metadata.tags = new ArrayList<>();
                if (metaTag.contains("tags")) {
                    String tagsStr = metaTag.getString("tags");
                    if (!tagsStr.isEmpty()) {
                        for (String tag : tagsStr.split(",")) {
                            data.metadata.tags.add(tag.trim());
                        }
                    }
                }
            }
            
            if (nbt.contains("dimensions")) {
                CompoundTag dimTag = nbt.getCompound("dimensions");
                data.dimensions = new SchematicDimensions();
                data.dimensions.x = dimTag.getInt("x");
                data.dimensions.y = dimTag.getInt("y");
                data.dimensions.z = dimTag.getInt("z");
            }
            
            if (nbt.contains("blocks")) {
                data.blocks = BG2Data.statePosListFromNBTMapArray(nbt.getCompound("blocks"));
            }
            
            return data;
        } catch (IOException e) {
            System.err.println("Failed to load schematic data: " + file.getName());
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean saveSchematic(File file, String name, String description, List<String> tags, 
                                       ArrayList<StatePos> blocks, UUID copyUUID, String author) {
        try {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("version", 1);
            
            CompoundTag metadata = new CompoundTag();
            metadata.putString("name", name);
            if (description != null && !description.isEmpty()) {
                metadata.putString("description", description);
            }
            metadata.putLong("created", System.currentTimeMillis());
            metadata.putLong("modified", System.currentTimeMillis());
            if (author != null) {
                metadata.putString("author", author);
            }
            if (tags != null && !tags.isEmpty()) {
                metadata.putString("tags", String.join(",", tags));
            }
            nbt.put("metadata", metadata);
            
            if (blocks != null && !blocks.isEmpty()) {
                nbt.putInt("blockCount", blocks.size());
                
                int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
                
                for (StatePos sp : blocks) {
                    minX = Math.min(minX, sp.pos.getX());
                    minY = Math.min(minY, sp.pos.getY());
                    minZ = Math.min(minZ, sp.pos.getZ());
                    maxX = Math.max(maxX, sp.pos.getX());
                    maxY = Math.max(maxY, sp.pos.getY());
                    maxZ = Math.max(maxZ, sp.pos.getZ());
                }
                
                CompoundTag dimensions = new CompoundTag();
                dimensions.putInt("x", maxX - minX + 1);
                dimensions.putInt("y", maxY - minY + 1);
                dimensions.putInt("z", maxZ - minZ + 1);
                nbt.put("dimensions", dimensions);
                
                CompoundTag blocksNBT = BG2Data.statePosListToNBTMapArray(blocks);
                nbt.put("blocks", blocksNBT);
            } else {
                nbt.putInt("blockCount", 0);
                CompoundTag dimensions = new CompoundTag();
                dimensions.putInt("x", 0);
                dimensions.putInt("y", 0);
                dimensions.putInt("z", 0);
                nbt.put("dimensions", dimensions);
            }
            
            if (copyUUID != null) {
                nbt.putString("copyUUID", copyUUID.toString());
            }
            
            NbtIo.writeCompressed(nbt, file.toPath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save schematic: " + file.getName());
            e.printStackTrace();
            return false;
        }
    }
    
    public static class SchematicData {
        public int version;
        public SchematicMetadata metadata;
        public SchematicDimensions dimensions;
        public int blockCount;
        public String copyUUID;
        public ArrayList<StatePos> blocks;
    }
    
    public static class SchematicMetadata {
        public String name;
        public String description;
        public long created;
        public long modified;
        public String author;
        public List<String> tags;
    }
    
    public static class SchematicDimensions {
        public int x;
        public int y;
        public int z;
        
        @Override
        public String toString() {
            return x + " x " + y + " x " + z;
        }
    }
}

