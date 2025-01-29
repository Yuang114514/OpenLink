package fun.moystudio.openlink.gui;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class ImageButtonWithHoveredState extends ImageButton {
    public ImageButtonWithHoveredState(int i, int j, int k, int l, ResourceLocation rl1, ResourceLocation rl2, OnPress onPress) {
        super(i, j, k, l, new WidgetSprites(rl1, rl2), onPress);
    }
}
