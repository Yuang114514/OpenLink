package fun.moystudio.openlink.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum OnlineModeTabs {
    ONLINE_MODE(new TranslatableComponent("cycle.openlink.onlinemode")),OFFLINE_MODE(new TranslatableComponent("cycle.openlink.offlinemode")),OFFLINE_FIXUUID(new TranslatableComponent("cycle.openlink.fixuuid"));
    public final Component component;
    OnlineModeTabs(Component component1){
        component=component1;
    }
}
