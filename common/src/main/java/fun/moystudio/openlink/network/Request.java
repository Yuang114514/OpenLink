package fun.moystudio.openlink.network;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

public class Request {
    public static String Authorization=null;
    public final static Map<String,List<String>> DEFAULT_HEADER=new HashMap<>(){{
        put("Content-Type", Collections.singletonList("application/json"));
    }};

    public static String token=null;

    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body) throws Exception {
        AtomicReference<Pair<String, Map<String, List<String>>>> res = new AtomicReference<>();
        Thread thread = new Thread(()->{
            try {
                res.set(POST(url, header, body, false));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },"Request thread");
        thread.start();
        thread.join();
        return res.get();
    }

    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body, boolean _skip) throws Exception {
        URL postUrl=new URL(url);
        HttpsURLConnection connection=(HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("POST");
        if(!_skip) {
            header.forEach(((s, strings) -> {
                strings.forEach(s1 -> {
                    connection.addRequestProperty(s, s1);
                });
            }));
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder re = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    re.append(line.trim());
                }
                return new Pair<>(re.toString(), connection.getHeaderFields());
            } catch (Exception e) {
                if (connection.getResponseCode() >= 400) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                    StringBuilder re = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        re.append(line.trim());
                    }
                    return new Pair<>(re.toString(), connection.getHeaderFields());
                }
                throw new RuntimeException(e);
            }
        }
        else {
            return null;
        }
    }

    public static String GET(String url, Map<String,List<String>> header) throws Exception{//他甚至不需要用（zirran先别删awa）
        URL postUrl=new URL(url);
        HttpsURLConnection connection=(HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("GET");
        header.forEach(((s, strings) -> {
            strings.forEach(s1 -> {
                connection.addRequestProperty(s,s1);
            });
        }));
        try(BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"))){
            StringBuilder re=new StringBuilder();
            String line;
            while((line=br.readLine())!=null){
                re.append(line.trim());
            }
            return re.toString();
        }catch (Exception e){
            if(connection.getResponseCode()>=400){
                BufferedReader br=new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8"));
                StringBuilder re=new StringBuilder();
                String line;
                while((line=br.readLine())!=null){
                    re.append(line.trim());
                }
                return re.toString();
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
        OpenLink.PREFERENCES.put("authorization",Authorization);
    }

    public static void readSession() {
        Authorization=OpenLink.PREFERENCES.get("authorization",null);
        if(Authorization==null){
            OpenLink.LOGGER.warn("The session does not exists in user preferences!");
            return;
        }
        try{
            JsonResponseWithData<JsonUserInfo> responseWithData = getUserInfo();
            if(!responseWithData.flag){
                Authorization=null;
                OpenLink.LOGGER.warn("The session has been expired!");
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static JsonResponseWithData<JsonUserInfo> getUserInfo() throws Exception {
        if(Authorization==null) return null;
        Gson gson=new Gson();
        Pair<String, Map<String, List<String>>> response=POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserInfo",getHeaderWithAuthorization(DEFAULT_HEADER),"{}");
        JsonResponseWithData<JsonUserInfo> res=gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonUserInfo>>(){}.getType());
        if(res.data!=null)
            Request.token=res.data.token;
        return res;
    }

    public static JsonResponseWithData<JsonTotalAndList<JsonNode>> getNodeList() throws Exception {
        if(Authorization==null) return null;
        Gson gson=new Gson();
        Pair<String, Map<String, List<String>>> response=POST(Uris.openFrpAPIUri.toString()+"frp/api/getNodeList",getHeaderWithAuthorization(DEFAULT_HEADER),"{}");
        JsonResponseWithData<JsonTotalAndList<JsonNode>> res=gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonNode>>>(){}.getType());
        return res;
    }
}
