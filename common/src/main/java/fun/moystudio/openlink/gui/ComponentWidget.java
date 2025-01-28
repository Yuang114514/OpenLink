package fun.moystudio.openlink.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ComponentWidget extends AbstractWidget {
    public final boolean centered;
    public final Font font;
    public final int color;
    public ComponentWidget(Font font1, int x1, int y1, int color1, Component component1, boolean centered1){
        super(x1,y1,font1.width(component1),font1.lineHeight,component1);
        font=font1;
        color=color1;
        centered=centered1;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if(centered) guiGraphics.drawCenteredString(font,this.getMessage(),this.getX(),this.getY(),color);
        else guiGraphics.drawString(font,this.getMessage(),this.getX(),this.getY(),color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE,this.getMessage());
    }
}
