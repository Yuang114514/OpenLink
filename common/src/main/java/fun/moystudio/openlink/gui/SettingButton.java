package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SettingButton extends ImageButton {
    private ResourceLocation resourceLocation;
    private int yTexStart;
    private int xTexStart;
    private int yDiffTex;
    private int textureWidth;
    private int textureHeight;
    private static final ResourceLocation HOVERED=new ResourceLocation("openlink","textures/gui/setting_hover.png");

    public SettingButton(int i, int j, int k, int l, int m, int n, ResourceLocation resourceLocation, OnPress onPress) {
        super(i, j, k, l, m, n, resourceLocation, onPress);
        this.xTexStart = m;
        this.yTexStart = n;
        this.resourceLocation = resourceLocation;
    }

    public SettingButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, OnPress onPress) {
        super(i, j, k, l, m, n, o, resourceLocation, onPress);
        this.xTexStart = m;
        this.yTexStart = n;
        this.yDiffTex = o;
        this.resourceLocation = resourceLocation;
    }

    public SettingButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, OnPress onPress) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress);
        this.textureWidth = p;
        this.textureHeight = q;
        this.xTexStart = m;
        this.yTexStart = n;
        this.yDiffTex = o;
        this.resourceLocation = resourceLocation;
    }

    public SettingButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, OnPress onPress, Component component) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress, component);
        this.textureWidth = p;
        this.textureHeight = q;
        this.xTexStart = m;
        this.yTexStart = n;
        this.yDiffTex = o;
        this.resourceLocation = resourceLocation;
    }

    public SettingButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, OnPress onPress, OnTooltip onTooltip, Component component) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress, onTooltip, component);
        this.textureWidth = p;
        this.textureHeight = q;
        this.xTexStart = m;
        this.yTexStart = n;
        this.yDiffTex = o;
        this.resourceLocation = resourceLocation;
    }
    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        int k = this.yTexStart;
        if (this.isHoveredOrFocused()) {
            k += this.yDiffTex;
            RenderSystem.setShaderTexture(0, HOVERED);
        }

        RenderSystem.enableDepthTest();
        blit(poseStack, this.x, this.y, (float)this.xTexStart, (float)k, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered) {
            this.renderToolTip(poseStack, i, j);
        }
    }
}
