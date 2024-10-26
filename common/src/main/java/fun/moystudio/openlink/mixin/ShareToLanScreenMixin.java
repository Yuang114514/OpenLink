package fun.moystudio.openlink.mixin;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.*;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.*;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.gson.Gson;

import java.io.PrintStream;
import java.util.*;

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
        editBox=new EditBox(this.font,this.width / 2 + 5, 130, 150, 20,new TranslatableComponent("text.openlink.port"));
        editBox.setSuggestion(new TranslatableComponent("text.openlink.port").getString());
        this.addRenderableWidget(editBox);
        usingfrp=CycleButton.onOffBuilder(isUsingFrp).create(this.width / 2 - 155, 130, 150, 20, new TranslatableComponent("text.openlink.usingfrp"),((cycleButton, bool) -> {
            this.isUsingFrp=bool;
            editBox.setVisible(isUsingFrp);
        }));
        this.addRenderableWidget(usingfrp);
    }

    @Override
    public void tick(){
        String val = editBox.getValue();
        couldShare=true;
        if(val.isBlank()||!isUsingFrp){
            editBox.setSuggestion(new TranslatableComponent("text.openlink.port").getString());
            return;
        }
        else{
            editBox.setSuggestion("");
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
                        this.minecraft.gui.getChat().addMessage(component);
                        this.minecraft.updateTitle();
                        return;
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
                        JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                        //OpenLink隧道命名规则：openlink_mc_[本地端口号]
                        for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                            if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                                try {
                                    Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                                    OpenLink.LOGGER.info("Deleted proxy: "+jsonUserProxy.proxyName);
                                } catch (Exception e) {
                                    break;
                                }
                            }
                        }//删除以前用过的隧道
                        Thread.sleep(1000);
                        response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                        userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                        JsonResponseWithData<JsonUserInfo> userinfo=Request.getUserInfo();
                        if(userinfo.data.proxies==userProxies.data.total){
                            throw new Exception(new TranslatableComponent("text.openlink.userproxieslimited").getString());
                        }
                        JsonResponseWithData<JsonTotalAndList<JsonNode>> nodelist=Request.getNodeList();
                        List<JsonNode> canUseNodes=new ArrayList<>();
                        for(JsonNode now:nodelist.data.list){
                            if(!now.group.contains(userinfo.data.group)||!now.protocolSupport.tcp||now.status!=200||now.fullyLoaded||(now.needRealname&&!userinfo.data.realname)){
                                continue;
                            }
                            canUseNodes.add(now);
                        }
                        if(canUseNodes.isEmpty()){
                            throw new Exception("Unable to use any node???");
                        }
                        canUseNodes.sort(((o1, o2) -> {
                            if(Math.abs(o1.bandwidth*o1.bandwidthMagnification-o2.bandwidth*o2.bandwidthMagnification)<1e-5)
                                return o2.bandwidth*o2.bandwidthMagnification>o1.bandwidth*o1.bandwidthMagnification?1:-1;
                            if(o1.classify!=o2.classify)
                                return (int)(o1.classify-o2.classify);
                            if(userinfo.data.realname&&o1.needRealname!=o2.needRealname)
                                return o1.needRealname?-1:1;
                            return 0;
                        }));
                        JsonNode node=canUseNodes.get(0);//选取最优节点
                        JsonNewProxy newProxy=new JsonNewProxy();
                        newProxy.name="openlink_mc_"+String.valueOf(i);
                        newProxy.local_port= String.valueOf(i);
                        newProxy.node_id=node.id;
                        Random random=new Random();
                        int start,end;
                        if(node.allowPort==null||node.allowPort.isBlank()){
                            start=30000;
                            end=60000;
                        }
                        else{
                            start=Integer.parseInt(node.allowPort.substring(1,5));
                            end=Integer.parseInt(node.allowPort.substring(7,11));
                        }
                        boolean found=false;
                        for (int j = 1; j <= 5; j++) {
                            newProxy.remote_port = random.nextInt(end - start + 1) + start;
                            if((!editBox.getValue().isBlank())){
                                newProxy.remote_port=Integer.parseInt(editBox.getValue());
                            }
                            response=Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/newProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), gson.toJson(newProxy));
                            if(gson.fromJson(response.getFirst(), JsonResponseWithData.class).flag){
                                found=true;
                                break;
                            }
                        }//创建隧道
                        if(!found) throw new Exception(new TranslatableComponent("text.openlink.remoteportnotfound").getString());
                        response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                        userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                        JsonUserProxy runningproxy=null;
                        for(JsonUserProxy jsonUserProxy:userProxies.data.list){
                            if(jsonUserProxy.proxyName.equals("openlink_mc_"+String.valueOf(i))){
                                runningproxy=jsonUserProxy;
                                break;
                            }
                        }
                        if(runningproxy==null) throw new Exception("Can not find the proxy???");
                        //启动Frpc
                        Frpc.runFrpc((int) runningproxy.id);
                        //check
                        Thread.sleep(3000);
                        response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                        userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                        runningproxy=null;
                        for(JsonUserProxy jsonUserProxy:userProxies.data.list){
                            if(jsonUserProxy.proxyName.equals("openlink_mc_"+String.valueOf(i))){
                                runningproxy=jsonUserProxy;
                                break;
                            }
                        }
                        if(runningproxy==null) throw new Exception("Can not find the proxy???");
                        if(!runningproxy.online){
                            Frpc.stopFrpc();
                            throw new Exception("Can not start frpc???");
                        }
                        JsonUserProxy finalRunningproxy = runningproxy;
                        Component tmp= ComponentUtils.wrapInSquareBrackets((new TranslatableComponent("text.openlink.frpcstartsucessfully")).withStyle((style -> {
                            return style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, finalRunningproxy.connectAddress))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(finalRunningproxy.connectAddress)))
                                    .withInsertion(finalRunningproxy.connectAddress);
                        })));
                        this.minecraft.gui.getChat().addMessage(tmp);
                    } catch (Exception e) {
                        Component tmp=new TextComponent("§4[OpenLink] "+e.getMessage());
                        e.printStackTrace();
                        this.minecraft.gui.getChat().addMessage(tmp);
                    }
                });
            }
        }
        return par1;
    }
}
