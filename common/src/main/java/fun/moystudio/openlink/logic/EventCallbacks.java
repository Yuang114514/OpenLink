package fun.moystudio.openlink.logic;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.FrpcManager;
import fun.moystudio.openlink.frpc.OpenFrpFrpcImpl;
import fun.moystudio.openlink.gui.*;
import fun.moystudio.openlink.mixin.IScreenAccessor;
import fun.moystudio.openlink.mixin.IShareToLanLastScreenAccessor;
import fun.moystudio.openlink.network.SSLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;

public class EventCallbacks {
    private static final ResourceLocation OPENLINK_SETTING = Utils.createResourceLocation("openlink", "textures/gui/setting_button.png");
    private static final ResourceLocation OPENLINK_SETTING_HOVERED = Utils.createResourceLocation("openlink", "textures/gui/setting_button_hovered.png");

    public static void onScreenInit(Minecraft minecraft, Screen screen){
        if(screen instanceof ShareToLanScreen shareToLanScreen){
            minecraft.setScreen(new NewShareToLanScreen(((IShareToLanLastScreenAccessor)shareToLanScreen).getLastScreen()));
            return;
        }
        if(OpenLink.disabled) return;
        for(Pair<String, Class<?>> classPair:OpenLink.CONFLICT_CLASS){
            if(classPair.getSecond().isInstance(screen)){
                if(ConflictSelectionScreen.canOpen!=null&& ConflictSelectionScreen.canOpen.equals(classPair)){
                    continue;
                }
                minecraft.setScreen(new ConflictSelectionScreen(classPair.getFirst()));
                return;
            }
        }
        if(screen instanceof TitleScreen){
            FrpcManager.getInstance().stop();
            ((IScreenAccessor)screen).invokeAddRenderableWidget(new ImageButtonWithHoveredState(screen.width / 2 + 129, screen.height / 4 + 48 + 72 + 12,
                    20, 20, 0, 0, 20, OPENLINK_SETTING, OPENLINK_SETTING_HOVERED, 20, 20, (button) -> minecraft.setScreen(new SettingScreen(null))));
        }
    }
    public static void onClientStop(){
        FrpcManager.getInstance().stop();
    }
    public static void onLevelClear(){
        ConflictSelectionScreen.canOpen=null;
        if(OpenLink.disabled) return;
        FrpcManager.getInstance().stop();
    }

    public static void onClientTick(Minecraft minecraft){
        if(minecraft.screen instanceof TitleScreen){
            if(OpenLink.disabled) return;
            if (SSLUtils.sslIgnored){
                minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed->{
                    if(confirmed){
                        SSLUtils.sslIgnored=false;
                        try {
//                            OldFrpc.init();//安装/检查更新frpc版本 TODO: no more OldFrpc
                            OpenFrpFrpcImpl.readSession();//读取以前的SessionID
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        OpenLink.LOGGER.error("Minecraft closed because of SSL.");
                        minecraft.stop();
                    }
                    minecraft.setScreen(null);
                }, Utils.literalText("SSL Handshake Error"), Utils.translatableText("text.openlink.sslignored")));
            }
//            if (OldFrpc.hasUpdate) {//TODO: no more OldFrpc
//                minecraft.setScreen(new UpdateScreen());
//            }
            FrpcManager.getInstance().stop();
        }
    }

    public static void onAllModLoadingFinish() {
    }
}
