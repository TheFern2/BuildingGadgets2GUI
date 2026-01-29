package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.client.ClipboardUtils;
import dev.thefern.buildinggadgets2gui.client.TEDataClientCache;
import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.CreateFolderDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.SaveSchematicDialog;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFile;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFolder;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicManager;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicsList;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SchematicsTab extends TabPanel {
    
    private static final int LIST_WIDTH = 240;
    private static final int LIST_HEIGHT = 120;
    private static final int INFO_PANEL_WIDTH = 140;
    private static final int PADDING = 10;
    private static final int CLIPBOARD_ROW_HEIGHT = 22;
    
    private ClipboardUtils.CopyData copyData = new ClipboardUtils.CopyData();
    private SchematicsList schematicsList;
    private SchematicFile selectedFile = null;
    private Button rootButton;
    private Button upButton;
    private Button createFolderButton;
    private Button copyFromToolButton;
    private Button saveButton;
    private Button deleteButton;
    private Button sendToToolButton;
    private Button clearClipboardButton;
    
    private String clipboardTimestamp = null;
    private int clipboardHighlightTicks = 0;
    private static final int HIGHLIGHT_DURATION = 40;
    
    public SchematicsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        copyData = ClipboardUtils.checkCopyData();
        
        int buttonY = y + 5;
        
        copyFromToolButton = Button.builder(
            Component.literal("Copy from Tool"),
            button -> onCopyFromToolPressed()
        )
        .bounds(x + PADDING, buttonY, 120, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Copy blocks and TileEntity data from BG2 Copy/Paste tool")
        ))
        .build();
        copyFromToolButton.active = copyData.hasCopyData;
        widgets.add(copyFromToolButton);
        
        saveButton = Button.builder(
            Component.literal("Save Schematic"),
            button -> onSavePressed()
        )
        .bounds(x + PADDING + 215, buttonY, 120, 20)
        .build();
        widgets.add(saveButton);
        updateSaveButtonText();
        
        int clipboardRowY = buttonY + 25;
        
        clearClipboardButton = Button.builder(
            Component.literal("Clear"),
            button -> onClearClipboardPressed()
        )
        .bounds(x + PADDING + 290, clipboardRowY, 45, 18)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Clear clipboard data")
        ))
        .build();
        widgets.add(clearClipboardButton);
        updateClearButtonState();
        
        int navButtonY = clipboardRowY + CLIPBOARD_ROW_HEIGHT + 3;
        int navButtonWidth = 38;
        
        rootButton = Button.builder(
            Component.literal("/"),
            button -> onRootPressed()
        )
        .bounds(x + PADDING, navButtonY, navButtonWidth, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Root folder")
        ))
        .build();
        widgets.add(rootButton);
        
        upButton = Button.builder(
            Component.literal("↑"),
            button -> onBackPressed()
        )
        .bounds(x + PADDING + navButtonWidth + 2, navButtonY, navButtonWidth, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Back")
        ))
        .build();
        upButton.active = false;
        widgets.add(upButton);
        
        createFolderButton = Button.builder(
            Component.literal("+"),
            button -> onCreateFolderPressed()
        )
        .bounds(x + PADDING + (navButtonWidth + 2) * 2, navButtonY, navButtonWidth, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Create new folder")
        ))
        .build();
        widgets.add(createFolderButton);
        
        int listY = navButtonY + 25;
        schematicsList = new SchematicsList(
            Minecraft.getInstance(),
            LIST_WIDTH,
            LIST_HEIGHT,
            listY,
            24,
            this
        );
        schematicsList.setX(x + PADDING);
        
        int sendButtonWidth = 120;
        int sendButtonX = x + width - sendButtonWidth - PADDING;
        int sendButtonY = y + height - 30;
        
        sendToToolButton = Button.builder(
            Component.literal("Send to Tool"),
            button -> onSendToToolPressed()
        )
        .bounds(sendButtonX, sendButtonY, sendButtonWidth, 20)
        .build();
        sendToToolButton.active = false;
        widgets.add(sendToToolButton);
    }
    
    private void onCopyFromToolPressed() {
        ClipboardUtils.copyFromTool();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        clipboardTimestamp = sdf.format(new Date());
        clipboardHighlightTicks = HIGHLIGHT_DURATION;
        updateSaveButtonText();
        updateClearButtonState();
    }
    
    private void onClearClipboardPressed() {
        ClipboardUtils.clearClipboard();
        clipboardTimestamp = null;
        updateSaveButtonText();
        updateClearButtonState();
    }
    
    private void updateSaveButtonText() {
        if (saveButton == null) return;
        
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        if (clipboardBlocks != null && !clipboardBlocks.isEmpty()) {
            saveButton.setMessage(Component.literal("Save Schematic (" + clipboardBlocks.size() + ")"));
            saveButton.active = true;
        } else {
            saveButton.setMessage(Component.literal("Save Schematic"));
            saveButton.active = false;
        }
    }
    
    private void updateClearButtonState() {
        if (clearClipboardButton == null) return;
        
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        clearClipboardButton.active = clipboardBlocks != null && !clipboardBlocks.isEmpty();
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        if (schematicsList != null) {
            schematicsList.refreshList();
            updateNavigationButtons();
            updateActionButtons();
        }
        updateSaveButtonText();
        updateClearButtonState();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        renderClipboardInfo(guiGraphics);
        
        int buttonY = y + 5;
        int clipboardRowY = buttonY + 25;
        int navButtonY = clipboardRowY + CLIPBOARD_ROW_HEIGHT + 3;
        int listY = navButtonY + 25;
        int listX = x + PADDING;
        
        guiGraphics.fill(listX, listY, listX + LIST_WIDTH, listY + LIST_HEIGHT, 0xFF202020);
        
        if (schematicsList != null) {
            schematicsList.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        renderInfoPanel(guiGraphics, listY);
        
        String currentPath = "Path: " + SchematicManager.getCurrentPath();
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            currentPath,
            x + PADDING,
            y + height - 15,
            0xAAAAAA,
            false
        );
    }
    
    private void renderClipboardInfo(GuiGraphics guiGraphics) {
        int buttonY = y + 5;
        int clipboardRowY = buttonY + 25;
        int rowX = x + PADDING;
        int rowWidth = 330;
        
        int bgColor = 0xFF1A1A1A;
        if (clipboardHighlightTicks > 0) {
            float alpha = (float) clipboardHighlightTicks / HIGHLIGHT_DURATION;
            int green = (int) (alpha * 80);
            bgColor = 0xFF000000 | (green << 8) | 0x1A1A;
        }
        guiGraphics.fill(rowX, clipboardRowY, rowX + rowWidth, clipboardRowY + CLIPBOARD_ROW_HEIGHT, bgColor);
        
        guiGraphics.fill(rowX, clipboardRowY, rowX + 1, clipboardRowY + CLIPBOARD_ROW_HEIGHT, 0xFF444444);
        guiGraphics.fill(rowX + rowWidth - 1, clipboardRowY, rowX + rowWidth, clipboardRowY + CLIPBOARD_ROW_HEIGHT, 0xFF444444);
        guiGraphics.fill(rowX, clipboardRowY, rowX + rowWidth, clipboardRowY + 1, 0xFF444444);
        guiGraphics.fill(rowX, clipboardRowY + CLIPBOARD_ROW_HEIGHT - 1, rowX + rowWidth, clipboardRowY + CLIPBOARD_ROW_HEIGHT, 0xFF444444);
        
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        int textY = clipboardRowY + 6;
        
        if (clipboardBlocks != null && !clipboardBlocks.isEmpty()) {
            String dimensions = calculateDimensions(clipboardBlocks);
            String clipboardText = "Clipboard: " + clipboardBlocks.size() + " blocks";
            if (dimensions != null) {
                clipboardText += " (" + dimensions + ")";
            }
            
            int textColor = clipboardHighlightTicks > 0 ? 0x55FF55 : 0x55FF55;
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                clipboardText,
                rowX + 5,
                textY,
                textColor,
                false
            );
            
            if (clipboardTimestamp != null) {
                String timeText = "@ " + clipboardTimestamp;
                int timeX = rowX + 200;
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    timeText,
                    timeX,
                    textY,
                    0xAAAAAA,
                    false
                );
            }
        } else {
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                "Clipboard: Empty",
                rowX + 5,
                textY,
                0x888888,
                false
            );
        }
    }
    
    private String calculateDimensions(ArrayList<StatePos> blocks) {
        if (blocks == null || blocks.isEmpty()) return null;
        
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (StatePos statePos : blocks) {
            BlockPos pos = statePos.pos;
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        
        return sizeX + " x " + sizeY + " x " + sizeZ;
    }
    
    private void renderInfoPanel(GuiGraphics guiGraphics, int listY) {
        int infoX = x + PADDING + LIST_WIDTH + PADDING;
        int infoY = listY;
        
        guiGraphics.fill(infoX, infoY, infoX + INFO_PANEL_WIDTH, infoY + LIST_HEIGHT, 0xFF2A2A2A);
        
        if (selectedFile != null) {
            int textY = infoY + 10;
            int textX = infoX + 5;
            
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                "Schematic Info",
                textX,
                textY,
                0xFFFFFF,
                false
            );
            textY += 15;
            
            SchematicFile.SchematicMetadata metadata = selectedFile.getMetadata();
            if (metadata != null) {
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    "Name:",
                    textX,
                    textY,
                    0xAAAAAA,
                    false
                );
                textY += 10;
                
                String name = metadata.name;
                if (name.length() > 18) {
                    name = name.substring(0, 15) + "...";
                }
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    name,
                    textX,
                    textY,
                    0xFFFFFF,
                    false
                );
                textY += 15;
                
                SchematicFile.SchematicData data = selectedFile.loadData();
                if (data != null) {
                    guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        "Blocks: " + data.blockCount,
                        textX,
                        textY,
                        0xAAAAAA,
                        false
                    );
                    textY += 12;
                    
                    if (data.dimensions != null) {
                        guiGraphics.drawString(
                            Minecraft.getInstance().font,
                            "Size: " + data.dimensions.toString(),
                            textX,
                            textY,
                            0xAAAAAA,
                            false
                        );
                        textY += 12;
                    }
                }
                
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
                String dateStr = sdf.format(new Date(metadata.created));
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    "Created:",
                    textX,
                    textY,
                    0xAAAAAA,
                    false
                );
                textY += 10;
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    dateStr,
                    textX,
                    textY,
                    0xFFFFFF,
                    false
                );
            }
        } else {
            int textY = infoY + LIST_HEIGHT / 2;
            String text = "Select a file";
            int textWidth = Minecraft.getInstance().font.width(text);
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                text,
                infoX + (INFO_PANEL_WIDTH - textWidth) / 2,
                textY,
                0x888888,
                false
            );
        }
    }
    
    public void onFolderSelected(SchematicFolder folder) {
        selectedFile = null;
        updateActionButtons();
    }
    
    public void onFileSelected(SchematicFile file) {
        selectedFile = file;
        updateActionButtons();
        System.out.println("Selected file: " + file.getName());
    }
    
    public void onNavigate() {
        selectedFile = null;
        updateNavigationButtons();
        updateActionButtons();
    }
    
    private void onRootPressed() {
        SchematicManager.navigateToRoot();
        schematicsList.refreshList();
        onNavigate();
    }
    
    private void onBackPressed() {
        SchematicManager.navigateUp();
        schematicsList.refreshList();
        onNavigate();
    }
    
    private void onCreateFolderPressed() {
        CreateFolderDialog dialog = new CreateFolderDialog(
            parentScreen,
            folderName -> onCreateFolderConfirmed(folderName),
            () -> System.out.println("Folder creation cancelled")
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void onCreateFolderConfirmed(String folderName) {
        boolean success = SchematicManager.createFolder(folderName);
        if (success) {
            System.out.println("Created folder: " + folderName);
            if (schematicsList != null) {
                schematicsList.refreshList();
            }
        } else {
            System.err.println("Failed to create folder: " + folderName);
        }
    }
    
    private void onSavePressed() {
        ArrayList<StatePos> clipboardBlocks = HistoryTab.getClipboardBlocks();
        ArrayList<TagPos> clipboardTEData = HistoryTab.getClipboardTEData();
        UUID clipboardCopyUUID = HistoryTab.getClipboardCopyUUID();
        
        if (clipboardBlocks == null || clipboardBlocks.isEmpty()) {
            System.out.println("Clipboard is empty! Use 'Copy from Tool' first.");
            return;
        }
        
        SaveSchematicDialog dialog = new SaveSchematicDialog(
            parentScreen,
            result -> onSaveConfirmed(result, clipboardBlocks, clipboardTEData, clipboardCopyUUID),
            v -> System.out.println("Save cancelled")
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void onSaveConfirmed(SaveSchematicDialog.SaveResult result, ArrayList<StatePos> blocks, ArrayList<TagPos> teData, UUID copyUUID) {
        File file = SchematicManager.createSchematicFile(result.name);
        String author = Minecraft.getInstance().getUser().getName();
        
        boolean success = SchematicFile.saveSchematic(
            file,
            result.name,
            result.description,
            result.tags,
            blocks,
            teData,
            copyUUID,
            author
        );
        
        if (success) {
            System.out.println("Schematic saved: " + file.getName() + " (TileEntities: " + (teData != null ? teData.size() : 0) + ")");
            schematicsList.refreshList();
        } else {
            System.err.println("Failed to save schematic!");
        }
    }
    
    private void onDeletePressed() {
        if (selectedFile == null) return;
        
        String fileName = selectedFile.getName();
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Delete Schematic",
            "Delete " + fileName + "?",
            confirmed -> {
                if (confirmed) {
                    boolean success = SchematicManager.deleteFile(selectedFile);
                    if (success) {
                        System.out.println("Deleted: " + fileName);
                        selectedFile = null;
                        schematicsList.refreshList();
                        updateActionButtons();
                    }
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void onSendToToolPressed() {
        if (selectedFile == null) return;
        
        ConfirmationDialog dialog = new ConfirmationDialog(
            parentScreen,
            "Send to Tool",
            "This will override current tool copy data. Continue?",
            confirmed -> {
                if (confirmed) {
                    SchematicFile.SchematicData data = selectedFile.loadData();
                    if (data != null && data.blocks != null) {
                        HistoryTab.setClipboard(data.blocks, 
                            data.teData,
                            data.copyUUID != null ? UUID.fromString(data.copyUUID) : null, 
                            data.blockCount);
                        ClipboardUtils.sendToTool();
                    }
                }
            }
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void updateNavigationButtons() {
        if (rootButton != null) {
            rootButton.active = !SchematicManager.isAtRoot();
        }
        if (upButton != null) {
            upButton.active = !SchematicManager.isAtRoot();
        }
    }
    
    private void updateActionButtons() {
        boolean hasSelection = selectedFile != null;
        if (deleteButton != null) {
            deleteButton.active = hasSelection;
        }
        if (sendToToolButton != null) {
            sendToToolButton.active = hasSelection;
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (schematicsList != null && isActive) {
            return schematicsList.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (schematicsList != null && isActive) {
            return schematicsList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }
    
    @Override
    public void tick() {
        copyData = ClipboardUtils.checkCopyData();
        if (copyFromToolButton != null) {
            copyFromToolButton.active = copyData.hasCopyData;
        }
        
        if (copyData.hasCopyData && copyData.gadgetUUID != null) {
            if (!TEDataClientCache.hasTEData(copyData.gadgetUUID) && 
                !TEDataClientCache.isPendingRequest(copyData.gadgetUUID)) {
                ClipboardUtils.requestTEDataFromServer();
            }
        }
        
        if (clipboardHighlightTicks > 0) {
            clipboardHighlightTicks--;
        }
        
        updateSaveButtonText();
        updateClearButtonState();
    }
}

