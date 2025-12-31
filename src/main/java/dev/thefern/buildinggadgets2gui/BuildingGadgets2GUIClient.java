package dev.thefern.buildinggadgets2gui;

import dev.thefern.buildinggadgets2gui.client.HistoryManager;
import dev.thefern.buildinggadgets2gui.client.tabs.HistoryTab;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = BuildingGadgets2GUI.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = BuildingGadgets2GUI.MODID, value = Dist.CLIENT)
public class BuildingGadgets2GUIClient {
    public BuildingGadgets2GUIClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        BuildingGadgets2GUI.LOGGER.info("HELLO FROM CLIENT SETUP");
        BuildingGadgets2GUI.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        BuildingGadgets2GUI.LOGGER.info("Initializing CopyPasteDataMonitor for BuildingGadgets2 integration");
        
        event.enqueueWork(() -> {
            HistoryManager.init();
            HistoryTab.loadHistory();
            BuildingGadgets2GUI.LOGGER.info("History system initialized and loaded");
        });
    }
}
