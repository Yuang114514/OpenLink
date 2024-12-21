package fun.moystudio.openlink.fabriclike;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.Minecraft;

public final class OpenLinkFabricLike {
    public static void init(String version,String loader,String loader_version) throws Exception {
        // Run our common setup.
        OpenLink.init(version,loader,loader_version);
        ClientCommandManager.DISPATCHER.register(ClientCommandManager
                        .literal("proxyrestart")
                        .executes(context -> Frpc.openFrp(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }
}