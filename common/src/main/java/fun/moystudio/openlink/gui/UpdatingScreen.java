package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.frpc.Frpc;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;

public class UpdatingScreen extends Screen {
    @Unique
    public int tickCount=0;
    public UpdatingScreen() {
        super(new TranslatableComponent("text.openlink.updatefrpc"));
    }
    public MultiLineLabel text;
    boolean updated=false;
    @Override
    protected void init() {
        text=MultiLineLabel.create(this.font,(FormattedText) new TranslatableComponent("text.openlink.updatingfrpc"),this.width-50);
    }
    @Override
    public void tick(){
        tickCount++;
        if(updated){
            this.onClose();
        }
        if(tickCount==5){
            new Thread(()->{
                try {
                    Frpc.update();
                    updated=true;
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }, "Frpc download thread").start();
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        text.renderCentered(poseStack,this.width/2,this.height/2,16,0xffffff);
        super.render(poseStack,i,j,f);
    }
}
