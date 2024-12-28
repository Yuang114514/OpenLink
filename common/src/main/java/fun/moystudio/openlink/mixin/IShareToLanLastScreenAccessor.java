package fun.moystudio.openlink.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShareToLanScreen.class)
public interface IShareToLanLastScreenAccessor {
    @Accessor("lastScreen")
    Screen getLastScreen();
}
