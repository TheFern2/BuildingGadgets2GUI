## UI Cleanup

- Clipboard needs to be cleaner - done
- A button to see materials - done
- History needs consistency like schematic for delete - done
- Material list an icon for the blocks - done
- Send to tool from clipboard and history rows, and a > and tooltip - done
- A how to screen, or best way for instructions

---

# Clipboard Visibility Improvements

## Problem

When users click "Copy from Tool", the data goes to an internal clipboard but there's **no visual feedback**. Users don't know:

1. That the copy was successful
2. What data is currently in the clipboard
3. That they can now save it as a schematic

## Proposed Solutions

### Option 1: Clipboard Info Panel (Recommended)

Add a dedicated "Clipboard" section between the top buttons and the file list that shows the current clipboard status:

```
┌─────────────────────────────────────────────────────────────┐
│ [Copy from Tool]                         [Save Schematic]   │
├─────────────────────────────────────────────────────────────┤
│  📋 Clipboard: 20 blocks (5x4x1)  [Clear]                   │
│  └─ Copied at 14:32:05                                      │
├─────────────────────────────────────────────────────────────┤
│ [/] [↑] [+]                                                 │
│ ┌─────────────────────┐  ┌──────────────────┐               │
│ │ House test 1        │  │ Schematic Info   │               │
│ │ wall               ←│  │ Name: wall       │               │
│ │                      │  │ Blocks: 20       │               │
│ └─────────────────────┘  └──────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

**Features:**

- Shows block count and dimensions when clipboard has data
- Shows "Empty" or hidden when clipboard is empty
- Shows timestamp of when data was copied
- Optional "Clear" button to empty clipboard
- Visual indicator (highlight/glow) briefly when copy occurs

### Option 2: Toast/Notification

Show a temporary notification when copy succeeds:

- "Copied 20 blocks to clipboard!"
- Fades after 2-3 seconds
- Less intrusive but also less persistent

### Option 3: Button State Change

Change the "Save Schematic" button to show clipboard info:

- When empty: "Save Schematic" (grayed out)
- When has data: "Save Schematic (20 blocks)"
- Button could briefly flash/highlight when data is added

### Option 4: Combined Approach (Best UX)

1. Add clipboard info panel (Option 1) for persistent visibility
2. Add brief animation/highlight when copy succeeds
3. Update "Save Schematic" button to show it's active: "Save (20 blocks)"

## Recommendation

Go with **Option 4** - the combined approach:

1. **Add Clipboard Info Row** - A single row between buttons and nav, showing:
  - "Clipboard: Empty" when no data
  - "Clipboard: {count} blocks ({dimensions})" when has data
  - Timestamp of last copy
2. **Brief Visual Feedback** - Flash/highlight the clipboard row when copy happens
3. **Update Save Button** - Show "Save ({count})" when clipboard has data

## Implementation Details

### Files to Modify

1. `SchematicsTab.java`:
  - Add clipboard info rendering
  - Track clipboard state
  - Add visual feedback on copy
2. `ClipboardUtils.java`:
  - Return success/data info from `copyFromTool()` for feedback

### UI Layout Adjustments

Current layout needs to shift down to accommodate clipboard info:

- Button row: y + 5
- **NEW: Clipboard info row: y + 30** (single line of text)
- Navigation buttons: y + 50 (was y + 25)
- List: y + 80 (was y + 55)

### Clipboard Info Content

When clipboard is empty:

```
Clipboard: Empty - Use "Copy from Tool" to copy block data
```

When clipboard has data:

```
Clipboard: 20 blocks (5 x 4 x 1) | Copied at 14:32:05
```

## Tasks

- Add clipboard status tracking to SchematicsTab
- Render clipboard info row below buttons
- Shift navigation buttons and list down
- Add brief highlight animation on successful copy
- Update Save button text to show block count
- Add "Empty - Copy from Tool" helper text when clipboard is empty

