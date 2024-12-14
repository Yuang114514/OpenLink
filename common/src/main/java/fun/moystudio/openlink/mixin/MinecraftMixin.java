package fun.moystudio.openlink.mixin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonTotalAndList;
import fun.moystudio.openlink.json.JsonUserProxy;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.logic.OnlineModeTabs;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final private PlayerSocialManager playerSocialManager;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "close",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;close()V", shift = At.Shift.AFTER))
    public void close(CallbackInfo ci){
        Frpc.stopFrpc();
    }

    @Inject(method = "prepareForMultiplayer",at = @At("TAIL"))
    public void prepareForMultiplayer(CallbackInfo ci) {
        if(LanConfig.getAuthMode()!=OnlineModeTabs.ONLINE_MODE){
            LOGGER.warn("Server will run in offline mode!");
            this.playerSocialManager.stopOnlineMode();
        }
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",at = @At("TAIL"))
    public void clearLevel(Screen screen, CallbackInfo ci) {
        if(OpenLink.disabled) return;
        try{
            Pair<String, Map<String, List<String>>> response= Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
            Gson gson=new Gson();
            JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
            for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                    try {
                        Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/forceOff", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                        Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                        OpenLink.LOGGER.info("Deleted proxy: "+jsonUserProxy.proxyName);
                    } catch (Exception e) {
                        break;
                    }
                }
            }//删除隧道
        } catch (Exception ignore){}
    }
}
