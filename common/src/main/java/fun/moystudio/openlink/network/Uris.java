package fun.moystudio.openlink.network;

import java.net.URI;
import java.net.URISyntaxException;

public class Uris {
    public static final URI frpcDownloadUri;

    static {
        try {
            frpcDownloadUri = new URI("https://o.of.cd/client/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final URI frpcDownloadUri1;

    static {
        try {
            frpcDownloadUri1 = new URI("https://r.zyghit.cn/client/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final URI openidLoginUri;

    static {
        try {
            openidLoginUri = new URI("https://openid.17a.ink/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final URI openFrpAPIUri;

    static {
        try {
            openFrpAPIUri = new URI("https://api.openfrp.net/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final URI ipstackUri;

    static {
        try{
            ipstackUri = new URI("https://fcd09628a76b.aapq.net/ip");
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    public static final URI weavatarUri;

    static {
        try{
            weavatarUri = new URI("https://weavatar.com/avatar/");
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

}
