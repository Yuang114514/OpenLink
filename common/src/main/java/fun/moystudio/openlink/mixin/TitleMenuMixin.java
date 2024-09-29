package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.UpdateScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleMenuMixin extends Screen {
    protected TitleMenuMixin(Component component) {
        super(component);
    }
    @Inject(method = "tick",at = @At("TAIL"))
    public void tickMixin(CallbackInfo ci){
        if(Frpc.hasUpdate){
            this.minecraft.setScreen(new UpdateScreen());
        }
    }
}
