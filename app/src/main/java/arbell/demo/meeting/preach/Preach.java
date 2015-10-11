package arbell.demo.meeting.preach;

import android.os.Handler;
import android.util.Log;

import com.android.volley.Response;

import org.json.JSONObject;

import arbell.demo.meeting.network.HttpHelper;
import arbell.demo.meeting.network.Request;

public class Preach implements Runnable {
    private Request mMonitor;
    private String mSetPrefix;
    private PreachListener mListener;

    private Handler mHandler = new Handler();
    private String mLastMsg;

    public static final int IDLE = 0;
    public static final int SCANING = 1;
    public static final int FOLLOW = 2;
    public static final int PREACH = 3;
    private int mMode = -1;

    private static final int SHORT_INTERVAL = 2000;
    private static final int LONG_INTERVAL = 5000;

    public Preach(String meetingId) {
        mSetPrefix = "meetingid=" + meetingId;
        mMonitor = new Request(Request.Method.POST,
                HttpHelper.URL_BASE + "getCache",
                mSetPrefix,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String msg;
                        if(response.has("data")) {
                            msg = response.optString("data");
                            if(msg.length() == 0)
                                msg = null;
                        }
                        else {
                            msg = null;
                        }
                        if(msg == mLastMsg || (msg != null && msg.equals(mLastMsg)))
                            return;
                        if(mListener != null)
                            mListener.onUpdate(msg);
                        mLastMsg = msg;
                    }
                });
    }

    public void setListener(PreachListener listener) {
        mListener = listener;
    }

    public void checkPreacher(final PreachListener listener) {
        Request request = new Request(Request.Method.GET,
                mMonitor.getUrl(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String msg;
                        if(response.has("data")) {
                            msg = response.optString("data");
                            if(msg.length() == 0)
                                msg = null;
                        }
                        else {
                            msg = null;
                        }
                        if(listener != null)
                            listener.onUpdate(msg);
                    }
                });
        HttpHelper.sRequestQueue.add(request);
    }

    public void upload(String msg) {
        String set = mSetPrefix;
        if(msg != null) {
            set += "&msg=" + msg;
        }
        Request upload = new Request(Request.Method.GET,
                HttpHelper.URL_BASE + "setCache",
                set,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Net", "Preach upload " + response);
                    }
                });
        HttpHelper.sRequestQueue.add(upload);
    }

    public void setMode(int mode) {
        if(mode == mMode)
            return;
        switch (mode) {
            case IDLE:
            case SCANING:
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, 500);
                break;
            case FOLLOW:
                mLastMsg = null;
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, 500);
                break;
            case PREACH:
                mHandler.removeCallbacks(this);
                break;
        }
        mMode = mode;
    }

    public void stop() {
        mHandler.removeCallbacks(this);
    }

    public String getMsg(){
        return mLastMsg;
    }

    public int getMode() {
        return mMode;
    }

    @Override
    public void run() {
        switch (mMode) {
            case IDLE:
            case SCANING:
                HttpHelper.sRequestQueue.add(mMonitor);
                mHandler.postDelayed(this, LONG_INTERVAL);
                break;
            case FOLLOW:
                HttpHelper.sRequestQueue.add(mMonitor);
                mHandler.postDelayed(this, SHORT_INTERVAL);
                break;
        }
    }

    public interface PreachListener {
        void onUpdate(String msg);
    }
}
