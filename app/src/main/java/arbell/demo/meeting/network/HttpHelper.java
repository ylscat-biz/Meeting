package arbell.demo.meeting.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.artifex.mupdfdemo.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {
    public static RequestQueue sRequestQueue;
    public static Handler sHandler = new Handler();

    public static final String SETTINGS = "settings";
    public static final String SERVER = "server";
    public static final String DEFAULT_SERVER = "http://222.221.6.114:8066";
    public static String URL_BASE = "http://222.221.6.114:8066/app/";

    public static void test(Activity activity) {
        Volley.newRequestQueue(activity, new HurlStack());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    URL url = new URL("http://192.168.199.130:8080/app/getMeeting");
                    URL url = new URL("http://222.221.6.114:8066/app/login");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("POST");
                    con.setConnectTimeout(2000);
                    con.setReadTimeout(2000);
                    con.setUseCaches(false);
                    con.setDoInput(true);

                    OutputStream os = con.getOutputStream();
                    os.write("username=az&password=123123".getBytes());
                    os.flush();
                    os.close();

                    InputStream is = con.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    for(String line = br.readLine(); line != null; line = br.readLine())
                        Log.d("Net", line);
                    con.disconnect();
                }
                catch (IOException e) {
                    Log.d("Net", e.getMessage(), e);
                }
            }
        });
    }

    public static String get(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection)u.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);
            con.setUseCaches(false);
            con.setDoInput(true);

            InputStream is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for(String line = br.readLine(); line != null; line = br.readLine())
                sb.append(line).append('\n');
            con.disconnect();
            if(sb.length() > 0)
                return sb.substring(0, sb.length() - 1);
        }
        catch (IOException e) {
            Log.d("Net", e.getMessage(), e);
        }

        return null;
    }

    public static String getServer(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getString(SERVER, DEFAULT_SERVER);
    }

    public static void setServer(Context context, String server) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        String old = sp.getString(SERVER, DEFAULT_SERVER);
        if(old.equals(server)) {
            return;
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SERVER, server);
        editor.commit();
        setServer(server);
    }

    public static void setServer(String server) {
        if(!server.startsWith("http")) {
            server = "http://" + server;
        }
        URL_BASE = server + "/app/";
    }
}
