package fun.moystudio.openlink;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;

public final class OpenLink {
    public static final String MOD_ID = "openlink";
    public static final Logger LOGGER = LogManager.getLogger("OpenLink");
    public static final String CONFIG_DIR = "config" + File.separator + MOD_ID + File.separator;

    public static void init() throws Exception {
        LOGGER.info("Initializing OpenLink!");
        File configdir=new File(CONFIG_DIR);
        configdir.mkdirs();
        //跳过ssl功能
        try{
            Request.POST("https://example.com/",Request.DEFAULT_HEADER,"{}",true);
        }catch (SSLHandshakeException e){
            e.printStackTrace();
            LOGGER.error("SSL Handshake Error! Ignoring SSL(Not Secure)");
            SSLUtils.ignoreSsl();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        if(!SSLUtils.sslIgnored){
            Frpc.init();//安装/检查更新frpc版本
            Request.readSession();//读取以前的SessionID
        }
        else{
            LOGGER.warn("SSL is ignored. The confirm screen will show after the main game screen loaded.");
        }
        LanConfig.readConfig();

        //直接用mixin打开更新屏幕就行
        LOGGER.info("  ✧✧✧✧✧✧✧✧✧✧✧✧✧✧  Star Carefree  ✧✧✧✧✧✧✧✧✧✧✧✧✧✧  \n" +
            "  ✧  ____                  _           _      _   ✧  \n" +
            "  ✧ / __ \\                | |         | |    | |  ✧  \n" +
            "  ✧| |  | | ___  _ __ ___| |__   __ _| | ___| |__✧  \n" +
            "  ✧| |  | |/ _ \\| '__/ _ \\ '_ \\ / _` | |/ _ \\ '_ \\  ✧  \n" +
            "  ✧| |__| | (_) | | |  __/ | | | (_| | |  __/ | | |✧  \n" +
            "  ✧ \\____/ \\___/|_|  \\___|_| |_|\\__,_|_|\\___|_| |_|  ✧  \n" +
            "  ✧                                               ✧  \n" +
            "  ✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧✧  \n");




    }
}
