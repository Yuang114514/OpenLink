package fun.moystudio.openlink.frpc;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.*;
import fun.moystudio.openlink.logic.Extract;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Frpc {
    private static final String DEFAULT_FOLDER_NAME = "OpenFRP_0.60.1_c6b0deb1_20240914/";
    public static final int MAX_BUFFER_SIZE = 10485760;
    private static String suffix = "";
    private static String zsuffix = ".tar.gz";
    public static String osName;
    public static String osArch;
    public static boolean hasUpdate = false;
    public static int frpcVersionDate = 0;
    public static String folderName = DEFAULT_FOLDER_NAME;
    public static final File frpcVersionFile = new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"frpc.json");
    public static File frpcExecutableFile;
    public static File frpcArchiveFile;
    public static int latestVersionDate = 0;
    public static Process runtimeProcess = null;


    public static void init() throws Exception {
        Gson gson=new Gson();
        if(!frpcVersionFile.exists()){
            OpenLink.LOGGER.warn("frpc.json(frpc version file) does not exist, creating...");
            frpcVersionFile.createNewFile();
            try (FileOutputStream frpcVersionFileOutput = new FileOutputStream(frpcVersionFile)){
                frpcVersionFileOutput.write("{\"versiondate\":0}".getBytes());
            }
            OpenLink.LOGGER.info("Created frpc.json(frpc version file)!");
        }
        try(FileInputStream frpcVersionFileInput = new FileInputStream(frpcVersionFile)){
            frpcVersionDate=gson.fromJson(new String(frpcVersionFileInput.readAllBytes()), JsonFrpcVersion.class).versiondate;
        }
        String os_name=System.getProperty("os.name");
        osArch=System.getProperty("os.arch").toLowerCase();
        if(osArch.contains("i386")){
            osArch="386";
        }
        if(os_name.contains("Windows")) {
            osName="windows";
        } else if (os_name.contains("OS X")) {
            osName="darwin";
        } else if (os_name.contains("Linux")||os_name.contains("Unix")) {
            osName="linux";
        } else if (os_name.contains("FreeBSD")){
            osName="freebsd";
        } else {
            OpenLink.LOGGER.error("Unsupported operating system detected!");
            throw new RuntimeException("[OpenLink] Unsupported operating system detected!");
        }
        if(osName.equals("windows")){
            suffix=".exe";
            zsuffix=".zip";
        }
        frpcExecutableFile=new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"frpc_"+osName+"_"+osArch+suffix);
        frpcArchiveFile=new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"frpc"+zsuffix);
        if(checkUpdate()){
            OpenLink.LOGGER.info("The update screen will show after the main game screen loaded.");
        }
    }

    public static void update() throws Exception {
        downloadFrpcByUrl(Uris.frpcDownloadUri.toString()+folderName+"frpc_"+osName+"_"+osArch+zsuffix);
        OpenLink.LOGGER.info("Extracting frpc archive file...");
        Extract.ExtractBySuffix(frpcArchiveFile.getAbsoluteFile(),zsuffix);
        OpenLink.LOGGER.info("Extracted frpc archive file sucessfully!");
        frpcArchiveFile.delete();
        OpenLink.LOGGER.info("Deleted frpc archive file!");
        frpcVersionDate=latestVersionDate;
        try (FileOutputStream frpcVersionFileOutput = new FileOutputStream(frpcVersionFile)){
            frpcVersionFileOutput.write(("{\"versiondate\":"+ Integer.toString(latestVersionDate) +"}").getBytes());
        }
        hasUpdate=false;
    }

    public static int getLatestVersionDate() throws Exception{//这玩意是手写的POST(暂时不用后面写的logic包里的POST，因为这个是检测用的)
        Gson gson=new Gson();
        AtomicInteger res= new AtomicInteger(Math.max(20240914,frpcVersionDate));
        URL url= Uris.frpcDownloadUri.toURL();
        try{
            HttpsURLConnection connection=(HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setDoOutput(true);
            String jsonInput="{\"action\":\"get\",\"items\":{\"href\":\"/client/\",\"what\":1}}";
            try(OutputStream os=connection.getOutputStream()){
                byte[] in=jsonInput.getBytes("utf-8");
                os.write(in,0,in.length);
            }
            try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"))){
                StringBuilder re=new StringBuilder();
                String reline=null;
                while((reline=bufferedReader.readLine())!=null){
                    re.append(reline.trim());
                }
                JsonItems jsonItems=gson.fromJson(re.toString(),new TypeToken<JsonItems>(){}.getType());
                jsonItems.items.forEach((jsonDownloadFile)->{
                    if(jsonDownloadFile.href.contains("/client/OpenFRP")){
                        res.set(Math.max(res.get(),Integer.valueOf(jsonDownloadFile.href.substring(jsonDownloadFile.href.length() - 9, jsonDownloadFile.href.length() - 1))));
                        folderName=jsonDownloadFile.href.substring(jsonDownloadFile.href.length()-33);
                    }
                });

            }
        }catch (SSLHandshakeException e){
            e.printStackTrace();
            OpenLink.LOGGER.error("SSL Handshake Error! Ignoring SSL(Not Secure)");
            SSLUtils.ignoreSsl();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return res.get();
    }

    public static boolean checkUpdate() throws Exception {
        latestVersionDate=getLatestVersionDate();

        if(!frpcExecutableFile.exists()||frpcVersionDate<latestVersionDate){
            hasUpdate=true;
            if(!frpcExecutableFile.exists()){
                OpenLink.LOGGER.warn("Frpc Executable File does not exist!");
            } else {
                OpenLink.LOGGER.info("A frpc update was found! Latest version date:"+Integer.toString(latestVersionDate)+" Old version date:"+Integer.toString(frpcVersionDate));
            }
            return true;
        }
        hasUpdate=false;
        return false;
    }

    private static void downloadFrpcByUrl(String str) throws InterruptedException {
        Thread thread=new Thread(()->{
            OpenLink.LOGGER.info("Downloading/Updating frpc from "+str+"...");
            try {
                URL url = new URL(str);
                String fileName = "frpc" + zsuffix;
                BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
                FileOutputStream outputStream = new FileOutputStream(frpcArchiveFile);
                byte[] buffer = new byte[MAX_BUFFER_SIZE];
                int read;
                while ((read = inputStream.read(buffer, 0, MAX_BUFFER_SIZE)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                inputStream.close();
                outputStream.close();
                OpenLink.LOGGER.info("Download/Update frpc sucessfully!");
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        },"Frpc download thread");
        thread.start();
        thread.join();
    }

    public static void runFrpc(int proxyid) throws Exception {
        Request.getUserInfo();
        runtimeProcess =Runtime.getRuntime().exec(new String[]{frpcExecutableFile.getAbsolutePath(),"-u",Request.token,"-p",String.valueOf(proxyid)});
    }
    public static void stopFrpc(){
        if(runtimeProcess !=null){
            runtimeProcess.destroy();
        }
        runtimeProcess =null;
    }
    public static boolean openFrp(int i, String val){
        Frpc.stopFrpc();
        new Thread(()->{
            String finalval=val;
            Gson gson=new Gson();
            try {
                if(SSLUtils.sslIgnored){
                    //SSL警告
                    Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("text.openlink.sslwarning"));
                }
                Pair<String, Map<String, List<String>>> response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                //OpenLink隧道命名规则：openlink_mc_[本地端口号]
                for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                    if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                        try {
                            Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                            OpenLink.LOGGER.info("Deleted proxy: "+jsonUserProxy.proxyName);
                        } catch (Exception e) {
                            break;
                        }
                    }
                }//删除以前用过的隧道
                Thread.sleep(1000);
                response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                JsonResponseWithData<JsonUserInfo> userinfo=Request.getUserInfo();
                if(userinfo.data.proxies==userProxies.data.total){
                    throw new Exception(new TranslatableComponent("text.openlink.userproxieslimited").getString());
                }
                JsonResponseWithData<JsonTotalAndList<JsonNode>> nodelist=Request.getNodeList();
                List<JsonNode> canUseNodes=new ArrayList<>();
                for(JsonNode now:nodelist.data.list){
                    if(!now.group.contains(userinfo.data.group)||!now.protocolSupport.tcp||now.status!=200||now.fullyLoaded||(now.needRealname&&!userinfo.data.realname)){
                        continue;
                    }
                    canUseNodes.add(now);
                }
                if(canUseNodes.isEmpty()){
                    throw new Exception("Unable to use any node???");
                }
                canUseNodes.sort(((o1, o2) -> {
                    if(Math.abs(o1.bandwidth*o1.bandwidthMagnification-o2.bandwidth*o2.bandwidthMagnification)<1e-5)
                        return o2.bandwidth*o2.bandwidthMagnification>o1.bandwidth*o1.bandwidthMagnification?1:-1;
                    if(o1.classify!=o2.classify)
                        return (int)(o1.classify-o2.classify);
                    if(userinfo.data.realname&&o1.needRealname!=o2.needRealname)
                        return o1.needRealname?-1:1;
                    return 0;
                }));
                JsonNode node=canUseNodes.get(0);//选取最优节点
                JsonNewProxy newProxy=new JsonNewProxy();
                newProxy.name="openlink_mc_"+String.valueOf(i);
                newProxy.local_port= String.valueOf(i);
                newProxy.node_id=node.id;
                Random random=new Random();
                int start,end;
                if(node.allowPort==null||node.allowPort.isBlank()){
                    start=30000;
                    end=60000;
                }
                else{
                    start=Integer.parseInt(node.allowPort.substring(1,5));
                    end=Integer.parseInt(node.allowPort.substring(7,11));
                }
                boolean found=false;
                if((finalval==null||finalval.isBlank())&&LanConfig.cfg.last_port_value!=null&&!LanConfig.cfg.last_port_value.isBlank()){
                    finalval=LanConfig.cfg.last_port_value;
                }
                for (int j = 1; j <= 5; j++) {
                    newProxy.remote_port = random.nextInt(end - start + 1) + start;
                    if(finalval!=null&&!finalval.isBlank()&&j==1){
                        newProxy.remote_port=Integer.parseInt(finalval);
                    }
                    response=Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/newProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), gson.toJson(newProxy));
                    if(gson.fromJson(response.getFirst(), JsonResponseWithData.class).flag){
                        found=true;
                        break;
                    }
                }//创建隧道
                if(!found) throw new Exception(new TranslatableComponent("text.openlink.remoteportnotfound").getString());
                LanConfig.cfg.last_port_value=String.valueOf(newProxy.remote_port);
                response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                JsonUserProxy runningproxy=null;
                for(JsonUserProxy jsonUserProxy:userProxies.data.list){
                    if(jsonUserProxy.proxyName.equals("openlink_mc_"+String.valueOf(i))){
                        runningproxy=jsonUserProxy;
                        break;
                    }
                }
                if(runningproxy==null) throw new Exception("Can not find the proxy???");
                //启动Frpc
                Frpc.runFrpc((int) runningproxy.id);
                //check
                Thread.sleep(5000);
                response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                runningproxy=null;
                for(JsonUserProxy jsonUserProxy:userProxies.data.list){
                    if(jsonUserProxy.proxyName.equals("openlink_mc_"+String.valueOf(i))){
                        runningproxy=jsonUserProxy;
                        break;
                    }
                }
                if(runningproxy==null) throw new Exception("Can not find the proxy???");
                if(!runningproxy.online){
                    Frpc.stopFrpc();
                    throw new Exception("Can not start frpc???");
                }
                JsonUserProxy finalRunningproxy = runningproxy;
                Component tmp=(new TranslatableComponent("text.openlink.frpcstartsucessfully","§n"+finalRunningproxy.connectAddress))
                        .withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, finalRunningproxy.connectAddress))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(finalRunningproxy.connectAddress)))));
                Minecraft.getInstance().gui.getChat().addMessage(tmp);
            } catch (Exception e) {
                Component tmp=new TextComponent("§4[OpenLink] "+e.getMessage());
                e.printStackTrace();
                Minecraft.getInstance().gui.getChat().addMessage(tmp);
                tmp=ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("text.openlink.clicktorestart"))
                        .withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/proxyrestart"))));
                Minecraft.getInstance().gui.getChat().addMessage(tmp);
            }
        },"Proxy start thread").start();

        return true;
    }

}
