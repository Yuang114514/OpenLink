package fun.moystudio.openlink.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Request {
    public static String Authorization = null;
    public final static Map<String, List<String>> DEFAULT_HEADER = new HashMap<>() {{
        put("Content-Type", Collections.singletonList("application/json"));
    }};
    
    public static String token = null;
    private static final ObjectMapper objectMapper = new ObjectMapper(); // 使用Jackson的ObjectMapper

    public static CompletableFuture<Pair<String, Map<String, List<String>>>> POST(String url, Map<String, List<String>> header, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Pair<String, Map<String, List<String>>> response = POST(url, header, body, false);
                if (response.getSecond().containsKey("Authorization")) {
                    String newAuthorization = response.getSecond().get("Authorization").get(0);
                    if (Authorization == null || !Authorization.equals(newAuthorization)) {
                        Authorization = newAuthorization;
                        writeSession();
                    }
                }
                return response;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Pair<String, Map<String, List<String>>> POST(String url, Map<String, List<String>> header, String body, boolean _skip) throws Exception {
        if (_skip) return null;

        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        setRequestHeaders(connection, header);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes("utf-8"));
        }

        return getResponse(connection);
    }

    public static CompletableFuture<String> GET(String url, Map<String, List<String>> header) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                setRequestHeaders(connection, header);
                return getResponse(connection).getFirst();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static void setRequestHeaders(HttpURLConnection connection, Map<String, List<String>> headers) {
        headers.forEach((key, values) -> values.forEach(value -> connection.addRequestProperty(key, value)));
    }

    private static Pair<String, Map<String, List<String>>> getResponse(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(), "utf-8"))) {
            String response = br.lines().collect(Collectors.joining("\n"));
            return new Pair<>(response, connection.getHeaderFields());
        }
    }

    public static Map<String, List<String>> getHeaderWithCookieByResponse(Pair<String, Map<String, List<String>>> response, Map<String, List<String>> header) {
        if (response.getSecond().containsKey("Set-Cookie")) {
            List<String> cookie = response.getSecond().get("Set-Cookie");
            header.put("Cookie", cookie);
        }
        return header;
    }

    public static Map<String, List<String>> getHeaderWithAuthorization(Map<String, List<String>> header) {
        if (Authorization != null && !header.containsKey("Authorization")) {
            header.put("Authorization", Collections.singletonList(Authorization));
        }
        return header;
    }

    public static void writeSession() {
        OpenLink.PREFERENCES.put("authorization", Authorization);
    }

    public static void readSession() {
        Authorization = OpenLink.PREFERENCES.get("authorization", null);
        if (Authorization == null) {
            OpenLink.LOGGER.warn("The session does not exist in user preferences!");
            return;
        }
        try {
            CompletableFuture<JsonResponseWithData<JsonUserInfo>> responseFuture = ApiService.getUserInfo();
            responseFuture.thenAccept(responseWithData -> {
                if (responseWithData == null || !responseWithData.flag) {
                    Authorization = null;
                    OpenLink.LOGGER.warn("The session has expired!");
                }
            }).exceptionally(e -> {
                throw new RuntimeException(e);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class ApiService {
        public static CompletableFuture<JsonResponseWithData<JsonUserInfo>> getUserInfo() {
            return getUserInfoOrNodeList("frp/api/getUserInfo", JsonUserInfo.class);
        }

        public static CompletableFuture<JsonResponseWithData<JsonTotalAndList<JsonNode>>> getNodeList() {
            return getUserInfoOrNodeList("frp/api/getNodeList", JsonTotalAndList.class);
        }

        private static <T> CompletableFuture<JsonResponseWithData<T>> getUserInfoOrNodeList(String endpoint, Class<T> clazz) {
            if (Authorization == null) return CompletableFuture.completedFuture(null);

            return POST(Uris.openFrpAPIUri.toString() + endpoint, Request.getHeaderWithAuthorization(DEFAULT_HEADER), "{}").thenApply(response -> {
                try {
                    JsonResponseWithData<T> res = objectMapper.readValue(response.getFirst(), new TypeToken<JsonResponseWithData<T>>() {}.getType());
                    if (clazz == JsonUserInfo.class && res.data != null) {
                        Request.token = res.data.token;
                    }
                    return res;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}