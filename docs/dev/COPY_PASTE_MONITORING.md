# Copy/Paste Data Monitoring Implementation

## Overview

This implementation provides real-time monitoring of BuildingGadgets2 copy/paste gadget data changes. The system detects when players copy blocks, modify ranges, or clear data, and logs these events to the console.

## Implementation Components

### CopyPasteEventListener
**Location**: `src/main/java/dev/thefern/buildinggadgets2gui/CopyPasteEventListener.java`

A server-side event listener that:
- Monitors all players holding copy/paste gadgets every server tick
- Tracks copy data snapshots (copyUUID and block count) per gadget
- Detects event types: NEW_COPY, MODIFIED_COPY, RANGE_ADJUSTED, CLEARED
- Logs detailed information to console
- Provides foundation for future GUI integration

## How It Works

### Data Flow
1. Player uses copy/paste gadget (copies blocks or presses G to adjust range)
2. Server processes the action and updates BG2Data world storage
3. Every server tick, `CopyPasteEventListener` checks all players holding copy/paste gadgets
4. For each gadget, it compares current data (copyUUID and block count) with last known snapshot
5. If changes detected, event type is determined and logged to console
6. Snapshot is updated for next tick comparison

### Event Types

- **NEW_COPY**: First time copying blocks with a gadget (gadget now has data)
- **MODIFIED_COPY**: Copy UUID changed (new copy operation or G menu Confirm with different range)
- **RANGE_ADJUSTED**: Block count changed but same copy UUID (rare edge case)
- **CLEARED**: Block count went from >0 to 0 (Clear button pressed in G menu)

## Console Output Examples

```
[14:23:45] NEW_COPY detected | Player: YourName | Gadget UUID: a1b2c3d4... | Copy UUID: e5f6g7h8... | Blocks: 127
[14:24:12] MODIFIED_COPY detected | Player: YourName | Gadget UUID: a1b2c3d4... | Old Copy UUID: e5f6g7h8... | New Copy UUID: i9j0k1l2... | Blocks: 127 -> 256
[14:25:03] CLEARED detected | Player: YourName | Gadget UUID: a1b2c3d4... | Previous blocks: 256 | Now: 0
```

## API for GUI Integration

The `CopyPasteDataMonitor` class exposes these methods for your custom GUI screens:

### Listener Registration
```java
CopyPasteDataMonitor.registerListener(event -> {
    // React to copy data changes
    switch (event.eventType) {
        case NEW_COPY:
            // Enable GUI buttons, show preview, etc.
            break;
        case CLEARED:
            // Disable GUI buttons, hide preview, etc.
            break;
    }
});
```

### Data Queries
```java
// Check if gadget has copy data
boolean hasData = CopyPasteDataMonitor.hasCopyData(gadgetUUID);

// Get block count
int blockCount = CopyPasteDataMonitor.getBlockCount(gadgetUUID);

// Get current copy UUID
UUID copyUUID = CopyPasteDataMonitor.getCopyUUID(gadgetUUID);
```

## Testing

To test the implementation:

1. Build the mod: `./gradlew build`
2. Run the game: `./gradlew runClient`
3. In-game, get a copy/paste gadget from BuildingGadgets2
4. Copy some blocks (right-click start, shift+right-click end)
5. Press G to open the range adjustment GUI
6. Click Confirm or Clear
7. Check the console logs for monitoring output

## Future Enhancements

This monitoring system provides the foundation for:
- Real-time GUI updates when copy data changes
- Preview rendering of copied structures
- Block count displays in custom screens
- Enable/disable buttons based on data presence
- Automatic screen refreshes when data is modified

## Technical Notes

- The monitor uses `ConcurrentHashMap` for thread-safe tracking
- Mixin injection point is at TAIL to ensure data is updated before monitoring
- The system is client-side only (appropriate for GUI integration)
- No modifications to BuildingGadgets2 source code required
- Compatible with BuildingGadgets2 version 1.3.9+

