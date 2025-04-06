package fun.moystudio.openlink.frpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface OpenLinkFrpcImpl {
    String id();
    String name() default "";
}
