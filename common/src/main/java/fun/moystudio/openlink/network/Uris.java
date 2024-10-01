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
            openFrpAPIUri = new URI("https://of-dev-api.bfsea.xyz/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
