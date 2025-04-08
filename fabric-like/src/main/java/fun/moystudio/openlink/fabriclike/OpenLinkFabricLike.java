package fun.moystudio.openlink.fabriclike;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.OldFrpc;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.Supplier;

public final class OpenLinkFabricLike {
    public static void init(String version, String loader, String loader_version, Supplier<List<String>> getAllModPrefix) throws Exception {
        // Run our common setup.
        OpenLink.init(version,loader,loader_version,getAllModPrefix);
        ClientCommandManager.DISPATCHER.register(ClientCommandManager
                        .literal("proxyrestart")
                        .executes(context -> OldFrpc.openFrp(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight)->{
            EventCallbacks.onScreenInit(client,screen);
        });
        ServerWorldEvents.UNLOAD.register((server, world)->{
            EventCallbacks.onLevelClear();
        });
        ClientTickEvents.END_CLIENT_TICK.register(EventCallbacks::onClientTick);
    }
}