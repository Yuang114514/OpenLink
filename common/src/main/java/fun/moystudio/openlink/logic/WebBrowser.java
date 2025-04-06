package fun.moystudio.openlink.logic;
import net.minecraft.Util;

public class WebBrowser {
    public boolean browserOpened=false;
    public String url;
    public WebBrowser(String u){
        url=u;
    }

    public void openBrowser(){
        if(!browserOpened) {
            Util.getPlatform().openUri(url);
            browserOpened = true;
        }
    }
}