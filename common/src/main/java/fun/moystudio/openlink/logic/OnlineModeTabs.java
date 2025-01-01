package fun.moystudio.openlink.logic;

import net.minecraft.network.chat.Component;

public enum OnlineModeTabs {
    ONLINE_MODE(Utils.translatableText("cycle.openlink.onlinemode")),
    OFFLINE_MODE(Utils.translatableText("cycle.openlink.offlinemode")),
    OFFLINE_FIXUUID(Utils.translatableText("cycle.openlink.fixuuid"));
    public final Component component;
    OnlineModeTabs(Component component1){
        component=component1;
    }
}
