package fun.moystudio.openlink.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum OnlineModeTabs {
    ONLINE_MODE(new TranslatableComponent("cycle.openlink.onlinemode")),OFFLINE_MODE(new TranslatableComponent("cycle.openlink.offlinemode"));
    public Component component;
    OnlineModeTabs(Component component1){
        component=component1;
    }
}
