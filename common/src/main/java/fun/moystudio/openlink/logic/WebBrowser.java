package fun.moystudio.openlink.logic;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.network.Uris;

import java.lang.reflect.Method;

public class WebBrowser {
    public boolean browserOpened=false;
    public Process browserProcess=null;
    public String url=null;
    public WebBrowser(String u){
        url=u;
    }
    public void openBrowser(){
        if(!browserOpened) {
            try{
                switch (Frpc.osName) {
                    case "windows" ->
                        browserProcess=Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);//Windows自带的链接打开方式
                    case "darwin" -> {//百度搜的macos，没用过（
                        Class fileMgr = Class.forName("com.apple.eio.FileManager");
                        Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                        openURL.invoke(null, url);
                    }
                    case "linux", "freebsd" -> {
                        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};//Linux常用浏览器

                        String browser = null;
                        for (int i = 0; i < browsers.length && browser == null; i++) {
                            if (Runtime.getRuntime().exec(new String[]{"which", browsers[i]}).waitFor() == 0) {
                                browser = browsers[i];
                            }
                        }
                        if (browser == null) {
                            OpenLink.LOGGER.error("What the hell are you using?");
                            throw new RuntimeException("[OpenLink] What the hell are you using?");
                        }
                        browserProcess=Runtime.getRuntime().exec(browser + " " + url);
                    }
                    default -> {
                        OpenLink.LOGGER.error("What the hell are you using?");
                        throw new RuntimeException("[OpenLink] What the hell are you using?");
                    }
                }
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
            browserOpened=true;
        }
    }

}
