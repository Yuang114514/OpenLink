package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SettingScreenButton extends Button {
    public static final ResourceLocation SETTING_WIDGET_LOCATION=new ResourceLocation("openlink","textures/gui/widgets_setting.png");
    public SettingScreenButton(int i, int j, int k, int l, Component component, OnPress onPress) {
        super(i, j, k, l, component, onPress);
    }
    protected int packedFGColor = -1;

    public SettingScreenButton(int i, int j, int k, int l, Component component, OnPress onPress, OnTooltip onTooltip) {
        super(i, j, k, l, component, onPress, onTooltip);
    }

    @Override
    public void renderButton(PoseStack arg, int k, int l, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SETTING_WIDGET_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(arg, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(arg, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(arg, minecraft, k, l);
        int j = this.getFGColor();
        drawCenteredString(arg, font, this.getMessage(), this.x + (this.width/2), this.y + (this.height-8)/2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    public int getFGColor() {
        if (this.packedFGColor != -1) {
            return this.packedFGColor;
        } else {
            return this.active ? 16777215 : 10526880;
        }
    }

    public void setFGColor(int color) {
        this.packedFGColor = color;
    }

    public void clearFGColor() {
        this.packedFGColor = -1;
    }

}
