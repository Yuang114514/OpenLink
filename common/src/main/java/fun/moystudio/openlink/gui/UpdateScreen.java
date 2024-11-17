package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.frpc.Frpc;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateScreen extends Screen {
    public UpdateScreen() {
        super(new TranslatableComponent("gui.openlink.updatefrpctitle"));
    }
    public Button yes;
    public Button no;
    public MultiLineLabel text;

    @Override
    protected void init(){
        List<String> strings=Arrays.asList(new TranslatableComponent("text.openlink.nofrpcfile").getString().split("\n"));
        List<Component> list = new ArrayList<>();
        strings.forEach((String)->{
            list.add(new TextComponent(String));
        });
        yes=new Button(this.width/4-40,this.height/5*4-10,80,20,CommonComponents.GUI_YES,button -> {
            this.minecraft.setScreen(new UpdatingScreen());
        });
        no=new Button(this.width/4*3-40,this.height/5*4-10,80,20,CommonComponents.GUI_NO,button -> {
            this.onClose();
        }, (button, poseStack, i, j) -> {
            if(Frpc.frpcVersionDate==0){
                renderComponentTooltip(poseStack, list, i, j);
            }
        });
        if(Frpc.frpcVersionDate==0){
            no.active=false;
        }
        text=MultiLineLabel.create(this.font,(FormattedText) new TranslatableComponent("text.openlink.updatefrpc", Frpc.latestVersionDate, String.valueOf(Frpc.frpcVersionDate == 0 ? "does not exist" : Frpc.frpcVersionDate)),this.width-50);
        this.addRenderableWidget(yes);
        this.addRenderableWidget(no);
        //以下为原版语言按钮(修改了一下位置)
        this.addRenderableWidget(new ImageButton(this.width/4-70, this.height/5*4-10, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> {
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
        }, new TranslatableComponent("narrator.button.language")));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        text.renderCentered(poseStack,this.width/2,this.height/10,16,0xffffff);
        super.render(poseStack,i,j,f);
    }
}
