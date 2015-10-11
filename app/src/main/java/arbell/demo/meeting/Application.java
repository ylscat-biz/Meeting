package arbell.demo.meeting;

import android.content.Context;

import com.android.volley.toolbox.Volley;

import arbell.demo.meeting.network.HttpHelper;

/**
 * Created by sony on 2015/9/12.
 */
public class Application extends android.app.Application {
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        HttpHelper.setServer(HttpHelper.getServer(this));
        HttpHelper.sRequestQueue = Volley.newRequestQueue(this);
        sContext = this;
    }
}
