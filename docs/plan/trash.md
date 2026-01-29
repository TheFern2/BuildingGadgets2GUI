# Trash Tab Implementation Plan

## Overview

Implement a Trash tab to manage deleted schematics. When users delete schematics from the Schematics tab, files are moved to a trash folder instead of being permanently deleted. The Trash tab allows users to:
1. View deleted schematics
2. Restore schematics to their original location (or schematics root)
3. Permanently delete schematics from trash
4. Configure trash retention settings (max items, auto-delete after time)

---

## Current State

- `SchematicManager` already has `trashRoot` initialized at `config/buildinggadgets2gui/trash/`
- `SchematicManager.deleteFile()` already moves files to trash folder
- `TrashTab.java` exists but only shows placeholder "Coming Soon" text

---

## Phase 1: Trash Manager Infrastructure

### 1.1 Create `TrashManager.java`

A new class to manage trash operations:

- **Location**: `src/main/java/dev/thefern/buildinggadgets2gui/client/schematics/TrashManager.java`
- **Responsibilities**:
  - Load/save trash metadata to JSON file (`buildinggadgets2gui/trash.json`)
  - Track original paths for restore functionality
  - Provide CRUD operations: `getTrashItems()`, `restoreFile()`, `permanentlyDelete()`, `emptyTrash()`
  - Handle auto-cleanup based on settings

### 1.2 Trash Entry Data Structure

```java
public class TrashEntry {
    public String fileName;           // Current filename in trash folder
    public String originalPath;       // Relative path from schematics root where file was deleted from
    public long deletedAt;            // Timestamp when file was trashed
    public SchematicMetadata metadata; // Cached metadata for display
}
```

### 1.3 Trash Metadata File

Store trash metadata at `config/buildinggadgets2gui/trash.json`:

```json
{
  "version": 1,
  "entries": [
    {
      "fileName": "my_house.bg2schem",
      "originalPath": "builds/houses",
      "deletedAt": 1706500000000
    },
    {
      "fileName": "farm_1.bg2schem",
      "originalPath": "",
      "deletedAt": 1706499000000
    }
  ]
}
```

---

## Phase 2: Update SchematicManager for Trash Tracking

### 2.1 Modify `deleteFile()` Method

Update `SchematicManager.deleteFile()` to:
1. Calculate relative path from schematics root
2. Call `TrashManager.addToTrash()` with original path info
3. Move file to trash folder (existing behavior)

> **Note**: Folder deletion is not supported for non-empty folders. Users must delete individual schematics first.

---

## Phase 3: Config Settings for Trash

### 3.1 Add Config Options in `Config.java`

```java
public static final ModConfigSpec.IntValue MAX_TRASH_ITEMS = BUILDER
    .comment("Maximum number of items to keep in trash (0 = unlimited)")
    .defineInRange("maxTrashItems", 50, 0, 500);

public static final ModConfigSpec.IntValue TRASH_RETENTION_DAYS = BUILDER
    .comment("Days to keep items in trash before auto-delete (0 = never auto-delete)")
    .defineInRange("trashRetentionDays", 30, 0, 365);
```

### 3.2 Update SettingsTab

Add UI controls for trash settings:
- Max Trash Items: `[-] [value] [+]`
- Trash Retention Days: `[-] [value] [+]`

---

## Phase 4: Implement TrashTab UI

### 4.1 TrashTab Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ Trash (X items)                              [Empty Trash]      │
├─────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ [Scrollable List of Trash Items]                            │ │
│ │                                                             │ │
│ │  📄 my_house.bg2schem          [🔄 Restore] [🗑️ Delete]    │ │
│ │     From: builds/houses - 2 days ago                        │ │
│ │                                                             │ │
│ │  📄 farm_design.bg2schem       [🔄 Restore] [🗑️ Delete]    │ │
│ │     From: root - 5 days ago                                 │ │
│ │                                                             │ │
│ └─────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│ [Info Panel - Selected Item Details]                            │
│ Name: my_house                                                  │
│ Blocks: 1,234 | Size: 10 x 8 x 12                              │
│ Deleted: Jan 28, 2026 | Original: builds/houses                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 TrashTab Components

**Header Section:**
- Title showing "Trash" with item count
- "Empty Trash" button (with confirmation dialog)

**List Section:**
- Scrollable list similar to `SchematicsList`
- Each entry shows:
  - File icon and name
  - Original location and time since deletion
  - Action buttons: Restore, Delete

**Info Panel:**
- Shows details of selected trash item
- Same info as SchematicsTab: name, block count, dimensions, tags
- Additional: deletion date, original path

### 4.3 Create `TrashList.java`

A new widget for rendering trash entries:

