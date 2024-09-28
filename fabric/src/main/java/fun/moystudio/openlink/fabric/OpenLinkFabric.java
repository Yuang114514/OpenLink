package fun.moystudio.openlink.fabric;

import net.fabricmc.api.ModInitializer;

import fun.moystudio.openlink.fabriclike.OpenLinkFabricLike;

import java.io.IOException;

public final class OpenLinkFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run the Fabric-like setup.
        try {
            OpenLinkFabricLike.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
