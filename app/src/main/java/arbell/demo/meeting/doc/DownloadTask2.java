package arbell.demo.meeting.doc;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.artifex.mupdfdemo.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static arbell.demo.meeting.doc.DownloadTask.DownloadListener;

/**
 * Created by yls on 2015/11/21.
 */
public class DownloadTask2 extends AsyncTask<Void, Integer, File> {
    private static final String TAG = "download";
    private HttpURLConnection mConnection;
    private DownloadListener mListener;
    private String mUrl;
    private File mTarget;
    private Thread mBackground;

    public DownloadTask2(DownloadListener listener,
                         String url, File target) {
        mListener = listener;
        mUrl = url;
        mTarget = target;
    }

    @Override
    protected File doInBackground(Void... params) {
        for(int retry = 0; retry < 3; retry++) {
            File f = download();
            if(f != null)
                return f;
            Log.w(TAG, "download retry " + retry);
        }

        return null;
    }

    private File download() {
        mBackground = Thread.currentThread();
        int start = (int)mTarget.length();
        android.util.Log.d(TAG, "Download \nfrom:" + mUrl +"\nto:" + mTarget + "\nstart at " + start);
        HttpURLConnection con = null;
        try {
            URL url = new URL(mUrl);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setConnectTimeout(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS);
            con.setReadTimeout(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS);
            con.addRequestProperty("Range", String.format("bytes=%d-", start));
            switch (con.getResponseCode()) {
                case HttpURLConnection.HTTP_NO_CONTENT:
                    return mTarget;
                case HttpURLConnection.HTTP_PARTIAL:
                case HttpURLConnection.HTTP_OK:
                    break;
                default:
                    android.util.Log.e(TAG, "connection failed code="+con.getResponseCode());
                    return null;
            }

            mConnection = con;
            int totalLength = con.getContentLength() + start;
            publishProgress(start, totalLength);
            final int STRIDE = totalLength/100;
            InputStream is = con.getInputStream();
            File downloadFile = mTarget;
            FileOutputStream fos = new FileOutputStream(downloadFile, true);
            final int BUF_LEN = 1024*4;
            byte[] buffer = new byte[BUF_LEN];
            int length = is.read(buffer);
            int progress = start, preProgress = start;
            while (length > 0) {
                if(isCancelled()) {
                    android.util.Log.d(TAG, "Download canceled");
                    break;
                }
                fos.write(buffer, 0, length);
                progress += length;
                length = is.read(buffer);
                if(preProgress + STRIDE < progress) {
                    publishProgress(progress, totalLength);
                    preProgress = progress;
                }
            }
            fos.close();
            return downloadFile;
        } catch (IOException e) {
            android.util.Log.e(TAG, "Downloading error", e);
        }
        finally {
            if(con != null)
                con.disconnect();
            mConnection = null;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(mListener == null)
            return;
        if(values[0] == 0)
            mListener.begin(values[1]);
        else
            mListener.update(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(File file) {
        if(mListener != null)
            mListener.complete(file);
    }

    public void cancel() {
        cancel(true);
//        if(mConnection != null) {
//            new Thread() {
//                @Override
//                public void run() {
//                    mConnection.disconnect();
//                    if(mBackground != null) {
//                        mBackground.interrupt();
//                    }
////                    mBackground.interrupt();
//                }
//            }.start();
//        }
    }
}
