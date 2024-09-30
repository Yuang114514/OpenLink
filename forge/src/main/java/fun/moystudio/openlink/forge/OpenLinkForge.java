package fun.moystudio.openlink.forge;

import net.minecraftforge.fml.common.Mod;

import fun.moystudio.openlink.OpenLink;

import java.io.IOException;

@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() throws Exception {
        // Run our common setup.
        OpenLink.init();
    }
}
