package fun.moystudio.openlink.network;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.json.JsonSession;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    public static String sessionID=null;
    public static String Authorization=null;
    public static final File sessionCodeJson=new File("sessioncode.json");
    public final static Map<String,List<String>> DEFAULT_HEADER=new HashMap<>(){{
        put("Content-Type", Collections.singletonList("application/json"));
    }};
    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body) throws Exception {
        URL postUrl=new URL(url);
        HttpsURLConnection connection=(HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("POST");
        header.forEach(((s, strings) -> {
            strings.forEach(s1 -> {
                connection.addRequestProperty(s,s1);
            });
        }));
        connection.setDoOutput(true);
        try(OutputStream os=connection.getOutputStream()){
            byte[] input=body.getBytes("utf-8");
            os.write(input,0,input.length);
        }
        try(BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"))){
            StringBuilder re=new StringBuilder();
            String line;
            while((line=br.readLine())!=null){
                re.append(line.trim());
            }
            return new Pair<>(re.toString(), connection.getHeaderFields());
        }catch (Exception e){
            if(connection.getResponseCode()>=400){
                BufferedReader br=new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8"));
                StringBuilder re=new StringBuilder();
                String line;
                while((line=br.readLine())!=null){
                    re.append(line.trim());
                }
                return new Pair<>(re.toString(), connection.getHeaderFields());
            }
            throw new RuntimeException(e);
        }
    }

    public static Map<String,List<String>> getHeaderWithCookieByResponse(Pair<String,Map<String, List<String>>> response,Map<String,List<String>> header){
        if(!response.getSecond().containsKey("Set-Cookie")){
            return header;
        }
        List<String> cookie=response.getSecond().get("Set-Cookie");
        Map<String,List<String>> headerWithCookie=header;
        headerWithCookie.put("Cookie",cookie);
        return headerWithCookie;
    }

    public static Map<String,List<String>> getHeaderWithAuthorization(Map<String,List<String>> header){
        if(Authorization==null){
            return header;
        }
        header.put("Authorization", Collections.singletonList(Authorization));
        return header;
    }

    public static void writeSession() {
        try {
            sessionCodeJson.createNewFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(sessionID==null||Authorization==null){
            OpenLink.LOGGER.error("Can not write sessioncode.json(SessionID or Authorization does not exist)");
            return;
        }
        try (FileOutputStream fo = new FileOutputStream(sessionCodeJson)){
            fo.write(("{\"sessionid\":\""+sessionID+"\",\"authorization\":\""+Authorization+"\"}").getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void readSession() {
        Gson gson=new Gson();
        if(!sessionCodeJson.exists()){
            OpenLink.LOGGER.error("Can not read sessioncode.json(sessioncode.json does not exist!)");
            return;
        }
        try (FileInputStream fi=new FileInputStream(sessionCodeJson)){
            JsonSession session=gson.fromJson(new String(fi.readAllBytes()),JsonSession.class);
            sessionID=session.sessionid;
            Authorization=session.authorization;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
