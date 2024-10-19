package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.LoginScreen;
import fun.moystudio.openlink.gui.SettingButton;
import fun.moystudio.openlink.gui.SettingScreen;
import fun.moystudio.openlink.gui.UpdateScreen;
import fun.moystudio.openlink.network.Request;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.Button;
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

    private long lastRenderTime = 0;
    private static final long RENDER_INTERVAL_MS = 50;

    @Inject(method = "render", at = @At("TAIL"))
    public void renderMixin(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRenderTime >= RENDER_INTERVAL_MS) {
            lastRenderTime = currentTime;

            updateScreenState();
            Frpc.stopFrpc();
        }
    }

    private void updateScreenState() {
        if (Frpc.hasUpdate) {
            this.minecraft.setScreen(new UpdateScreen());
        } else if (Request.sessionID == null || Request.Authorization == null) {
            this.minecraft.setScreen(new LoginScreen());
        }
    }

    @Unique
    private static final ResourceLocation OPENLINK_SETTING = new ResourceLocation("openlink", "textures/gui/setting.png");

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        this.addRenderableWidget(new SettingButton(this.width / 2 + 129, this.height / 4 + 48 + 72 + 12,
            20, 20, 0, 0, 20, OPENLINK_SETTING, 20, 20, button -> {
                this.minecraft.setScreen(new SettingScreen());
            }));
    }
}