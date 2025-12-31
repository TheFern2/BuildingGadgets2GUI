# Schematic Browser Implementation Plan

## Overview
Create a file browser-style interface for managing schematics, inspired by Litematica's layout. Users can navigate folders, create/save schematics, and manage them with tags and filtering.

## UI Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ [Copy from Tool]                                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────────────────────────┐  ┌──────────────────────┐  │
│  │ 📁 Folder Navigation          │  │ Schematic Info       │  │
│  │                               │  │                      │  │
│  │ 📁 schematics (root)          │  │ When a schematic is  │  │
│  │   📁 sync                     │  │ selected, show:      │  │
│  │   📄 1.16_S30_AFK_World...    │  │                      │  │
│  │   📄 1.17ResearchLab          │  │ - Name               │  │
│  │   📄 10_Chest_Hall...         │  │ - Block count        │  │
│  │   📄 120MultiItemSorter...    │  │ - Dimensions         │  │
│  │   📁 142Lifeboat              │  │ - Date created       │  │
│  │   📁 14dfurnacearraymodule    │  │ - Tags               │  │
│  │   📄 16segmentDisplay         │  │ - Description        │  │
│  │   📄 1728_Verylaggy           │  │                      │  │
│  │   ...                         │  │ [🔖 Filter Icon]     │  │
│  │                               │  │ [Delete] [SD to Tool]│  │
│  └───────────────────────────────┘  └──────────────────────┘  │
│                                                                  │
│  [Save Schematic]  Current Path: /schematics/                   │
└─────────────────────────────────────────────────────────────────┘
```

## Core Features

### 1. File/Folder Navigation
- **Display Format**: Scrollable list with checkboxes (for multi-select in future)
- **Folders**: Show 📁 icon, clickable to navigate into
- **Files**: Show 📄 icon, clickable to select/view info
- **Current Path Display**: Show breadcrumb or path at bottom
- **Navigation**: 
  - Click folder to enter
  - Need "Up/Back" button or breadcrumb navigation

### 2. File Operations
- **Copy from Tool**: Existing button - saves current tool data to selected folder
- **Save Schematic**: Save dialog with:
  - Name input
  - Optional description
  - Optional tags
  - Saves to currently selected folder
- **Delete**: 
  - Shows confirmation popup: "Delete [name]? Yes/No"
  - Moves to trash (soft delete) for recovery
- **Send to Tool (SD to Tool)**:
  - Shows confirmation popup: "This will override current tool copy data. Continue? Yes/No"
  - Loads schematic data to tool

### 3. Filtering System
- **Filter Icon** (🔖): Opens popup dialog
- **Tag Selection**: Multi-select checkboxes for tags
- **Filter applies to current folder view**
- **Clear filter option**

### 4. Schematic Info Panel
When a schematic is selected, display:
- **Name**: Full schematic name
- **Block Count**: Number of blocks
- **Dimensions**: X x Y x Z size
- **Date Created**: Timestamp
- **Tags**: List of tags (clickable to add to filter?)
- **Description**: User-provided description (optional)
- **Preview**: (Future) Small 3D preview or thumbnail

## Data Structure

### Schematic File Format (.bg2schem)
```json
{
  "version": 1,
  "metadata": {
    "name": "MySchematic",
    "description": "Optional description",
    "created": 1234567890,
    "modified": 1234567890,
    "author": "PlayerName",
    "tags": ["industrial", "compact", "automatic"]
  },
  "dimensions": {
    "x": 10,
    "y": 5,
    "z": 8
  },
  "blockCount": 234,
  "copyUUID": "uuid-string",
  "blocks": [
    // StatePos data (existing format)
  ]
}
```

### Directory Structure
```
config/
  buildinggadgets2gui/
    schematics/           # Root schematics folder
      *.bg2schem         # Schematic files
      subfolder/         # User-created folders
        *.bg2schem
    trash/                # Deleted schematics (soft delete)
      *.bg2schem
    tags.json             # Global tag list
```

## Implementation Phases

### Phase 1: Basic File Browser (START HERE)
- [ ] Create `SchematicFile` class to represent file metadata
- [ ] Create `SchematicFolder` class to represent folders
- [ ] Create `SchematicsList` component (extends `ObjectSelectionList`)
- [ ] Implement folder/file listing from disk
- [ ] Implement folder navigation (enter folder, go back)
- [ ] Display current path
- [ ] Select file to show in info panel

### Phase 2: Info Panel & Basic Operations
- [ ] Create info panel component
- [ ] Display selected schematic metadata
- [ ] Implement "Save Schematic" dialog
- [ ] Save clipboard data as .bg2schem file
- [ ] Load .bg2schem file and send to tool

### Phase 3: File Operations
- [ ] Implement delete with confirmation popup
- [ ] Implement "Send to Tool" with confirmation popup
- [ ] Create folder functionality
- [ ] Rename file/folder functionality
- [ ] Move files between folders (drag-drop or cut/paste)

### Phase 4: Tag System
- [ ] Create tag management system
- [ ] Add tags to schematics
- [ ] Filter icon button and popup
- [ ] Tag selection in filter dialog
- [ ] Apply filter to list view

### Phase 5: Polish & Advanced Features
- [ ] Sorting options (name, date, size, etc.)
- [ ] Search functionality
- [ ] Thumbnail generation (optional)
- [ ] Import/Export schematics
- [ ] Schematic templates/favorites

## Technical Components

### New Classes Needed

1. **SchematicFile.java**
   - Represents a schematic file
   - Handles loading/saving metadata
   - Manages file I/O

2. **SchematicFolder.java**
   - Represents a folder
   - Lists contents (files and subfolders)
   - Handles navigation

3. **SchematicsList.java** (extends ObjectSelectionList)
   - Scrollable list widget
   - Displays folders and files
   - Handles selection and interaction

4. **SchematicInfoPanel.java**
   - Right-side info display
   - Shows selected schematic details
   - Contains action buttons

5. **SchematicManager.java**
   - Central manager for schematics
   - File operations (CRUD)
   - Tag management
   - Filter logic

6. **ConfirmationDialog.java**
   - Reusable popup for confirmations
   - Yes/No buttons
   - Custom message

7. **FilterDialog.java**
   - Tag selection popup
   - Multi-select checkboxes
   - Apply/Cancel buttons

8. **SaveSchematicDialog.java**
   - Name input
   - Description input (optional)
   - Tag selection
   - Save/Cancel buttons

## Notes

- Use plain files on disk for easy debugging and external editing
- File extension: `.bg2schem` (Building Gadgets 2 Schematic)
- Start simple with just file/folder display
- Add features incrementally
- Keep existing clipboard/history functionality intact
- Consider adding export to Litematica format in future

## Integration with Existing Code

- `SchematicsTab.java`: Replace with new schematic browser layout
- `ClipboardUtils.copyFromTool()`: Save to selected folder instead of just clipboard
- `ClipboardUtils.sendToTool()`: Load from selected file
- `HistoryTab.java`: Could optionally save history entries as auto-saved schematics

