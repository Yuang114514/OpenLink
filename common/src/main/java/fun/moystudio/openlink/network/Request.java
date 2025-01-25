package fun.moystudio.openlink.network;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    public static String Authorization=null;
    public final static Map<String,List<String>> DEFAULT_HEADER=new HashMap<>(){{
        put("Content-Type", Collections.singletonList("application/json"));
    }};

    public static String token=null;

    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body) throws Exception {
        Pair<String, Map<String, List<String>>> returnval=POST(url, header, body, false);
        if(returnval!=null&&returnval.getSecond().containsKey("Authorization")){
            if(Authorization==null||!Authorization.equals(returnval.getSecond().get("Authorization").get(0))){
                Authorization=returnval.getSecond().get("Authorization").get(0);
                writeSession();
            }
        }
        return returnval;
    }

    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body, boolean _skip) throws Exception {
        URL postUrl=new URL(url);
        HttpsURLConnection connection=(HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("POST");
        if(!_skip) {
            header.forEach(((s, strings) -> strings.forEach(s1 -> connection.addRequestProperty(s, s1))));
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder re = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    re.append(line.trim());
                }
                return new Pair<>(re.toString(), connection.getHeaderFields());
            } catch (Exception e) {
                if (connection.getResponseCode() >= 400) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder re = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        re.append(line.trim());
                    }
                    return new Pair<>(re.toString(), connection.getHeaderFields());
                }
                throw e;
            }
        }
        else {
            return null;
        }
    }

    public static String GET(String url, Map<String,List<String>> header) throws Exception{
        URL postUrl=new URL(url);
        HttpsURLConnection connection=(HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("GET");
        header.forEach(((s, strings) -> strings.forEach(s1 -> connection.addRequestProperty(s,s1))));
        try(BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))){
            StringBuilder re=new StringBuilder();
            String line;
            while((line=br.readLine())!=null){
                re.append(line.trim());
            }
            return re.toString();
        }catch (Exception e){
            if(connection.getResponseCode()>=400){
                BufferedReader br=new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder re=new StringBuilder();
                String line;
                while((line=br.readLine())!=null){
                    re.append(line.trim());
                }
                return re.toString();
            }
            throw e;
        }

    }

    public static Map<String,List<String>> getHeaderWithCookieByResponse(Pair<String,Map<String, List<String>>> response,Map<String,List<String>> header){
        if(!response.getSecond().containsKey("Set-Cookie")){
            return header;
        }
        List<String> cookie=response.getSecond().get("Set-Cookie");
        header.put("Cookie",cookie);
        return header;
    }

    public static Map<String,List<String>> getHeaderWithAuthorization(Map<String,List<String>> header){
        if(Authorization==null){
            return header;
        }
        header.put("Authorization", Collections.singletonList(Authorization));
        return header;
    }

    public static void writeSession() {
        OpenLink.PREFERENCES.put("authorization", Objects.requireNonNullElse(Authorization, "null"));
    }

    public static void readSession() {
        Authorization=OpenLink.PREFERENCES.get("authorization",null);
        if(Authorization.equals("null")) Authorization=null;
        if(Authorization==null){
            OpenLink.LOGGER.warn("The session does not exists in user preferences!");
            return;
        }
        try{
            JsonResponseWithData<JsonUserInfo> responseWithData = getUserInfo();
            if(responseWithData==null||!responseWithData.flag){
                Authorization=null;
                writeSession();
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