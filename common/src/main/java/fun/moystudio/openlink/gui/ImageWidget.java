package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

public class ImageWidget implements Widget, GuiEventListener {
    public ResourceLocation texture;
    private int x,y,width,height,textureWidth,textureHeight;
    private float uOffset,vOffset;
    public ImageWidget(int x1, int y1, float uOffset1, float vOffset1, int width1, int height1, int textureWidth1, int textureHeight1, ResourceLocation rl){
        x=x1;
        y=y1;
        uOffset=uOffset1;
        vOffset=vOffset1;
        width=width1;
        height=height1;
        textureWidth=textureWidth1;
        textureHeight=textureHeight1;
        texture=rl;
    }
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
        RenderSystem.setShaderTexture(0,texture);
        GuiComponent.blit(poseStack,x,y,uOffset,vOffset,width,height,textureWidth,textureHeight);
    }
}
