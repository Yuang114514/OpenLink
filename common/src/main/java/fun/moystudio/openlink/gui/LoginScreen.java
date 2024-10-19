package fun.moystudio.openlink.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.json.JsonResponseWithCode;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.network.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.TextComponent;
import java.util.List;
import java.util.Map;

public class LoginScreen extends Screen {
    public LoginScreen() {
        super(new TranslatableComponent("gui.openlink.loginscreentitle"));
    }
    MultiLineLabel loginTips;
//    EditBox url;
    EditBox username;
    EditBox password;
    String wrongmsg="";

    boolean browserOpened=false;

    @Override
    protected void init() {
//        if(!browserOpened) {
//            this.minecraft.keyboardHandler.setClipboard(Uris.openidLoginUri.toString());
//            try{
//                switch (Frpc.osName) {
//                    case "windows" ->
//                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + Uris.openidLoginUri.toString());//Windows自带的链接打开方式
//                    case "darwin" -> {//百度搜的macos，没用过（
//                        Class fileMgr = Class.forName("com.apple.eio.FileManager");
//                        Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
//                        openURL.invoke(null, Uris.openidLoginUri.toString());
//                    }
//                    case "linux", "freebsd" -> {
//                        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};//Linux常用浏览器
//
//                        String browser = null;
//                        for (int i = 0; i < browsers.length && browser == null; i++) {
//                            if (Runtime.getRuntime().exec(new String[]{"which", browsers[i]}).waitFor() == 0) {
//                                browser = browsers[i];
//                            }
//                        }
//                        if (browser == null) {
//                            OpenLink.LOGGER.error("What the hell are you using?");
//                            throw new RuntimeException("[OpenLink] What the hell are you using?");
//                        }
//                        Runtime.getRuntime().exec(browser + " " + Uris.openidLoginUri.toString());
//                    }
//                    default -> {
//                        OpenLink.LOGGER.error("What the hell are you using?");
//                        throw new RuntimeException("[OpenLink] What the hell are you using?");
//                    }
//                }
//            }catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            browserOpened=true;
//        }
        loginTips=MultiLineLabel.create(this.font,new TranslatableComponent("text.openlink.logintips"),this.width-50);
//        url=new EditBox(this.font,50,this.height/5*2+20*loginTips.getLineCount(),this.width-100,20,new TextComponent(Uris.openidLoginUri.toString()));
//        url.setValue(Uris.openidLoginUri.toString());
//        this.addRenderableWidget(url);
        username=new EditBox(this.font,this.width/2-100,this.height/6+68,200,20,new TranslatableComponent("text.openlink.username"));
        password=new EditBox(this.font,this.width/2-100,this.height/6+108,200,20,new TranslatableComponent("text.openlink.password"));
        this.addRenderableWidget(username);
        this.addRenderableWidget(password);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            if(this.username.getValue().isBlank()||this.password.getValue().isBlank()){
                wrongmsg=new TranslatableComponent("text.openlink.notcompleted").getString();
                return;
            }
            wrongmsg="";
            Pair<String, Map<String, List<String>>> response;
            Gson gson=new Gson();
            try {
                response=Request.POST(Uris.openidLoginUri.toString()+"api/public/login",Request.DEFAULT_HEADER,"{\"user\":\""+username.getValue()+"\",\"password\":\""+password.getValue()+"\"}");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            JsonResponseWithCode<?> loginFlag = gson.fromJson(response.getFirst(),JsonResponseWithCode.class);
            if(!loginFlag.flag){
                wrongmsg=loginFlag.msg;
                return;
            }
            Map<String,List<String>> headerWithCookie=Request.getHeaderWithCookieByResponse(response,Request.DEFAULT_HEADER);
            try {
                response=Request.POST(Uris.openidLoginUri.toString()+"api/oauth2/authorize?response_type=code&redirect_uri="+Uris.openFrpAPIUri.toString()+"oauth_callback&client_id=openfrp",headerWithCookie,"{}");//空body
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JsonResponseWithData<Map<String,String>> loginCode=gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<Map<String,String>>>(){}.getType());//返回的code（用于下一步传参）
            String code=loginCode.data.get("code");
            try {
                response=Request.POST(Uris.openFrpAPIUri.toString()+"oauth2/callback?code="+code,headerWithCookie,"{}");//空body
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JsonResponseWithData<String> sessionID=gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<String>>(){}.getType());
            Request.sessionID=sessionID.data;
            Request.Authorization=response.getSecond().get("Authorization").get(0);
            Request.writeSession();//写入sessioncode.json
            this.onClose();
        }));
        //以下为原版语言按钮
        int l = this.height / 4 + 48;
        this.addRenderableWidget(new ImageButton(this.width / 2 - 130, this.height / 6 + 168, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> {
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
        }, new TranslatableComponent("narrator.button.language")));
    }
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        loginTips.renderCentered(poseStack,this.width/2,15,16,0xffffff);
        drawString(poseStack,this.font,new TranslatableComponent("text.openlink.username"),this.width/2-100,this.height/6+43,0xffffff);
        drawString(poseStack,this.font,new TranslatableComponent("text.openlink.password"),this.width/2-100,this.height/6+93,0xffffff);
        drawString(poseStack,this.font,new TextComponent(wrongmsg),this.width/2-100,this.height/6+133,0xff0000);
//        if(!url.getValue().equals(Uris.openidLoginUri.toString())){
//            url.setValue(Uris.openidLoginUri.toString());
//        }
        super.render(poseStack, i, j, f);
    }
    @Override
    public boolean shouldCloseOnEsc(){
        return false;
    }


}
