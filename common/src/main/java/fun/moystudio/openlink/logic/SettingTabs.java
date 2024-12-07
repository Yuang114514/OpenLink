package fun.moystudio.openlink.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum SettingTabs {
    LOG(new TranslatableComponent("tab.openlink.setting_log")),//隧道日志
    USER(new TranslatableComponent("tab.openlink.setting_user")),//用户信息
    INFO(new TranslatableComponent("tab.openlink.setting_info")),//相关信息
    SETTING(new TranslatableComponent("tab.openlink.setting_setting"));//设置
    public final Component component;
    SettingTabs(Component component1){
        component=component1;
    }
}
