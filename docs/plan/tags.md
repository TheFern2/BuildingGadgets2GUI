## Tags Management Implementation Plan

### Overview

This plan covers implementing a tag management system that allows users to:
1. Create and manage global tags
2. Add/remove tags from schematics

> **Note**: Tag filtering will be implemented in a future iteration.

---

### Phase 1: Tag Storage & Management Infrastructure

#### 1.1 Create `TagManager.java`

A new singleton class to manage all tags across the application:

- **Location**: `src/main/java/dev/thefern/buildinggadgets2gui/client/schematics/TagManager.java`
- **Responsibilities**:
  - Load/save tags to a JSON file (`buildinggadgets2gui/tags.json`)
  - Maintain a global list of available tags
  - Provide CRUD operations: `addTag()`, `removeTag()`, `renameTag()`, `getAllTags()`

#### 1.2 Tag Data Structure

```java
public class TagInfo {
    public String name;
}
```

---

### Phase 2: Update SchematicFile for Tag Operations

#### 2.1 Add Tag Modification Methods to `SchematicFile.java`

Add methods to modify tags on existing schematics:

- `addTag(String tag)` - Add a tag and re-save the file
- `removeTag(String tag)` - Remove a tag and re-save the file
- `setTags(List<String> tags)` - Replace all tags
- `updateMetadata()` - Update metadata and save (preserving blocks/TE data)

This requires loading the full schematic data, modifying metadata, and re-saving.

---

### Phase 3: UI Components for Tag Management

#### 3.1 Create `ManageTagsDialog.java`

A dialog for managing the global tag list:

- **Location**: `src/main/java/dev/thefern/buildinggadgets2gui/client/dialogs/ManageTagsDialog.java`
- **Features**:
  - List all existing tags
  - Add new tag (text input + "Add" button)
  - Delete tag (with confirmation if tag is in use on schematics)
  - Rename tag (propagates to all schematics using it)

#### 3.2 Create `EditTagsDialog.java`

A dialog for editing tags on a specific schematic:

- **Location**: `src/main/java/dev/thefern/buildinggadgets2gui/client/dialogs/EditTagsDialog.java`
- **Features**:
  - Show current tags on the schematic (as removable chips/badges)
  - Show available tags (from TagManager) that can be added
  - Quick-add: type a new tag name to create and add in one step
  - Save/Cancel buttons

#### 3.3 Update `SaveSchematicDialog.java`

Enhance the existing save dialog:

- Add a "Tags" section below description
- Show available tags as clickable chips
- Allow typing new tag to create on-the-fly
- Selected tags displayed as removable chips

---

### Phase 4: Integrate Tags into SchematicsTab

#### 4.1 Add "Tags" Action Button to File Entries

In `SchematicsList.FileEntry`:

- Add a new `RowActionButtons.ButtonType.TAGS` (🏷️ icon)
- On click, open `EditTagsDialog` for that schematic

#### 4.2 Add "Manage Tags" Button to SchematicsTab

- Add a button near the navigation buttons (next to create folder)
- Opens `ManageTagsDialog`

#### 4.3 Update Info Panel

In `SchematicsTab.renderInfoPanel()`:

- Display tags for selected schematic

---

### Implementation Order

| Step | Task | Files Modified/Created |
|------|------|------------------------|
| 1 | Create `TagManager` with load/save/CRUD | `TagManager.java` (new) |
| 2 | Add tag modification methods to `SchematicFile` | `SchematicFile.java` |
| 3 | Update `SaveSchematicDialog` to include tag selection | `SaveSchematicDialog.java` |
| 4 | Create `EditTagsDialog` for editing schematic tags | `EditTagsDialog.java` (new) |
| 5 | Add Tags button type to `RowActionButtons` | `RowActionButtons.java` |
| 6 | Add tags button to file entries in list | `SchematicsList.java` |
| 7 | Create `ManageTagsDialog` for global tag management | `ManageTagsDialog.java` (new) |
| 8 | Add "Manage Tags" button to SchematicsTab | `SchematicsTab.java` |
| 9 | Update info panel to show tags | `SchematicsTab.java` |

---

### New Files to Create

1. `TagManager.java` - Central tag management
2. `EditTagsDialog.java` - Edit tags on a schematic
3. `ManageTagsDialog.java` - Manage global tag list

### Files to Modify

1. `SchematicFile.java` - Add tag modification methods
2. `SaveSchematicDialog.java` - Add tag selection UI
3. `RowActionButtons.java` - Add TAGS button type
4. `SchematicsList.java` - Add tags button to file entries
5. `SchematicsTab.java` - Add manage tags button, update info panel

---

### Data Persistence

**Tags stored in two places:**

1. **Global tags list**: `config/buildinggadgets2gui/tags.json`
```json
{
  "tags": ["house", "farm", "redstone", "storage", "decoration"]
}
```

2. **Per-schematic tags**: Already stored in `.bg2schem` files as comma-separated string in metadata (existing implementation)

---

### Future Enhancements (Not in scope)

- Tag filtering in SchematicsTab
- Tag colors for visual distinction
- Tag usage counting