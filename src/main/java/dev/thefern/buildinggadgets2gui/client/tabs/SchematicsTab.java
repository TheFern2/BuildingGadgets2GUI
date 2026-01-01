package dev.thefern.buildinggadgets2gui.client.tabs;

import dev.thefern.buildinggadgets2gui.client.ClipboardUtils;
import dev.thefern.buildinggadgets2gui.client.dialogs.ConfirmationDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.CreateFolderDialog;
import dev.thefern.buildinggadgets2gui.client.dialogs.SaveSchematicDialog;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFile;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicFolder;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicManager;
import dev.thefern.buildinggadgets2gui.client.schematics.SchematicsList;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SchematicsTab extends TabPanel {
    
    private static final int LIST_WIDTH = 240;
    private static final int LIST_HEIGHT = 140;
    private static final int INFO_PANEL_WIDTH = 140;
    private static final int PADDING = 10;
    
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
    
    public SchematicsTab(Screen parentScreen, int x, int y, int width, int height) {
        super(parentScreen, x, y, width, height);
    }
    
    @Override
    public void init() {
        copyData = ClipboardUtils.checkCopyData();
        
        int buttonY = y + 5;
        
        copyFromToolButton = Button.builder(
            Component.literal("Copy from Tool"),
            button -> ClipboardUtils.copyFromTool()
        )
        .bounds(x + PADDING, buttonY, 120, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal("Copy blocks with the BG2 Copy and Paste tool")
        ))
        .build();
        copyFromToolButton.active = copyData.hasCopyData;
        widgets.add(copyFromToolButton);
        
        int navButtonY = buttonY + 25;
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
        
        saveButton = Button.builder(
            Component.literal("Save Schematic"),
            button -> onSavePressed()
        )
        .bounds(x + PADDING + 215, buttonY, 120, 20)
        .build();
        widgets.add(saveButton);
        
        int listY = y + 55;
        schematicsList = new SchematicsList(
            Minecraft.getInstance(),
            LIST_WIDTH,
            LIST_HEIGHT,
            listY,
            24,
            this
        );
        schematicsList.setX(x + PADDING);
        
        int buttonX = x + PADDING + LIST_WIDTH + PADDING + 5;
        int buttonWidth = INFO_PANEL_WIDTH - 10;
        int infoButtonY = y + 55 + LIST_HEIGHT + 10;
        
        deleteButton = Button.builder(
            Component.literal("Delete"),
            button -> onDeletePressed()
        )
        .bounds(buttonX, infoButtonY, buttonWidth, 20)
        .build();
        deleteButton.active = false;
        widgets.add(deleteButton);
        
        sendToToolButton = Button.builder(
            Component.literal("Send to Tool"),
            button -> onSendToToolPressed()
        )
        .bounds(buttonX, infoButtonY + 24, buttonWidth, 20)
        .build();
        sendToToolButton.active = false;
        widgets.add(sendToToolButton);
    }
    
    @Override
    public void onTabActivated() {
        super.onTabActivated();
        if (schematicsList != null) {
            schematicsList.refreshList();
            updateNavigationButtons();
            updateActionButtons();
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isActive) return;
        
        int listX = x + PADDING;
        int listY = y + 55;
        
        guiGraphics.fill(listX, listY, listX + LIST_WIDTH, listY + LIST_HEIGHT, 0xFF202020);
        
        if (schematicsList != null) {
            schematicsList.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        renderInfoPanel(guiGraphics);
        
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
    
    private void renderInfoPanel(GuiGraphics guiGraphics) {
        int infoX = x + PADDING + LIST_WIDTH + PADDING;
        int infoY = y + 55;
        
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
        UUID clipboardCopyUUID = HistoryTab.getClipboardCopyUUID();
        
        if (clipboardBlocks == null || clipboardBlocks.isEmpty()) {
            System.out.println("Clipboard is empty! Use 'Copy from Tool' first.");
            return;
        }
        
        SaveSchematicDialog dialog = new SaveSchematicDialog(
            parentScreen,
            result -> onSaveConfirmed(result, clipboardBlocks, clipboardCopyUUID),
            v -> System.out.println("Save cancelled")
        );
        Minecraft.getInstance().setScreen(dialog);
    }
    
    private void onSaveConfirmed(SaveSchematicDialog.SaveResult result, ArrayList<StatePos> blocks, UUID copyUUID) {
        File file = SchematicManager.createSchematicFile(result.name);
        String author = Minecraft.getInstance().getUser().getName();
        
        boolean success = SchematicFile.saveSchematic(
            file,
            result.name,
            result.description,
            result.tags,
            blocks,
            copyUUID,
            author
        );
        
        if (success) {
            System.out.println("Schematic saved: " + file.getName());
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
    }
}

