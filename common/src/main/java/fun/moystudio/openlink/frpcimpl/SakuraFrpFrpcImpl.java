package fun.moystudio.openlink.frpcimpl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.JsonFrpcSakura;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class SakuraFrpFrpcImpl implements Frpc {
    private final static Logger LOGGER = LogManager.getLogger(SakuraFrpFrpcImpl.class);
    private String osArch,osName,downloadUrl,latestVersion,frpcVersion;
    public static String token;

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
        readSession();
    }

    @Override
    public boolean isOutdated(@Nullable Path frpcExecutableFilePath) {
        return checkUpdate(frpcExecutableFilePath);
    }

    @Override
    public List<String> getUpdateFileUrls() {
        return List.of(downloadUrl);
    }

    private static void readSession() {
        token=OpenLink.PREFERENCES.get("token_sakura",null);
        if(token==null||token.equals("null")){
            token=null;
            LOGGER.warn("The session does not exists in user preferences!");
            return;
        }
        try{
            JsonResponseWithData<JsonUserInfo> responseWithData = /*getUserInfo()*/null;//TODO: getUserInfo()
            if(responseWithData==null||!responseWithData.flag){
                token=null;
                writeSession();
                LOGGER.warn("The session has been expired!");
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void writeSession() {
        OpenLink.PREFERENCES.put("token_sakura", Objects.requireNonNullElse(token, "null"));
    }

    private boolean checkUpdate(Path path) {
        Gson gson=new Gson();
        JsonFrpcSakura response;
        try {
            response = gson.fromJson(Request.GET(Uris.sakuraFrpAPIUri+"/system/clients", Request.DEFAULT_HEADER).getFirst(),new TypeToken<JsonFrpcSakura>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        downloadUrl = response.frpc.archs.get(osName+"_"+osArch).url;
        boolean result = false;
        latestVersion=response.frpc.ver;
        if(path == null || !path.toFile().exists()){
            LOGGER.warn("The frpc executable file does not exist!");
            result = true;
        } else {
            getFrpcVersion(path);
            if(frpcVersion==null||!frpcVersion.equals(latestVersion)){
                LOGGER.info("A frpc update was found! Latest version:{} Old version:{}", latestVersion, frpcVersion);
                result = true;
            }
        }
        return result;
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
        try {
            String version = new String(Runtime.getRuntime().exec(new String[]{frpcExecutableFilePath.toFile().getAbsolutePath(),"-v"}).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return frpcVersion = version.substring(0,version.length()-1);
        } catch (Exception e) {
            return "does not exists";
        }
    }
}
