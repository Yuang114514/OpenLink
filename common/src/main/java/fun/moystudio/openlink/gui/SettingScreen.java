package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.SettingTabs;
import fun.moystudio.openlink.network.Request;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class SettingScreen extends Screen {
    public SettingScreen(Screen last) {
        super(new TranslatableComponent("gui.openlink.settingscreentitle"));
        lastscreen=last;
    }
    MultiLineLabel title;
    Screen lastscreen=null;
    SettingTabs tab=SettingTabs.LOG;
    SettingScreenButton buttonLog,buttonTraffic,buttonUser,buttonMod;
    JsonResponseWithData<JsonUserInfo> userInfo=null;

    public static final ResourceLocation BACKGROUND_SETTING=new ResourceLocation("openlink","textures/gui/background_setting.png");

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    protected void init(){
        title=MultiLineLabel.create(this.font,new TranslatableComponent("gui.openlink.settingscreentitle"));
        int i=(this.width-10)/4;
        buttonLog=new SettingScreenButton(5,40,i,20,new TranslatableComponent("text.openlink.setting_log"),(button -> {
            tab=SettingTabs.LOG;
        }));
        buttonTraffic=new SettingScreenButton(5+i,40,i,20,new TranslatableComponent("text.openlink.setting_traffic"),(button -> {
            tab=SettingTabs.TRAFFIC;
        }));
        buttonUser=new SettingScreenButton(5+i*2,40,i,20,new TranslatableComponent("text.openlink.setting_user"),(button -> {
            tab=SettingTabs.USER;
            try {
                userInfo = Request.getUserInfo();
                if(!userInfo.flag)
                    this.minecraft.setScreen(new LoginScreen(this,lastscreen));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        buttonMod=new SettingScreenButton(5+i*3,40,i,20,new TranslatableComponent("text.openlink.setting_mod"),(button -> {
            tab=SettingTabs.MOD;
        }));
        addRenderableWidget(buttonLog);
        addRenderableWidget(buttonTraffic);
        addRenderableWidget(buttonUser);
        addRenderableWidget(buttonMod);
    }

    @Override
    public void render(PoseStack poseStack,int i,int j,float f){
        this.renderBackground(poseStack);
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
        RenderSystem.setShaderTexture(0,BACKGROUND_SETTING);
        blit(poseStack,0,0,0,0,this.width,this.height,this.width,this.height);
        fill(poseStack,5,60,this.buttonMod.x+this.buttonMod.getWidth(),60+this.height-75,0x8F000000);
        title.renderCentered(poseStack,this.width/2,15);
        drawCenteredString(poseStack,this.font,new TranslatableComponent("temp.openlink.tobedone"),this.width/2,this.height/2,0xffffff);
        super.render(poseStack,i,j,f);
    }


    @Override
    public void tick(){
        if (OpenLink.disabled) {
            this.onClose();
        }
        if (Request.Authorization == null) {
            this.minecraft.setScreen(new LoginScreen(this,lastscreen));
        }
        switch (tab){
            case LOG -> {
                buttonLog.active=false;
                buttonTraffic.active=true;
                buttonUser.active=true;
                buttonMod.active=true;
            }
            case MOD -> {
                buttonLog.active=true;
                buttonTraffic.active=true;
                buttonUser.active=true;
                buttonMod.active=false;
            }
            case USER -> {
                buttonLog.active=true;
                buttonTraffic.active=true;
                buttonUser.active=false;
                buttonMod.active=true;
            }
            case TRAFFIC -> {
                buttonLog.active=true;
                buttonTraffic.active=false;
                buttonUser.active=true;
                buttonMod.active=true;
            }
        }
    }

}
