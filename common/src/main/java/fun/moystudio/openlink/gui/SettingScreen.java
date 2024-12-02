package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.SettingTabs;
import fun.moystudio.openlink.network.Request;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

public class SettingScreen extends Screen {
    public SettingScreen(Screen last) {
        super(new TranslatableComponent("gui.openlink.settingscreentitle"));
        lastscreen=last;
    }
    MultiLineLabel title;
    Screen lastscreen;
    SettingTabs tab=SettingTabs.USER;
    SettingTabs lasttab=null;
    SettingScreenButton buttonLog, buttonInfo,buttonUser, buttonAck;
    JsonResponseWithData<JsonUserInfo> userInfo=null;
    List<Widget> renderableTabWidgets,tabLog=new ArrayList<>(),tabInfo=new ArrayList<>(),tabUser=new ArrayList<>(),tabAck=new ArrayList<>();

    public static final ResourceLocation BACKGROUND_SETTING=new ResourceLocation("openlink","textures/gui/background_setting.png");

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    protected void init(){
        title=MultiLineLabel.create(this.font,new TranslatableComponent("gui.openlink.settingscreentitle"));
        int i=(this.width-10)/4;
        buttonUser=new SettingScreenButton(5,40,i,20,SettingTabs.USER.component,(button -> tab=SettingTabs.USER));
        buttonLog=new SettingScreenButton(5+i,40,i,20,SettingTabs.LOG.component,(button -> tab=SettingTabs.LOG));
        buttonInfo=new SettingScreenButton(5+i*2,40,i,20,SettingTabs.INFO.component,(button -> tab=SettingTabs.INFO));
        buttonAck=new SettingScreenButton(5+i*3,40,i,20,SettingTabs.ACKLIST.component,(button -> tab=SettingTabs.ACKLIST));
        addRenderableWidget(buttonLog);
        addRenderableWidget(buttonInfo);
        addRenderableWidget(buttonUser);
        addRenderableWidget(buttonAck);
        //tabUser.add(new Image(10,65,0,0,64,64,64,64,new ResourceLocation("openlink","textures/gui/avatar.png")));
    }

    @Override
    public void render(PoseStack poseStack,int i,int j,float f){
        this.renderBackground(poseStack);
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
        RenderSystem.setShaderTexture(0,BACKGROUND_SETTING);
        blit(poseStack,0,0,0,0,this.width,this.height,this.width,this.height);
        fill(poseStack,5,60,this.buttonAck.x+this.buttonAck.getWidth(),60+this.height-75,0x8F000000);
        title.renderCentered(poseStack,this.width/2,15);
        if(renderableTabWidgets!=null) renderableTabWidgets.forEach(widget -> widget.render(poseStack,i,j,f));
        super.render(poseStack,i,j,f);
    }

    @Unique
    private void onTab() throws Exception {
        boolean first=lasttab!=tab;
        switch(tab){
            case LOG -> {
                buttonLog.active=false;
                buttonInfo.active=true;
                buttonUser.active=true;
                buttonAck.active=true;
                renderableTabWidgets=tabLog;
            }
            case ACKLIST -> {
                buttonLog.active=true;
                buttonInfo.active=true;
                buttonUser.active=true;
                buttonAck.active=false;
                renderableTabWidgets=tabAck;
            }
            case USER -> {
                buttonLog.active=true;
                buttonInfo.active=true;
                buttonUser.active=false;
                buttonAck.active=true;
                renderableTabWidgets=tabUser;
                if(first)
                    userInfo=Request.getUserInfo();
                if(userInfo==null||!userInfo.flag)
                    this.minecraft.setScreen(new LoginScreen(this,lastscreen));
            }
            case INFO -> {
                buttonLog.active=true;
                buttonInfo.active=false;
                buttonUser.active=true;
                buttonAck.active=true;
                renderableTabWidgets=tabInfo;
            }
        }
    }

    @Override
    public void tick(){
        if (OpenLink.disabled) {
            this.onClose();
        }
        if (Request.Authorization == null) {
            this.minecraft.setScreen(new LoginScreen(this,lastscreen));
        }
        try {
            onTab();
        } catch (Exception e) {
            e.printStackTrace();
            this.onClose();
        }
        lasttab=tab;
    }

}
