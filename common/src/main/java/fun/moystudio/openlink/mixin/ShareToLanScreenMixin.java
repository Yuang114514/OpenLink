package fun.moystudio.openlink.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
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

@Mixin(ShareToLanScreen.class)
public abstract class ShareToLanScreenMixin extends Screen{
    @Shadow private GameType gameMode;

    @Shadow private boolean commands;

    @Unique boolean isUsingFrp=true;

    @Unique EditBox editBox;

    @Unique boolean couldShare=true;

    protected ShareToLanScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init",at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        this.addRenderableWidget(CycleButton.onOffBuilder(isUsingFrp).create(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("text.openlink.usingfrp"),((cycleButton, bool) -> {
            this.isUsingFrp=bool;
        })));
        editBox=new EditBox(this.font,this.width / 2 + 5, 100, 150, 20,new TranslatableComponent("text.openlink.port"));
        editBox.setSuggestion(new TranslatableComponent("text.openlink.port").getString());
        editBox.setValue("11451");
        this.addRenderableWidget(editBox);
    }

    @Override
    public void tick(){
        String val = editBox.getValue();
        couldShare=true;
        if(!isUsingFrp){
            return;
        }
        if(val.length() != 5){
            couldShare=false;
            return;
        }
        for(int i=0;i<val.length();i++){
            if(!Character.isDigit(val.charAt(i))){
                couldShare=false;
                return;
            }
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
                    //差openfrp具体实现
                    this.minecraft.gui.getChat().addMessage(component);
                    this.minecraft.updateTitle();
                });
            }
        }
        return par1;
    }
}
