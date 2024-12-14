package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.*;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleMenuMixin extends Screen {
    protected TitleMenuMixin(Component component) {
        super(component);
    }

    @Unique
    private static final ResourceLocation OPENLINK_SETTING = new ResourceLocation("openlink", "textures/gui/setting.png");

    @Inject(method = "tick",at=@At("TAIL"))
    public void tick(CallbackInfo ci){
        if(OpenLink.disabled) return;
        if (SSLUtils.sslIgnored){
            this.minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed->{
                if(confirmed){
                    SSLUtils.sslIgnored=false;
                    try {
                        Frpc.init();//安装/检查更新frpc版本
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Request.readSession();//读取以前的SessionID
                }
                else{
                    OpenLink.LOGGER.error("Minecraft closed because of SSL.");
                    this.minecraft.close();
                }
                this.minecraft.setScreen(null);
            },new TextComponent("SSL Handshake Error"),new TranslatableComponent("text.openlink.sslignored")));
        }
        if (Frpc.hasUpdate) {
            this.minecraft.setScreen(new UpdateScreen());
        }
        Frpc.stopFrpc();
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        if(OpenLink.disabled) return;
        this.addRenderableWidget(new SettingButton(this.width / 2 + 129, this.height / 4 + 48 + 72 + 12, 
            20, 20, 0, 0, 20, OPENLINK_SETTING, 20, 20, (button) -> {
                this.minecraft.setScreen(new SettingScreen(null));
            }));
    }
}
