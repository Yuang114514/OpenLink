package fun.moystudio.openlink.fabriclike;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.Minecraft;

public final class OpenLinkFabricLike {
    public static void init() throws Exception {
        // Run our common setup.
        OpenLink.init();
        ClientCommandManager.DISPATCHER.register(ClientCommandManager
                        .literal("proxyrestart")
                        .executes(context -> Frpc.openFrp(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }
}