- **Location**: `src/main/java/dev/thefern/buildinggadgets2gui/client/schematics/TrashList.java`
- Similar to `SchematicsList` but:
  - No folder navigation
  - Different action buttons (Restore, Permanent Delete)
  - Shows original path and deletion date

### 4.4 Action Buttons

Use existing `RowActionButtons` with new button types:

```java
public enum ButtonType {
    // ... existing types ...
    RESTORE,      // 🔄 Restore to original location
    DELETE_PERM   // 🗑️ Permanent delete
}
```

---

## Phase 5: Dialogs

### 5.1 Empty Trash Confirmation Dialog

Use existing `ConfirmationDialog` pattern:
- Title: "Empty Trash?"
- Message: "This will permanently delete X items. This cannot be undone."
- Buttons: "Empty Trash" / "Cancel"

### 5.2 Restore Conflict Dialog

If original location has a file with same name:
- Title: "File Already Exists"
- Message: "A file with this name exists at the original location."
- Options: "Replace", "Keep Both" (adds suffix), "Cancel"

---

## Phase 6: Auto-Cleanup

### 6.1 Implement in TrashManager

```java
public static void performAutoCleanup() {
    // Called on mod init and when trash tab is opened
    
    // 1. Remove items older than retention days
    if (Config.TRASH_RETENTION_DAYS.get() > 0) {
        removeItemsOlderThan(Config.TRASH_RETENTION_DAYS.get());
    }
    
    // 2. Remove excess items beyond max limit
    if (Config.MAX_TRASH_ITEMS.get() > 0) {
        trimToMaxItems(Config.MAX_TRASH_ITEMS.get());
    }
}
```

### 6.2 Cleanup Triggers

- On mod initialization (`BuildingGadgets2GUIClient.init()`)
- When Trash tab is opened
- When a new item is added to trash

---

## Implementation Order

| Step | Task | Files Modified/Created |
|------|------|------------------------|
| 1 | Create `TrashManager` with load/save/CRUD | `TrashManager.java` (new) |
| 2 | Add trash entry data structure | `TrashManager.java` |
| 3 | Update `SchematicManager.deleteFile()` to track original path | `SchematicManager.java` |
| 4 | Add trash config options | `Config.java` |
| 5 | Create `TrashList` widget | `TrashList.java` (new) |
| 6 | Add RESTORE and DELETE_PERM button types | `RowActionButtons.java` |
| 7 | Implement full `TrashTab` UI | `TrashTab.java` |
| 8 | Add trash settings to SettingsTab | `SettingsTab.java` |
| 9 | Implement restore conflict dialog | `ConfirmationDialog.java` or new dialog |
| 10 | Implement auto-cleanup logic | `TrashManager.java` |
| 11 | Initialize TrashManager on mod startup | `BuildingGadgets2GUIClient.java` |

---

## New Files to Create

1. `TrashManager.java` - Central trash management and metadata persistence
2. `TrashList.java` - Scrollable list widget for trash entries

## Files to Modify

1. `SchematicManager.java` - Update deleteFile() to track original path
2. `TrashTab.java` - Full implementation replacing placeholder
3. `RowActionButtons.java` - Add RESTORE and DELETE_PERM button types
4. `Config.java` - Add trash settings
5. `SettingsTab.java` - Add trash settings UI
6. `BuildingGadgets2GUIClient.java` - Initialize TrashManager

---

## Data Flow

### Delete Operation Flow
```
User clicks Delete on schematic
    ↓
SchematicManager.deleteFile(file)
    ↓
Calculate relative path from schematics root
    ↓
TrashManager.addToTrash(fileName, originalPath)
    ↓
Move file to trash folder
    ↓
Save trash.json metadata
    ↓
Trigger auto-cleanup if needed
```

### Restore Operation Flow
```
User clicks Restore in Trash tab
    ↓
TrashManager.restoreFile(trashEntry)
    ↓
Check if original path exists, create folders if needed
    ↓
Check for filename conflict
    ↓
[If conflict] Show conflict dialog
    ↓
Move file back to original location
    ↓
Remove entry from trash.json
```

---

## Edge Cases to Handle

1. **Original folder deleted**: Create folder when restoring
2. **File with same name exists**: Show conflict dialog with options
3. **Trash folder manually deleted**: Gracefully handle missing files
4. **Large number of items**: Ensure list scrolling is performant
5. **Trash metadata out of sync**: Validate entries exist on load
6. **Multiple files with same name in trash**: Already handled by counter suffix in `deleteFile()`

---

## Future Enhancements (Not in scope)

- Search/filter within trash
- Bulk restore/delete selection
- Preview schematic before restore
- Undo delete (quick restore of last deleted item)
