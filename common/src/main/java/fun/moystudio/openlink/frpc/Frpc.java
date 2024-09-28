package fun.moystudio.openlink.frpc;

import com.google.gson.Gson;
import fun.moystudio.openlink.OpenLink;
import oshi.util.FileUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class Frpc {
    public static final String postUrl="https://d.of.gs/client/";
    private static String suffix="";
    private static String zsuffix=".tar.gz";
    public static String osName;
    public static String osArch;
    public static boolean hasUpdate=false;
    public static int frpcVersionDate=0;
    public static String folderName="";
    public static final File frpcVersionFile=new File("frpc.json");
    public static File frpcExecutableFile;
    public static final int MAX_BUFFER_SIZE=10485760;

    public static void init() throws IOException {
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
        } else{
            OpenLink.LOGGER.error("What the hell are you using???");
            throw new RuntimeException("[OpenLink] What the hell are you using???");
        }
        if(osName.equals("windows")){
            suffix=".exe";
            zsuffix=".zip";
        }
        frpcExecutableFile=new File("frpc_"+osName+"_"+osArch+suffix);
        if(checkUpdate()){
            downloadFrpcByUrl(postUrl+folderName+"frpc_"+osName+"_"+osArch+zsuffix);
            hasUpdate=false;
        }
    }

    public static int getLatestVersionDate() throws IOException{
        Gson gson=new Gson();
        AtomicInteger res= new AtomicInteger(frpcVersionDate);
        URL url=new URL(postUrl);
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
        try (FileOutputStream frpcVersionFileOutput = new FileOutputStream(frpcVersionFile)){
            frpcVersionFileOutput.write(("{\"versiondate\":"+ res.toString() +"}").getBytes());
        }

        return res.get();
    }

    public static boolean checkUpdate() throws IOException {
        int latestVersionDate=getLatestVersionDate();

        if(!frpcExecutableFile.exists()||frpcVersionDate<latestVersionDate){
            hasUpdate=true;
            return true;
        }
        hasUpdate=false;
        return false;
    }

    private static void downloadFrpcByUrl(String str) throws IOException {
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
}
