package fun.moystudio.openlink.network;

import java.net.URI;
import java.net.URISyntaxException;

public class Uris {
    public static final URI frpcDownloadUri,frpcDownloadUri1,frpcDownloadUri2,openidLoginUri,openFrpAPIUri,ipstackUri,weavatarUri,advertiseUri;
    static {
        try {
            frpcDownloadUri = new URI("https://o.of.cd/client/");
            frpcDownloadUri1 = new URI("https://r.zyghit.cn/client/");
            frpcDownloadUri2 = new URI("https://staticassets.naids.com/client/");
            openidLoginUri = new URI("https://account.naids.com/");
            openFrpAPIUri = new URI("https://api.openfrp.net/");
            ipstackUri = new URI("https://fcd09628a76b.aapq.net/ip");
            weavatarUri = new URI("https://weavatar.com/avatar/");
            advertiseUri = new URI("https://started.ink/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
