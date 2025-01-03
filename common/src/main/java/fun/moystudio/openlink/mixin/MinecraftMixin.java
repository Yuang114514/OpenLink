package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.logic.EventCallbacks;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.logic.OnlineModeTabs;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "stop",at = @At("TAIL"))
    public void stop(CallbackInfo ci){
        EventCallbacks.onClientStop();
    }

    @Inject(method = "prepareForMultiplayer", at = @At("HEAD"), cancellable = true)
    public void prepareForMultiplayer(CallbackInfo ci) {
        if(LanConfig.getAuthMode()!=OnlineModeTabs.ONLINE_MODE){
            LOGGER.warn("Server will run in offline mode!");
            ci.cancel();
        }
    }
}
