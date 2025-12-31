package dev.thefern.buildinggadgets2gui.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI.MODID, value = Dist.CLIENT)
public class ClientKeyHandler {
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (KeyBindings.OPEN_GUI.consumeClick()) {
            mc.setScreen(new TabbedCopyPasteScreen());
        }
        
        if (KeyBindings.COPY_DATA.consumeClick()) {
            System.out.println("==============================================");
            System.out.println("Copy Data keybind pressed!");
            System.out.println("==============================================");
        }
        
        if (KeyBindings.PASTE_DATA.consumeClick()) {
            System.out.println("==============================================");
            System.out.println("Paste Data keybind pressed!");
            System.out.println("==============================================");
        }
    }
}

