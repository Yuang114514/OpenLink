package fun.moystudio.openlink.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface IScreenAccessor {
    @Accessor("children")
    List<GuiEventListener> getChildren();
    @Invoker("addButton")
    <T extends AbstractWidget> T invokeAddButton(T abstractWidget);
}
