package dev.thefern.buildinggadgets2gui.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = dev.thefern.buildinggadgets2gui.BuildingGadgets2GUI.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
    
    public static final String CATEGORY = "key.categories.buildinggadgets2gui";
    
    public static final KeyMapping OPEN_GUI = new KeyMapping(
        "key.buildinggadgets2gui.open_gui",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        CATEGORY
    );
    
    public static final KeyMapping COPY_DATA = new KeyMapping(
        "key.buildinggadgets2gui.copy_data",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        CATEGORY
    );
    
    public static final KeyMapping PASTE_DATA = new KeyMapping(
        "key.buildinggadgets2gui.paste_data",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY
    );
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI);
        event.register(COPY_DATA);
        event.register(PASTE_DATA);
    }
}

