package fun.moystudio.openlink.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import fun.moystudio.openlink.fabriclike.OpenLinkFabricLike;

import java.io.IOException;

public final class OpenLinkQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        // Run the Fabric-like setup.
        try {
            OpenLinkFabricLike.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
