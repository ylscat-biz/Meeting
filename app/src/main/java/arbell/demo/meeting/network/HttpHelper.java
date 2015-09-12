package arbell.demo.meeting.network;

import android.app.Activity;
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

/**
 * Created by sony on 2015/9/12.
 */
public class HttpHelper {
    public static RequestQueue sRequestQueue;

    public static final String URL_BASE = "http://222.221.6.114:8066/app/";

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
}
