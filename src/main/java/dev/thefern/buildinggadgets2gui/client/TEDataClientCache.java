package dev.thefern.buildinggadgets2gui.client;

import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TEDataClientCache {
    private static final HashMap<UUID, ArrayList<TagPos>> teDataCache = new HashMap<>();
    private static final HashMap<UUID, Boolean> pendingRequests = new HashMap<>();
    
    public static void storeTEData(UUID gadgetUUID, ArrayList<TagPos> teData) {
        teDataCache.put(gadgetUUID, teData);
        pendingRequests.remove(gadgetUUID);
    }
    
    public static ArrayList<TagPos> getTEData(UUID gadgetUUID) {
        return teDataCache.get(gadgetUUID);
    }
    
    public static boolean hasTEData(UUID gadgetUUID) {
        return teDataCache.containsKey(gadgetUUID);
    }
    
    public static void markPendingRequest(UUID gadgetUUID) {
        pendingRequests.put(gadgetUUID, true);
    }
    
    public static boolean isPendingRequest(UUID gadgetUUID) {
        return pendingRequests.getOrDefault(gadgetUUID, false);
    }
    
    public static void clearCache(UUID gadgetUUID) {
        teDataCache.remove(gadgetUUID);
        pendingRequests.remove(gadgetUUID);
    }
    
    public static void clearAll() {
        teDataCache.clear();
        pendingRequests.clear();
    }
}
