package fun.moystudio.openlink.frpc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OpenLinkFrpcImpl {
    String id();
    String name() default "";
}
