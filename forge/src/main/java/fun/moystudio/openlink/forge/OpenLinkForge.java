package fun.moystudio.openlink.forge;

import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.fml.common.Mod;

import fun.moystudio.openlink.OpenLink;

@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() throws Exception {
        // Run our common setup.
        OpenLink.init();
    }
}
