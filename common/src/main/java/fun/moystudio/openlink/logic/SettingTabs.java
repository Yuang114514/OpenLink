package fun.moystudio.openlink.logic;

import net.minecraft.network.chat.Component;

public enum SettingTabs {
    LOG(Utils.translatableText("tab.openlink.setting_log")),//隧道日志
    USER(Utils.translatableText("tab.openlink.setting_user")),//用户信息
    INFO(Utils.translatableText("tab.openlink.setting_info")),//相关信息
    SETTING(Utils.translatableText("tab.openlink.setting_setting"));//设置
    public final Component component;
    SettingTabs(Component component1){
        component=component1;
    }
}
