package fun.moystudio.openlink.logic;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class Request {
    public static String POST(String url, Pair<String,String>[] header, String body) throws Exception {
        URL postUrl=new URL(url);
        HttpsURLConnection connection=(HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("POST");
        for (Pair<String, String> stringStringPair : header) {
            connection.setRequestProperty(stringStringPair.getFirst(), stringStringPair.getSecond());
        }
        connection.setDoOutput(true);
        try(OutputStream os=connection.getOutputStream()){
            byte[] input=body.getBytes("utf-8");
            os.write(input,0,input.length);
        }
        try(BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"))){
            StringBuilder re=new StringBuilder();
            String line=null;
            while((line=br.readLine())!=null){
                re.append(line.trim());
            }
            return re.toString();
        }
    }
//    public static String GET(String url, Pair<String,String>[] header, Pair<String,String>[] parameter){//GET不想写了
//
//    }
}
