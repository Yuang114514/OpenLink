package fun.moystudio.openlink.frpcimpl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.JsonDownloadFile;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SakuraFrpFrpcImpl implements Frpc {
    private final static Logger LOGGER = LogManager.getLogger(SakuraFrpFrpcImpl.class);
    private String osArch,osName, archiveSuffix;
    public List<String> downloadUrls = new ArrayList<>();

    @Override
    public String id() {
        return "sakurafrp";
    }

    @Override
    public String name() {
        return "SakuraFrp";
    }

    @Override
    public ResourceLocation getIcon() {
        return Utils.createResourceLocation("openlink", "textures/gui/sakurafrp_icon.png");
    }

    @Override
    public void init() throws Exception{
        String os_arch=System.getProperty("os.arch").toLowerCase(),os_name=System.getProperty("os.name");
        if(os_arch.contains("i386")){
            os_arch="386";
        }
        if(os_name.contains("Windows")) {
            osName="windows";
        } else if (os_name.contains("OS X")) {
            osName="darwin";
            os_arch=os_arch.equals("x86_64")?"amd64":"arm64";
        } else if (os_name.contains("Linux")||os_name.contains("Unix")) {
            osName="linux";
        } else if (os_name.contains("FreeBSD")){
            osName="freebsd";
        } else {
            LOGGER.error("Unsupported operating system detected!");
            throw new Exception("[OpenLink] Unsupported operating system detected!");
        }
        osArch = os_arch;
        if(osName.equals("windows")){
            archiveSuffix=".zip";
        } else {
            archiveSuffix=".tar.gz";
        }
        Gson gson = new Gson();
        JsonResponseWithData<JsonDownloadFile> response = gson.fromJson(Request.GET(Uris.openFrpAPIUri+"commonQuery/get?key=software", Request.DEFAULT_HEADER).getFirst(),new TypeToken<JsonResponseWithData<JsonDownloadFile>>(){}.getType());
        response.data.source.forEach(source -> {
            downloadUrls.add(source.value+"/");
        });
//        readSession();
    }

    @Override
    public boolean isOutdated(@Nullable Path frpcExecutableFilePath) {
        return false;
    }

    @Override
    public Process createFrpcProcess(Path frpcExecutableFilePath, int localPort, @Nullable String remotePort) throws Exception {
        return null;
    }

    @Override
    public String createProxy(int localPort, @Nullable String remotePort) throws Exception {
        return "";
    }

    @Override
    public String getFrpcVersion(Path frpcExecutableFilePath) {
        return "";
    }
}
