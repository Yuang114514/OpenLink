package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.mixin.IDynamicTextureMixin;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class SelfCleaningDynamicTexture extends DynamicTexture {
    public SelfCleaningDynamicTexture(NativeImage nativeImage) {
        super(nativeImage);
    }

    @Override
    public void upload(){
        super.upload();
        ((IDynamicTextureMixin)this).setPixelsAccess(new NativeImage(1,1,true));
    }
}
