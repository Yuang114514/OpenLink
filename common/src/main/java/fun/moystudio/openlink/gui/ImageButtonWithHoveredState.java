package fun.moystudio.openlink.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class ImageButtonWithHoveredState extends ImageButton {
    private final ResourceLocation resourceLocation;
    private final ResourceLocation resourceLocationHovered;

    public ImageButtonWithHoveredState(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, ResourceLocation rl2, int p, int q, OnPress onPress) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress);
        this.resourceLocation = resourceLocation;
        this.resourceLocationHovered = rl2;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderTexture(guiGraphics, this.isHoveredOrFocused() ? this.resourceLocationHovered : this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
