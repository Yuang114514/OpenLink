package fun.moystudio.openlink;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import fun.moystudio.openlink.network.Uris;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.PrintStream;

public final class OpenLink {
    public static final String MOD_ID = "openlink";
    public static final Logger LOGGER = LogManager.getLogger("OpenLink");

    public static void init() throws Exception {
        LOGGER.info("Initializing OpenLink!");
        //由于某作者的逆天电脑，特意添加跳过ssl功能
        try{
            Request.POST("https://example.com/",Request.DEFAULT_HEADER,"{}",true);
        }catch (SSLHandshakeException e){
            e.printStackTrace();
            LOGGER.error("SSL Handshake Error! Ignoring SSL(Not Secure)");
            SSLUtils.ignoreSsl();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        if(!SSLUtils.SSLIgnored){
            Frpc.init();//安装/检查更新frpc版本
            Request.readSession();//读取以前的SessionID
        }
        else{
            LOGGER.warn("SSL is ignored. The confirm screen will show after the main game screen loaded.");
        }

        //直接用mixin打开更新屏幕就行
        LOGGER.info("\n   ____                       _       _         _    \n" +
                "  / __ \\                     | |     (_)       | |   \n" +
                " | |  | | _ __    ___  _ __  | |      _  _ __  | | __\n" +
                " | |  | || '_ \\  / _ \\| '_ \\ | |     | || '_ \\ | |/ /\n" +
                " | |__| || |_) ||  __/| | | || |____ | || | | ||   < \n" +
                "  \\____/ | .__/  \\___||_| |_||______||_||_| |_||_|\\_\\\n" +
                "         | |                                         \n" +
                "         |_|                                         ");

    }
}
