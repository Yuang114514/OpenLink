package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

public class LogSelectionList extends ObjectSelectionList<LogSelectionList.LogSelectionListEntry> {
    public LogSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }
    public final class LogSelectionListEntry extends ObjectSelectionList.Entry<LogSelectionListEntry>{

        public final Level world;
        public final Minecraft minecraft;
        public final SettingScreen screen;

        public LogSelectionListEntry(Level world, Minecraft minecraft, SettingScreen screen) {
            this.world = world;
            this.minecraft = minecraft;
            this.screen=screen;
        }

        @Override
        public Component getNarration() {
            return new TextComponent("");
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {

        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            return super.mouseClicked(d, e, i);
        }
    }
}
