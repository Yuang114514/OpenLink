package fun.moystudio.openlink.frpc;

import com.google.gson.Gson;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonFrpcVersion;
import fun.moystudio.openlink.json.JsonItems;
import fun.moystudio.openlink.logic.Extract;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class Frpc {
    private static String suffix="";//默认Linux执行文件后缀（若为Windows会改为.exe）
    private static String zsuffix=".tar.gz";//默认Linux压缩后缀（若为windows会改为zip）
    public static String osName;
    public static String osArch;
    public static boolean hasUpdate=false;
    public static int frpcVersionDate=0;
    public static String folderName="";
    public static final File frpcVersionFile=new File("frpc.json");
    public static File frpcExecutableFile;
    public static File frpcArchiveFile=new File("frpc.zip");
    public static final int MAX_BUFFER_SIZE=10485760;
    public static int latestVersionDate=0;
    public static Process runtimeprocess=null;

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
        } else if (os_name.contains("Mac")) {
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
        frpcExecutableFile=new File("frpc_"+osName+"_"+osArch+suffix);
        if(checkUpdate()){
            if(!frpcExecutableFile.exists()){
                update();
            }
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
        AtomicInteger res= new AtomicInteger(frpcVersionDate);
        URL url= Uris.frpcDownloadUri.toURL();
        HttpsURLConnection connection=(HttpsURLConnection)url.openConnection();
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
            JsonItems jsonItems=gson.fromJson(re.toString(),JsonItems.class);
            jsonItems.items.forEach((jsonDownloadFile)->{
                if(jsonDownloadFile.href.contains("/client/OpenFRP")){
                    res.set(Math.max(res.get(),Integer.valueOf(jsonDownloadFile.href.substring(jsonDownloadFile.href.length() - 9, jsonDownloadFile.href.length() - 1))));
                    folderName=jsonDownloadFile.href.substring(jsonDownloadFile.href.length()-33);
                }
            });

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

    private static void downloadFrpcByUrl(String str) throws Exception {
        OpenLink.LOGGER.info("Downloading/Updating frpc from "+str+"...");
        URL url=new URL(str);
        String fileName="frpc"+zsuffix;
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
        FileOutputStream outputStream = new FileOutputStream(fileName);
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int read;
        while((read=inputStream.read(buffer,0,MAX_BUFFER_SIZE))!=-1){
            outputStream.write(buffer,0,read);
        }
        inputStream.close();
        outputStream.close();
        OpenLink.LOGGER.info("Download/Update frpc sucessfully!");
    }

    public static void runFrpc(int proxyid) throws Exception {
        Request.getUserInfo();
        runtimeprocess=Runtime.getRuntime().exec(new String[]{frpcExecutableFile.getAbsolutePath(),"-u",Request.token,"-p",String.valueOf(proxyid)});
    }
    public static void stopFrpc(){
        if(runtimeprocess!=null){
            runtimeprocess.destroy();
        }
        runtimeprocess=null;
    }
}
