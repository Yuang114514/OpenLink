package fun.moystudio.openlink.fabric;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.fabriclike.OpenLinkFabricLike;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.util.ArrayList;
import java.util.List;

public final class OpenLinkFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run the Fabric-like setup.
        try {
            OpenLinkFabricLike.init(FabricLoader.getInstance().getModContainer(OpenLink.MOD_ID).get().getMetadata().getVersion().getFriendlyString(),"Fabric", FabricLoaderImpl.VERSION, () -> {
                List<String> res = new ArrayList<>();
                FabricLoader.getInstance().getEntrypoints("main", ModInitializer.class).forEach(entrypoint -> {
                    String packageName = entrypoint.getClass().getPackageName();
                    res.add(packageName.substring(0, packageName.lastIndexOf('.')));
                });
                return res;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
