package fun.moystudio.openlink.mixin;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.SettingButton;
import fun.moystudio.openlink.gui.SettingScreen;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.logic.OnlineModeTabs;
import fun.moystudio.openlink.logic.UUIDFixer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(ShareToLanScreen.class)
public abstract class ShareToLanScreenMixin extends Screen{
    @Unique
    private static final ResourceLocation OPENLINK_SETTING = new ResourceLocation("openlink", "textures/gui/setting.png");

    @Shadow private GameType gameMode;

    @Shadow private boolean commands;

    @Unique
    private static EditBox editBox;

    @Unique CycleButton<Boolean> usingfrp;

    @Unique CycleButton<OnlineModeTabs> onlinemode;

    @Unique CycleButton<Boolean> allowpvp;

    @Unique boolean couldShare=true;

    protected ShareToLanScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init",at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        editBox=new EditBox(this.font,this.width / 2 + 5, 160, 150, 20,new TranslatableComponent("text.openlink.port"));
        editBox.setSuggestion(new TranslatableComponent("text.openlink.port").getString());
        editBox.setValue(LanConfig.cfg.last_port_value);
        this.addRenderableWidget(editBox);
        usingfrp=CycleButton.onOffBuilder(LanConfig.cfg.use_frp).create(this.width / 2 - 155, 160, 150, 20, new TranslatableComponent("text.openlink.usingfrp"),((cycleButton, bool) -> {
            LanConfig.cfg.use_frp=bool;
            editBox.setVisible(LanConfig.cfg.use_frp);
        }));
        onlinemode=CycleButton.builder((OnlineModeTabs o)-> o.component)
                .withValues(OnlineModeTabs.values())
                .withInitialValue(LanConfig.getAuthMode())
                .create(this.width / 2 - 155, 130, 150, 20, new TranslatableComponent("text.openlink.onlinemodebutton"),   (button, o)->LanConfig.setAuthMode(o));
        allowpvp=CycleButton.onOffBuilder(LanConfig.cfg.allow_pvp).create(this.width / 2 + 5, 130, 150, 20, new TranslatableComponent("mco.configure.world.pvp"),(cycleButton, object) -> LanConfig.cfg.allow_pvp=object);
        this.addRenderableWidget(usingfrp);
        this.addRenderableWidget(onlinemode);
        this.addRenderableWidget(allowpvp);
        this.addRenderableWidget(new SettingButton(this.width / 2 + 5+150+10, this.height - 28,
                20, 20, 0, 0, 20, OPENLINK_SETTING, 20, 20, (button) -> {
            this.minecraft.setScreen(new SettingScreen(this));
        }));
    }

    @Override
    public void tick(){
        String val = editBox.getValue();
        couldShare=true;
        if(val.isBlank()||!LanConfig.cfg.use_frp){
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
                    this.minecraft.getSingleplayerServer().setUsesAuthentication(LanConfig.getAuthMode()==OnlineModeTabs.ONLINE_MODE);
                    this.minecraft.getSingleplayerServer().setPvpAllowed(LanConfig.cfg.allow_pvp);
                    UUIDFixer.EnableUUIDFixer=LanConfig.getAuthMode()==OnlineModeTabs.OFFLINE_FIXUUID;
                    UUIDFixer.ForceOfflinePlayers=Collections.emptyList();//暂时用着，后面再改
                    //以上是(被我修改了一点的)原版的代码，以下是OpenLink的Frpc启动及隧道创建，节点选择等主要功能
                    if(!LanConfig.cfg.use_frp){
                        return;
                    }
                    Frpc.openFrp(i,editBox.getValue());
                    try {
                        LanConfig.writeConfig();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return par1;
    }
}
