package fun.moystudio.openlink.forge;

import fun.moystudio.openlink.OpenLink;
import net.minecraftforge.fml.common.Mod;

@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() {
        // Run our common setup.
        OpenLink.init();
    }
}
