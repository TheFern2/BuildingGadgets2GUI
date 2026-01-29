# Help Tab Implementation Plan

## Overview

Add a Help tab (with a "?" icon) after the Settings tab to teach users how to use the BuildingGadgets2 GUI mod.

## Tab Button Implementation

- Position: After Settings tab (⚙), before Debug tab (🐛) if enabled
- Icon: `?` (question mark)
- Tooltip: "Help"
- Width: `ICON_TAB_WIDTH` (30px) - same as other icon tabs

## Help Tab Content Structure

The help screen should display scrollable help content covering all features of the mod.

### Help Sections

#### 1. Getting Started

- **Opening the GUI**: Press the keybind (default: ` backtick/grave accent) while holding a BuildingGadgets2 Copy/Paste gadget
- **Tabs Overview**: Brief description of each tab (Schematics, History, Trash, Settings, Help)

#### 2. Schematics Tab

- **Copy from Tool**: Copies the current block selection from your BG2 Copy/Paste gadget into the GUI clipboard
- **Save Schematic**: Saves the clipboard contents to a file with name, description, and tags
- **Clipboard Row**: Shows current clipboard contents (block count, dimensions, timestamp)
  - Material List (📋): View all blocks/materials needed
  - Send to Tool (🔧): Transfer clipboard data back to the gadget
  - Clear: Empty the clipboard
- **File Browser**:
  - `/` (Root): Navigate to root schematics folder
  - `↑` (Up): Navigate to parent folder
  - `+` (New Folder): Create a new folder
- **Schematic Files**: Click to select, double-click folders to enter
- **Info Panel**: Shows selected schematic details (name, block count, size, date)

#### 3. History Tab

- **Purpose**: Automatically saves all copy operations for later use
- **Entry Actions**:
  - `X`: Delete the history entry
  - 📋 (Material): View material list for the entry
  - 📄 (Clipboard): Send entry to clipboard
  - 🔧 (Tool): Send entry directly to the gadget
- **Scrolling**: Mouse wheel to scroll through history
- **Clear All History**: Remove all history entries

#### 4. Trash Tab

- **Purpose**: Holds deleted schematics for recovery
- **Restore**: Recover deleted schematics back to original location
- **Empty Trash**: Permanently delete all trashed items

#### 5. Settings Tab

- **Max History Entries**: Adjust the maximum number of history entries to keep (use +/- buttons)

### Workflow Tips Section

#### Basic Workflow

1. Use BG2 Copy/Paste gadget to copy blocks in-game
2. Open GUI with keybind
3. Click "Copy from Tool" to import the selection
4. Click "Save Schematic" to save with a name
5. Later: Select schematic, click "Send to Tool" to load it back

#### Quick Tips

- Hold the gadget when opening the GUI to enable "Copy from Tool"
- Use folders to organize schematics by project/category
- History automatically saves copies - useful for undo/versioning
- Material list helps plan resource gathering before building

## UI Layout

```
┌─────────────────────────────────────────────┐
│ Help                                        │
├─────────────────────────────────────────────┤
│                                             │
│  [Scrollable Content Area]                  │
│                                             │
│  Section headers in white/yellow            │
│  Body text in light gray                    │
│  Keybinds/buttons in highlighted color      │
│                                             │
│                      ▲                      │
│                      │                      │
│                      ▼                      │
│                                             │
└─────────────────────────────────────────────┘
```

## Implementation Tasks

1. **Create HelpTab.java** in `client/tabs/`
  - Extend `TabPanel`
  - Implement scrollable text rendering
  - Define help sections as structured data
2. **Update TabbedCopyPasteScreen.java**
  - Add `HELP` to `TabType` enum
  - Add `helpTab` field and `helpButton` field
  - Create help button after settings button
  - Add help tab initialization and widget registration
  - Update `switchTab()` and `getCurrentTabPanel()` methods
  - Update `renderTabButtons()` for help tab highlighting
3. **Help Content Rendering**
  - Section headers: Bold/colored (0xFFFF00 yellow or 0xFFFFFF white)
  - Body text: Light gray (0xAAAAAA)
  - Key highlights: Green (0x55FF55) or cyan (0x55FFFF)
  - Scroll support with mouse wheel
  - Max scroll limit based on content height
4. **Optional Enhancements**
  - Clickable links to jump between sections
  - Mini icons next to feature descriptions
  - "First time" detection to auto-show help tab

## Files to Modify

- `src/main/java/dev/thefern/buildinggadgets2gui/client/TabbedCopyPasteScreen.java`
- `src/main/resources/assets/buildinggadgets2gui/lang/en_us.json` (optional tooltips)

## Files to Create

- `src/main/java/dev/thefern/buildinggadgets2gui/client/tabs/HelpTab.java`

