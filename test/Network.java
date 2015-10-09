import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Author: Yin
 * Create on: 15-9-20 22:13
 */
public class Network {
    public static String post(String URL, HashMap<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(String key : params.keySet()) {
            sb.append(key).append('=');
            sb.append(params.get(key)).append("&");
        }
        String p = null;
        if(sb.length() > 0)
            p = sb.substring(0, sb.length() - 1);
        
        URL url = new URL(URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");

        if(p != null) {
            OutputStream os = con.getOutputStream();
            os.write(p.getBytes("UTF-8"));
            os.close();
        }
        
        InputStream is = con.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        sb = new StringBuilder();
        for(String str = br.readLine(); str != null; str = br.readLine()) {
            sb.append(str).append('\n');
        }
        return sb.toString();
    }

    public static String get(String url, HashMap<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(String key : params.keySet()) {
            sb.append(key).append('=');
            sb.append(params.get(key)).append("&");
        }
        String p = null;
        if(sb.length() > 0) {
            p = sb.substring(0, sb.length() - 1);
            url += ("?" + p);
        }

        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("GET");

        InputStream is = con.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        sb = new StringBuilder();
        for(String str = br.readLine(); str != null; str = br.readLine()) {
            sb.append(str).append('\n');
        }
        return sb.toString();
    }
}
