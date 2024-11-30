package fun.moystudio.openlink.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonResponseWithCode;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.logic.WebBrowser;
import fun.moystudio.openlink.network.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import java.util.List;
import java.util.Map;

public class LoginScreen extends Screen {
    public LoginScreen(Screen last,Screen llast) {
        super(new TranslatableComponent("gui.openlink.loginscreentitle"));
        lastscreen=last;
        llastscreen=llast;
    }
    Screen lastscreen=null;
    Screen llastscreen=null;
    MultiLineLabel loginTips;
    EditBox username;
    EditBox password;
    String wrongmsg = "";
    Checkbox remember;
    WebBrowser browser=new WebBrowser(Uris.openidLoginUri.toString());

    @Override
    protected void init() {
        loginTips = MultiLineLabel.create(this.font, new TranslatableComponent("text.openlink.logintips"), this.width - 50);

        username = new EditBox(this.font, this.width / 2 - 100, this.height / 6 + 58, 200, 20, new TranslatableComponent("text.openlink.username"));
        password = new EditBox(this.font, this.width / 2 - 100, this.height / 6 + 98, 200, 20, new TranslatableComponent("text.openlink.password"));
        username.setValue(OpenLink.PREFERENCES.get("last_username",""));
        password.setValue(OpenLink.PREFERENCES.get("last_password",""));
        remember=new Checkbox(this.width / 2 - 100,this.height/6+133,150,20,new TranslatableComponent("text.openlink.rememberuserandpassword"),false);
        this.addRenderableWidget(username);
        this.addRenderableWidget(password);
        this.addRenderableWidget(remember);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 178, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            if (this.username.getValue().isBlank() || this.password.getValue().isBlank()) {
                wrongmsg = new TranslatableComponent("text.openlink.notcompleted").getString();
                return;
            }
            wrongmsg = "";
            Pair<String, Map<String, List<String>>> response;
            Gson gson = new Gson();
            try {
                response = Request.POST(Uris.openidLoginUri.toString() + "api/public/login", Request.DEFAULT_HEADER, "{\"user\":\"" + username.getValue() + "\",\"password\":\"" + password.getValue() + "\"}");
            } catch (Exception e) {
                wrongmsg = e.getMessage();
                e.printStackTrace();
                return;
            }

            JsonResponseWithCode<?> loginFlag = gson.fromJson(response.getFirst(), JsonResponseWithCode.class);
            if (!loginFlag.flag) {
                wrongmsg = loginFlag.msg;
                return;
            }
            Map<String, List<String>> headerWithCookie = Request.getHeaderWithCookieByResponse(response, Request.DEFAULT_HEADER);
            try {
                response = Request.POST(Uris.openidLoginUri.toString() + "api/oauth2/authorize?response_type=code&redirect_uri=" + Uris.openFrpAPIUri.toString() + "oauth_callback&client_id=openfrp", headerWithCookie, "{}");
            } catch (Exception e) {
                wrongmsg = e.getMessage();
                e.printStackTrace();
                return;
            }
            JsonResponseWithData<Map<String, String>> loginCode = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<Map<String, String>>>(){}.getType()); // 返回的code（用于下一步传参）
            String code = loginCode.data.get("code");
            try {
                Request.POST(Uris.openFrpAPIUri.toString() + "oauth2/callback?code=" + code, headerWithCookie, "{}");
            } catch (Exception e) {
                wrongmsg = e.getMessage();
                e.printStackTrace();
                return;
            }
            //Authorization会在post里写储存的
            Request.writeSession(); //将session写入注册表
            if(remember.selected()){
                OpenLink.PREFERENCES.put("last_username",this.username.getValue());
                OpenLink.PREFERENCES.put("last_password",this.password.getValue());
            }
            else {
                OpenLink.PREFERENCES.remove("last_username");
                OpenLink.PREFERENCES.remove("last_password");
            }
            this.onClose();
        }));

        //原版语言按钮
        this.addRenderableWidget(new ImageButton(this.width / 2 - 130, this.height / 6 + 178, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> {
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
        }, new TranslatableComponent("narrator.button.language")));

        //注册
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 158 , 200, 20, new TranslatableComponent("text.openlink.no_account"), (button) -> {
            browser.openBrowser();
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        loginTips.renderCentered(poseStack, this.width / 2, 15, 16, 0xffffff);
        drawString(poseStack, this.font, new TranslatableComponent("text.openlink.username"), this.width / 2 - 100, this.height / 6 + 33, 0xffffff);
        drawString(poseStack, this.font, new TranslatableComponent("text.openlink.password"), this.width / 2 - 100, this.height / 6 + 83, 0xffffff);
        drawString(poseStack, this.font, new TextComponent(wrongmsg), this.width / 2 - 100, this.height / 6 + 123, 0xff0000);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(Request.Authorization==null?llastscreen:lastscreen);
    }
}
