package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.SettingTabs;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Unique;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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
        ResourceLocation lastlocationimage=!tabUser.isEmpty()?((ImageWidget)tabUser.get(0)).texture:new ResourceLocation("openlink","textures/gui/default_avatar.png");
        Component lastcomponent1=tabUser.size()>=2?((ComponentWidget)tabUser.get(1)).component:TextComponent.EMPTY;
        Component lastcomponent2=tabUser.size()>=3?((ComponentWidget)tabUser.get(2)).component:TextComponent.EMPTY;
        Component lastcomponent3=tabUser.size()>=4?((ComponentWidget)tabUser.get(3)).component:TextComponent.EMPTY;
        Component lastcomponent4=tabUser.size()>=5?((ComponentWidget)tabUser.get(4)).component:TextComponent.EMPTY;
        Component lastcomponent5=tabUser.size()>=6?((ComponentWidget)tabUser.get(5)).component:TextComponent.EMPTY;
        int lastx2=tabUser.size()>=3?((ComponentWidget)tabUser.get(2)).x:10;
        List<Pair<String,Long>> lastdatapoints=tabUser.size()>=7?((LineChartWidget)tabUser.get(6)).dataPoints:readTraffic();
        tabUser.clear();
        tabLog.clear();
        tabAck.clear();
        tabInfo.clear();
        int j=Math.min((this.width-20)/4,(this.height-75)/5*3);
        tabUser.add(new ImageWidget(10,65,0,0,j,j,j,j,lastlocationimage));
        tabUser.add(new ComponentWidget(this.font,10,65+j+5,0xffffff,lastcomponent1,false));
        tabUser.add(new ComponentWidget(this.font,lastx2,65+j+5,0xacacac,lastcomponent2,false));
        tabUser.add(new ComponentWidget(this.font,10,65+j+5+10,0xacacac,lastcomponent3,false));
        tabUser.add(new ComponentWidget(this.font,10,65+j+5+20,0xacacac,lastcomponent4,false));
        tabUser.add(new ComponentWidget(this.font,10,65+j+5+30,0xacacac,lastcomponent5,false));
        tabUser.add(new LineChartWidget(
                    this.font,
                    10+j+20, 65+5,
                    this.width-20, 60+this.height-75-15,
                    new TranslatableComponent("text.openlink.x_axis_label"), new TranslatableComponent("text.openlink.y_axis_label"), lastdatapoints,
                    (dataXY, poseStack, i1, j1)-> renderComponentTooltip(poseStack,
                            Arrays.stream(new Component[]{new TextComponent(dataXY.getFirst()+", "+dataXY.getSecond()+"MiB")}).toList(),
                            i1,j1)));
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
    private void onTab() {
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
                if(first) {
                    ImageWidget nowavatar=(ImageWidget)tabUser.get(0);
                    ComponentWidget nowuser=(ComponentWidget)tabUser.get(1);
                    ComponentWidget nowid=(ComponentWidget)tabUser.get(2);
                    ComponentWidget nowemail=(ComponentWidget)tabUser.get(3);
                    ComponentWidget nowgroup=(ComponentWidget)tabUser.get(4);
                    ComponentWidget nowproxy=(ComponentWidget)tabUser.get(5);
                    LineChartWidget nowtraffic=(LineChartWidget)tabUser.get(6);
                    nowuser.component=new TranslatableComponent("text.openlink.loading");
                    nowid.component=TextComponent.EMPTY;
                    nowemail.component=TextComponent.EMPTY;
                    nowgroup.component=TextComponent.EMPTY;
                    nowproxy.component=TextComponent.EMPTY;
                    tabUser.set(1,nowuser);
                    new Thread(() -> {
                        try {
                            userInfo = Request.getUserInfo();
                        } catch (Exception e) {
                            e.printStackTrace();
                            this.minecraft.setScreen(new LoginScreen(this, lastscreen));
                            return;
                        }
                        MessageDigest messageDigest=null;
                        try {
                            messageDigest=MessageDigest.getInstance("SHA-256");
                        } catch (NoSuchAlgorithmException ignored) {}
                        StringBuilder sha256=new StringBuilder();
                        for (byte b:messageDigest.digest(userInfo.data.email.toLowerCase().getBytes(StandardCharsets.UTF_8)))
                            sha256.append(String.format("%02x",b));
                        nowavatar.texture=new WebTextureResourceLocation(Uris.weavatarUri.toString()+sha256.toString()+".png?s=400").location;
                        nowuser.component=new TextComponent(userInfo.data.username);
                        nowid.component=new TextComponent("#"+userInfo.data.id);
                        nowid.x=10+nowuser.font.width(nowuser.component)+1;
                        nowemail.component=new TextComponent(userInfo.data.email);
                        nowgroup.component=new TextComponent(userInfo.data.friendlyGroup);
                        nowproxy.component=new TranslatableComponent("text.openlink.proxycount",userInfo.data.used,userInfo.data.proxies);
                        List<Pair<String,Long>> dataPoints=readTraffic();
                        dataPoints.add(new Pair<>(new TranslatableComponent("text.openlink.now").getString(),userInfo.data.traffic));
                        nowtraffic.dataPoints=dataPoints;
                        tabUser.set(0,nowavatar);
                        tabUser.set(1,nowuser);
                        tabUser.set(2,nowid);
                        tabUser.set(3,nowemail);
                        tabUser.set(4,nowgroup);
                        tabUser.set(5,nowproxy);
                        if (!userInfo.flag)
                            this.minecraft.setScreen(new LoginScreen(this, lastscreen));
                    }, "Request thread").start();
                }
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

    public List<Pair<String,Long>> readTraffic(){
        String origin=OpenLink.PREFERENCES.get("traffic_storage","");
        String[] spilt=origin.split(";");
        List<Pair<String,Long>> res=new ArrayList<>();
        for(String s:spilt) {
            if(!s.isEmpty()) {
                String[] split = s.split(",");
                res.add(new Pair<>(split[0], Long.parseLong(split[1])));
            }
        }
        return res;
    }

}
