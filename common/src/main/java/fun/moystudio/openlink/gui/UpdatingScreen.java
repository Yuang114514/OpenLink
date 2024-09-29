package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.frpc.Frpc;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class UpdatingScreen extends Screen {
    public UpdatingScreen() {
        super(new TranslatableComponent("text.openlink.updatefrpc"));
    }
    public MultiLineLabel text;
    @Override
    protected void init() {
        text=MultiLineLabel.create(this.font,(FormattedText) new TranslatableComponent("text.openlink.updatingfrpc"),this.width-50);
        super.init();


    }
    @Override
    public void tick(){
        if(Frpc.hasUpdate){
            try {
                Frpc.update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.onClose();
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        text.renderCentered(poseStack,this.width/2,this.height/2,16,0xffffff);
        super.render(poseStack,i,j,f);
    }
}
