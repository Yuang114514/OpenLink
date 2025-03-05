package fun.moystudio.openlink.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.neilalexander.jnacl.NaCl;
import fun.moystudio.openlink.json.JsonLogin;
import fun.moystudio.openlink.json.JsonQueryLogin;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.logic.WebBrowser;
import fun.moystudio.openlink.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import com.neilalexander.jnacl.crypto.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginScreen extends Screen {
    public LoginScreen(Screen last) {
        super(Utils.translatableText("gui.openlink.loginscreentitle"));
        lastscreen=last;
    }
    private static final SecureRandom secureRandom = new SecureRandom();
    Screen lastscreen;
    MultiLineLabel loginTips;
    EditBox authorization=new EditBox(Minecraft.getInstance().font, this.width / 2 - 200, this.height / 6 * 3, 355, 20, Utils.translatableText("text.openlink.authorization"));
    WebBrowser browser=new WebBrowser(Uris.openidLoginUri.toString());
    int timer = 0;

    @Override
    protected void init() {
        loginTips = MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.logintips"), this.width - 50);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 * 2, 200, 20, Utils.translatableText("text.openlink.fastlogin"), (button) ->{
            Gson gson = new Gson();
            byte[] clientPublic = new byte[curve25519xsalsa20poly1305.crypto_secretbox_PUBLICKEYBYTES];
            byte[] clientPrivate = new byte[curve25519xsalsa20poly1305.crypto_secretbox_SECRETKEYBYTES];
            curve25519xsalsa20poly1305.crypto_box_keypair(clientPublic, clientPrivate);
            try {
                Pair<String, Map<String, List<String>>> response = Request.POST("https://access.openfrp.net/argoAccess/requestLogin", Request.DEFAULT_HEADER, "{\"public_key\":\""+ Base64.getUrlEncoder().encodeToString(clientPublic) +"\"}");
                JsonResponseWithData<JsonLogin> jsonLogin = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonLogin>>(){}.getType());
                this.minecraft.keyboardHandler.setClipboard(jsonLogin.data.authorization_url);
                new WebBrowser(jsonLogin.data.authorization_url).openBrowser();
                String requestUUID = jsonLogin.data.request_uuid;
                button.active=false;
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    if(timer>=300||!(this.minecraft.screen instanceof LoginScreen)){
                        timer=0;
                        scheduledExecutorService.shutdown();
                    }
                    try {
                        Pair<String, Map<String, List<String>>> response1 = Request.GETWithHeader("https://access.openfrp.net/argoAccess/pollLogin?request_uuid="+requestUUID, Request.DEFAULT_HEADER);
                        if(response1.getSecond().containsKey("x-request-public-key") || response1.getSecond().containsKey("X-Request-Public-Key")){
                            JsonResponseWithData<JsonQueryLogin> jsonQueryLogin = gson.fromJson(response1.getFirst(), new TypeToken<JsonResponseWithData<JsonQueryLogin>>(){}.getType());
                            if(jsonQueryLogin.data!=null){
                                String stringServerPublic = response1.getSecond().containsKey("x-request-public-key")?response1.getSecond().get("x-request-public-key").get(0):response1.getSecond().get("X-Request-Public-Key").get(0);
                                byte[] serverPublic = Base64.getDecoder().decode(stringServerPublic);
                                byte[] message = Base64.getDecoder().decode(jsonQueryLogin.data.authorization_data);
                                byte[] nonce = new byte[curve25519xsalsa20poly1305.crypto_secretbox_NONCEBYTES];
                                secureRandom.nextBytes(nonce);
                                byte[] decrypted = new NaCl(clientPrivate, serverPublic).decrypt(message, nonce);
                                String authorization = NaCl.asHex(decrypted)/*new String(decrypted, StandardCharsets.UTF_8)*/;
                                System.out.println(authorization);
                                timer=0;
                                scheduledExecutorService.shutdown();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timer++;
                }, 0, 1, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            LoginGetCodeHttpServer codeHttpServer = new LoginGetCodeHttpServer();
//            codeHttpServer.start();
//            try {
//                JsonResponseWithData<String> response = gson.fromJson(Request.POST("https://api.openfrp.net/oauth2/login?redirect_url=http://localhost:"+codeHttpServer.port,Request.DEFAULT_HEADER, "{}").getFirst(),new TypeToken<JsonResponseWithData<String>>(){}.getType());
//                new WebBrowser(response.data).openBrowser();
//                this.minecraft.keyboardHandler.setClipboard(response.data);
//                button.active=false;
//                this.minecraft.setScreen(new ConfirmScreenWithLanguageButton(confirmed -> {if(Request.Authorization!=null){this.onClose();}}, Utils.translatableText("text.openlink.fastlogin"), Utils.translatableText("text.openlink.fastloginconfirm")));
//            } catch (Exception e) {
//                e.printStackTrace();
//                this.onClose();
//            }
        }));
        authorization.setMaxLength(100);
        authorization.setX(this.width / 2 - 200);
        authorization.y=this.height/2;
        this.addRenderableWidget(authorization);
        this.addRenderableWidget(new Button(this.width / 2 + 160, this.height / 2, 40, 20, CommonComponents.GUI_DONE, button -> {
            Request.Authorization = authorization.getValue();
            try {
                JsonResponseWithData<JsonUserInfo> response = Request.getUserInfo();
                if(response!=null&&response.flag){
                    this.onClose();
                } else {
                    Request.Authorization = null;
                }
            } catch (Exception e) {
                Request.Authorization = null;
            }
            Request.writeSession();
        }));
        //注册
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 * 4 , 200, 20, Utils.translatableText("text.openlink.no_account"), (button) -> browser.openBrowser()));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        loginTips.renderCentered(poseStack, this.width / 2, 15, 16, 0xffffff);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void tick(){
        if(authorization.getValue().isBlank()){
            authorization.setSuggestion(Utils.translatableText("text.openlink.authorization").getString());
        } else {
            authorization.setSuggestion("");
        }
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }
}
