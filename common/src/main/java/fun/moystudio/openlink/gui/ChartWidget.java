package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;

public class ChartWidget extends GuiComponent implements Widget {
    public Component title;
    public int x,y,width,height,color;
    public ChartWidget(int x, int y, int width, int height, Component title, int color) {
        this.title=title;
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.color=color;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        fill(poseStack,x,y,x+width,y+height,color);
        drawCenteredString(poseStack,Minecraft.getInstance().font,title,x+width/2,y+5,0xffffff);
    }
}
