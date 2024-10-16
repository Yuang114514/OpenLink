package fun.moystudio.openlink.mixin;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonTotalAndList;
import fun.moystudio.openlink.json.JsonUserProxy;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

@Mixin(ShareToLanScreen.class)
public abstract class ShareToLanScreenMixin extends Screen{
    @Shadow private GameType gameMode;

    @Shadow private boolean commands;

    @Unique boolean isUsingFrp=true;

    @Unique EditBox editBox;

    @Unique CycleButton<Boolean> usingfrp;

    @Unique boolean couldShare=true;

    protected ShareToLanScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init",at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        editBox=new EditBox(this.font,this.width / 2 + 5, 100, 150, 20,new TranslatableComponent("text.openlink.port"));
        editBox.setSuggestion(new TranslatableComponent("text.openlink.port").getString());
        this.addRenderableWidget(editBox);
        usingfrp=CycleButton.onOffBuilder(isUsingFrp).create(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("text.openlink.usingfrp"),((cycleButton, bool) -> {
            this.isUsingFrp=bool;
            editBox.setVisible(isUsingFrp);
        }));
        this.addRenderableWidget(usingfrp);
    }

    @Override
    public void tick(){
        String val = editBox.getValue();
        couldShare=true;
        if(val==null||val.isBlank()||!isUsingFrp){
            return;
        }
        if(val.length() != 5){
            couldShare=false;
            return;
        }
        boolean _0=true;
        for(int i=0;i<val.length();i++){
            if(i==0&&val.charAt(i)=='0'){
                couldShare=false;
                return;
            }
            if(val.charAt(i)!='0') _0=false;
            if(!Character.isDigit(val.charAt(i))){
                couldShare=false;
                return;
            }
        }
        if(_0){
            couldShare=false;
        }
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ShareToLanScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    public GuiEventListener buttonCreateMixin(GuiEventListener par1){
        if(par1 instanceof Button){
            Button button=(Button)(par1);
            if(button.getMessage().equals(new TranslatableComponent("lanServer.start"))){
                return new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("lanServer.start"), (button1) -> {
                    if(!this.couldShare) return;
                    this.minecraft.setScreen((Screen)null);
                    int i = HttpUtil.getAvailablePort();
                    TranslatableComponent component;
                    if (this.minecraft.getSingleplayerServer().publishServer(this.gameMode, this.commands, i)) {
                        component = new TranslatableComponent("commands.publish.started", new Object[]{i});
                    } else {
                        component = new TranslatableComponent("commands.publish.failed");
                    }
                    this.minecraft.gui.getChat().addMessage(component);
                    this.minecraft.updateTitle();
                    //以上是原版的代码，以下是OpenLink的Frp启动及隧道创建，节点选择等主要功能
                    if(!isUsingFrp){
                        return;
                    }
                    Gson gson=new Gson();
                    try {
                        Pair<String, Map<String, List<String>>> response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                        JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), JsonResponseWithData.class);
                        //OpenLink隧道命名规则：openlink_mc_[本地端口号]
                        for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                            if (jsonUserProxy.proxyName.startsWith("openlink_mc_")) {
                                try {
                                    Pair<String, Map<String, List<String>>> response1=Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                                } catch (Exception e) {
                                    break;
                                }
                            }
                        }
                        if(Request.getUserInfo().data.proxies==userProxies.data.total){
                            Component tmp=new TranslatableComponent("text.openlink.userproxieslimited");
                            tmp.getStyle().withColor(ChatFormatting.RED);
                            this.minecraft.gui.getChat().addMessage(tmp);
                            return;
                        }
                        //隧道创建逻辑未完成
                    } catch (Exception e) {
                        Component tmp=new TextComponent(e.getMessage());
                        tmp.getStyle().withColor(ChatFormatting.RED);
                        this.minecraft.gui.getChat().addMessage(tmp);
                    }
                });
            }
        }
        return par1;
    }
}
