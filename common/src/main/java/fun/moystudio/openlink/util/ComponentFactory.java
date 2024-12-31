package fun.moystudio.openlink.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.TextComponent;

public class ComponentFactory {

    //组件类型的枚举。
    public enum ComponentType {
        TRANSLATABLE, // 可翻译组件
        TEXT          // 文本组件
    }

    /**
     * 根据指定的类型创建聊天组件
     * @param type 组件的类型（TRANSLATABLE 或 TEXT）
     * @param keyOrText 翻译键或要显示的纯文本
     * @param args 可选的翻译参数
     * @return 创建的聊天组件
     * @throws IllegalArgumentException 如果组件类型未知
     */
    public static Component createComponent(ComponentType type, String keyOrText, Object... args) {
        switch (type) {
            case TRANSLATABLE:
                return new TranslatableComponent(keyOrText, args);
            case TEXT:
                return new TextComponent(keyOrText);
            default:
                throw new IllegalArgumentException("Unknown component type:" + type);
        }
    }

    /**
     * @param key 翻译键
     * @param args 可选的翻译参数
     * @return 创建的可翻译聊天组件
     */
    public static Component createTranslatable(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    /**
     * @param text 要显示的纯文本
     * @return 创建的文本聊天组件
     */
    public static Component createText(String text) {
        return new TextComponent(text);
    }
}
