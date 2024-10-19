package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.frpc.Frpc;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "close",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;close()V", shift = At.Shift.AFTER))
    public void close(CallbackInfo ci){
        Frpc.stopFrpc();
    }
}
