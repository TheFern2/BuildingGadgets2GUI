package dev.thefern.buildinggadgets2gui;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(BuildingGadgets2GUI.MODID)
public class BuildingGadgets2GUI {
    public static final String MODID = "buildinggadgets2gui";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuildingGadgets2GUI(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerNetworking);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    
    private void registerNetworking(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar(MODID);
        
        registrar.playToServer(
            dev.thefern.buildinggadgets2gui.network.SendClipboardToGadgetPayload.TYPE,
            dev.thefern.buildinggadgets2gui.network.SendClipboardToGadgetPayload.STREAM_CODEC,
            dev.thefern.buildinggadgets2gui.network.PacketSendClipboardToGadget.get()::handle
        );
        
        registrar.playToServer(
            dev.thefern.buildinggadgets2gui.network.RequestTEDataPayload.TYPE,
            dev.thefern.buildinggadgets2gui.network.RequestTEDataPayload.STREAM_CODEC,
            dev.thefern.buildinggadgets2gui.network.PacketRequestTEData.get()::handle
        );
        
        registrar.playToClient(
            dev.thefern.buildinggadgets2gui.network.SendTEDataPayload.TYPE,
            dev.thefern.buildinggadgets2gui.network.SendTEDataPayload.STREAM_CODEC,
            dev.thefern.buildinggadgets2gui.network.PacketSendTEData.get()::handle
        );
    }
}
