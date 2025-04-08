package fun.moystudio.openlink.frpc;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrpcManager {
    Map<String, Pair<String, Class<? extends Frpc>>> frpcImplClasses = new HashMap<>();
    private final static Logger LOGGER = LogManager.getLogger("OpenLink/FrpcManager");
    public void init() {
        List<String> modPrefixes = OpenLink.GET_ALL_MOD_PREFIX.get();
        for (String prefix : modPrefixes) {
            frpcImplClasses.putAll(getFrpcImplByPrefix(prefix));
        }
    }
    public Map<String, Pair<String, Class<? extends Frpc>>> getFrpcImplByPrefix(String prefix){
        Set<Class<?>> classes = new Reflections(prefix).getTypesAnnotatedWith(OpenLinkFrpcImpl.class);
        Map<String, Pair<String, Class<? extends Frpc>>> res = new HashMap<>();
        for(Class<?> clazz:classes){
            if(Frpc.class.isAssignableFrom(clazz)){
                Class<? extends Frpc> clazz2 = clazz.asSubclass(Frpc.class);
                OpenLinkFrpcImpl annotation = clazz2.getAnnotation(OpenLinkFrpcImpl.class);
                res.put(annotation.id(),Pair.of(annotation.name(),clazz2));
            } else {
                LOGGER.error("Class {} is annotated with @OpenLinkFrpcImpl, but it does not implement Frpc!",clazz.getPackageName());
            }
        }
        return res;
    }
}
