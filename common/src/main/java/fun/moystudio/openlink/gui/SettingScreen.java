package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class SettingScreen extends Screen {
    public SettingScreen() {
        super(new TranslatableComponent("gui.openlink.settingscreentitle"));
    }
    public MultiLineLabel title;
    @Override
    protected void init(){
        title=MultiLineLabel.create(this.font,new TranslatableComponent("gui.openlink.settingscreentitle"));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }));
    }

    @Override
    public void render(PoseStack poseStack,int i,int j,float f){
        this.renderDirtBackground(0);
        title.renderCentered(poseStack,this.width/2,15);
        super.render(poseStack,i,j,f);
    }

}
