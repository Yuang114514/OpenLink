package fun.moystudio.openlink.logic;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonLanConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class LanConfig {
    public static JsonLanConfig cfg=new JsonLanConfig();
    public static final File cfgfile=new File(OpenLink.CONFIG_DIR+"lanconfig.json");
    public static OnlineModeTabs getAuthMode(){
        return OnlineModeTabs.valueOf(cfg.auth_mode);
    }
    public static void setAuthMode(OnlineModeTabs o){
        cfg.auth_mode=o.name();
    }
    public static void readConfig() throws Exception {
        Gson gson=new Gson();
        if(!cfgfile.exists()){
            cfgfile.createNewFile();
            writeConfig();
        }
        FileInputStream fi=new FileInputStream(cfgfile);
        cfg=gson.fromJson(new String(fi.readAllBytes(),"utf-8"),new TypeToken<JsonLanConfig>(){}.getType());
    }
    public static void writeConfig() throws Exception {
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        cfgfile.createNewFile();
        FileOutputStream fo = new FileOutputStream(cfgfile);
        fo.write(gson.toJson(cfg,new TypeToken<JsonLanConfig>(){}.getType()).getBytes(StandardCharsets.UTF_8));
    }
}
