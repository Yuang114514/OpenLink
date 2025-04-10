package fun.moystudio.openlink.quilt;

import fun.moystudio.openlink.OpenLink;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import fun.moystudio.openlink.fabriclike.OpenLinkFabricLike;

import java.util.ArrayList;
import java.util.List;

public final class OpenLinkQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        // Run the Fabric-like setup.
        try {
            OpenLinkFabricLike.init(QuiltLoader.getModContainer(OpenLink.MOD_ID).get().metadata().version().raw(),"Quilt", QuiltLoaderImpl.VERSION, () -> {
                List<String> res = new ArrayList<>();
                QuiltLoader.getEntrypoints("init", Object.class).forEach(entrypoint -> {
                    res.add(entrypoint.getClass().getPackageName());
                });
                return res;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
