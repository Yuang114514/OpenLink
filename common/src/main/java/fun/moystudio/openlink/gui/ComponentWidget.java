package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class ComponentWidget implements Widget, GuiEventListener {
    public Component component;
    public final boolean centered;
    public final Font font;
    public int x;
    public int y;
    public final int color;
    public ComponentWidget(Font font1, int x1, int y1, int color1, Component component1, boolean centered1){
        component=component1;
        centered=centered1;
        font=font1;
        x=x1;
        y=y1;
        color=color1;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if(centered) GuiComponent.drawCenteredString(poseStack,font,component,x,y,color);
        else GuiComponent.drawString(poseStack,font,component,x,y,color);
    }
}
